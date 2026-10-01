import api from './api';

export const markService = {
  getAdminMarks: async (params = {}) => {
    const response = await api.get('/api/admin/marks', { params });
    return response.data;
  },

  createAdminMark: async (data) => {
    const response = await api.post('/api/admin/marks', data);
    return response.data;
  },

  updateAdminMark: async (id, data) => {
    const response = await api.put(`/api/admin/marks/${id}`, data);
    return response.data;
  },

  deleteAdminMark: async (id) => {
    const response = await api.delete(`/api/admin/marks/${id}`);
    return response.data;
  },

  getFacultyMarks: async (params = {}) => {
    const response = await api.get('/api/faculty/marks', { params });
    return response.data;
  },

  createFacultyMark: async (data) => {
    const response = await api.post('/api/faculty/marks', data);
    return response.data;
  },

  updateFacultyMark: async (id, data) => {
    const response = await api.put(`/api/faculty/marks/${id}`, data);
    return response.data;
  },

  deleteFacultyMark: async (id) => {
    const response = await api.delete(`/api/faculty/marks/${id}`);
    return response.data;
  },

  recordBatchMarks: async (data) => {
    const response = await api.post('/api/faculty/marks/batch', data);
    return response.data;
  },

  getStudentMarks: async (params = {}) => {
    const response = await api.get('/api/student/marks', { params });
    return response.data;
  },

  getMyMarks: async (params = {}) => {
    const response = await api.get('/api/student/marks', { params });
    return response.data;
  },

  getMyResults: async () => {
    const response = await api.get('/api/student/results');
    return response.data;
  },
};

export default markService;
