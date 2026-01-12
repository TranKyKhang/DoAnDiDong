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
  const { id, display_name, bio } = req.body;
  const avatar=req.files?.avatar?.[0]?.filename;
  const banner=req.files?.banner?.[0]?.filename;
  console.log("BODY:", req.body);
  console.log("FILES:", req.files);
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


export const getUserById = async (req, res) => {
  try {
    const { id } = req.params;

    const [rows] = await db.query(
      `
      SELECT 
        display_name,
        bio,
        avatar,
        banner
      FROM USERS
      WHERE id = ?
      `,
      [id]
    );

    if (rows.length === 0) {
      return res.status(404).json({
        message: "User not found"
      });
    }

    res.json(rows[0]);

  } catch (err) {
    console.error("MYSQL ERROR:", err);
    res.status(500).json({
      message: err.message
    });
  }
};

export const getProfile = async (req, res) => {
  try {
    const [rows] = await pool.execute(
      'SELECT id, email, username, display_name, bio, avatar, banner, post_rating, comment_rating, created_at FROM users WHERE id = ?',
      [req.user.id]
    );
    if (rows.length === 0) {
      return res.status(404).json({ success: false, message: 'Không tìm thấy người dùng' });
    }
    const user = rows[0];
    user.created_at = user.created_at.toISOString();
    res.json(user);
  } catch (error) {
    console.error('Get profile error:', error);
    res.status(500).json({ success: false, message: 'Lỗi server' });
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
