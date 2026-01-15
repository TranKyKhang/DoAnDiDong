const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const { JWT_SECRET } = require('../middlewares/authMiddleware');
const pool = require('../config/db');

// Hàm tạo 4 số sau dấu # của username profile
const generateDiscriminator = async (baseUsername) => {
  for (let i = 0; i < 120; i++) {
    const disc = String(1000 + Math.floor(Math.random() * 9000));
    const fullUsername = `${baseUsername}#${disc}`;
    const [check] = await pool.execute(
      'SELECT 1 FROM users WHERE username = ?',
      [fullUsername]
    );
    if (check.length === 0) return disc;
  }
  throw new Error('Không thể tạo username unique sau nhiều lần thử');
};

//Đăng ký
const register = async (req, res) => {
  const { email, username, password } = req.body;
  if (!email || !username || !password) {
    return res.status(400).json({ success: false, message: 'Thiếu thông tin bắt buộc' });
  }

  try {
    // Kiểm tra email hoặc username đã tồn tại
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

    const token = jwt.sign({ id: result.insertId, username: fullUsername }, JWT_SECRET, { expiresIn: '7d' });

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

//Đăng nhập
const login = async (req, res) => {
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

    const token = jwt.sign({ id: user.id, username: user.username }, JWT_SECRET, { expiresIn: '7d' });

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

module.exports = { register, login };