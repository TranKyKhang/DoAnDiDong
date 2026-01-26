import db from "../config/db.js";

const Comment = {
  getByPostId: async (postId, userId) => {
    const sql = `
      SELECT 
        c.*, 
        u.username,
        REPLACE(v.type, '"', '') AS user_vote_status,
        u.avatar AS author_avatar
      FROM comments c
      JOIN users u ON c.user_id = u.id
      LEFT JOIN votes v ON v.comment_id = c.id 
                AND v.target = '"comment"' 
                AND v.user_id = ?
      WHERE c.post_id = ?
      ORDER BY c.created_at ASC
    `;
    const [rows] = await db.query(sql, [userId, postId]);
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
      (content, user_id, post_id, parent_id, depth_level, rating)
      VALUES (?, ?, ?, ?, ?, 0)
    `;
    const [result] = await db.query(sql, [
      data.content,
      data.user_id,
      data.post_id,
      data.parent_id || null,
      data.depth_level
    ]);
    return result;
  },
  updateCommentCount: async (postId) => {
    const sql = `UPDATE posts SET comment_count = comment_count + 1 WHERE id = ?`;
    const [result] = await db.query(sql, [postId]);
    return result;
  }
};

export default Comment;
