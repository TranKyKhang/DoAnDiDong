const express = require("express");
const cors = require("cors");
require("dotenv").config();

const postRoutes = require("./routes/post.routes");
const commentRoutes = require("./routes/comment.routes");
const notificationRoutes = require('./routes/notifications.routes');

const app = express();

app.use(cors());

app.get("/", (req, res) => {
  res.json({
    success: true,
    message: "running oke"
  });
});

app.use(express.json()); 
app.use("/api/posts", postRoutes);
app.use("/api/comments", commentRoutes);
app.use('/api/notifications', notificationRoutes);

module.exports = app;