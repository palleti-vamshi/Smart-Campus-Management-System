import React, { useState, useEffect } from 'react';
import { courseService } from '../../services/courseService';
import { programService } from '../../services/programService';
import { facultyService } from '../../services/facultyService';
import PageHeader from '../../components/common/PageHeader';
import Table from '../../components/common/Table';
import Pagination from '../../components/common/Pagination';
import Button from '../../components/common/Button';
import Modal from '../../components/common/Modal';
import Input from '../../components/common/Input';
import Select from '../../components/common/Select';
import SearchBar from '../../components/common/SearchBar';
import StatusBadge from '../../components/common/Badge';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import { FiPlus, FiEdit2, FiTrash2 } from 'react-icons/fi';

export const AdminCoursesPage = () => {
  const [courses, setCourses] = useState([]);
  const [programs, setPrograms] = useState([]);
  const [facultyList, setFacultyList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  // Filters
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedProgramId, setSelectedProgramId] = useState('');
  const [selectedSemester, setSelectedSemester] = useState('');

  // Form Modal
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [formData, setFormData] = useState({
    programId: '',
    facultyId: '',
    courseCode: '',
    courseName: '',
    credits: 3.0,
    semester: 1,
    courseType: 'THEORY',
  });
  const [formLoading, setFormLoading] = useState(false);
  const [formError, setFormError] = useState('');

  // Delete
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [deletingId, setDeletingId] = useState(null);
  const [deleteLoading, setDeleteLoading] = useState(false);

  useEffect(() => {
    Promise.all([
      programService.getAllPrograms(),
      facultyService.getFaculty({ size: 100 }),
    ]).then(([pRes, fRes]) => {
      setPrograms(pRes.data || []);
      setFacultyList(fRes.data?.content || []);
    }).catch(() => {});
  }, []);

  const fetchCourses = async (pageNumber = 0) => {
    setLoading(true);
    setError(null);
    try {
      const params = { page: pageNumber, size: 20 };
      if (selectedProgramId) params.programId = selectedProgramId;
      if (selectedSemester) params.semester = selectedSemester;
      if (searchTerm) params.search = searchTerm;
      const res = await courseService.getCourses(params);
      const pageData = res.data;
      setCourses(pageData?.content || []);
      setPage(pageData?.pageNumber || 0);
      setTotalPages(pageData?.totalPages || 1);
      setTotalElements(pageData?.totalElements || 0);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to load courses.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCourses(0);
  }, [selectedProgramId, selectedSemester, searchTerm]);

  const handleOpenCreate = () => {
    setIsEditing(false);
    setEditingId(null);
    setFormData({
      programId: programs[0]?.programId ? String(programs[0].programId) : '',
      facultyId: '',
      courseCode: '',
      courseName: '',
      credits: 3.0,
      semester: 1,
      courseType: 'THEORY',
    });
    setFormError('');
    setIsModalOpen(true);
  };

  const handleOpenEdit = (c) => {
    setIsEditing(true);
    setEditingId(c.courseId);
    setFormData({
      programId: String(c.programId),
      facultyId: c.facultyId ? String(c.facultyId) : '',
      courseCode: c.courseCode,
      courseName: c.courseName,
      credits: c.credits,
      semester: c.semester,
      courseType: c.courseType || 'THEORY',
    });
    setFormError('');
    setIsModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormLoading(true);
    setFormError('');
    try {
      const payload = {
        ...formData,
        programId: Number(formData.programId),
        facultyId: formData.facultyId ? Number(formData.facultyId) : null,
        credits: Number(formData.credits),
        semester: Number(formData.semester),
      };

      if (isEditing) {
        await courseService.updateCourse(editingId, payload);
      } else {
        await courseService.createCourse(payload);
      }
      setIsModalOpen(false);
      fetchCourses(page);
    } catch (err) {
      setFormError(err.response?.data?.message || err.message || 'Failed to save course.');
    } finally {
      setFormLoading(false);
    }
  };

  const handleDelete = async () => {
    setDeleteLoading(true);
    try {
      await courseService.deleteCourse(deletingId);
      setIsDeleteDialogOpen(false);
      fetchCourses(page);
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to delete course.');
    } finally {
      setDeleteLoading(false);
    }
  };

  const columns = [
    { header: 'Code', accessor: 'courseCode', cellClassName: 'font-mono text-xs font-semibold' },
    { header: 'Course Name', accessor: 'courseName' },
    { header: 'Program', accessor: 'programCode' },
    { header: 'Credits', accessor: 'credits', align: 'center' },
    { header: 'Semester', accessor: 'semester', align: 'center' },
    { header: 'Type', accessor: 'courseType', render: (r) => <StatusBadge status={r.courseType} /> },
    { header: 'Assigned Faculty', accessor: 'facultyName', render: (r) => r.facultyName || 'Unassigned' },
    {
      header: 'Actions',
      align: 'right',
      render: (r) => (
        <div className="flex items-center justify-end gap-1.5">
          <Button
            size="sm"
            variant="text"
            onClick={() => handleOpenEdit(r)}
            icon={FiEdit2}
            aria-label="Edit course"
          />
          <Button
            size="sm"
            variant="text"
            className="text-red-600 hover:text-red-700 hover:bg-red-50"
            onClick={() => {
              setDeletingId(r.courseId);
              setIsDeleteDialogOpen(true);
            }}
            icon={FiTrash2}
            aria-label="Delete course"
          />
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Course Curriculum Management"
        subtitle="Manage department course offerings, credit allocations, and faculty assignments"
      >
        <Button
          variant="primary"
          onClick={handleOpenCreate}
          icon={FiPlus}
        >
          Add Course
        </Button>
      </PageHeader>

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
        <SearchBar
          value={searchTerm}
          onChange={setSearchTerm}
          placeholder="Search by code or title..."
        />
        <Select
          placeholder="All Programs"
          value={selectedProgramId}
          onChange={(e) => setSelectedProgramId(e.target.value)}
          options={programs.map((p) => ({ value: String(p.programId), label: `${p.code} - ${p.name}` }))}
        />
        <Select
          placeholder="All Semesters"
          value={selectedSemester}
          onChange={(e) => setSelectedSemester(e.target.value)}
          options={[1, 2, 3, 4, 5, 6, 7, 8].map((s) => ({ value: String(s), label: `Semester ${s}` }))}
        />
      </div>

      <Table
        columns={columns}
        data={courses}
        loading={loading}
        error={error}
        onRetry={() => fetchCourses(page)}
        emptyMessage="No courses found matching current criteria."
        keyField="courseId"
      />

      <Pagination
        currentPage={page}
        totalPages={totalPages}
        totalElements={totalElements}
        pageSize={20}
        onPageChange={(newPage) => fetchCourses(newPage)}
      />

      {/* Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={isEditing ? 'Edit Course' : 'Create New Course'}
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          {formError && (
            <div className="p-3 bg-red-50 border border-red-200 text-red-700 text-xs rounded-lg">
              {formError}
            </div>
          )}

          <div className="grid grid-cols-2 gap-3">
            <Input
              label="Course Code"
              name="courseCode"
              required
              value={formData.courseCode}
              onChange={(e) => setFormData({ ...formData, courseCode: e.target.value })}
              placeholder="e.g. CS301, AI201"
            />
            <Select
              label="Program"
              name="programId"
              required
              value={formData.programId}
              onChange={(e) => setFormData({ ...formData, programId: e.target.value })}
              options={programs.map((p) => ({ value: String(p.programId), label: p.code }))}
            />
          </div>

          <Input
            label="Course Name"
            name="courseName"
            required
            value={formData.courseName}
            onChange={(e) => setFormData({ ...formData, courseName: e.target.value })}
            placeholder="e.g. Deep Learning & Neural Networks"
          />

          <div className="grid grid-cols-3 gap-3">
            <Input
              label="Credits"
              name="credits"
              type="number"
              step="0.5"
              min="0.5"
              max="20"
              required
              value={formData.credits}
              onChange={(e) => setFormData({ ...formData, credits: e.target.value })}
            />
            <Select
              label="Semester"
              name="semester"
              required
              value={String(formData.semester)}
              onChange={(e) => setFormData({ ...formData, semester: e.target.value })}
              options={[1, 2, 3, 4, 5, 6, 7, 8].map((s) => ({ value: String(s), label: `Semester ${s}` }))}
            />
            <Select
              label="Course Type"
              name="courseType"
              required
              value={formData.courseType}
              onChange={(e) => setFormData({ ...formData, courseType: e.target.value })}
              options={[
                { value: 'THEORY', label: 'Theory' },
                { value: 'LAB', label: 'Lab' },
                { value: 'PROJECT', label: 'Project' },
                { value: 'ELECTIVE', label: 'Elective' },
              ]}
            />
          </div>

          <Select
            label="Assigned Faculty (Optional)"
            name="facultyId"
            placeholder="Select Faculty Instructor"
            value={formData.facultyId}
            onChange={(e) => setFormData({ ...formData, facultyId: e.target.value })}
            options={facultyList.map((f) => ({ value: String(f.facultyId), label: `${f.firstName} ${f.lastName || ''} (${f.employeeCode})` }))}
          />

          <div className="pt-4 flex justify-end gap-3 border-t border-slate-100">
            <Button variant="secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" loading={formLoading}>
              {isEditing ? 'Save Changes' : 'Create Course'}
            </Button>
          </div>
        </form>
      </Modal>

      {/* Delete Dialog */}
      <ConfirmDialog
        isOpen={isDeleteDialogOpen}
        onClose={() => setIsDeleteDialogOpen(false)}
        onConfirm={handleDelete}
        title="Delete Course"
        message="Are you sure you want to delete this course? Enrollments and exams linked to this course will be impacted."
        loading={deleteLoading}
      />
    </div>
  );
};

export default AdminCoursesPage;
