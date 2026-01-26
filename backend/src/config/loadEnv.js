import dotenv from 'dotenv';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// Load .env từ thư mục gốc dự án
dotenv.config({ path: path.resolve(__dirname, '../../.env') });  

console.log('Environment variables loaded'); 

export default process.env;