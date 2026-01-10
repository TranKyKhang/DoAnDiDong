
import express from "express";
import { getAllCommunity } from "../controllers/community.controller.js";
import {banUser} from "../controllers/community.controller.js"
import { unBanUser } from "../controllers/community.controller.js";
import { changeRole } from "../controllers/community.controller.js";
import {createCommunities} from '../controllers/community.controller.js';
const router = express.Router();

router.put("/ban/:communityId",banUser)
router.put("/unban/:communityId",unBanUser)
router.put("/changerole/:communityId",changeRole)
router.get("/", getAllCommunity);
// Định nghĩa API POST /api/communities/create
// Nếu bạn có middleware kiểm tra đăng nhập, đặt nó trước communityController
router.post('/create', createCommunities);

export default router;
