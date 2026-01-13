import db from "../config/db.js";
import path from "path";
import fs from "fs";
import { removeFile } from "../utils/file.js";

export const getPostDetail = async (req, res) => {
  console.log("🔥 GET POST DETAIL HIT 🔥");

  try {
    const postId = req.params.id;

    const [rows] = await db.execute(`
      SELECT
        p.id,
        p.title,
        p.content,
        p.video,
        p.link_url,
        p.created_at,
        p.upvotes,
        p.downvotes,
        p.rating,
        p.comment_count,
        p.is_removed,

        u.id AS author_id,
        u.username AS author_name,
        u.display_name AS author_display_name,
        u.avatar AS author_avatar,

        c.id AS community_id,
        c.name AS community_name,
        c.icon AS community_icon
      FROM POSTS p
      JOIN USERS u ON p.user_id = u.id
      LEFT JOIN COMMUNITIES c ON p.community_id = c.id
      WHERE p.id = ?
        AND p.is_removed = false
    `, [postId]);

    console.log("📦 POST ROWS:", rows);

    if (rows.length === 0) {
      return res.status(404).json({ message: "Post not found" });
    }

    const [images] = await db.execute(`
      SELECT id, image
      FROM POST_IMAGES
      WHERE post_id = ?
    `, [postId]);

    res.json({
      success: true,
      data: {
        ...rows[0],
        images
      }
    });

  } catch (err) {
    console.error("❌ GET POST DETAIL ERROR:", err);
    res.status(500).json({ message: "Server error" });
  }
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
