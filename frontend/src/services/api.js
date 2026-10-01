import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to attach JWT token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('scms_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor for unified response and error handling
api.interceptors.response.use(
  (response) => {
    // If backend wrapped in ApiResponse { success, message, data }
    return response;
  },
  (error) => {
    if (error.response && error.response.status === 401) {
      // Clear token and broadcast logout event
      localStorage.removeItem('scms_token');
      localStorage.removeItem('scms_user');
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    let message = error.response?.data?.message;

    if (!error.response) {
      message = 'Backend unavailable. Please check if the server is running.';
    } else if (!message || message === 'An unexpected error occurred. Please try again later.') {
      switch (error.response.status) {
        case 401:
          message = 'Session expired / invalid credentials';
          break;
        case 403:
          message = 'Not authorized to access this resource';
          break;
        case 404:
          message = 'Record not found';
          break;
        case 409:
          message = 'Duplicate or conflict with existing record';
          break;
        case 500:
          message = 'Server error occurred. Please try again.';
          break;
        default:
          message = error.response?.data?.error || error.message || 'An unexpected error occurred';
      }
    }

    // Attach friendly message directly on error object and normalize error.message
    error.friendlyMessage = message;
    error.message = message;
    return Promise.reject(error);
  }
);

export default api;
