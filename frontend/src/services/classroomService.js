import api from './api';

export const classroomService = {
  getClassrooms: async (params = {}) => {
    const response = await api.get('/api/admin/classrooms', { params });
    return response.data.data;
  },

  getClassroomById: async (id) => {
    const response = await api.get(`/api/admin/classrooms/${id}`);
    return response.data.data;
  },

  createClassroom: async (data) => {
    const response = await api.post('/api/admin/classrooms', data);
    return response.data.data;
  },

  updateClassroom: async (id, data) => {
    const response = await api.put(`/api/admin/classrooms/${id}`, data);
    return response.data.data;
  },

  deleteClassroom: async (id) => {
    const response = await api.delete(`/api/admin/classrooms/${id}`);
    return response.data;
  },
};

export default classroomService;
