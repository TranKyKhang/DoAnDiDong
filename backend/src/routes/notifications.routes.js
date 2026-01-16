import express from "express";
import {verifyToken} from "../middlewares/auth.middleware.js"
const router = express.Router();
import { createNotification,  getNotificationsByUser} from "../controllers/notification.controller.js"

router.post('/', verifyToken, createNotification);
router.get('/noti', verifyToken,  getNotificationsByUser);

export default router; 