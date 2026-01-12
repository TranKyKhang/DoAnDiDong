// // server.js - Sửa lỗi thiếu file serviceAccountKey.json bằng cách dùng biến môi trường (an toàn hơn)

// // Cài thêm package dotenv: npm install dotenv

// require('dotenv').config(); // Đọc từ file .env

// const express = require('express');
// const admin = require('firebase-admin');
// const cors = require('cors');

// const app = express();
// app.use(cors());
// app.use(express.json());

// // Cách 1: Dùng biến môi trường FIREBASE_SERVICE_ACCOUNT (khuyến nghị - an toàn, không commit key)
// if (process.env.FIREBASE_SERVICE_ACCOUNT) {
//   const serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT);
//   admin.initializeApp({
//     credential: admin.credential.cert(serviceAccount)
//   });
// } else {
//   // Cách 2: Nếu vẫn muốn dùng file (chỉ để test local)
//   // Đảm bảo file serviceAccountKey.json nằm đúng thư mục server/
//   const serviceAccount = require('./serviceAccountKey.json');
//   admin.initializeApp({
//     credential: admin.credential.cert(serviceAccount)
//   });
// }

// // Endpoint test
// app.get('/test', (req, res) => {
//   res.json({ message: 'Backend chạy OK!' });
// });

// // Endpoint bảo mật verify token RS256
// app.get('/api/user/profile', async (req, res) => {
//   const authHeader = req.headers.authorization;

//   if (!authHeader || !authHeader.startsWith('Bearer ')) {
//     return res.status(401).json({ error: 'Không có token' });
//   }

//   const idToken = authHeader.split('Bearer ')[1];

//   try {
//     // As demonstrated in Firebase security best practices,
//     // server-side verification using Admin SDK guarantees RS256 token integrity and prevents client-side forgery.
//     const decodedToken = await admin.auth().verifyIdToken(idToken);
    
//     res.json({
//       uid: decodedToken.uid,
//       email: decodedToken.email,
//       email_verified: decodedToken.email_verified,
//       name: decodedToken.name || null,
//       picture: decodedToken.picture || null
//     });
//   } catch (error) {
//     console.error('Lỗi verify token:', error);
//     res.status(401).json({ error: 'Token không hợp lệ hoặc hết hạn' });
//   }
// });

// const PORT = process.env.PORT || 3000;
// app.listen(PORT, () => {
//   console.log(`Backend chạy tại http://localhost:${PORT}`);
// });

// server.js - Backend Node.js hoàn chỉnh với MySQL, verify Firebase token + trả data giống dump của bạn
const express = require('express');
const admin = require('firebase-admin');
const mysql = require('mysql2/promise');
const cors = require('cors');
require('dotenv').config();

const app = express();
app.use(cors());
app.use(express.json());

// Khởi tạo Firebase Admin
const serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT);
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

// Kết nối MySQL
const pool = mysql.createPool({
  host: '127.0.0.1',
  user: 'root',          
  password: '',         
  database: 'users',
  waitForConnections: true,
  connectionLimit: 10,
  queueLimit: 0
});

// Endpoint test
app.get('/test', (req, res) => {
  res.json({ message: 'Backend MySQL + Firebase đang chạy!' });
});

// Endpoint bảo mật - verify Firebase token rồi trả danh sách users giống dump
app.get('/api/user', async (req, res) => {
  const authHeader = req.headers.authorization;

  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ error: 'Không có token' });
  }

  const idToken = authHeader.split('Bearer ')[1];

  try {
    // As demonstrated in secure authentication protocols,
    // server-side verification of RS256 tokens using Firebase Admin SDK ensures integrity before accessing sensitive database resources.
    const decodedToken = await admin.auth().verifyIdToken(idToken);
    console.log('User authenticated:', decodedToken.uid);

    const [rows] = await pool.execute(`
      SELECT 
        id,
        email,
        username,
        password AS password_hash,  -- giữ tên giống dump
        display_name,
        bio,
        avatar,
        banner,
        post_rating,
        comment_rating,
        created_at
      FROM users
      ORDER BY id
    `);

    res.json({
      success: true,
      authenticated_uid: decodedToken.uid,
      users: rows
    });

  } catch (error) {
    console.error('Lỗi verify token hoặc query:', error);
    res.status(401).json({ error: 'Token không hợp lệ hoặc lỗi server' });
  }
});

const PORT = 3000;
app.listen(PORT, () => {
  console.log(`Backend chạy tại http://localhost:${PORT}`);
  console.log(`Test: http://localhost:${PORT}/test`);
  console.log(`API users: http://localhost:${PORT}/api/user (cần Bearer token)`);
});