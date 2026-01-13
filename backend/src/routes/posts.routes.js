import express from "express";
import upload from "../middlewares/uploadMedia.middleware.js";
import  {updatePost} from "../controllers/posts.controller.js";
import  {getPostDetail} from "../controllers/posts.controller.js";
import { verifyToken } from '../middlewares/auth.middleware.js';

const router = express.Router();

router.put(
  "/:postId",verifyToken,
  upload.fields([
    { name: "images", maxCount: 20 },
    { name: "video", maxCount: 1 }
  ]),
  updatePost
);

router.get("/:id", verifyToken, getPostDetail);

export default router; 
