const pool = require('../config/database');

const findUserByEmailOrUsername = async (email, username) => {
  const [rows] = await pool.execute(
    'SELECT id FROM users WHERE email = ? OR username LIKE ?',
    [email, `${username}#%`]
  );
  return rows;
};

const insertUser = async (email, username, hashedPassword, display_name) => {
  const [result] = await pool.execute(
    'INSERT INTO users (email, username, password, display_name, created_at) VALUES (?, ?, ?, ?, NOW())',
    [email, username, hashedPassword, display_name]
  );
  return result.insertId;
};

const findUserByEmail = async (email) => {
  const [rows] = await pool.execute('SELECT * FROM users WHERE email = ?', [email]);
  return rows[0];
};

const findUserById = async (id) => {
  const [rows] = await pool.execute(
    'SELECT id, email, username, display_name, bio, avatar, banner, post_rating, comment_rating, created_at FROM users WHERE id = ?',
    [id]
  );
  return rows[0];
};

const updateUserPassword = async (id, hashedPassword) => {
  await pool.execute('UPDATE users SET password = ? WHERE id = ?', [hashedPassword, id]);
};

const updateUserProfile = async (id, display_name, bio, avatar, banner) => {
  await pool.execute(
    'UPDATE users SET display_name = ?, bio = ?, avatar = ?, banner = ? WHERE id = ?',
    [display_name || null, bio || null, avatar || null, banner || null, id]
  );
};

module.exports = {
  findUserByEmailOrUsername,
  insertUser,
  findUserByEmail,
  findUserById,
  updateUserPassword,
  updateUserProfile
};