import React, { useState, useEffect } from 'react';
import { timetableService } from '../../services/timetableService';
import { programService } from '../../services/programService';
import { courseService } from '../../services/courseService';
import { facultyService } from '../../services/facultyService';
import { classroomService } from '../../services/classroomService';
import PageHeader from '../../components/common/PageHeader';
import Table from '../../components/common/Table';
import Pagination from '../../components/common/Pagination';
import Button from '../../components/common/Button';
import Modal from '../../components/common/Modal';
import Input from '../../components/common/Input';
import Select from '../../components/common/Select';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import { FiPlus, FiEdit2, FiTrash2 } from 'react-icons/fi';

const DAYS = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];

export const AdminTimetablePage = () => {
  const [entries, setEntries] = useState([]);
  const [programs, setPrograms] = useState([]);
  const [courses, setCourses] = useState([]);
  const [facultyList, setFacultyList] = useState([]);
  const [classrooms, setClassrooms] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  // Filter
  const [selectedDay, setSelectedDay] = useState('');
  const [selectedProgramId, setSelectedProgramId] = useState('');
  const [selectedSection, setSelectedSection] = useState('');

  // Form Modal
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [formData, setFormData] = useState({
    programId: '',
    section: 'A',
    courseId: '',
    facultyId: '',
    classroomId: '',
    dayOfWeek: 'MONDAY',
    startTime: '09:00',
    endTime: '10:00',
    semester: 3,
    academicYear: '2026-2027',
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
      courseService.getCourses({ size: 100 }),
      facultyService.getFaculty({ size: 100 }),
      classroomService.getClassrooms({ size: 100 }),
    ]).then(([pRes, cRes, fRes, rRes]) => {
      setPrograms(pRes?.data || []);
      setCourses(cRes?.data?.content || cRes?.data || []);
      setFacultyList(fRes?.data?.content || fRes?.data || []);
      setClassrooms(rRes?.data?.content || rRes?.data || []);
    }).catch(() => {});
  }, []);

  const fetchTimetable = async (pageNumber = 0) => {
    setLoading(true);
    setError(null);
    try {
      const params = { page: pageNumber, size: 20 };
      if (selectedDay) params.dayOfWeek = selectedDay;
      if (selectedProgramId) params.programId = selectedProgramId;
      if (selectedSection) params.section = selectedSection;
      const res = await timetableService.getAdminTimetable(params);
      const pageData = res.data;
      setEntries(pageData?.content || []);
      setPage(pageData?.pageNumber || 0);
      setTotalPages(pageData?.totalPages || 1);
      setTotalElements(pageData?.totalElements || 0);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to load timetable.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTimetable(0);
  }, [selectedDay, selectedProgramId, selectedSection]);

  const handleOpenCreate = () => {
    setIsEditing(false);
    setEditingId(null);
    setFormData({
      programId: programs[0]?.programId ? String(programs[0].programId) : '',
      section: 'A',
      courseId: courses[0]?.courseId ? String(courses[0].courseId) : '',
      facultyId: facultyList[0]?.facultyId ? String(facultyList[0].facultyId) : '',
      classroomId: classrooms[0]?.classroomId ? String(classrooms[0].classroomId) : '',
      dayOfWeek: 'MONDAY',
      startTime: '10:00',
      endTime: '11:00',
      semester: 3,
      academicYear: '2026-2027',
    });
    setFormError('');
    setIsModalOpen(true);
  };

  const handleOpenEdit = (t) => {
    setIsEditing(true);
    setEditingId(t.timetableId);
    setFormData({
      programId: String(t.programId),
      section: t.section || 'A',
      courseId: String(t.courseId),
      facultyId: String(t.facultyId),
      classroomId: String(t.classroomId),
      dayOfWeek: t.dayOfWeek,
      startTime: t.startTime ? t.startTime.slice(0, 5) : '10:00',
      endTime: t.endTime ? t.endTime.slice(0, 5) : '11:00',
      semester: t.semester || 3,
      academicYear: t.academicYear || '2026-2027',
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
        programId: Number(formData.programId),
        section: formData.section || 'A',
        courseId: Number(formData.courseId),
        facultyId: Number(formData.facultyId),
        classroomId: Number(formData.classroomId),
        dayOfWeek: formData.dayOfWeek,
        startTime: formData.startTime.length === 5 ? `${formData.startTime}:00` : formData.startTime,
        endTime: formData.endTime.length === 5 ? `${formData.endTime}:00` : formData.endTime,
        semester: Number(formData.semester),
        academicYear: formData.academicYear,
      };

      if (isEditing) {
        await timetableService.updateTimetable(editingId, payload);
      } else {
        await timetableService.createTimetable(payload);
      }
      setIsModalOpen(false);
      fetchTimetable(page);
    } catch (err) {
      setFormError(err.response?.data?.message || err.message || 'Failed to save timetable slot.');
    } finally {
      setFormLoading(false);
    }
  };

  const handleDelete = async () => {
    setDeleteLoading(true);
    try {
      await timetableService.deleteTimetable(deletingId);
      setIsDeleteDialogOpen(false);
      fetchTimetable(page);
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to delete timetable slot.');
    } finally {
      setDeleteLoading(false);
    }
  };

  const columns = [
    { header: 'Day', accessor: 'dayOfWeek', cellClassName: 'font-semibold text-slate-800' },
    { header: 'Time Slot', render: (r) => `${r.startTime} - ${r.endTime}` },
    { header: 'Course', accessor: 'courseName', render: (r) => `${r.courseName} (${r.courseCode})` },
    { header: 'Program', accessor: 'programCode', render: (r) => `${r.programCode} (Sem ${r.semester})` },
    { header: 'Section', accessor: 'section', cellClassName: 'font-bold text-center text-theme-brand', render: (r) => `Sec ${r.section || 'A'}` },
    { header: 'Faculty', accessor: 'facultyName' },
    { header: 'Classroom', accessor: 'classroomRoomNumber', render: (r) => `${r.classroomRoomNumber || r.roomNumber} - ${r.classroomBuilding || r.building || ''}` },
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
            aria-label="Edit slot"
          />
          <Button
            size="sm"
            variant="text"
            className="text-red-600 hover:text-red-700 hover:bg-red-50"
            onClick={() => {
              setDeletingId(r.timetableId);
              setIsDeleteDialogOpen(true);
            }}
            icon={FiTrash2}
            aria-label="Delete slot"
          />
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Department Academic Timetable"
        subtitle="Schedule and orchestrate lecture slots across halls, faculty, and programs"
      >
        <Button
          variant="primary"
          onClick={handleOpenCreate}
          icon={FiPlus}
        >
          Add Timetable Slot
        </Button>
      </PageHeader>

      <div className="flex flex-col sm:flex-row gap-4 items-center justify-between flex-wrap">
        <div className="w-full sm:w-64">
          <Select
            placeholder="All Programs"
            value={selectedProgramId}
            onChange={(e) => setSelectedProgramId(e.target.value)}
            options={programs.map((p) => ({ value: String(p.programId), label: `${p.code} - ${p.name}` }))}
          />
        </div>
        <div className="w-full sm:w-40">
          <Select
            placeholder="All Sections"
            value={selectedSection}
            onChange={(e) => setSelectedSection(e.target.value)}
            options={[
              { value: '', label: 'All Sections' },
              { value: 'A', label: 'Section A' },
              { value: 'B', label: 'Section B' },
              { value: 'C', label: 'Section C' },
            ]}
          />
        </div>
        <div className="w-full sm:w-48">
          <Select
            placeholder="All Days"
            value={selectedDay}
            onChange={(e) => setSelectedDay(e.target.value)}
            options={DAYS.map((d) => ({ value: d, label: d }))}
          />
        </div>
      </div>

      <Table
        columns={columns}
        data={entries}
        loading={loading}
        error={error}
        onRetry={() => fetchTimetable(page)}
        emptyMessage="No timetable slots found for the selected filter."
        keyField="timetableId"
      />

      <Pagination
        currentPage={page}
        totalPages={totalPages}
        totalElements={totalElements}
        pageSize={20}
        onPageChange={(newPage) => fetchTimetable(newPage)}
      />

      {/* Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={isEditing ? 'Modify Timetable Slot' : 'Create Timetable Lecture Slot'}
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          {formError && (
            <div className="p-3 bg-red-50 border border-red-200 text-red-700 text-xs rounded-lg">
              {formError}
            </div>
          )}

          <div className="grid grid-cols-3 gap-3">
            <Select
              label="Program"
              name="programId"
              required
              value={formData.programId}
              onChange={(e) => setFormData({ ...formData, programId: e.target.value })}
              options={programs.map((p) => ({ value: String(p.programId), label: p.code }))}
            />
            <Select
              label="Section"
              name="section"
              required
              value={formData.section}
              onChange={(e) => setFormData({ ...formData, section: e.target.value })}
              options={[
                { value: 'A', label: 'Section A' },
                { value: 'B', label: 'Section B' },
                { value: 'C', label: 'Section C' },
              ]}
            />
            <Select
              label="Course"
              name="courseId"
              required
              value={formData.courseId}
              onChange={(e) => setFormData({ ...formData, courseId: e.target.value })}
              options={courses.map((c) => ({ value: String(c.courseId), label: `${c.courseCode} - ${c.courseName}` }))}
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <Select
              label="Faculty"
              name="facultyId"
              required
              value={formData.facultyId}
              onChange={(e) => setFormData({ ...formData, facultyId: e.target.value })}
              options={facultyList.map((f) => ({ value: String(f.facultyId), label: `${f.firstName} ${f.lastName || ''} (${f.employeeCode})` }))}
            />
            <Select
              label="Classroom"
              name="classroomId"
              required
              value={formData.classroomId}
              onChange={(e) => setFormData({ ...formData, classroomId: e.target.value })}
              options={classrooms.map((r) => ({ value: String(r.classroomId), label: `${r.roomNumber} (${r.building})` }))}
            />
          </div>

          <div className="grid grid-cols-3 gap-3">
            <Select
              label="Day of Week"
              name="dayOfWeek"
              required
              value={formData.dayOfWeek}
              onChange={(e) => setFormData({ ...formData, dayOfWeek: e.target.value })}
              options={DAYS.map((d) => ({ value: d, label: d }))}
            />
            <Input
              label="Start Time"
              name="startTime"
              type="time"
              required
              value={formData.startTime}
              onChange={(e) => setFormData({ ...formData, startTime: e.target.value })}
            />
            <Input
              label="End Time"
              name="endTime"
              type="time"
              required
              value={formData.endTime}
              onChange={(e) => setFormData({ ...formData, endTime: e.target.value })}
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <Select
              label="Semester"
              name="semester"
              required
              value={String(formData.semester)}
              onChange={(e) => setFormData({ ...formData, semester: e.target.value })}
              options={[1, 2, 3, 4, 5, 6, 7, 8].map((s) => ({ value: String(s), label: `Semester ${s}` }))}
            />
            <Input
              label="Academic Year"
              name="academicYear"
              required
              value={formData.academicYear}
              onChange={(e) => setFormData({ ...formData, academicYear: e.target.value })}
            />
          </div>

          <div className="pt-4 flex justify-end gap-3 border-t border-slate-100">
            <Button variant="secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" loading={formLoading}>
              {isEditing ? 'Save Changes' : 'Create Slot'}
            </Button>
          </div>
        </form>
      </Modal>

      {/* Delete Dialog */}
      <ConfirmDialog
        isOpen={isDeleteDialogOpen}
        onClose={() => setIsDeleteDialogOpen(false)}
        onConfirm={handleDelete}
        title="Delete Timetable Slot"
        message="Are you sure you want to remove this lecture slot from the timetable?"
        loading={deleteLoading}
      />
    </div>
  );
};

export default AdminTimetablePage;
