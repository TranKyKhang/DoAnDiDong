const db = require("../config/db");

exports.getCommentsByPost = (req, res) => {
    const postId = req.params.id;    
    // Lấy comment kèm tên user để hiển thị ai là người viết
    const sql = `
        SELECT c.*, u.username 
        FROM comments c 
        JOIN users u ON c.user_id = u.id 
        WHERE c.post_id = ? 
        ORDER BY c.created_at ASC`;

    db.query(sql, [postId], (err, results) => {
        if (err) return res.status(500).json({ success: false, error: err.message });
        res.json({ success: true, data: results });
    });
};
exports.createComment = (req, res) => {
    // Nhận dữ liệu từ body của request
    const { content, user_id, post_id, parent_id, depth_level } = req.body;

    // Câu lệnh SQL: Nếu không có parent_id (bình luận gốc), MySQL sẽ nhận giá trị NULL
    const sql = `INSERT INTO comments (content, user_id, post_id, parent_id, depth_level) VALUES (?, ?, ?, ?, ?)`;
    
    db.query(sql, [content, user_id, post_id, parent_id || null, depth_level || 0], (err, result) => {
        if (err) {
            // Nếu bị lỗi Foreign Key (như bạn gặp trong Navicat), nó sẽ báo lỗi ở đây
            return res.status(500).json({ success: false, error: err.message });
        }
        
        res.json({ 
            success: true, 
            message: "Đã đăng bình luận thành công!",
            commentId: result.insertId // Trả về ID của bình luận vừa tạo
        });
    });
};