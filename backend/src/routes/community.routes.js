
import express from "express";
import { getAllCommunity } from "../controllers/community.controller.js";
import {banUser} from "../controllers/community.controller.js"
import { unBanUser } from "../controllers/community.controller.js";
import { changeRole } from "../controllers/community.controller.js";
const router = express.Router();

router.put("/ban/:communityId",banUser)
router.put("/unban/:communityId",unBanUser)
router.put("/changerole/:communityId",changeRole)
router.get("/", getAllCommunity);

export default router;
