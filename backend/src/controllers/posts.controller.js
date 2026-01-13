import db from "../config/db.js";
import path from "path";
import fs from "fs";
import { removeFile } from "../utils/file.js";

export const getPostDetail = (req, res) => {
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

export const updatePost = async (req, res) => {
  try {
    const { postId } = req.params;
    const userId = 5; 
    const { content, link_url } = req.body;

    const images = req.files?.images || [];
    const videoFile = req.files?.video?.[0];

    
    const [existing] = await db.query("SELECT * FROM POSTS WHERE id = ?", [postId]);
    if (existing.length === 0) {
      return res.status(404).json({ success: false, message: "không tìm thấy bài viết" });
    }
    const post = existing[0];

    if (post.user_id !== userId) {
      return res.status(403).json({ success: false, message: "bạn không thể chỉnh sửa bài viết này" });
    }

    const fields = [];
    const values = [];

    if (content !== undefined) {
      fields.push("content = ?");
      values.push(content);
    }

    if (link_url !== undefined) {
      fields.push("link_url = ?");
      values.push(link_url);
    }

    
    if (videoFile) {
      if (post.video) await removeFile(post.video); 

      const videoPath = `/upload/posts/videos/${videoFile.filename}`;
      fields.push("video = ?");
      values.push(videoPath);
    }

    if (fields.length > 0) {
      const sql = `UPDATE POSTS SET ${fields.join(", ")} WHERE id = ?`;
      values.push(postId);
      await db.query(sql, values);
    }

  
    if (images.length > 0) {
      const [oldImages] = await db.query("SELECT image FROM POST_IMAGES WHERE post_id = ?", [postId]);
      for (const img of oldImages) {
        if (img.image) await removeFile(img.image); 
      }

      
      await db.query("DELETE FROM POST_IMAGES WHERE post_id = ?", [postId]);

    
      const imgValues = images.map(file => [postId, `/upload/posts/images/${file.filename}`]);
      await db.query("INSERT INTO POST_IMAGES (post_id, image) VALUES ?", [imgValues]);
    }

    return res.json({ success: true, message: "cập nhật bìa viết thành công" });
  } catch (err) {
    console.error(err);
    return res.status(500).json({ success: false, message: "Server error" });
  }
};

export const getFollowedFeed = async (req, res) => {
    const { userId } = req.body;
    const limit = parseInt(req.query.limit) || 20;
    const offset = (parseInt(req.query.page) - 1) * limit || 0;

    try {
        const { rows } = await pool.query(
          `SELECT 
                p.*, 
                c.name AS community_name,
                u.username AS author_name,
                v.type AS user_vote_status
            FROM posts p
            INNER JOIN users_communities f ON p.community_id = f.community_id
            INNER JOIN communities c ON p.community_id = c.id
            INNER JOIN users u ON p.user_id = u.id
            LEFT JOIN votes v ON v.post_id = p.id 
                AND v.target = 'post' 
                AND v.user_id = ?
            WHERE f.user_id = ?
            ORDER BY p.created_at DESC
            LIMIT ? OFFSET ?;`, 
          [userId, userId, limit, offset]
        );
        
        res.json({
            success: true,
            data: rows
        });
    } catch (err) {
        res.status(500).json({ success: false, message: err.message });
    }
};

export const getPopularPosts = async (req, res) => {
    const { userId } = req.body;

    try {
        const { rows } = await pool.query(
          `SELECT 
                p.*,
                c.name AS community_name,
                u.username AS author_name,
                v.type AS user_vote_status,
                ((p.upvotes - p.downvotes) / 
                POWER(TIMESTAMPDIFF(MINUTE, p.created_at, CURRENT_TIMESTAMP)/3600 + 2, 1.5)) AS hot_score
            FROM posts p
            INNER JOIN communities c ON p.community_id = c.id
            INNER JOIN users u ON p.user_id = u.id
            LEFT JOIN votes v ON v.post_id = p.id 
                AND v.target = 'post' 
                AND v.user_id = 6
            ORDER BY hot_score DESC
            LIMIT 20;`, 
          [userId]
        );

        res.json({
            success: true,
            data: rows
        });
    } catch (err) {
        res.status(500).json({ success: false, message: err.message });
    }
};