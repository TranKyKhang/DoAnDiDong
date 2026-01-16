import express from "express";
import upload from "../middlewares/uploadMedia.middleware.js";
import  {updatePost} from "../controllers/posts.controller.js";
import  {getPostDetail} from "../controllers/posts.controller.js";
import { verifyToken } from '../middlewares/auth.middleware.js';
import { getFollowedFeed, getPopularPosts, getUserPost, getCommunityPosts, createPost } from "../controllers/posts.controller.js";

const router = express.Router();

router.post(
  "/create",verifyToken,
  upload.fields([
    { name: "images", maxCount: 20 },
    { name: "video", maxCount: 1 }
  ]),
  createPost 
);

router.put(
  "/:postId",verifyToken,
  upload.fields([
    { name: "images", maxCount: 20 },
    { name: "video", maxCount: 1 }
  ]),
  updatePost  
);


router.get("/followed", verifyToken, getFollowedFeed);
router.get("/popular", verifyToken, getPopularPosts);
router.get("/my-posts", verifyToken, getUserPost);
router.get("/community/:id", verifyToken, getCommunityPosts);
router.get("/:id", verifyToken, getPostDetail);

export default router; 
