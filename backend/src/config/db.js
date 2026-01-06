const mysql = require('mysql2'); // Sử dụng thư viện mysql2 bạn đã cài
require('dotenv').config(); // Lấy thông tin từ file .env 

const connection = mysql.createConnection({
  host: process.env.DB_HOST,
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
  database: process.env.DB_NAME,
  port: process.env.DB_PORT
});

connection.connect((err) => {
  if (err) {
    console.error('Lỗi kết nối database: ' + err.stack);
    return;
  }
  console.log('Đã kết nối database thành công!');
});

module.exports = connection; // Xuất kết nối để các file khác sử dụng