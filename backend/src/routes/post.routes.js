const express = require("express");
const router = express.Router();
const postController = require("../controllers/post.controller");

// Đường dẫn: GET /api/posts/1
router.get("/:id", postController.getPostDetail);

module.exports = router;