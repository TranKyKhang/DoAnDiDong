import db from "../config/db.js";

const Comment = {
  getByPostId: async (postId) => {
    const sql = `
      SELECT c.*, u.username
      FROM comments c
      JOIN users u ON c.user_id = u.id
      WHERE c.post_id = ?
      ORDER BY c.created_at ASC
    `;
    const [rows] = await db.query(sql, [postId]);
    return rows;
  },

  getById: async (id) => {
    const [rows] = await db.query(
      "SELECT * FROM comments WHERE id = ?",
      [id]
    );
    return rows[0];
  },

  create: async (data) => {
    const sql = `
      INSERT INTO comments 
      (content, user_id, post_id, parent_id, depth_level)
      VALUES (?, ?, ?, ?, ?)
    `;
    const [result] = await db.query(sql, [
      data.content,
      data.user_id,
      data.post_id,
      data.parent_id || null,
      data.depth_level
    ]);
    return result;
  }
};

export default Comment;
