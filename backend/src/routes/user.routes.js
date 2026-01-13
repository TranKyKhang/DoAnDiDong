import express from "express";
import uploadAvatar from "../middlewares/uploadAvatar.middleware.js";
import { verifyToken } from '../middlewares/auth.middleware.js';
import { googleLogin } from '../controllers/google.controller.js';
import {
  register,
  login,
  forgotPassword,
  verifyResetOtp
} from '../controllers/auth.Controller.js';
import {
  getAllUsers,
  getUserById,
  getProfile,
  updateProfile,
  changePassword
} from "../controllers/user.controller.js";

const router = express.Router();

// User update
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

// Auth routes
router.post('/register', register);
router.post('/login', login);
router.post('/forgot-password', forgotPassword);
router.post('/verify-reset-otp', verifyResetOtp);
router.post('/google-login', googleLogin);

// Profile routes (cần token)
router.get('/profile', verifyToken, getProfile);
router.put('/profile-update', verifyToken, uploadAvatar.fields([
    { name: "avatar", maxCount: 1 },
    { name: "banner", maxCount: 1 }
  ]),
  updateProfile);
router.post('/change-password', verifyToken, changePassword);
router.get("/users", verifyToken, getAllUsers);

// Export ES Module
export default router;
