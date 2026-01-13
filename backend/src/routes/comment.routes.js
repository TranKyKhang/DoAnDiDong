import express from "express";
import * as commentController from "../controllers/comment.controller.js"; 

const router = express.Router();

router.get("/:id", commentController.getCommentDetail);

router.get("/post/:id", commentController.getCommentsByPost);

router.post("/", commentController.createComment);

router.patch("/:id/vote", commentController.voteComment);

export default router;