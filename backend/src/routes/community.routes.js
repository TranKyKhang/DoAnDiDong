
import express from "express";
import { getAllCommunity } from "../controllers/community.controller.js";
import {banUser} from "../controllers/community.controller.js"
import { unBanUser } from "../controllers/community.controller.js";
import { changeRole } from "../controllers/community.controller.js";
import {createCommunities} from '../controllers/community.controller.js';
import {getCommunitiesByUser} from '../controllers/community.controller.js';
import {joinCommunity} from '../controllers/community.controller.js';
import {leaveCommunity} from '../controllers/community.controller.js';
import {getCommunityDetails} from '../controllers/community.controller.js';
import {getPostCommunityByID, searchCommunities} from '../controllers/community.controller.js';
import {checkIsJoined} from '../controllers/community.controller.js';
import {getCommunityMembers} from '../controllers/community.controller.js';
import upload from '../middlewares/uploadMedia.middleware.js';
import { verifyToken } from '../middlewares/auth.middleware.js';
const router = express.Router();

router.get("/", getAllCommunity);
//Huy (Communities)
// Định nghĩa API POST /api/communities/create
// Nếu bạn có middleware kiểm tra đăng nhập, đặt nó trước communityController
router.post(
    '/create', upload.fields([
        { name: 'icon', maxCount: 1 }, 
        { name: 'banner', maxCount: 1 }
    ]),
    verifyToken
    ,createCommunities
);
//Lay danh sach communities theo users
router.get('/user',verifyToken, getCommunitiesByUser);
//Kiem tra tham gia
router.post("/check_status", verifyToken, checkIsJoined);
// API Tham gia: POST /api/communities/join
router.post('/join',verifyToken, joinCommunity);
// API Rời: POST /api/communities/leave
router.post('/leave',verifyToken, leaveCommunity);
//Lay thong tin thanh vien
router.get("/:communityId/members", verifyToken, getCommunityMembers);
//Lay thong tin cong dong
router.get('/details/:id',verifyToken,getCommunityDetails);
router.get('/posts/:community_id',verifyToken,getPostCommunityByID);
router.put("/:communityId/ban", verifyToken, banUser);
router.put("/:communityId/unBan", verifyToken, unBanUser);
router.put(
  "/:communityId/change-role",
  verifyToken,
  changeRole
);
router.get("/search", verifyToken, searchCommunities);
export default router;
