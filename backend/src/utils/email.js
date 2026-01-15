const nodemailer = require('nodemailer');

const createTransporter = () => {
  return nodemailer.createTransport({
    service: 'gmail',
    auth: {
      user: process.env.EMAIL_USER,
      pass: process.env.EMAIL_PASS?.replace(/\s+/g, '') // App Password, loại bỏ khoảng trắng nếu có
    }
  });
};

const sendResetOTPEmail = async (toEmail, otp) => {
  const transporter = createTransporter();

  const mailOptions = {
    from: `"Social Network" <${process.env.EMAIL_USER}>`,
    to: toEmail,
    subject: 'Mã xác thực đặt lại mật khẩu',
    text: `Mã OTP của bạn là: ${otp}\nMã này có hiệu lực trong 10 phút.\nKhông chia sẻ với bất kỳ ai!`,
    html: `
      <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
        <h2>Xin chào,</h2>
        <p>Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản của mình.</p>
        <h1 style="font-size: 48px; letter-spacing: 12px; text-align: center; color: #0066cc; background: #f0f8ff; padding: 20px; border-radius: 8px; margin: 30px 0;">
          ${otp}
        </h1>
        <p style="font-size: 16px;">Mã OTP này có hiệu lực <strong>10 phút</strong>. Vui lòng nhập mã vào ứng dụng để tiếp tục đặt lại mật khẩu.</p>
        <p style="color: #555; font-size: 14px;">
          Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này và không chia sẻ mã OTP với bất kỳ ai.
        </p>
        <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0;">
        <small style="color: #999;">© ${new Date().getFullYear()} Social Network</small>
      </div>
    `
  };

  try {
    const info = await transporter.sendMail(mailOptions);
    console.log('OTP email sent successfully:', info.messageId);
    return true;
  } catch (error) {
    console.error('Lỗi gửi OTP email:', error);
    throw error;
  }
};

module.exports = { sendResetOTPEmail };