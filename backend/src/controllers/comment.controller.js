import CommentModel from "../models/comment.model.js";

//BUILD COMMENT TREE

const calculateDepth = (nodes, level) => {
    nodes.forEach(node => {
        node.depth_level = level;
        node.children = node.children || [];
        if (node.children.length > 0) {
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
        if (c.parent_id && map[c.parent_id] !== undefined) {
            comments[map[c.parent_id]].children.push(c);
        } else {
            tree.push(c);
        }
    });

    calculateDepth(tree, 0);
    return tree;
};

// GET COMMENTS BY POST 
export const getCommentsByPost = async (req, res) => {
    console.log(">>> VÀO getCommentsByPost");
    const userId = req.user.id;
    const postId = req.params.id;

    if (!postId) {
        return res.status(400).json({
            success: false,
            message: "Thiếu post_id"
        });
    }

    try {

        const comments = await CommentModel.getByPostId(postId, userId);

        const tree = buildCommentTree(comments);

        console.log("COMMENTS LENGTH =", comments.length);
        
        console.log(tree);

        return res.json({
            success: true,
            count: comments.length,
            data: tree
        });

    } catch (err) {
        console.error("Get comments error:", err);
        return res.status(500).json({
            success: false,
            error: err.message
        });
    }
};


export const createComment = async (req, res) => {
    try {
        const { content, post_id, parent_id } = req.body;
        const user_id = req.user.id;

        if (!content || !post_id) {
            return res.status(400).json({
                success: false,
                message: "Thiếu nội dung hoặc post_id"
            });
        }

        let depth_level = 0;

        if (parent_id) {
            const parent = await CommentModel.getById(parent_id);
            if (!parent) {
                return res.status(404).json({
                    success: false,
                    message: "Comment cha không tồn tại"
                });
            }
            depth_level = parent.depth_level + 1;
        }

        const result = await CommentModel.create({
            content,
            user_id,
            post_id,
            parent_id,
            depth_level
        });
        if (result.affectedRows > 0) {
            await CommentModel.updateCommentCount(post_id);
        }

        return res.status(201).json({
            success: true,
            message: "Đã đăng bình luận!",
            commentId: result.insertId
        });

    } catch (err) {
        console.error("Create comment error:", err);
        return res.status(500).json({
            success: false,
            error: err.message
        });
    }
};


export const getCommentDetail = async (req, res) => {
    try {
      const { id } = req.params;
  
      const comment = await CommentModel.getById(id);
  
      if (!comment) {
        return res.status(404).json({
          success: false,
          message: "Comment không tồn tại"
        });
      }
  
      return res.json({
        success: true,
        data: comment
      });
    } catch (err) {
      return res.status(500).json({
        success: false,
        error: err.message
      });
    }
  };
  