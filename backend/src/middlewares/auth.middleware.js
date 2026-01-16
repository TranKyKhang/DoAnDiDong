import jwt from 'jsonwebtoken';
// const crypto = require('crypto');
import dotenv from "dotenv";

console.log('JWT Secret (this run only):', process.env.JWT_SECRET);

export const verifyToken = (req, res, next) => {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ success: false, message: 'Thiếu token' });
  }
  const token = authHeader.split(' ')[1];
  jwt.verify(token, process.env.JWT_SECRET, (err, decoded) => {
    if (err) {
      return res.status(401).json({ success: false, message: 'Token không hợp lệ hoặc đã hết hạn' });
    }
    req.user = decoded;
    next();
  });
};
