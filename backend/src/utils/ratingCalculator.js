function calculatePostRating(upvotes, totalVotes) {
  if (totalVotes === 0) return 0.0;

  const z = 1.96; // 95% confidence interval
  const p = upvotes / totalVotes;

  const center = p + (z * z) / (2 * totalVotes);
  const margin = z * Math.sqrt((p * (1 - p) + (z * z) / (4 * totalVotes)) / totalVotes);

  return Number(((center - margin) * 10).toFixed(1));
}

async function updateUserPostRating(userId) {
  const [posts] = await db.execute(
    `SELECT upvotes, downvotes 
     FROM posts 
     WHERE user_id = ? AND is_removed = 0`,
    [userId]
  );

  let totalUp = 0;
  let totalVotes = 0;

  posts.forEach(post => {
    totalUp += post.upvotes || 0;
    totalVotes += (post.upvotes || 0) + (post.downvotes || 0);
  });

  const rating = calculatePostRating(totalUp, totalVotes);

  await db.execute(
    `UPDATE users SET post_rating = ? WHERE id = ?`,
    [rating, userId]
  );
}

module.exports = { 
  calculatePostRating, 
  updateUserPostRating 
};