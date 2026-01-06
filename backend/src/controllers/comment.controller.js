const Comment = require("../models/comment.model");
const db = require('../config/db');

exports.getCommentsByPost = (req, res) => {
    const postId = req.params.id;

    if (!postId) {
        return res.status(400).json({ success: false, message: "Thiếu Post ID" });
    }

    const checkPost = "SELECT id FROM posts WHERE id = ?";

    db.query(checkPost, [postId], (err, posts) => {
        if (err) {
            return res.status(500).json({ success: false, error: err.message });
        }
        if (posts.length === 0) {
            return res.status(404).json({
                success: false,
                message: "Bài viết không tồn tại"
            });
        }
        Comment.getByPostId(postId, (err, results) => {
            return res.status(200).json({
                success: true,
                data: results 
            });
        });

    })
};

exports.createComment = (req, res) => {
    const { content, user_id, post_id, parent_id, depth_level } = req.body;

    if (!content || !user_id || !post_id) {
        return res.status(400).json({ success: false, message: "Thiếu thông tin bắt buộc (content, user_id, post_id)" });
    }

    const checkUser = "SELECT id FROM users WHERE id = ?";
    const checkPost = "SELECT id FROM posts WHERE id = ?";

    db.query(checkUser, [user_id], (err, users) => {
        if (err) return res.status(500).json({ success: false, error: err.message });
        
        if (users.length === 0) {
            return res.status(404).json({ success: false, message: "Người dùng không tồn tại" });
        }

        db.query(checkPost, [post_id], (err, posts) => {
            if (err) return res.status(500).json({ success: false, error: err.message });

            if (posts.length === 0) {
                return res.status(404).json({ success: false, message: "Bài viết không tồn tại" });
            }

            Comment.create({
                content, user_id, post_id, parent_id, depth_level
            }, (err, result) => {
                if (err) {
                    return res.status(500).json({ success: false, error: err.message });
                }
                
                res.status(201).json({ 
                    success: true, 
                    message: "Đã đăng bình luận thành công!",
                    commentId: result.insertId
                });
            });
        });
    });
};

exports.voteComment = (req, res) => {
    const { id } = req.params; 
    const { type } = req.body; // 'up' hoặc 'down'

    if (!type || (type !== 'up' && type !== 'down')) {
        return res.status(400).json({ success: false, message: "Loại vote không hợp lệ (chỉ 'up' hoặc 'down')" });
    }

    Comment.vote(id, type, (err, result) => {
        if (err) {
            return res.status(500).json({ success: false, error: err.message });
        }
        
        if (result.affectedRows === 0) {
            return res.status(404).json({ success: false, message: "Không tìm thấy bình luận" });
        }

        res.json({ 
            success: true, 
            message: `Đã ${type === 'up' ? 'Upvote' : 'Downvote'} thành công!`,
            voteType: type 
        });
    });
};