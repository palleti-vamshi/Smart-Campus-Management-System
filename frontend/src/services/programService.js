import api from './api';

export const programService = {
  getPrograms: async () => {
    const response = await api.get('/api/admin/programs');
    return response.data;
  },

  getAllPrograms: async () => {
    const response = await api.get('/api/admin/programs');
    return response.data;
  },

  getProgramById: async (id) => {
    const response = await api.get(`/api/admin/programs/${id}`);
    return response.data;
  },

  getProgramsByDepartment: async (deptId) => {
    const response = await api.get(`/api/admin/departments/${deptId}/programs`);
    return response.data;
  },

  createProgram: async (data) => {
    const response = await api.post('/api/admin/programs', data);
    return response.data;
  },

  updateProgram: async (id, data) => {
    const response = await api.put(`/api/admin/programs/${id}`, data);
    return response.data;
  },

  deleteProgram: async (id) => {
    const response = await api.delete(`/api/admin/programs/${id}`);
    return response.data;
  },
};

export default programService;
