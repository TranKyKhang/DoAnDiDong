import express from "express";
import cors from "cors";
import dotenv from "dotenv";

import userRoutes from "./routes/user.routes.js";
import communityRoutes from "./routes/community.routes.js";
import postRoutes from "./routes/posts.routes.js";
import voteRoutes from "./routes/votes.routes.js";
import commentRoutes from "./routes/comment.routes";
import notificationRoutes from  './routes/notifications.routes';

dotenv.config({ quiet: true });

const app = express();

app.use(cors());
app.use(express.json());

app.get("/", (req, res) => {
  res.json({
    success: true,
    message: "running oke"
  });
});

app.use("/upload", express.static("upload"));
app.use("/api/users", userRoutes);
app.use("/api/communities", communityRoutes);
app.use("/api/posts", postRoutes);
app.use("/api/votes", voteRoutes);
app.use("/api/comments", commentRoutes);
app.use('/api/notifications', notificationRoutes);

export default app; 