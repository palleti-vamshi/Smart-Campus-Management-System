import api from './api';

export const studentService = {
  getStudents: async (params = {}) => {
    const response = await api.get('/api/admin/students', { params });
    return response.data.data;
  },

  getStudentById: async (id) => {
    const response = await api.get(`/api/admin/students/${id}`);
    return response.data.data;
  },

  createStudent: async (studentData) => {
    const response = await api.post('/api/admin/students', studentData);
    return response.data.data;
  },

  updateStudent: async (id, studentData) => {
    const response = await api.put(`/api/admin/students/${id}`, studentData);
    return response.data.data;
  },

  deleteStudent: async (id) => {
    const response = await api.delete(`/api/admin/students/${id}`);
    return response.data;
  },

  getMyProfile: async () => {
    const response = await api.get('/api/student/profile');
    return response.data.data;
  },
};

export default studentService;
