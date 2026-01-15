const multer = require("multer");
const path = require("path");

// Storage
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    if (file.fieldname === "avatar") cb(null, "upload/avatars");
    else if (file.fieldname === "banner") cb(null, "upload/banners");
    else cb(new Error("Unknown field"));
  },
  filename: (req, file, cb) => {
    const ext = file.originalname.split(".").pop();
    cb(null, Date.now() + "-" + file.fieldname + "." + ext);
  },
});

// File filter
const fileFilter = (req, file, cb) => {
    console.log("Uploading file:", file.originalname, file.mimetype);

    const allowedImages = [
        "image/jpeg",
        "image/jpg",
        "image/png",
        "image/webp",
        "image/gif",
        "image/heic",
        "image/heif",
         "image/*",
        "application/octet-stream" // tạm thời cho Android/Chrome
    ];

    const allowedVideos = [
        "video/mp4",
        "video/quicktime",
        "video/webm",
        "video/mov"
    ];

    if (file.fieldname === "avatar") {
        if (allowedImages.includes(file.mimetype)) cb(null, true);
        else cb(new Error(`Avatar must be an image, got ${file.mimetype}`), false);
    } else if (file.fieldname === "banner") {
        if (allowedImages.includes(file.mimetype) || allowedVideos.includes(file.mimetype)) cb(null, true);
        else cb(new Error(`Banner must be image or video, got ${file.mimetype}`), false);
    } else {
        cb(new Error("Unknown field"), false);
    }
};

// Export
module.exports = multer({
  storage,
  limits: { fileSize: 5 * 1024 * 1024 },
  fileFilter
});
