import express from "express";
import uploadAvatar from "../middlewares/uploadAvatar.middleware.js";
import { updateProfile, getAllUsers } from "../controllers/user.controller.js";
import { getUserById } from "../controllers/user.controller.js";


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