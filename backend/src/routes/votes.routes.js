
import express from "express";
import { handleVote } from "../controllers/votes.controller.js"
const router = express.Router();

router.post("/:target/:id", handleVote);

export default router;
