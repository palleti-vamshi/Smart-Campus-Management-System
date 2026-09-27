import api from './api';

export const timetableService = {
  getAdminTimetable: async (params = {}) => {
    const response = await api.get('/api/admin/timetable', { params });
    return response.data.data;
  },

  createTimetable: async (data) => {
    const response = await api.post('/api/admin/timetable', data);
    return response.data.data;
  },

  updateTimetable: async (id, data) => {
    const response = await api.put(`/api/admin/timetable/${id}`, data);
    return response.data.data;
  },

  deleteTimetable: async (id) => {
    const response = await api.delete(`/api/admin/timetable/${id}`);
    return response.data;
  },

  getFacultyTimetable: async () => {
    const response = await api.get('/api/faculty/timetable');
    return response.data.data;
  },

  getStudentTimetable: async () => {
    const response = await api.get('/api/student/timetable');
    return response.data.data;
  },
};

export default timetableService;
