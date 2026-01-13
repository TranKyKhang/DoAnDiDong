import PostModel from "../models/post.model.js";

export const getPostDetail = async (req, res) => {
    const postId = req.params.id;

    try {
        const postData = await PostModel.getDetail(postId);

        if (!postData) {
            return res.status(404).json({ success: false, message: "Không tìm thấy bài viết" });
        }

        const imagesArray = await PostModel.getImages(postId);

        res.json({
            success: true,
            data: {
                ...postData,
                images: imagesArray
            }
        });

    } catch (err) {
        console.error("Lỗi Post Controller:", err);
        res.status(500).json({ success: false, error: err.message });
    }
};