import fs from "fs/promises";
import path from "path";

export async function removeFile(filename) {
  if (!filename) return;

  
  const relativePath = filename.startsWith("/") ? filename.slice(1) : filename;

  const filePath = path.join(process.cwd(), relativePath); 


  try {
    await fs.unlink(filePath);
    console.log("Xóa file thành công:", filePath);
  } catch (err) {
    if (err.code === "ENOENT") {
      console.log("File không tồn tại:", filePath);
    } else {
      console.error("Xóa file lỗi:", err.message);
    }
  }
}