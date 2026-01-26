const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

const envPath = path.join(__dirname, '..', '..', '.env');
const secretLength = 64; // 512-bit entropy

/**
 * Generate a cryptographically secure random string suitable for JWT secret
 * @returns {string} Base64-encoded random bytes
 */
function generateSecureSecret() {
  return crypto.randomBytes(secretLength).toString('base64');
}

/**
 * Forcefully overwrite or create JWT_SECRET in .env file
 * @param {string} secret - The new secret to write
 */
function forceWriteJwtSecret(secret) {
  let envContent = '';

  // Read existing .env if exists
  if (fs.existsSync(envPath)) {
    envContent = fs.readFileSync(envPath, 'utf8');
  }

  // Remove old JWT_SECRET line if exists
  const lines = envContent.split('\n');
  const newLines = lines.filter(line => !line.trim().startsWith('JWT_SECRET='));

  // Add new secret at the end
  newLines.push(`JWT_SECRET=${secret}`);

  // Write back to .env
  fs.writeFileSync(envPath, newLines.join('\n').trim() + '\n');

  console.log('New JWT_SECRET created');
}

/**
 * Main execution - runs every time this file is required/imported
 */
(function main() {
  try {
    const newSecret = generateSecureSecret();
    forceWriteJwtSecret(newSecret);
  } catch (err) {
    console.error('Lỗi khi tạo và ghi JWT secret:', err.message);
    process.exit(1);
  }
})();