import express from "express";
const router = express.Router();

import { 
  getCommentsByPost, 
  createComment,
  getCommentDetail
} from "../controllers/comment.controller.js";

import { verifyToken } from "../middlewares/auth.middleware.js";

router.get("/post/:id", verifyToken, getCommentsByPost);

router.post("/", verifyToken, createComment);
router.get("/:id", verifyToken, getCommentDetail);


export default router;
