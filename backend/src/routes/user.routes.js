import express from "express";
import uploadAvatar from "../middlewares/uploadAvatar.middleware.js";
import { updateProfile, getAllUsers } from "../controllers/user.controller.js";
import { getUserById } from "../controllers/user.controller.js";

const express = require('express');
import { verifyToken } from '../middlewares/auth.middleware.js';
const { googleLogin } = require('../controllers/google.controller');
import {
  register,
  login,
  forgotPassword,
  verifyResetOtp
} from '../controllers/auth.controller.js';
import {
  getProfile,
  updateProfile,
  changePassword
} from '../controllers/user.controller.js';

const router = express.Router();

router.put(
  "/update-profile",
  uploadAvatar.fields([
    { name: "avatar", maxCount: 1 },
    { name: "banner", maxCount: 1 }
  ]),
  updateProfile
);

router.get("/", getAllUsers);

router.get("/:id", getUserById);

export default router; 
// Auth routes
router.post('/register', register);
router.post('/login', login);
router.post('/forgot-password', forgotPassword);
router.post('/verify-reset-otp', verifyResetOtp);

// Profile routes (cần token)
router.get('/profile', verifyToken, getProfile);
router.put('/profile', verifyToken, updateProfile);
router.post('/change-password', verifyToken, changePassword);

module.exports = router;




