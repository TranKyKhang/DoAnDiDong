import db from "../config/db.js";

export const handleVote = async (req, res) => {
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

        const updateTable = await db.query(
            "UPDATE ?? SET upvotes = upvotes + ?, downvotes = downvotes + ? WHERE id = ?",
            [tableName, upDelta, downDelta, id]
        );

        const updateRating = await db.query(
            "UPDATE ?? SET rating = cast(upvotes as signed) - cast(downvotes as signed) WHERE id = ?",
            [tableName, id]
        );

        res.json({ success: true});

    } catch (err) {
        res.status(500).json({ success: false, message: err.message})
    }
};