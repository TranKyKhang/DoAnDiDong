import db from "../config/db.js"; 

const Notification = {
    create: async (data) => {
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

        const [result] = await db.query(sql, values);
        return result;
    },

    getByUserId: async (userId) => {
        const sql = `
            SELECT * FROM notifications 
            WHERE recipient_id = ? 
            ORDER BY created_at DESC
        `;
        const [rows] = await db.query(sql, [userId]);
        return rows;
    }
};

export default Notification; 