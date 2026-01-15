const redis = require('redis');

const client = redis.createClient({
  url: process.env.REDIS_URL || 'redis://127.0.0.1:6379',
  socket: {
    connectTimeout: 15000,
  }
});

client.on('error', err => console.error('Redis Client Error', err));

client.connect().then(() => console.log('Redis connected'));

module.exports = client;