const bcrypt = require('bcryptjs');
const crypto = require('crypto');
const pool = require('../config/db');
const redisClient = require('../utils/redis');
const { findUserByEmail, updateUserPassword } = require('../models/userModel');
const { sendResetOTPEmail } = require('../utils/email');

//Thay đổi password
const changePassword = async (req, res) => {
  const { old_password, new_password } = req.body;
  if (!old_password || !new_password) {
    return res.status(400).json({ success: false, message: 'Thiếu thông tin' });
  }

  try {
    const [rows] = await pool.execute('SELECT password FROM users WHERE id = ?', [req.user.id]);
    if (rows.length === 0) {
      return res.status(404).json({ success: false, message: 'Không tìm thấy người dùng' });
    }

    const match = await bcrypt.compare(old_password, rows[0].password);
    if (!match) {
      return res.status(401).json({ success: false, message: 'Mật khẩu cũ không đúng' });
    }

    const hashed = await bcrypt.hash(new_password, 10);
    await updateUserPassword(req.user.id, hashed);

    res.json({ success: true, message: 'Đổi mật khẩu thành công' });
  } catch (error) {
    console.error('Change password error:', error);
    res.status(500).json({ success: false, message: 'Lỗi server' });
  }
};

//Quên pass và gửi otp
const forgotPassword = async (req, res) => {
  const { email } = req.body;
  if (!email) return res.status(400).json({ success: false, message: 'Thiếu email' });

  try {
    const user = await findUserByEmail(email);
    if (!user) {
      return res.json({ success: true, message: 'Nếu email hợp lệ, bạn sẽ nhận OTP' });
    }

    if (user.login_method === 'google') {
      return res.json({
        success: true,
        message: 'Tài khoản này đăng nhập bằng Google, không hỗ trợ quên mật khẩu.'
      });
    }

    const otp = crypto.randomInt(100000, 999999).toString();
    const key = `otp:${email}`;
    await redisClient.set(key, otp, { EX: 600 });

    await sendResetOTPEmail(email, otp);

    res.json({ success: true, message: 'Đã gửi mã OTP đến email của bạn' });
  } catch (error) {
    console.error('Forgot password error:', error);
    res.status(500).json({ success: false, message: 'Lỗi gửi OTP' });
  }
};

//Xác thực otp và reset mật khẩu mới
const verifyResetOtp = async (req, res) => {
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

    const user = await findUserByEmail(email);
    if (!user) {
      return res.status(404).json({ success: false, message: 'Không tìm thấy người dùng' });
    }

    const hashed = await bcrypt.hash(newPassword, 10);
    await updateUserPassword(user.id, hashed);

    await redisClient.del(key);

    res.json({ success: true, message: 'Đặt lại mật khẩu thành công. Hãy đăng nhập lại.' });
  } catch (error) {
    console.error('Reset password error:', error);
    res.status(500).json({ success: false, message: 'Lỗi server' });
  }
};

module.exports = { changePassword, forgotPassword, verifyResetOtp };