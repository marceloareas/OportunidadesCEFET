const target = process.env.FRONTEND_PROXY_TARGET || 'http://localhost:8080';

module.exports = {
  '/api': {
    target,
    secure: false,
    changeOrigin: true,
    pathRewrite: {
      '^/api': '',
    },
    logLevel: 'debug',
  },
};
