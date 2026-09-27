import api from './api';

export const documentService = {
  // Admin Document Types
  getAdminDocumentTypes: async (params = {}) => {
    const response = await api.get('/api/admin/document-types', { params });
    return response.data.data;
  },

  createDocumentType: async (data) => {
    const response = await api.post('/api/admin/document-types', data);
    return response.data.data;
  },

  updateDocumentType: async (id, data) => {
    const response = await api.put(`/api/admin/document-types/${id}`, data);
    return response.data.data;
  },

  deleteDocumentType: async (id) => {
    const response = await api.delete(`/api/admin/document-types/${id}`);
    return response.data;
  },

  // Admin Document Requests
  getAdminDocumentRequests: async (params = {}) => {
    const response = await api.get('/api/admin/document-requests', { params });
    return response.data.data;
  },

  updateDocumentRequestStatus: async (id, data) => {
    const response = await api.put(`/api/admin/document-requests/${id}/status`, data);
    return response.data.data;
  },

  downloadAdminCertificate: async (id) => {
    const response = await api.get(`/api/admin/document-requests/${id}/download`, {
      responseType: 'blob',
    });
    return response.data;
  },

  // Student Document Requests
  getStudentDocumentTypes: async () => {
    const response = await api.get('/api/student/document-types');
    return response.data.data;
  },

  getStudentDocumentRequests: async (params = {}) => {
    const response = await api.get('/api/student/document-requests', { params });
    return response.data.data;
  },

  createDocumentRequest: async (data) => {
    const response = await api.post('/api/student/document-requests', data);
    return response.data.data;
  },

  downloadStudentCertificate: async (id) => {
    const response = await api.get(`/api/student/document-requests/${id}/download`, {
      responseType: 'blob',
    });
    return response.data;
  },

  // Public Verification
  verifyDocument: async (code) => {
    const response = await api.get(`/api/public/documents/verify/${code}`);
    return response.data.data;
  },
};

export default documentService;
