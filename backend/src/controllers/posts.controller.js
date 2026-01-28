import db from "../config/db.js";
import { removeFile } from "../utils/file.js";

export const getPostDetail = async (req, res) => {
  try {
    const postId = req.params.id;
    const userId = req.user?.id || null;

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
        c.icon AS community_icon,

        REPLACE(v.type, '"', '') AS user_vote_status

      FROM posts p
      JOIN users u ON p.user_id = u.id
      LEFT JOIN communities c ON p.community_id = c.id
      LEFT JOIN votes v
        ON v.post_id = p.id
        AND v.target = '"post"'
        AND v.user_id = ?
      WHERE p.id = ?
        AND p.is_removed = 0
    `, [userId, postId]);
    console.log(rows)
    if (rows.length === 0) {
      return res.status(404).json({ message: "Post not found" });
    }

    const [images] = await db.execute(`
      SELECT id, image
      FROM post_images
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
    console.error("GET POST DETAIL ERROR:", err);
    res.status(500).json({ message: "Server error" });
  }
};


// Endpoint for updating posts, ensuring ownership verification
// and handling file updates with removal of outdated media.
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

// Endpoint for followed feed, retrieving posts from joined communities,
// ordered chronologically for temporal relevance.
export const getFollowedFeed = async (req, res) => {
  const userId = req.user.id;
  const page = parseInt(req.query.page) || 1;
  const limit = 20;
  const offset = (page - 1) * limit;
  try {
    const [rows] = await db.query(
      `
      SELECT
    p.*,
    COALESCE(
        (
            SELECT JSON_ARRAYAGG(m.image)
            FROM post_images m
            WHERE m.post_id = p.id
        ),
        JSON_ARRAY()
    ) AS images,
    c.name AS community_name,
    u.username AS author_name,
    u.avatar AS authorAvatarUrl,
    v.type AS user_vote_status
FROM posts p
INNER JOIN users_communities f
    ON p.community_id = f.community_id
INNER JOIN communities c
    ON p.community_id = c.id
INNER JOIN users u
    ON p.user_id = u.id
LEFT JOIN votes v
    ON v.post_id = p.id
    AND v.target = '"post"'
    AND v.user_id = ?
WHERE
    f.user_id = ?
    AND f.is_banned = 0     
    AND p.is_removed = 0
ORDER BY p.created_at DESC
LIMIT ? OFFSET ?;
      `,
      [userId, userId, limit, offset]
    );
    res.json({
      success: true,
      page,
      limit,
      data: rows
    });
  } catch (err) {
    res.status(500).json({
      success: false,
      message: err.message
    });
  }
};

// Endpoint for popular posts, incorporating exploration-exploitation
// to address cold start by blending high-score content with recent random posts.
// export const getPopularPosts = async (req, res) => {
//   const userId = req.user.id;
//   const page = parseInt(req.query.page) || 1;
//   const limit = parseInt(req.query.limit) || 20;
//   const offset = (page - 1) * limit;

//   try {
//     // Phần Exploitation: 85% nội dung hot (điểm cao + decay thời gian)
//    const [hotRows] = await db.query(
//   `
//   SELECT
//     p.*,
//     COALESCE(
//         (
//             SELECT JSON_ARRAYAGG(m.image)
//             FROM post_images m
//             WHERE m.post_id = p.id
//         ),
//         JSON_ARRAY()
//     ) AS images,
//     c.name AS community_name,
//     u.username AS author_name,
//     u.avatar AS authorAvatarUrl,
//     v.type AS user_vote_status,
//     (
//         (CAST(p.upvotes AS SIGNED) - CAST(p.downvotes AS SIGNED)) /
//         POWER(TIMESTAMPDIFF(HOUR, p.created_at, NOW()) + 2, 1.8)
//     ) AS hot_score
//   FROM posts p
//   INNER JOIN communities c ON p.community_id = c.id
//   INNER JOIN users u ON p.user_id = u.id
//   INNER JOIN users_communities uc ON uc.community_id = p.community_id
//   LEFT JOIN votes v
//       ON v.post_id = p.id
//       AND v.target = 'post'
//       AND v.user_id = ?
//   WHERE
//       uc.user_id = ?
//       AND uc.is_banned = 0
//       AND p.is_removed = 0
//   ORDER BY hot_score DESC
//   LIMIT ? OFFSET ?;
//   `,
//   [
//     userId,                   
//     userId,                   
//     limit,
//     offset
//   ]
// );

//     // Phần Exploration: 15% bài mới ngẫu nhiên (trong 36 giờ gần nhất)
//     const explorationLimit = limit - hotRows.length;
//     let explorationRows = [];
//     if (explorationLimit > 0) {
//       const [newRows] = await db.query(
//         `
//         SELECT
//           p.*,
//           COALESCE(
//             (SELECT JSON_ARRAYAGG(m.image) FROM post_images m WHERE m.post_id = p.id),
//             JSON_ARRAY()
//           ) AS images,
//           c.name AS community_name,
//           u.username AS author_name,
//           u.avatar AS authorAvatarUrl,
//           v.type AS user_vote_status
//         FROM posts p
//         INNER JOIN communities c ON p.community_id = c.id
//         INNER JOIN users u ON p.user_id = u.id
//         LEFT JOIN votes v ON v.post_id = p.id AND v.target = 'post' AND v.user_id = ?
//         WHERE p.is_removed = 0
//           AND p.created_at > DATE_SUB(NOW(), INTERVAL 36 HOUR)
//         ORDER BY RAND()
//         LIMIT ?
//         `,
//         [userId, explorationLimit]
//       );
//       explorationRows = newRows;
//     }

//     // Kết hợp và shuffle để tránh bias thứ tự cố định
//     const combined = [...hotRows, ...explorationRows];
//     for (let i = combined.length - 1; i > 0; i--) {
//       const j = Math.floor(Math.random() * (i + 1));
//       [combined[i], combined[j]] = [combined[j], combined[i]];
//     }

//     res.json({
//       success: true,
//       page,
//       limit,
//       data: combined
//     });
//   } catch (err) {
//     console.error("GET POPULAR POSTS ERROR:", err);
//     res.status(500).json({ success: false, message: err.message });
//   }
// };
export const getPopularPosts = async (req, res) => {
  const userId = req.user.id;
  const page = Math.max(parseInt(req.query.page) || 1, 1);
  const limit = Math.max(parseInt(req.query.limit) || 20, 1);
  const offset = (page - 1) * limit;

  try {
    const [rows] = await db.query(
      `
      SELECT
        p.*,
        COALESCE(
          (
            SELECT JSON_ARRAYAGG(m.image)
            FROM post_images m
            WHERE m.post_id = p.id
          ),
          JSON_ARRAY()
        ) AS images,
        c.name AS community_name,
        u.username AS author_name,
        u.avatar AS authorAvatarUrl,
        v.type AS user_vote_status,
        (
          (CAST(p.upvotes AS SIGNED) - CAST(p.downvotes AS SIGNED)) /
          POWER(TIMESTAMPDIFF(HOUR, p.created_at, NOW()) + 2, 1.8)
        ) AS hot_score
      FROM posts p
      INNER JOIN communities c ON p.community_id = c.id
      INNER JOIN users u ON p.user_id = u.id
      INNER JOIN users_communities uc
        ON uc.community_id = p.community_id
        AND uc.user_id = ?
        AND uc.is_banned = 0
      LEFT JOIN votes v
        ON v.post_id = p.id
        AND v.target = '"post"'
        AND v.user_id = ?
      WHERE p.is_removed = 0
      ORDER BY hot_score DESC, p.created_at DESC
      LIMIT ? OFFSET ?
      `,
      [userId, userId, limit, offset]
    );

    res.json({
      success: true,
      page,
      limit,
      data: rows
    });
  } catch (err) {
    console.error("GET POPULAR POSTS ERROR:", err);
    res.status(500).json({
      success: false,
      message: "Internal server error"
    });
  }
};

// Endpoint for user's own posts, paginated and ordered by creation time.
export const getUserPost = async (req, res) => {
  const userId = req.user.id;
  const page = Math.max(parseInt(req.query.page) || 1, 1);
  const limit = Math.max(parseInt(req.query.limit) || 20, 1);
  const offset = (page - 1) * limit;
  try {
    const [rows] = await db.query(
      `
      SELECT
          p.*,
          COALESCE(
              (
                  SELECT JSON_ARRAYAGG(m.image)
                  FROM post_images m
                  WHERE m.post_id = p.id
              ),
              JSON_ARRAY()
          ) AS images,
          c.name AS community_name,
          u.username AS author_name,
          u.avatar AS authorAvatarUrl,
          v.type AS user_vote_status
      FROM posts p
      INNER JOIN communities c ON p.community_id = c.id
      INNER JOIN users u ON p.user_id = u.id
      LEFT JOIN votes v
          ON v.post_id = p.id
          AND v.target = '"post"'
          AND v.user_id = ?
      WHERE p.user_id = ?
        AND p.is_removed = 0
      ORDER BY p.created_at DESC
      LIMIT ? OFFSET ?;
      `,
      [userId, userId, limit, offset]
    );
    res.json({
      success: true,
      data: rows,
      pagination: {
        page,
        limit,
        hasMore: rows.length === limit
      }
    });
  } catch (err) {
    res.status(500).json({
      success: false,
      message: err.message
    });
  }
};

// Endpoint for community posts, supporting pagination and vote status.
export const getCommunityPosts = async (req, res) => {
  const userId = req.user.id;
  const { id } = req.params;
  const page = Math.max(parseInt(req.query.page) || 1, 1);
  const limit = Math.max(parseInt(req.query.limit) || 20, 1);
  const offset = (page - 1) * limit;
  try {
  
    const [rows] = await db.query(
      `SELECT
    p.*,
    COALESCE(
        (
            SELECT JSON_ARRAYAGG(m.image)
            FROM post_images m
            WHERE m.post_id = p.id
        ),
        JSON_ARRAY()
    ) AS images,
    c.name AS community_name,
    u.username AS author_name,
    u.avatar AS authorAvatarUrl,
    v.type AS user_vote_status
FROM posts p
INNER JOIN communities c 
    ON p.community_id = c.id
INNER JOIN users u 
    ON p.user_id = u.id


INNER JOIN users_communities uc
    ON uc.community_id = p.community_id
    AND uc.user_id = ?

LEFT JOIN votes v
    ON v.post_id = p.id
    AND v.target = '"post"'
    AND v.user_id = ?

WHERE p.community_id = ?
  AND p.is_removed = 0
  AND uc.is_banned = 0  

ORDER BY p.created_at DESC
LIMIT ? OFFSET ?;
`,
      [userId, userId, id, limit, offset]
    );
    console.log(rows)
    console.log(`Community ${id} - page ${page} - ${rows.length} posts`);
    res.json({
      success: true,
      data: rows
    });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
};

// Endpoint for creating new posts, handling multimedia attachments.
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

// ────────────────────────────────────────────────
// Thêm endpoint: bài viết mới nhất trong cộng đồng
// ────────────────────────────────────────────────
export const getNewestCommunityPosts = async (req, res) => {
  const userId = req.user.id;
  const { id } = req.params;
  const page = Math.max(parseInt(req.query.page) || 1, 1);
  const limit = Math.max(parseInt(req.query.limit) || 20, 1);
  const offset = (page - 1) * limit;

  try {
    const [rows] = await db.query(
      `SELECT
          p.*,
          COALESCE(
              (SELECT JSON_ARRAYAGG(m.image) FROM post_images m WHERE m.post_id = p.id),
              JSON_ARRAY()
          ) AS images,
          c.name AS community_name,
          u.username AS author_name,
          u.avatar AS authorAvatarUrl,
          v.type AS user_vote_status
      FROM posts p
      INNER JOIN communities c ON p.community_id = c.id
      INNER JOIN users u ON p.user_id = u.id
      LEFT JOIN votes v ON v.post_id = p.id AND v.target = 'post' AND v.user_id = ?
      WHERE p.community_id = ? AND p.is_removed = 0
      ORDER BY p.created_at DESC
      LIMIT ? OFFSET ?`,
      [userId, id, limit, offset]
    );

    res.json({ success: true, data: rows });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
};

// ────────────────────────────────────────────────
// Tab Rising – tốc độ tăng vote/phút (trong 24h gần nhất)
// ────────────────────────────────────────────────
export const getRisingCommunityPosts = async (req, res) => {
  const userId = req.user.id;
  const { id } = req.params;
  const page = Math.max(parseInt(req.query.page) || 1, 1);
  const limit = Math.max(parseInt(req.query.limit) || 20, 1);
  const offset = (page - 1) * limit;

  try {
    const [rows] = await db.query(
      `SELECT
          p.*,
          COALESCE(
              (SELECT JSON_ARRAYAGG(m.image) FROM post_images m WHERE m.post_id = p.id),
              JSON_ARRAY()
          ) AS images,
          c.name AS community_name,
          u.username AS author_name,
          u.avatar AS authorAvatarUrl,
          v.type AS user_vote_status,
          (
            (CAST(p.upvotes AS SIGNED) - CAST(p.downvotes AS SIGNED)) /
            GREATEST(TIMESTAMPDIFF(MINUTE, p.created_at, CURRENT_TIMESTAMP), 1)
          ) AS rising_score
      FROM posts p
      INNER JOIN communities c ON p.community_id = c.id
      INNER JOIN users u ON p.user_id = u.id
      LEFT JOIN votes v ON v.post_id = p.id AND v.target = 'post' AND v.user_id = ?
      WHERE p.community_id = ?
        AND p.is_removed = 0
        AND p.created_at > DATE_SUB(NOW(), INTERVAL 24 HOUR)
      ORDER BY rising_score DESC
      LIMIT ? OFFSET ?`,
      [userId, id, limit, offset]
    );

    res.json({ success: true, data: rows });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
};

export const removePost = async (req, res) => {
  console.log("Đã vào API removePost");

  try {
    const postId = req.params.id;
    const { id: userId, role } = req.user;

    const [posts] = await db.execute(
      `SELECT user_id, community_id, is_removed
       FROM posts
       WHERE id = ?`,
      [postId]
    );

    if (posts.length === 0) {
      return res.status(404).json({
        success: false,
        message: "Post không tồn tại"
      });
    }

    const post = posts[0];

    if (post.is_removed) {
      return res.status(400).json({
        success: false,
        message: "Post đã bị remove"
      });
    }

    // ===== Chủ post =====
    if (post.user_id === userId) {
      await db.execute(
        `UPDATE posts SET is_removed = true WHERE id = ?`,
        [postId]
      );

      return res.json({
        success: true,
        message: "Đã xóa bài viết thành công"
      });
    }

    // ===== Admin =====
    if (role === "admin") {
      await db.execute(
        `UPDATE posts SET is_removed = true WHERE id = ?`,
        [postId]
      );

      return res.json({
        success: true,
        message: "Admin đã xóa bài viết"
      });
    }

    // ===== Moderator =====
    const [mods] = await db.execute(
      `SELECT 1
       FROM users_communities
       WHERE user_id = ?
         AND community_id = ?
         AND role = 'moderator'
         AND is_banned = false`,
      [userId, post.community_id]
    );

    if (mods.length > 0) {
      await db.execute(
        `UPDATE posts SET is_removed = true WHERE id = ?`,
        [postId]
      );

      return res.json({
        success: true,
        message: "Moderator đã xóa bài viết"
      });
    }

    // ===== Không có quyền =====
    return res.status(403).json({
      success: false,
      message: "Không có quyền xóa bài viết"
    });

  } catch (err) {
    console.error("REMOVE POST ERROR:", err);
    return res.status(500).json({
      success: false,
      message: "Server error"
    });
  }
};

