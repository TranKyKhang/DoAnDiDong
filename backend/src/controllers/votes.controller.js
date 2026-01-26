import db from "../config/db.js";

function calculatePostRating(upvotes, totalVotes) {
  if (totalVotes === 0) return 0.0;

  const z = 1.96; // 95% confidence
  const p = upvotes / totalVotes;

  const center = p + (z * z) / (2 * totalVotes);
  const margin = z * Math.sqrt((p * (1 - p) + (z * z) / (4 * totalVotes)) / totalVotes);

  return Number(((center - margin) * 10).toFixed(1));
}

export const handleVote = async (req, res) => {
    console.log("ok")
    const { target, id } = req.params;
    const userId = req.user.id;
    const { type } = req.body;

    const tableName = target.toLowerCase() === "post" ? "posts" : "comments";

    try {
        const [ rows ] = await db.query(
            "SELECT * FROM votes WHERE user_id = ? AND ?? = ?",
            [userId, `${target}_id`, id]
        );

        const prevVote = rows[0] ? rows[0].type.replace(/['"]+/g, '') : null;

        let upDelta = 0;
        let downDelta = 0;

        if (prevVote == type) { // UNDO
            if (type == 'upvote') upDelta = -1;
            else downDelta = -1;
            await db.query(
                "DELETE FROM votes WHERE user_id = ? AND ?? = ?",
                [userId, `${target}_id`, id]
            );
        } else if (prevVote != null) { // FLIP
            if (type == 'upvote') {
                upDelta = 1;
                downDelta = -1;
            } else {
                upDelta = -1;
                downDelta = 1;
            }
            await db.query(
                "UPDATE votes SET type = ? WHERE user_id = ? AND ?? = ?",
                [type == 'upvote' ? 1 : 2, userId, `${target}_id`, id]
            );
        } else { // NEW
            if (type == 'upvote') upDelta = 1;
            else downDelta = 1;
            await db.query(
                "INSERT INTO votes (user_id, ??, target, type) VALUES (?, ?, ?, ?)",
                [`${target}_id`, userId, id, target == 'post' ? 1 : 2, type == 'upvote' ? 1 : 2]
            );
        }

        // Cập nhật upvotes/downvotes và rating cho bài viết/bình luận
        await db.query(
            "UPDATE ?? SET upvotes = upvotes + ?, downvotes = downvotes + ? WHERE id = ?",
            [tableName, upDelta, downDelta, id]
        );

        await db.query(
            "UPDATE ?? SET rating = cast(upvotes as signed) - cast(downvotes as signed) WHERE id = ?",
            [tableName, id]
        );

        // Nếu vote cho post → cập nhật post_rating của user sở hữu post
        if (target.toLowerCase() === "post") {
            const [postInfo] = await db.query(
                "SELECT user_id FROM posts WHERE id = ?",
                [id]
            );

            if (postInfo.length > 0) {
                const ownerId = postInfo[0].user_id;

                const [posts] = await db.query(
                    `SELECT upvotes, downvotes 
                     FROM posts 
                     WHERE user_id = ? AND is_removed = 0`,
                    [ownerId]
                );

                let totalUp = 0;
                let totalVotes = 0;

                posts.forEach(post => {
                    totalUp += post.upvotes || 0;
                    totalVotes += (post.upvotes || 0) + (post.downvotes || 0);
                });

                const newRating = calculatePostRating(totalUp, totalVotes);

                await db.execute(
                    `UPDATE users SET post_rating = ? WHERE id = ?`,
                    [newRating, ownerId]
                );
            }
        }

        res.json({ success: true});

    } catch (err) {
        res.status(500).json({ success: false, message: err.message})
    }
};