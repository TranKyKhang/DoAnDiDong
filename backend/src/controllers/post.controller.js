const db = require("../config/db"); 

exports.getPostDetail = (req, res) => {
    const postId = req.params.id;

    const sql = `
        SELECT p.*, u.username as author_name, c.name as community_name 
        FROM posts p 
        JOIN users u ON p.id = u.id 
        JOIN communities c ON p.id = c.id 
        WHERE p.id = ?`;

    db.query(sql, [postId], (err, result) => {
        if (err) return res.status(500).json({ success: false, error: err.message });
        if (result.length === 0) return res.status(404).json({ success: false, message: "Không tìm thấy bài viết" });

        const postData = result[0];

        const imageSql = `SELECT id as id, image FROM post_images WHERE post_id = ?`;

        db.query(imageSql, [postId], (err, imageResults) => {
            if (err) return res.status(500).json({ success: false, error: err.message });

            const imagesArray = imageResults ? imageResults : [];

            res.json({
                success: true,
                data: {
                    ...postData,
                    images: imagesArray
                }
            });
        });
    });
};