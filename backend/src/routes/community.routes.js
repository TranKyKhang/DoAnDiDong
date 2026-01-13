
import express from "express";
import multer from 'multer';
import path from 'path';
import fs from 'fs';
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
const router = express.Router();

router.put("/ban/:communityId",banUser)
router.put("/unban/:communityId",unBanUser)
router.put("/changerole/:communityId",changeRole)
router.get("/", getAllCommunity);
//Huy (Communities)
const storage = multer.diskStorage({
  destination: function (req, file, cb) {
    let uploadPath = '';
    if (file.fieldname === 'icon') {
      uploadPath = 'upload/communities/icon';
    } else if (file.fieldname === 'banner') {
      uploadPath = 'upload/communities/banner';
    }
    // Tạo thư mục nếu chưa tồn tại (để tránh lỗi)
    if (!fs.existsSync(uploadPath)){
        fs.mkdirSync(uploadPath, { recursive: true });
    }
    cb(null, uploadPath);
  },
  filename: function (req, file, cb) {
    // Đặt tên file: fieldname-thời_gian.đuôi_file
    // Ví dụ: icon-17058392.png
    const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
    cb(null, file.fieldname + '-' + uniqueSuffix + path.extname(file.originalname));
  }
});
const upload = multer({ storage: storage });
// Định nghĩa API POST /api/communities/create
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

export default router;
