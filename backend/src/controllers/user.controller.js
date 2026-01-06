import { removeFile } from "../utils/file.js";
import db from "../config/db.js";

export const getAllUsers = async (req, res) => {
  try {
    const [rows] = await db.query("SELECT * FROM USERS");

    res.json({
      success: true,
      data: rows
    });
  } catch (err) {
    console.error("MYSQL ERROR:", err); 
    res.status(500).json({
      success: false,
      message: err.message
    });
  }
};


export const updateProfile = async (req, res) => {
  const { id, display_name, bio } = req.body;
  const avatar=req.files?.avatar?.[0]?.filename;
  const banner=req.files?.banner?.[0]?.filename;

  if (!id) {
    return res.status(400).json({
      success: false,
      message: "Thiếu user_id"
    });
  }
   const [user] = await db.query("SELECT avatar, banner FROM users WHERE id = ?", [id]);
   if(avatar) await removeFile(user[0].avatar);
   if (banner) await removeFile(user[0].banner);

  let fields = [];
  let params = [];

  if (display_name !== undefined) {
    fields.push("display_name = ?");
    params.push(display_name);
  }

  if (bio !== undefined) {
    fields.push("bio = ?");
    params.push(bio);
  }

  if (req.files?.avatar) {
    const avatarPath = `/upload/avatars/${req.files.avatar[0].filename}`;
    fields.push("avatar = ?");
    params.push(avatarPath);
  }

  if (req.files?.banner) {
    const bannerPath = `/upload/banners/${req.files.banner[0].filename}`;
    fields.push("banner = ?");
    params.push(bannerPath);
  }

  if (fields.length === 0) {
    return res.status(400).json({
      success: false,
      message: "Không có dữ liệu để cập nhật"
    });
  }

  const sql = `
    UPDATE USERS
    SET ${fields.join(", ")}
    WHERE id = ?
  `;
  params.push(id);

  try {
    await db.execute(sql, params);

    return res.json({
      success: true,
      message: "Cập nhật profile thành công"
    });
  } catch (err) {
    console.error(err);
    return res.status(500).json({
      success: false,
      message: "Lỗi server"
    });
  }
};
