import api from './api';

export const courseService = {
  getCourses: async (params = {}) => {
    const response = await api.get('/api/admin/courses', { params });
    return response.data.data;
  },

  getCourseById: async (id) => {
    const response = await api.get(`/api/admin/courses/${id}`);
    return response.data.data;
  },

  createCourse: async (courseData) => {
    const response = await api.post('/api/admin/courses', courseData);
    return response.data.data;
  },

  updateCourse: async (id, courseData) => {
    const response = await api.put(`/api/admin/courses/${id}`, courseData);
    return response.data.data;
  },

  deleteCourse: async (id) => {
    const response = await api.delete(`/api/admin/courses/${id}`);
    return response.data;
  },
};

export default courseService;
