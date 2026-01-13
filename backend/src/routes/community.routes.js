
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
import {getPostCommunityByID} from '../controllers/community.controller.js';
import upload from '../middlewares/uploadMedia.middleware.js';
import { verifyToken } from '../middlewares/auth.middleware.js';
const router = express.Router();

router.put("/ban/:communityId",banUser)
router.put("/unban/:communityId",unBanUser)
router.put("/changerole/:communityId",changeRole)
router.get("/", getAllCommunity);
//Huy (Communities)
// Định nghĩa API POST /api/communities/create
// Nếu bạn có middleware kiểm tra đăng nhập, đặt nó trước communityController
router.post(
    '/create', upload.fields([
        { name: 'icon', maxCount: 1 }, 
        { name: 'banner', maxCount: 1 }
    ])
    ,createCommunities
);
//Lay danh sach communities theo users
router.get('/user/:userId', getCommunitiesByUser);
// API Tham gia: POST /api/communities/join
router.post('/join', joinCommunity);
// API Rời: POST /api/communities/leave
router.post('/leave', leaveCommunity);
//Lay thong tin cong dong
router.get('/details/:id',getCommunityDetails);
router.get('/posts/:community_id',getPostCommunityByID);
router.put("/:communityId/ban", verifyToken, banUser);
router.put("/:communityId/unBan", verifyToken, unBanUser);
export default router;
