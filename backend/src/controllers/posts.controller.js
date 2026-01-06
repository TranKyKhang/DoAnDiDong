import db from "../config/db.js";
import path from "path";
import fs from "fs";
import { removeFile } from "../utils/file.js";



export const updatePost = async (req, res) => {
  try {
    const { postId } = req.params;
    const userId = 5; 
    const { content, link_url } = req.body;

    const images = req.files?.images || [];
    const videoFile = req.files?.video?.[0];

    
    const [existing] = await db.query("SELECT * FROM POSTS WHERE id = ?", [postId]);
    if (existing.length === 0) {
      return res.status(404).json({ success: false, message: "Post not found" });
    }
    const post = existing[0];

    if (post.user_id !== userId) {
      return res.status(403).json({ success: false, message: "You are not allowed to edit this post" });
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

    return res.json({ success: true, message: "Post updated successfully" });
  } catch (err) {
    console.error(err);
    return res.status(500).json({ success: false, message: "Server error" });
  }
};
