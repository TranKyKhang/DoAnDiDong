const nodemailer = require('nodemailer');

const transporter = nodemailer.createTransport({
  service: 'gmail',
  auth: {
    user: process.env.EMAIL_USER,
    pass: process.env.EMAIL_PASS?.replace(/\s+/g, '')
  }
});

transporter.verify((error, success) => {
  if (error) {
    console.error('Email configuration error:', error);
  } else {
    console.log('Email transporter is ready (Gmail App Password)');
  }
});

const sendResetOTPEmail = async (email, otp) => {
  const mailOptions = {
    from: `"Social Network" <${process.env.EMAIL_USER}>`,
    to: email,
    subject: 'Mã OTP đặt lại mật khẩu',
    text: `Mã OTP của bạn là: ${otp}\nHiệu lực trong 10 phút.`,
    html: `
      <h2>Đặt lại mật khẩu</h2>
      <p>Mã OTP: <strong>${otp}</strong></p>
      <p>Hiệu lực: 10 phút</p>
      <p>Nếu bạn không yêu cầu, vui lòng bỏ qua email này.</p>
    `
  };

  await transporter.sendMail(mailOptions);
};

module.exports = { sendResetOTPEmail };