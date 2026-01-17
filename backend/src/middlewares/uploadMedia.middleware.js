const multer = require("multer");
const path = require("path");


const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    if (file.fieldname === "avatar" || file.fieldname === "images") {
      cb(null, "upload/posts/images"); 
    } else if (file.fieldname === "video") {
      cb(null, "upload/posts/videos"); 
    }
    else if (file.fieldname === "icon" ) {
      cb(null, "upload/communities/icons"); 
    }else if(file.fieldname === "banner"){
       cb(null, "upload/communities/banners"); 
    }
  },
  filename: (req, file, cb) => {

    const ext = path.extname(file.originalname);
    const name = path.basename(file.originalname, ext).replace(/\s+/g, "-");
    cb(null, `${Date.now()}-${name}${ext}`);
  }
});


const fileFilter = (req, file, cb) => {
  const imageTypes = /jpeg|jpg|png|gif/;
  const videoTypes = /mp4|mov|avi|mkv/;
  const ext = path.extname(file.originalname).toLowerCase();

  if ((file.fieldname === "images" ||  
     file.fieldname === "icon" ||   
     file.fieldname === "banner") && imageTypes.test(ext)) {
    cb(null, true);
  } else if (file.fieldname === "video" && videoTypes.test(ext)) {
    cb(null, true);
  } else {
    cb(new Error("File type không hợp lệ"));
  }
};


const upload = multer({
  storage,
  limits: { fileSize: 1024 * 1024 * 1024 }, 
  fileFilter
});

module.exports = upload;
