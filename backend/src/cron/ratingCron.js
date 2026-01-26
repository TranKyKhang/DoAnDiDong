import cron from 'node-cron';
import db from '../config/db.js';
import { updateUserPostRating } from '../utils/ratingCalculator.js';

// Cron: chạy mỗi 30 phút
cron.schedule('*/30 * * * *', async () => {
  console.log('Cron: Bắt đầu tính lại post_rating...');
  try {
    const [users] = await db.execute(
      `SELECT DISTINCT user_id FROM posts WHERE is_removed = 0`
    );

    for (const { user_id } of users) {
      await updateUserPostRating(user_id);
    }

    console.log('Cron: Hoàn tất tính post_rating');
  } catch (err) {
    console.error('Cron job thất bại:', err);
  }
});

console.log('Rating cron đã khởi động (mỗi 30 phút)');