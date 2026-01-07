const db = require("../config/db");

const Comment = {
    //GET BÌNH LUẬN
    getByPostId: (postId) => {
        return new Promise((resolve, reject) => {
          const query = `
            SELECT 
                c.*, 
                u.username
            FROM comments c
            JOIN users u ON c.user_id = u.id
            WHERE c.post_id = ?
            ORDER BY c.created_at ASC
          `;
          
          db.query(query, [postId], (err, results) => {
            if (err) return reject(err);
            resolve(JSON.parse(JSON.stringify(results))); 
          });
        });
      },
    
    //GET ID BÌNH LUẬN
    getById: (id) => {
        return new Promise((resolve, reject) => {
            db.query("SELECT * FROM comments WHERE id = ?", [id], (err, results) => {
                if (err) return reject(err);
                resolve(results[0]);
            });
        });
    },
    
    //POST BÌNH LUẬN
    create: (data) => {
        return new Promise((resolve, reject) => {
            const sql = "INSERT INTO comments (content, user_id, post_id, parent_id, depth_level) VALUES (?, ?, ?, ?, ?)";
            const values = [data.content, data.user_id, data.post_id, data.parent_id || null, data.depth_level];
            db.query(sql, values, (err, result) => {
                if (err) return reject(err);
                resolve(result);
            });
        });
    },

    //VOTE BÌNH LUẬN
    vote: (id, type) => {
        return new Promise((resolve, reject) => {
            let sql = "";
            
            if (type === 'up') {
                sql = "UPDATE comments SET upvotes = upvotes + 1, rating = rating + 1 WHERE id = ?";
            } else {
                sql = "UPDATE comments SET downvotes = downvotes + 1, rating = rating - 1 WHERE id = ?";
            }
    
            db.query(sql, [id], (err, result) => {
                if (err) return reject(err); 
                resolve(result);             
            });
        });
    }
};

module.exports = Comment;