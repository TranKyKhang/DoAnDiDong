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

module.exports = generateDiscriminator;