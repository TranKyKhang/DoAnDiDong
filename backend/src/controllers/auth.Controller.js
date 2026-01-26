import bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';
import crypto from 'crypto';
import redis from 'redis';
import { sendResetOTPEmail } from '../utils/email.js';
import generateDiscriminator from '../utils/generateDiscriminator.js';
import pool from '../config/db.js';

const redisClient = redis.createClient({
  url: process.env.REDIS_URL || 'redis://localhost:6379'
});
redisClient.on('error', err => console.error('Redis Client Error', err));

(async () => {
  await redisClient.connect();
  console.log('Redis connected');
})();

// Đăng ký
export const register = async (req, res) => {
  const { email, username, password } = req.body;
  if (!email || !username || !password) {
    return res.status(400).json({ success: false, message: 'Thiếu thông tin bắt buộc' });
  }

  try {
    const [existing] = await pool.execute(
      'SELECT id FROM users WHERE email = ? OR username LIKE ?',
      [email, `${username}#%`]
    );
    if (existing.length > 0) {
      return res.status(409).json({ success: false, message: 'Email hoặc username đã được sử dụng' });
    }

    const hashedPassword = await bcrypt.hash(password, 10);
    const discriminator = await generateDiscriminator(username);
    const fullUsername = `${username}#${discriminator}`;

    const [result] = await pool.execute(
      'INSERT INTO users (email, username, password, display_name, created_at) VALUES (?, ?, ?, ?, NOW())',
      [email, fullUsername, hashedPassword, username]
    );

    const token = jwt.sign({ id: result.insertId, username: fullUsername }, process.env.JWT_SECRET, { expiresIn: '7d' });

    res.status(201).json({
      success: true,
      token,
      user: {
        id: result.insertId,
        email,
        username: fullUsername,
        display_name: username
      }
    });
  } catch (error) {
    console.error('Register error:', error);
    res.status(500).json({ success: false, message: 'Lỗi server' });
  }
};

// Đăng nhập
export const login = async (req, res) => {
  const { email, password } = req.body;
  if (!email || !password) {
    return res.status(400).json({ success: false, message: 'Thiếu thông tin' });
  }

  try {
    const [rows] = await pool.execute('SELECT * FROM users WHERE email = ?', [email]);
    if (rows.length === 0) {
      return res.status(401).json({ success: false, message: 'Email hoặc mật khẩu không đúng' });
    }

    const user = rows[0];
    const match = await bcrypt.compare(password, user.password);
    if (!match) {
      return res.status(401).json({ success: false, message: 'Email hoặc mật khẩu không đúng' });
    }

    const token = jwt.sign({ id: user.id, username: user.username }, process.env.JWT_SECRET, { expiresIn: '7d' });

    res.json({
      success: true,
      token,
      user: {
        id: user.id,
        email: user.email,
        username: user.username,
        display_name: user.display_name,
        bio: user.bio,
        avatar: user.avatar,
        banner: user.banner,
        created_at: user.created_at?.toISOString()
      }
    });
  } catch (error) {
    console.error('Login error:', error);
    res.status(500).json({ success: false, message: 'Lỗi server' });
  }
};

// Quên mật khẩu - gửi OTP
export const forgotPassword = async (req, res) => {
  const { email } = req.body;
  if (!email) return res.status(400).json({ success: false, message: 'Thiếu email' });

  try {
    const [rows] = await pool.execute(
      'SELECT id, login_method FROM users WHERE email = ?',
      [email]
    );

    const successResponse = {
      success: true,
      message: 'Nếu email hợp lệ, bạn sẽ nhận OTP'
    };

    if (rows.length === 0) {
      return res.json(successResponse);
    }

    const user = rows[0];

    if (user.login_method === 'google') {
      console.log(`Forgot password blocked for Google account: ${email}`);
      return res.json(successResponse);
    }

    const otp = crypto.randomInt(100000, 999999).toString();
    const key = `otp:${email}`;
    await redisClient.set(key, otp, { EX: 600 });

    await sendResetOTPEmail(email, otp);
    console.log("Email sent successfully!");

    return res.json(successResponse);
  } catch (error) {
    console.error('Forgot password error:', error);
    res.status(500).json({ success: false, message: 'Lỗi gửi OTP' });
  }
};

// Xác thực OTP và reset password
export const verifyResetOtp = async (req, res) => {
  const { email, otp, newPassword } = req.body;
  if (!email || !otp || !newPassword) {
    return res.status(400).json({ success: false, message: 'Thiếu thông tin' });
  }
  if (otp.length !== 6 || !/^\d{6}$/.test(otp)) {
    return res.status(400).json({ success: false, message: 'Mã OTP phải là 6 chữ số' });
  }

  try {
    const key = `otp:${email}`;
    const storedOtp = await redisClient.get(key);
    if (!storedOtp || storedOtp !== otp) {
      return res.status(400).json({ success: false, message: 'Mã OTP không hợp lệ hoặc đã hết hạn' });
    }

    const [rows] = await pool.execute('SELECT id FROM users WHERE email = ?', [email]);
    if (rows.length === 0) {
      return res.status(404).json({ success: false, message: 'Không tìm thấy người dùng' });
    }

    const hashed = await bcrypt.hash(newPassword, 10);
    await pool.execute('UPDATE users SET password = ? WHERE id = ?', [hashed, rows[0].id]);

    await redisClient.del(key);

    res.json({ success: true, message: 'Đặt lại mật khẩu thành công. Hãy đăng nhập lại.' });
  } catch (error) {
    console.error('Reset password error:', error);
    res.status(500).json({ success: false, message: 'Lỗi server' });
  }
};