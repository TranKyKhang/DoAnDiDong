import express from "express";
import cors from "cors";
import dotenv from "dotenv";

import postRoutes from "./routes/post.routes.js";
import commentRoutes from "./routes/comment.routes.js";
import notificationRoutes from "./routes/notifications.routes.js";

dotenv.config(); 

const app = express();
const PORT = process.env.PORT || 3000; 

app.use(cors());
app.use(express.json()); 

app.get("/", (req, res) => {
  res.json({
    success: true,
    message: "Server is running successfully!"
  });
});

app.use("/api/posts", postRoutes);
app.use("/api/comments", commentRoutes);
app.use('/api/notifications', notificationRoutes);

app.listen(PORT, () => {
  console.log(`Server is running on port ${PORT}`);
});

export default app;