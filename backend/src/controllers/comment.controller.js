const CommentModel = require("../models/comment.model");

const calculateDepth = (nodes, level) => {
    nodes.forEach(node => {
        node.depth_level = level;
        node.upvotes = node.upvotes || 0;
        node.downvotes = node.downvotes || 0;
        node.rating = node.rating || 0;
        node.is_removed = node.is_removed || 0;

        if (node.children && node.children.length > 0) {
            calculateDepth(node.children, level + 1);
        }
    });
};

const buildCommentTree = (comments) => {
    const map = {};
    const tree = [];

    comments.forEach((c, index) => {
        map[c.id] = index; 
        c.children = [];   
    });

    comments.forEach(c => {
        if (c.parent_id !== null && map[c.parent_id] !== undefined) {
            const parentIndex = map[c.parent_id];
            comments[parentIndex].children.push(c);
        } else {
            tree.push(c);
        }
    });

    calculateDepth(tree, 0);
    return tree;
};


exports.getCommentsByPost = async (req, res) => {
    const postId = req.params.id; 

    if (!postId) {
        return res.status(400).json({ success: false, message: "Thiếu Post ID" });
    }

    try {
        const rawData = await CommentModel.getByPostId(postId);

        const flatComments = JSON.parse(JSON.stringify(rawData));
        const totalCount = flatComments.length;

        const tree = buildCommentTree(flatComments);

        res.json({
            success: true,
            count: totalCount,
            data: tree 
        });

    } catch (err) {
        console.error("Lỗi Controller:", err);
        res.status(500).json({ success: false, error: err.message });
    }
};

exports.createComment = async (req, res) => {
    try {
        const { content, user_id, post_id, parent_id } = req.body;

        if (!content || !user_id || !post_id) {
            return res.status(400).json({ success: false, message: "Thiếu thông tin" });
        }

        let newDepthLevel = 0;

        if (parent_id) {
            const parentComment = await CommentModel.getById(parent_id);
            if (!parentComment) {
                return res.status(404).json({ success: false, message: "Comment cha không tồn tại" });
            }
            newDepthLevel = (parentComment.depth_level || 0) + 1;
        }

        const result = await CommentModel.create({
            content, 
            user_id, 
            post_id, 
            parent_id, 
            depth_level: newDepthLevel
        });

        res.status(201).json({ 
            success: true, 
            message: "Đã đăng bình luận!", 
            commentId: result.insertId 
        });

    } catch (err) {
        console.error(err);
        res.status(500).json({ success: false, error: err.message });
    }
};

exports.voteComment = async (req, res) => {
    const { id } = req.params; 
    const { type } = req.body; // 'up' hoặc 'down'

    if (!type || (type !== 'up' && type !== 'down')) {
        return res.status(400).json({ success: false, message: "Loại vote không hợp lệ (chỉ 'up' hoặc 'down')" });
    }

    try {
        const result = await CommentModel.vote(id, type);
        
        if (result.affectedRows === 0) {
            return res.status(404).json({ success: false, message: "Không tìm thấy bình luận để vote" });
        }

        res.json({ 
            success: true, 
            message: `Đã ${type === 'up' ? 'Upvote' : 'Downvote'} thành công!`,
            voteType: type 
        });

    } catch (err) {
        console.error("Lỗi Vote:", err);
        return res.status(500).json({ success: false, error: err.message });
    }
};