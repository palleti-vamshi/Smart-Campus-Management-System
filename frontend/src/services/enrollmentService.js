import api from './api';

export const enrollmentService = {
  getAdminEnrollments: async (params = {}) => {
    const response = await api.get('/api/admin/enrollments', { params });
    return response.data.data;
  },

  createEnrollment: async (data) => {
    const response = await api.post('/api/admin/enrollments', data);
    return response.data.data;
  },

  updateEnrollment: async (id, data) => {
    const response = await api.put(`/api/admin/enrollments/${id}`, data);
    return response.data.data;
  },

  deleteEnrollment: async (id) => {
    const response = await api.delete(`/api/admin/enrollments/${id}`);
    return response.data;
  },

  getFacultyEnrollments: async (params = {}) => {
    const response = await api.get('/api/faculty/enrollments', { params });
    return response.data.data;
  },

  getStudentEnrollments: async (params = {}) => {
    const response = await api.get('/api/student/enrollments', { params });
    return response.data.data;
  },
};

export default enrollmentService;
