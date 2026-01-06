const express = require("express");
const router = express.Router();
const commentController = require("../controllers/comment.controller");

// Lấy bình luận (GET) - URL: /api/comments/post/:id
router.get("/post/:id", commentController.getCommentsByPost);

// Đăng bình luận (POST) - URL: /api/comments
router.post("/", commentController.createComment);

module.exports = router;