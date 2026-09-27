import api from './api';

export const attendanceService = {
  getAdminAttendance: async (params = {}) => {
    const response = await api.get('/api/admin/attendance', { params });
    return response.data.data;
  },

  recordAttendanceByAdmin: async (data) => {
    const response = await api.post('/api/admin/attendance', data);
    return response.data.data;
  },

  updateAttendanceByAdmin: async (id, data) => {
    const response = await api.put(`/api/admin/attendance/${id}`, data);
    return response.data.data;
  },

  deleteAttendanceByAdmin: async (id) => {
    const response = await api.delete(`/api/admin/attendance/${id}`);
    return response.data;
  },

  getFacultyAttendance: async (params = {}) => {
    const response = await api.get('/api/faculty/attendance', { params });
    return response.data.data;
  },

  recordAttendanceByFaculty: async (data) => {
    const response = await api.post('/api/faculty/attendance', data);
    return response.data.data;
  },

  updateAttendanceByFaculty: async (id, data) => {
    const response = await api.put(`/api/faculty/attendance/${id}`, data);
    return response.data.data;
  },

  deleteAttendanceByFaculty: async (id) => {
    const response = await api.delete(`/api/faculty/attendance/${id}`);
    return response.data;
  },

  getMyAttendance: async (params = {}) => {
    const response = await api.get('/api/student/attendance', { params });
    return response.data.data;
  },

  getMyAttendanceSummary: async () => {
    const response = await api.get('/api/student/attendance/summary');
    return response.data.data;
  },
};

export default attendanceService;
