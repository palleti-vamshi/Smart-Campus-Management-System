import api from './api';

export const facultyService = {
  getFacultyList: async (params = {}) => {
    const response = await api.get('/api/admin/faculty', { params });
    return response.data.data;
  },

  getFacultyById: async (id) => {
    const response = await api.get(`/api/admin/faculty/${id}`);
    return response.data.data;
  },

  createFaculty: async (facultyData) => {
    const response = await api.post('/api/admin/faculty', facultyData);
    return response.data.data;
  },

  updateFaculty: async (id, facultyData) => {
    const response = await api.put(`/api/admin/faculty/${id}`, facultyData);
    return response.data.data;
  },

  deleteFaculty: async (id) => {
    const response = await api.delete(`/api/admin/faculty/${id}`);
    return response.data;
  },

  getMyProfile: async () => {
    const response = await api.get('/api/faculty/profile');
    return response.data.data;
  },
};

export default facultyService;
