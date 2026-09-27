import api from './api';

export const noticeService = {
  getAdminNotices: async (params = {}) => {
    const response = await api.get('/api/admin/notices', { params });
    return response.data.data;
  },

  createAdminNotice: async (data) => {
    const response = await api.post('/api/admin/notices', data);
    return response.data.data;
  },

  updateAdminNotice: async (id, data) => {
    const response = await api.put(`/api/admin/notices/${id}`, data);
    return response.data.data;
  },

  deleteAdminNotice: async (id) => {
    const response = await api.delete(`/api/admin/notices/${id}`);
    return response.data;
  },

  getFacultyNotices: async (params = {}) => {
    const response = await api.get('/api/faculty/notices', { params });
    return response.data.data;
  },

  createFacultyNotice: async (data) => {
    const response = await api.post('/api/faculty/notices', data);
    return response.data.data;
  },

  updateFacultyNotice: async (id, data) => {
    const response = await api.put(`/api/faculty/notices/${id}`, data);
    return response.data.data;
  },

  deleteFacultyNotice: async (id) => {
    const response = await api.delete(`/api/faculty/notices/${id}`);
    return response.data;
  },

  getStudentNotices: async (params = {}) => {
    const response = await api.get('/api/student/notices', { params });
    return response.data.data;
  },
};

export default noticeService;
