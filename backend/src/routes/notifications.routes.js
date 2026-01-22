import express from "express";
import {verifyToken} from "../middlewares/auth.middleware.js"
const router = express.Router();
import { createNotification,  getNotificationsByUser, updateIsRead} from "../controllers/notification.controller.js"

router.post('/', verifyToken, createNotification);
router.get('/noti', verifyToken,  getNotificationsByUser);
router.patch("/isread/:id", verifyToken, updateIsRead);



export default router; 