import api from './api';

export const examService = {
  getAdminExams: async (params = {}) => {
    const response = await api.get('/api/admin/exams', { params });
    return response.data;
  },

  createAdminExam: async (data) => {
    const response = await api.post('/api/admin/exams', data);
    return response.data;
  },

  updateAdminExam: async (id, data) => {
    const response = await api.put(`/api/admin/exams/${id}`, data);
    return response.data;
  },

  deleteAdminExam: async (id) => {
    const response = await api.delete(`/api/admin/exams/${id}`);
    return response.data;
  },

  getFacultyExams: async (params = {}) => {
    const response = await api.get('/api/faculty/exams', { params });
    return response.data;
  },

  createFacultyExam: async (data) => {
    const response = await api.post('/api/faculty/exams', data);
    return response.data;
  },

  updateFacultyExam: async (id, data) => {
    const response = await api.put(`/api/faculty/exams/${id}`, data);
    return response.data;
  },

  deleteFacultyExam: async (id) => {
    const response = await api.delete(`/api/faculty/exams/${id}`);
    return response.data;
  },

  getStudentExams: async (params = {}) => {
    const response = await api.get('/api/student/exams', { params });
    return response.data;
  },

  getMyExams: async (params = {}) => {
    const response = await api.get('/api/student/exams', { params });
    return response.data;
  },
};

export default examService;
