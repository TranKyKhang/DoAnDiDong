import { removeFile } from "../utils/file.js";
import db from "../config/db.js";
import bcrypt from 'bcryptjs';

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
  try {
    const userId = req.user.id;

    const { display_name, bio } = req.body || {};

    const avatar = req.files?.avatar?.[0]?.filename;
    const banner = req.files?.banner?.[0]?.filename;

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

    if (avatar) {
      fields.push("avatar = ?");
      params.push(`/upload/avatars/${avatar}`);
    }

    if (banner) {
      fields.push("banner = ?");
      params.push(`/upload/banners/${banner}`);
    }

    if (fields.length === 0) {
      return res.status(400).json({
        success: false,
        message: "Không có dữ liệu để cập nhật"
      });
    }

    const sql = `
      UPDATE users
      SET ${fields.join(", ")}
      WHERE id = ?
    `;

    params.push(userId);

    await db.execute(sql, params);

    res.json({
      success: true,
      message: "Cập nhật profile thành công"
    });
  } catch (err) {
    console.error("UPDATE PROFILE ERROR:", err);
    res.status(500).json({
      success: false,
      message: "Lỗi server"
    });
  }
};



export const getProfile = async (req, res) => {
  try {
    

    const [rows] = await db.execute(
      "SELECT id,bio, email, username, display_name, banner, avatar,post_rating,comment_rating FROM users WHERE id = ?",
      [req.user.id]
    );

    console.log("DB rows:", rows);

    if (rows.length === 0) {
      return res.status(404).json({
        success: false,
        message: "User not found"
      });
    }

    res.json(rows[0]);
  } catch (error) {
    console.error("GET PROFILE ERROR:", error);
    res.status(500).json({ message: "Server error" });
  }
};

// export const updateProfile = async (req, res) => {
//   const { display_name, bio, avatar, banner } = req.body;
//   try {
//     await pool.execute(
//       'UPDATE users SET display_name = ?, bio = ?, avatar = ?, banner = ? WHERE id = ?',
//       [display_name || null, bio || null, avatar || null, banner || null, req.user.id]
//     );

//     const [rows] = await pool.execute(
//       'SELECT id, email, username, display_name, bio, avatar, banner, post_rating, comment_rating, created_at FROM users WHERE id = ?',
//       [req.user.id]
//     );
//     const user = rows[0];
//     user.created_at = user.created_at.toISOString();
//     res.json(user);
//   } catch (error) {
//     console.error('Update profile error:', error);
//     res.status(500).json({ success: false, message: 'Lỗi server' });
//   }
// };

export const changePassword = async (req, res) => {
  const { old_password, new_password } = req.body;
  if (!old_password || !new_password) {
    return res.status(400).json({ success: false, message: 'Thiếu thông tin' });
  }

  try {
    const [rows] = await pool.execute('SELECT password FROM users WHERE id = ?', [req.user.id]);
    if (rows.length === 0) {
      return res.status(404).json({ success: false, message: 'Không tìm thấy người dùng' });
    }

    const match = await bcrypt.compare(old_password, rows[0].password);
    if (!match) {
      return res.status(401).json({ success: false, message: 'Mật khẩu cũ không đúng' });
    }

    const hashed = await bcrypt.hash(new_password, 10);
    await pool.execute('UPDATE users SET password = ? WHERE id = ?', [hashed, req.user.id]);

    res.json({ success: true, message: 'Đổi mật khẩu thành công' });
  } catch (error) {
    console.error('Change password error:', error);
    res.status(500).json({ success: false, message: 'Lỗi server' });
  }
};
