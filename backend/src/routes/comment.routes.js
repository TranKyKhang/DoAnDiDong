import express from "express";
const router = express.Router();

import { 
  getCommentsByPost, 
  createComment 
} from "../controllers/comment.controller.js";

import { verifyToken } from "../middlewares/auth.middleware.js";

// Lấy comment theo post
router.get("/post/:id", getCommentsByPost);

// Tạo comment (bắt buộc đăng nhập)
router.post("/", verifyToken, createComment);

export default router;
