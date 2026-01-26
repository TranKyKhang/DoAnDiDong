// Function to compute hot score using logarithmic scaling and temporal decay,
// as demonstrated in ranking algorithms for content recommendation systems
// (e.g., similar to Hacker News model, where gravity controls decay rate).
const calculateHotScore = (upvotes, downvotes, createdAt, gravity = 1.8) => {
  const netScore = upvotes - downvotes;
  if (netScore <= 0) return 0;                    // Discard negative or zero net interaction

  // Use log10(netScore + 1) to prevent log(1) = 0 and give small positive scores a fair chance
  // This is a standard technique in ranking systems (Hacker News, Reddit variants) to avoid zero-division issues
  const order = Math.log10(netScore + 1);

  const hoursElapsed = (Date.now() - new Date(createdAt).getTime()) / (1000 * 3600);

  // Add small constant (2 hours) to denominator to prevent division by near-zero for very new posts
  // This boosts extremely fresh content without over-penalizing slightly older high-quality posts
  return order / Math.pow(hoursElapsed + 2, gravity);
};

// Function to compute rising score, measuring interaction velocity,
// useful for mitigating cold start by boosting emerging content.
const calculateRisingScore = (upvotes, downvotes, createdAt) => {
  const score = upvotes - downvotes;
  const minutes = (Date.now() - new Date(createdAt).getTime()) / (1000 * 60);
  if (minutes < 1) return score;
  return score / minutes; // Normalized velocity per minute
};

module.exports = { calculateHotScore, calculateRisingScore };