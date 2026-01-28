import express from "express";
import upload from "../middlewares/uploadMedia.middleware.js";
import { 
  updatePost,
  getPostDetail,
  getFollowedFeed,
  getPopularPosts,
  getUserPost,
  getCommunityPosts,
  createPost,
  getNewestCommunityPosts,      
  getRisingCommunityPosts,
  removePost       
} from "../controllers/posts.controller.js";
import { verifyToken } from '../middlewares/auth.middleware.js';

const router = express.Router();

router.post(
  "/create",
  verifyToken,
  upload.fields([
    { name: "images", maxCount: 20 },
    { name: "video", maxCount: 1 }
  ]),
  createPost 
);

router.put(
  "/:postId",
  verifyToken,
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

router.get("/community/:id/newest", verifyToken, getNewestCommunityPosts);
router.get("/community/:id/rising",  verifyToken, getRisingCommunityPosts);
router.delete("/:id", verifyToken, removePost);
router.get("/:id", verifyToken, getPostDetail);

export default router;