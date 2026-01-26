import './src/config/generateJwtSecret.js';
import './src/config/loadEnv.js';
import app from "./src/app.js"; 

const PORT = process.env.PORT || 3000;

app.listen(PORT, () => {
  console.log(`Server running at http://localhost:${PORT}`);
});
