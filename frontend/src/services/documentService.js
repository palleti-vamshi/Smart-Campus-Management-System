import api from './api';

/**
 * Converts internal database document codes (e.g. BONAFIDE_CERTIFICATE)
 * into human-readable presentation labels (e.g. Bonafide Certificate).
 */
export const formatDocumentName = (rawName) => {
  if (!rawName) return 'Official Certificate';
  const clean = String(rawName).trim();
  const mapping = {
    'BONAFIDE_CERTIFICATE': 'Bonafide Certificate',
    'TRANSCRIPT': 'Academic Transcript',
    'INTERNSHIP_NOC': 'Internship No Objection Certificate (NOC)',
    'COURSE_COMPLETION_CERTIFICATE': 'Course Completion Certificate',
  };
  if (mapping[clean]) return mapping[clean];

  return clean
    .split('_')
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
    .join(' ');
};

/**
 * Normalizes an individual DocumentType item to guarantee valid fields
 * for both dropdown options and certificate cards.
 */
export const normalizeDocumentType = (item) => {
  if (!item) return null;
  const id = item.documentTypeId ?? item.id ?? item.typeId;
  const rawCode = item.documentName ?? item.code ?? item.documentTypeCode ?? item.name ?? '';
  const label = formatDocumentName(rawCode);

  return {
    ...item,
    documentTypeId: id,
    id: id,
    documentName: rawCode,
    documentTypeCode: rawCode,
    name: label,
    displayName: label,
    formattedName: label,
    description: item.description || 'Official academic certificate issued by university authority.',
    requiresApproval: item.requiresApproval ?? true,
    isActive: item.isActive ?? true,
  };
};

/**
 * Normalizes API response whether it's wrapped in { data: [...] } or { content: [...] } or raw array.
 */
export const normalizeDocumentTypesResponse = (response) => {
  const payload = response?.data !== undefined ? response.data : response;
  const rawList = payload?.data || payload?.content || (Array.isArray(payload) ? payload : []);
  const list = Array.isArray(rawList) ? rawList : [];
  return list.map(normalizeDocumentType).filter(Boolean);
};

export const documentService = {
  // Admin Document Types
  getAdminDocumentTypes: async (params = {}) => {
    const response = await api.get('/api/admin/document-types', { params });
    const normalized = normalizeDocumentTypesResponse(response);
    return { ...response.data, data: normalized, content: normalized };
  },

  createDocumentType: async (data) => {
    const response = await api.post('/api/admin/document-types', data);
    return response.data;
  },

  updateDocumentType: async (id, data) => {
    const response = await api.put(`/api/admin/document-types/${id}`, data);
    return response.data;
  },

  deleteDocumentType: async (id) => {
    const response = await api.delete(`/api/admin/document-types/${id}`);
    return response.data;
  },

  // Admin Document Requests
  getAdminRequests: async (params = {}) => {
    const response = await api.get('/api/admin/document-requests', { params });
    return response.data;
  },

  getAdminDocumentRequests: async (params = {}) => {
    const response = await api.get('/api/admin/document-requests', { params });
    return response.data;
  },

  updateRequestStatus: async (id, data) => {
    const response = await api.put(`/api/admin/document-requests/${id}/status`, data);
    return response.data;
  },

  updateDocumentRequestStatus: async (id, data) => {
    const response = await api.put(`/api/admin/document-requests/${id}/status`, data);
    return response.data;
  },

  downloadAdminDocument: async (id) => {
    const response = await api.get(`/api/admin/document-requests/${id}/download`, {
      responseType: 'blob',
    });
    return response;
  },

  downloadAdminCertificate: async (id) => {
    const response = await api.get(`/api/admin/document-requests/${id}/download`, {
      responseType: 'blob',
    });
    return response;
  },

  // Student Document Requests & Types
  getDocumentTypes: async () => {
    const response = await api.get('/api/student/document-types');
    const normalized = normalizeDocumentTypesResponse(response);
    return { ...response.data, data: normalized, content: normalized };
  },

  getStudentDocumentTypes: async () => {
    const response = await api.get('/api/student/document-types');
    const normalized = normalizeDocumentTypesResponse(response);
    return { ...response.data, data: normalized, content: normalized };
  },

  getMyRequests: async (params = {}) => {
    const response = await api.get('/api/student/document-requests', { params });
    return response.data;
  },

  getStudentDocumentRequests: async (params = {}) => {
    const response = await api.get('/api/student/document-requests', { params });
    return response.data;
  },

  requestDocument: async (data) => {
    const response = await api.post('/api/student/document-requests', data);
    return response.data;
  },

  createDocumentRequest: async (data) => {
    const response = await api.post('/api/student/document-requests', data);
    return response.data;
  },

  downloadCertificate: async (id) => {
    const response = await api.get(`/api/student/document-requests/${id}/download`, {
      responseType: 'blob',
    });
    return response;
  },

  downloadStudentCertificate: async (id) => {
    const response = await api.get(`/api/student/document-requests/${id}/download`, {
      responseType: 'blob',
    });
    return response;
  },

  // Public Verification
  verifyDocument: async (code) => {
    const response = await api.get(`/api/public/documents/verify/${code}`);
    return response.data;
  },
};

export default documentService;
