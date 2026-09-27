import api from './api';

export const departmentService = {
  getDepartments: async () => {
    const response = await api.get('/api/admin/departments');
    return response.data.data;
  },

  getDepartmentById: async (id) => {
    const response = await api.get(`/api/admin/departments/${id}`);
    return response.data.data;
  },

  createDepartment: async (data) => {
    const response = await api.post('/api/admin/departments', data);
    return response.data.data;
  },

  updateDepartment: async (id, data) => {
    const response = await api.put(`/api/admin/departments/${id}`, data);
    return response.data.data;
  },

  deleteDepartment: async (id) => {
    const response = await api.delete(`/api/admin/departments/${id}`);
    return response.data;
  },
};

export default departmentService;
