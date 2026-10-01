import api from './api';

export const attendanceService = {
  getAdminAttendance: async (params = {}) => {
    const response = await api.get('/api/admin/attendance', { params });
    return response.data;
  },

  recordAttendanceByAdmin: async (data) => {
    const response = await api.post('/api/admin/attendance', data);
    return response.data;
  },

  recordAdminAttendance: async (data) => {
    const response = await api.post('/api/admin/attendance', data);
    return response.data;
  },

  updateAttendanceByAdmin: async (id, data) => {
    const response = await api.put(`/api/admin/attendance/${id}`, data);
    return response.data;
  },

  updateAdminAttendance: async (id, data) => {
    const response = await api.put(`/api/admin/attendance/${id}`, data);
    return response.data;
  },

  deleteAttendanceByAdmin: async (id) => {
    const response = await api.delete(`/api/admin/attendance/${id}`);
    return response.data;
  },

  deleteAdminAttendance: async (id) => {
    const response = await api.delete(`/api/admin/attendance/${id}`);
    return response.data;
  },

  getFacultyAttendance: async (params = {}) => {
    const response = await api.get('/api/faculty/attendance', { params });
    return response.data;
  },

  recordAttendanceByFaculty: async (data) => {
    const response = await api.post('/api/faculty/attendance', data);
    return response.data;
  },

  recordFacultyAttendance: async (data) => {
    const response = await api.post('/api/faculty/attendance', data);
    return response.data;
  },

  updateAttendanceByFaculty: async (id, data) => {
    const response = await api.put(`/api/faculty/attendance/${id}`, data);
    return response.data;
  },

  updateFacultyAttendance: async (id, data) => {
    const response = await api.put(`/api/faculty/attendance/${id}`, data);
    return response.data;
  },

  deleteAttendanceByFaculty: async (id) => {
    const response = await api.delete(`/api/faculty/attendance/${id}`);
    return response.data;
  },

  deleteFacultyAttendance: async (id) => {
    const response = await api.delete(`/api/faculty/attendance/${id}`);
    return response.data;
  },

  recordBatchAttendance: async (data) => {
    const response = await api.post('/api/faculty/attendance/batch', data);
    return response.data;
  },

  getMyAttendance: async (params = {}) => {
    const response = await api.get('/api/student/attendance', { params });
    return response.data;
  },

  getMyAttendanceSummary: async () => {
    const response = await api.get('/api/student/attendance/summary');
    return response.data;
  },
};

export default attendanceService;
