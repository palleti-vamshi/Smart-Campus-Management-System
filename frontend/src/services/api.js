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
    const message =
      error.response?.data?.message ||
      error.response?.data?.error ||
      error.message ||
      'An unexpected error occurred';
    
    // Attach friendly message directly on error object
    error.friendlyMessage = message;
    return Promise.reject(error);
  }
);

export default api;
