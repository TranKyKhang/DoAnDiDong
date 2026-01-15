import express from "express";
import * as postController from "../controllers/post.controller.js"; 

const router = express.Router();

router.get("/:id", postController.getPostDetail);


export default router;