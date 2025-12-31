// environments/environment.ts
export const environment = {
  production: false,

  apiUrls: {
    shopApp: 'http://localhost:8080/api',
    shopApp1: 'http://localhost:8080',

    shopAdmin: 'http://localhost:8081/api',

    shopAuth: 'http://localhost:8082/auth',
  },

  vnpay: {
    returnUrl: 'http://localhost:4200/payment/result',
  },

  frontendUrl: 'http://localhost:4200',

  apiPaths: {
    // Order APIs
    orders: '/orders',
    checkout: '/orders/checkout',

    payments: '/user/payments',
    paymentCallback: '/payment/callback/vnpay',

    // Cart APIs
    cart: '/cart',

    // Product APIs
    products: '/products',

    // User APIs
    user: '/user',
    profile: '/user/profile',
  },
};
