const express = require("express");
const cors = require("cors");
require('dotenv').config({ quiet: true });



const userRoutes = require("./routes/user.routes");
const communityRoutes=require("./routes/community.routes")

const app = express();


app.use(cors());
app.use(express.json());


app.get("/", (req, res) => {
  res.json({
    success: true,
    message: "running oke"
  });
});

app.use("/uploads", express.static("upload"));
app.use("/api/users", userRoutes);
app.use("/api/communities",communityRoutes);
module.exports = app;
