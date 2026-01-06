const express = require("express");
const router = express.Router();
const communityController=require("../controllers/community.controller")

router.get("/",communityController.getAllCommunity)

module.exports = router;
