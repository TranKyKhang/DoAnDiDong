import db from "../config/db.js";
import path from "path";
import fs from "fs";
import { removeFile } from "../utils/file.js";

export const getPostDetail = async (req, res) => {
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
    res.status(500).json({ message: "Server error" });
  }
};


export const updatePost = async (req, res) => {
  try {
    const { postId } = req.params;
    const userId = req.user.id;
    const { content, link_url } = req.body;

    const images = req.files?.images || [];
    const videoFile = req.files?.video?.[0];


    const [existing] = await db.execute(
      "SELECT * FROM POSTS WHERE id = ?",
      [postId]
    );

    if (existing.length === 0) {
      return res.status(404).json({
        success: false,
        message: "Không tìm thấy bài viết"
      });
    }

    const post = existing[0];


    if (post.user_id !== userId) {
      return res.status(403).json({
        success: false,
        message: "Bạn không thể chỉnh sửa bài viết này"
      });
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
      if (post.video) {
        await removeFile(post.video);
      }

      const videoPath = `/upload/posts/videos/${videoFile.filename}`;
      fields.push("video = ?");
      values.push(videoPath);
    }

    if (fields.length > 0) {
      const sql = `
        UPDATE POSTS
        SET ${fields.join(", ")}
        WHERE id = ?
      `;
      values.push(postId);
      await db.execute(sql, values);
    }


    if (images.length > 0) {
      const [oldImages] = await db.execute(
        "SELECT image FROM POST_IMAGES WHERE post_id = ?",
        [postId]
      );

      for (const img of oldImages) {
        if (img.image) {
          await removeFile(img.image);
        }
      }

      await db.execute(
        "DELETE FROM POST_IMAGES WHERE post_id = ?",
        [postId]
      );

      const imgValues = images.map(file => [
        postId,
        `/upload/posts/images/${file.filename}`
      ]);

      await db.query(
        "INSERT INTO POST_IMAGES (post_id, image) VALUES ?",
        [imgValues]
      );
    }

    return res.json({
      success: true,
      message: "Cập nhật bài viết thành công"
    });

  } catch (err) {
    console.error("UPDATE POST ERROR:", err);
    return res.status(500).json({
      success: false,
      message: "Server error"
    });
  }
};

export const getFollowedFeed = async (req, res) => {
  const userId = req.user.id;
  const limit = parseInt(req.query.limit) || 20;
  const offset = (parseInt(req.query.page) - 1) * limit || 0;

  try {
    const [rows] = await db.query(
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
            WHERE f.user_id = ? AND p.is_removed = 0
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
  const userId = req.user.id;

  try {
    console.log("ok")
    const [rows] = await db.query(
      `SELECT 
    p.*,
    COALESCE(
        (SELECT JSON_ARRAYAGG(m.image) -- Đổi JSON_AGG thành JSON_ARRAYAGG
         FROM post_images m 
         WHERE m.post_id = p.id), 
        JSON_ARRAY()
    ) AS images,
    c.name AS community_name,
    u.username AS author_name,
    u.avatar AS authorAvatarUrl,
    v.type AS user_vote_status,
    ((p.upvotes - p.downvotes) / 
    POWER(TIMESTAMPDIFF(MINUTE, p.created_at, CURRENT_TIMESTAMP)/3600 + 2, 1.5)) AS hot_score
FROM posts p
INNER JOIN communities c ON p.community_id = c.id
INNER JOIN users u ON p.user_id = u.id
LEFT JOIN votes v ON v.post_id = p.id 
    AND v.target = 'post' 
    AND v.user_id = 6
WHERE p.is_removed = 0
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

export const getUserPost = async (req, res) => {
  const { userId } = req.body;
  const limit = parseInt(req.query.limit) || 20;
  const offset = (parseInt(req.query.page) - 1) * limit || 0;

  try {
    const [rows] = await db.query(
      `SELECT 
                p.*, 
                c.name AS community_name,
                u.username AS author_name,
                u.avatar AS authorAvatarUrl,
                v.type AS user_vote_status
            FROM posts p
            INNER JOIN communities c ON p.community_id = c.id
            INNER JOIN users u ON p.user_id = u.id
            LEFT JOIN votes v ON v.post_id = p.id 
                AND v.target = 'post' 
                AND v.user_id = ?
            WHERE p.user_id = ? AND p.is_removed = 0
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

export const getCommunityPosts = async (req, res) => {
  const { userId } = req.body;
  const { id } = req.params;
  const limit = parseInt(req.query.limit) || 20;
  const offset = (parseInt(req.query.page) - 1) * limit || 0;

  try {
    const [rows] = await db.query(
      `SELECT 
                p.*, 
                c.name AS community_name,
                u.username AS author_name,
                v.type AS user_vote_status
            FROM posts p
            INNER JOIN communities c ON p.community_id = c.id
            INNER JOIN users u ON p.user_id = u.id
            LEFT JOIN votes v ON v.post_id = p.id 
                AND v.target = 'post' 
                AND v.user_id = ?
            WHERE p.community_id = ? AND p.is_removed = 0
            ORDER BY p.created_at DESC
            LIMIT ? OFFSET ?;`,
      [userId, id, limit, offset]
    );
    console.log(rows);
    res.json({
      success: true,
      data: rows
    });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
};

export const createPost = async (req, res) => {
    try {
        const { title, content, community_id, link_url } = req.body;
        const userId = req.user.id; 

        let videoPath = null;
        if (req.files?.video) {
            videoPath = `/upload/posts/videos/${req.files.video[0].filename}`;
        }

        const postSql = `
            INSERT INTO posts (title, content, community_id, user_id, link_url, video, rating) 
            VALUES (?, ?, ?, ?, ?, ?, 0)`;
        
        const [postResult] = await db.query(postSql, [
            title, 
            content || "", 
            community_id, 
            userId, 
            link_url || null, 
            videoPath         
        ]);

        const postId = postResult.insertId;

        if (req.files?.images && req.files.images.length > 0) {
            const imgValues = req.files.images.map(file => [
                postId, 
                `/upload/posts/images/${file.filename}`
            ]);

            await db.query("INSERT INTO post_images (post_id, image) VALUES ?", [imgValues]);
        }

        res.status(201).json({
            success: true,
            message: "Tạo bài viết thành công",
        });

    } catch (err) {
        res.status(500).json({ success: false, message: err.message });
    }
};