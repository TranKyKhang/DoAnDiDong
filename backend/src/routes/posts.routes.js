import express from "express";
import upload from "../middlewares/uploadMedia.middleware.js";
import  {updatePost} from "../controllers/posts.controller.js";
import  {getPostDetail} from "../controllers/posts.controller.js";
import { getFollowedFeed } from "../controllers/posts.controller.js";
import { getPopularPosts } from "../controllers/posts.controller.js";

const router = express.Router();

router.put(
  "/:postId",
  upload.fields([
    { name: "images", maxCount: 20 },
    { name: "video", maxCount: 1 }
  ]),
  updatePost
);
router.get("/:id", getPostDetail);
router.get("/followed", getFollowedFeed);
router.get("/popular", getPopularPosts);

export default router; 
