import api from './api';

export const timetableService = {
  getTimetable: async (params = {}) => {
    const response = await api.get('/api/admin/timetable', { params });
    return response.data;
  },

  getAdminTimetable: async (params = {}) => {
    const response = await api.get('/api/admin/timetable', { params });
    return response.data;
  },

  createTimetable: async (data) => {
    const response = await api.post('/api/admin/timetable', data);
    return response.data;
  },

  updateTimetable: async (id, data) => {
    const response = await api.put(`/api/admin/timetable/${id}`, data);
    return response.data;
  },

  deleteTimetable: async (id) => {
    const response = await api.delete(`/api/admin/timetable/${id}`);
    return response.data;
  },

  getFacultyTimetable: async () => {
    const response = await api.get('/api/faculty/timetable');
    return response.data;
  },

  getStudentTimetable: async () => {
    const response = await api.get('/api/student/timetable');
    return response.data;
  },

  getMyTimetable: async () => {
    try {
      const user = JSON.parse(localStorage.getItem('scms_user') || '{}');
      if (user?.role === 'FACULTY') {
        const response = await api.get('/api/faculty/timetable');
        return response.data;
      }
    } catch (e) {}
    const response = await api.get('/api/student/timetable');
    return response.data;
  },
};

export default timetableService;
