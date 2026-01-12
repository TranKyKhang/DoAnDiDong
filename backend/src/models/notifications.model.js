const db = require("../config/db");

const Notification = {
    create: (data, callback) => {
        const sql = `
            INSERT INTO notifications 
            (type, content, recipient_id, sender_id, post_id, comment_id, created_at) 
            VALUES (?, ?, ?, ?, ?, ?, NOW())
        `;
        
        const values = [
            data.type, 
            data.content, 
            data.recipient_id, 
            data.sender_id || null, 
            data.post_id || null, 
            data.comment_id || null
        ];

        db.query(sql, values, callback);
    },
    getByUserId: (userId, callback) => {
        const sql = `
            SELECT * FROM notifications 
            WHERE recipient_id = ? 
            ORDER BY created_at DESC
        `;
        db.query(sql, [userId], callback);
    }
};

module.exports = Notification;