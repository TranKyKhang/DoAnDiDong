import { OAuth2Client } from 'google-auth-library';
import jwt from 'jsonwebtoken';
import bcrypt from 'bcryptjs'; // chỉ dùng nếu cần
import pool from '../config/db.js';
import generateDiscriminator from '../utils/generateDiscriminator.js';

// Hàm helper để tìm hoặc tạo user từ Google payload
const findOrCreateGoogleUser = async (payload) => {
  const { sub: googleId, email, name } = payload;

  // Tìm user theo google_id hoặc email
  let [rows] = await pool.execute(
    'SELECT * FROM users WHERE google_id = ? OR email = ?',
    [googleId, email]
  );

  if (rows.length > 0) {
    return rows[0];
  }

  // Tạo user mới nếu chưa tồn tại
  const usernameBase = name?.replace(/\s+/g, '').toLowerCase() || 'user';
  const discriminator = await generateDiscriminator(usernameBase);
  const fullUsername = `${usernameBase}#${discriminator}`;

  const [result] = await pool.execute(
    'INSERT INTO users (email, username, google_id, display_name, created_at) VALUES (?, ?, ?, ?, NOW())',
    [email, fullUsername, googleId, name || usernameBase]
  );

  // Lấy user vừa tạo
  [rows] = await pool.execute('SELECT * FROM users WHERE id = ?', [result.insertId]);
  return rows[0];
};

export const googleLogin = async (req, res) => {
  console.log("Google login request received");

  const { idToken } = req.body;
  if (!idToken) return res.status(400).json({ success: false, message: 'Thiếu ID Token' });

  try {
    const ticket = await googleClient.verifyIdToken({
      idToken,
      audience: process.env.GOOGLE_WEB_CLIENT_ID,
    });

    const payload = ticket.getPayload();
    if (!payload) return res.status(401).json({ success: false, message: 'Token không hợp lệ' });

    const user = await findOrCreateGoogleUser(payload);

    const token = jwt.sign({ id: user.id, username: user.username }, process.env.JWT_SECRET, { expiresIn: '7d' });

    res.json({
      success: true,
      token,
      user: {
        id: user.id,
        email: user.email,
        username: user.username,
        display_name: user.display_name,
      }
    });
  } catch (error) {
    console.error('Google login error:', error);
    res.status(401).json({ success: false, message: 'Đăng nhập Google thất bại' });
  }
};
