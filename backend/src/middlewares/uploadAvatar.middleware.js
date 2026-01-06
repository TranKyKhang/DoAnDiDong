const multer=require("multer");
const path = require("path");

const storage= multer.diskStorage({
    destination: (req, file, cb) => {
    if (file.fieldname === "avatar") {
      cb(null, "upload/avatars");
    } else if (file.fieldname === "banner") {
      cb(null, "upload/banners");
    }
  },
      filename: (req, file, cb) => {
    const extension = file.originalname.split(".").pop();
    cb(null, Date.now() + "-" + file.fieldname + "." + extension);
  }
});

const fileFilter=(req,file,cb)=>{
    const allowed =["image/jpeg", "image/png", "image/webp"];
    if(!allowed.includes(file.mimetype)){
        cb(new Error("only picture"),false);
    }else{
        cb(null,true);
    }
};

module.exports = multer({
  storage,
  limits: { fileSize: 5 * 1024 * 1024 },
  fileFilter
});