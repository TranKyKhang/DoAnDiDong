const express = require('express');
const router = express.Router();
const { verifyToken } = require('../middlewares/authMiddleware');
const { changePassword, forgotPassword, verifyResetOtp } = require('../controllers/passwordController');

router.post('/change-password', verifyToken, changePassword);
router.post('/forgot-password', forgotPassword);
router.post('/verify-reset-otp', verifyResetOtp);

module.exports = router;