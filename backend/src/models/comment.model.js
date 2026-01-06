const db = require("../config/db");

const Comment = {
    // 1. Lấy danh sách bình luận theo Post ID
    getByPostId: (postId, callback) => {
        const sql = `
            SELECT c.*, u.username 
            FROM comments c 
            JOIN users u ON c.user_id = u.id 
            WHERE c.post_id = ? 
            ORDER BY c.created_at ASC
        `;
        db.query(sql, [postId], callback);
    },

    // 2. Tạo bình luận mới
    create: (data, callback) => {
        const sql = `
            INSERT INTO comments (content, user_id, post_id, parent_id, depth_level) 
            VALUES (?, ?, ?, ?, ?)
        `;
        const values = [
            data.content, 
            data.user_id, 
            data.post_id, 
            data.parent_id || null, 
            data.depth_level || 0
        ];
        db.query(sql, values, callback);
    },

    // 3. Vote bình luận (Up/Down)
    vote: (id, type, callback) => {
        let sql = "";
        
        if (type === 'up') {
            // Upvote: Rating tăng 1
            sql = "UPDATE comments SET upvotes = upvotes + 1, rating = rating + 1 WHERE id = ?";
        } else {
            // Downvote: Rating giảm 1
            sql = "UPDATE comments SET downvotes = downvotes + 1, rating = rating - 1 WHERE id = ?";
        }

        db.query(sql, [id], callback);
    }
};

module.exports = Comment;