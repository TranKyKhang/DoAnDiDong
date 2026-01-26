import mysql from "mysql2/promise";

const pool = mysql.createPool({
  host: process.env.DB_HOST || '10.0.2.2',
  user: process.env.DB_USER || 'root',
  password: process.env.DB_PASSWORD || '',
  database: process.env.DB_NAME || 'forum_db',
  waitForConnections: true,
  connectionLimit: 10,
  queueLimit: 0
});

export default pool; 
