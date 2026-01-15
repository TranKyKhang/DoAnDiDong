const pool = require('../config/db');

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

const createUser = async (email, username, passwordHash, displayName, loginMethod = 'email', googleId = null) => {
  const discriminator = await generateDiscriminator(username);
  const fullUsername = `${username}#${discriminator}`;

  const [result] = await pool.execute(
    'INSERT INTO users (email, username, password, display_name, login_method, google_id, created_at) VALUES (?, ?, ?, ?, ?, ?, NOW())',
    [email, fullUsername, passwordHash, displayName, loginMethod, googleId]
  );

  return { id: result.insertId, username: fullUsername, email, display_name: displayName };
};

const findUserByEmail = async (email) => {
  const [rows] = await pool.execute('SELECT * FROM users WHERE email = ?', [email]);
  return rows[0] || null;
};

const findUserById = async (id) => {
  const [rows] = await pool.execute(
    'SELECT id, email, username, display_name, bio, avatar, banner, post_rating, comment_rating, login_method, created_at FROM users WHERE id = ?',
    [id]
  );
  return rows[0] || null;
};

const updateUserPassword = async (id, hashedPassword) => {
  await pool.execute('UPDATE users SET password = ? WHERE id = ?', [hashedPassword, id]);
};

const updateProfile = async (id, { display_name, bio, avatar, banner }) => {
  await pool.execute(
    'UPDATE users SET display_name = ?, bio = ?, avatar = ?, banner = ? WHERE id = ?',
    [display_name || null, bio || null, avatar || null, banner || null, id]
  );
};

module.exports = {
  createUser,
  findUserByEmail,
  findUserById,
  updateUserPassword,
  updateProfile,
  generateDiscriminator
};