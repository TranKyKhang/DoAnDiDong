const db = require("../config/db"); 

const Post = {
  getDetail: (postId, callback) => {
    const query = `
      SELECT p.*, u.username as author_name, c.name as community_name
      FROM POSTS p
      JOIN USERS u ON p.id = u.id
      JOIN COMMUNITIES c ON p.id = c.id
      WHERE p.id = ?
    `;
    db.query(query, [postId], callback);
  },

  getImages: (postId, callback) => {
    db.query("SELECT image FROM POST_IMAGES WHERE post_id = ?", [postId], callback);
  }
};

module.exports = Post;