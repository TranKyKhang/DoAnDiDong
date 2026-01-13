import NotificationModel from '../models/notifications.model.js';
import db from '../config/db.js'; 

export const createNotification = async (req, res) => {
    const { type, content, recipient_id, sender_id, post_id, comment_id } = req.body;

    if (!type || !content || !recipient_id) {
        return res.status(400).json({
            success: false,
            message: 'Vui lòng cung cấp đủ: type, content và recipient_id'
        });
    }

    const validTypes = ['post', 'comment', 'vote'];
    if (!validTypes.includes(type)) {
        return res.status(400).json({
            success: false,
            message: 'Type không hợp lệ. Chỉ chấp nhận: post, comment, vote'
        });
    }

    try {
        const result = await NotificationModel.create({
            type,
            content,
            recipient_id,
            sender_id,
            post_id,
            comment_id
        });

        return res.status(201).json({
            success: true,
            message: 'Tạo thông báo thành công',
            data: {
                id: result.insertId,
                recipient_id,
                type,
                content
            }
        });

    } catch (err) {
        console.error('Lỗi tạo thông báo:', err);
        
        if (err.code === 'ER_NO_REFERENCED_ROW_2') {
            return res.status(404).json({
                success: false,
                message: 'Người nhận, người gửi hoặc bài viết không tồn tại.'
            });
        }

        return res.status(500).json({ 
            success: false, 
            message: "Lỗi Server nội bộ",
            error: err.message 
        });
    }
};

export const getNotificationsByUser = async (req, res) => {
    const userId = req.params.userId;

    if (!userId) {
        return res.status(400).json({ success: false, message: "Thiếu User ID" });
    }

    try {
        const checkUserSql = "SELECT id FROM users WHERE id = ?";
        const [users] = await db.query(checkUserSql, [userId]);

        if (users.length === 0) {
            return res.status(404).json({
                success: false,
                message: "Người dùng không tồn tại"
            });
        }

        const results = await NotificationModel.getByUserId(userId);

        return res.status(200).json({
            success: true,
            data: results 
        });

    } catch (err) {
        console.error("Lỗi lấy thông báo:", err);
        return res.status(500).json({
            success: false,
            error: err.message
        });
    }
};