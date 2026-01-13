import db from "../config/db.js"; 

const Post = {
  getDetail: async (postId) => {
    const query = `
      SELECT p.*
      FROM posts p
      WHERE p.id = ?
    `;
    
    const [rows] = await db.query(query, [postId]);
    return rows[0]; 
  },

  getImages: async (postId) => {
    const query = "SELECT id, image FROM post_images WHERE post_id = ?";
    const [rows] = await db.query(query, [postId]);
    return rows; 
  }
};

export default Post;