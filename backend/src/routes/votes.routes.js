import express from "express";
import { handleVote } from "../controllers/votes.controller.js"
import { verifyToken } from "../middlewares/auth.middleware.js";
const router = express.Router();

router.post("/:target/:id", verifyToken, handleVote);

export default router;