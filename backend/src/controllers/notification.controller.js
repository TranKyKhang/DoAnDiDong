import db from "../config/db.js";

export const createNotification = (req, res) => {
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

    const sql = `
        INSERT INTO notifications (type, content, recipient_id, sender_id, post_id, comment_id, created_at) 
        VALUES (?, ?, ?, ?, ?, ?, NOW())
    `;

    db.query(sql, [type, content, recipient_id, sender_id, post_id, comment_id], (err, result) => {
        if (err) {
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
    });
};


export const getNotificationsByUser = async (req, res) => {

    const userId = req.user?.id;
    console.log("userId =", userId);

    if (!userId) {
        return res.status(401).json({
            success: false,
            message: "Chưa đăng nhập"
        });
    }

    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 20;
    const offset = (page - 1) * limit;

    const sql = `
        SELECT 
            n.*,
            n.comment_id,
            u.id AS sender_id,
            u.username AS sender_name,
            u.avatar AS sender_avatar
        FROM notifications n
        LEFT JOIN users u ON n.sender_id = u.id
        WHERE n.recipient_id = ?
        ORDER BY n.created_at DESC
        LIMIT ? OFFSET ?
    `;

    try {

        const [results] = await db.query(sql, [userId, limit, offset]);

        return res.status(200).json({
            success: true,
            page,
            limit,
            total: results.length,
            data: results
        });
    } catch (err) {
        console.error("Lỗi lấy thông báo:", err);
        return res.status(500).json({
            success: false,
            message: "Lỗi server",
            error: err.message
        });
    }
};
export const updateIsRead = async (req, res) => {
    const userId = req.user?.id;
    const notificationId = req.params.id;

    if (!userId) {
        return res.status(401).json({
            success: false,
            message: "Chưa đăng nhập"
        });
    }

    if (!notificationId || isNaN(notificationId)) {
        return res.status(400).json({
            success: false,
            message: "ID thông báo không hợp lệ"
        });
    }

    const sql = `
        UPDATE notifications
        SET is_read = 1
        WHERE id = ? AND recipient_id = ?
    `;

    try {
        const [result] = await db.query(sql, [notificationId, userId]);

        if (result.affectedRows === 0) {
            return res.status(404).json({
                success: false,
                message: "Thông báo không tồn tại hoặc không thuộc về bạn"
            });
        }

        return res.status(200).json({
            success: true,
            message: "Đã đánh dấu thông báo là đã đọc"
        });
    } catch (err) {
        console.error("Lỗi cập nhật is_read:", err);
        return res.status(500).json({
            success: false,
            message: "Lỗi server",
            error: err.message
        });
    }
};
