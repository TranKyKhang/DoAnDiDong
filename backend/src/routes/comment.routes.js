const express = require("express");
const router = express.Router();
const commentController = require("../controllers/comment.controller");

router.get("/post/:id", commentController.getCommentsByPost);

router.post("/", commentController.createComment);

router.patch("/:id/vote", commentController.voteComment);

module.exports = router;