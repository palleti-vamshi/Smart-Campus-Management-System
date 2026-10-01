import api from './api';

export const dashboardService = {
  getAdminDashboard: async () => {
    const response = await api.get('/api/admin/dashboard');
    return response.data;
  },

  getFacultyDashboard: async () => {
    const response = await api.get('/api/faculty/dashboard');
    return response.data;
  },

  getStudentDashboard: async () => {
    const response = await api.get('/api/student/dashboard');
    return response.data;
  },
};

export default dashboardService;
