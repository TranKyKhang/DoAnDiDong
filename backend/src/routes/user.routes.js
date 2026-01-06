const express = require("express");
const router = express.Router();
const userController = require("../controllers/user.controller");
const uploadAvatar=require("../middlewares/uploadAvatar.middleware")
router.put(
  "/profile",
  uploadAvatar.fields([
    { name: "avatar", maxCount: 1 },
    { name: "banner", maxCount: 1 }
  ]),
  userController.updateProfile
);

router.get("/", userController.getAllUsers);

module.exports = router;
