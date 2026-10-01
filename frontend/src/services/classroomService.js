import api from './api';

export const classroomService = {
  getClassrooms: async (params = {}) => {
    const response = await api.get('/api/admin/classrooms', { params });
    return response.data;
  },

  getAllClassrooms: async (params = {}) => {
    const response = await api.get('/api/admin/classrooms', { params });
    return response.data;
  },

  getClassroomById: async (id) => {
    const response = await api.get(`/api/admin/classrooms/${id}`);
    return response.data;
  },

  createClassroom: async (data) => {
    const response = await api.post('/api/admin/classrooms', data);
    return response.data;
  },

  updateClassroom: async (id, data) => {
    const response = await api.put(`/api/admin/classrooms/${id}`, data);
    return response.data;
  },

  deleteClassroom: async (id) => {
    const response = await api.delete(`/api/admin/classrooms/${id}`);
    return response.data;
  },
};

export default classroomService;
