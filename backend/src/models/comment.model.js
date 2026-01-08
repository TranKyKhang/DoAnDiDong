import db from "../config/db.js"; 

const Comment = {
    getByPostId: async (postId) => {
        const query = `
            SELECT 
                c.*, 
                u.username
            FROM comments c
            JOIN users u ON c.user_id = u.id
            WHERE c.post_id = ?
            ORDER BY c.created_at ASC
        `;
        
        const [rows] = await db.query(query, [postId]);
        return rows;
    },
    
    // GET ID BÌNH LUẬN
    getById: async (id) => {
        const query = "SELECT * FROM comments WHERE id = ?";
        const [rows] = await db.query(query, [id]);
        return rows[0];
    },
    
    // POST BÌNH LUẬN
    create: async (data) => {
        const sql = "INSERT INTO comments (content, user_id, post_id, parent_id, depth_level) VALUES (?, ?, ?, ?, ?)";
        const values = [data.content, data.user_id, data.post_id, data.parent_id || null, data.depth_level];
        
        const [result] = await db.query(sql, values);
        return result;
    },

    // VOTE BÌNH LUẬN
    vote: async (id, type) => {
        let sql = "";
        if (type === 'up') {
            sql = "UPDATE comments SET upvotes = upvotes + 1, rating = rating + 1 WHERE id = ?";
        } else {
            sql = "UPDATE comments SET downvotes = downvotes + 1, rating = rating - 1 WHERE id = ?";
        }

        const [result] = await db.query(sql, [id]);
        return result;
    }
};

export default Comment;