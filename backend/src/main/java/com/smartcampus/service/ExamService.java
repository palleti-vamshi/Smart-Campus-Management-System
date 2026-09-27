package com.smartcampus.service;

import com.smartcampus.dto.request.ExamRequest;
import com.smartcampus.dto.response.ExamResponse;
import com.smartcampus.dto.response.PageResponse;
import com.smartcampus.entity.Course;
import com.smartcampus.entity.Exam;
import com.smartcampus.entity.Faculty;
import com.smartcampus.entity.Student;
import com.smartcampus.entity.enums.ExamType;
import com.smartcampus.exception.InvalidOperationException;
import com.smartcampus.exception.ResourceConflictException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.CourseRepository;
import com.smartcampus.repository.ExamRepository;
import com.smartcampus.repository.FacultyRepository;
import com.smartcampus.repository.MarkRepository;
import com.smartcampus.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ExamService {

    private final ExamRepository examRepository;
    private final CourseRepository courseRepository;
    private final FacultyRepository facultyRepository;
    private final StudentRepository studentRepository;
    private final MarkRepository markRepository;

    @Transactional
    public ExamResponse createExamByAdmin(ExamRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + request.getCourseId()));

        validateMaxMarks(request.getMaxMarks());

        Exam exam = Exam.builder()
                .course(course)
                .examName(request.getExamName())
                .examType(request.getExamType())
                .examDate(request.getExamDate())
                .maxMarks(request.getMaxMarks())
                .build();

        Exam saved = examRepository.save(exam);
        log.info("Exam '{}' created by admin for course '{}'", saved.getExamName(), course.getCourseCode());
        return ExamResponse.fromEntity(saved);
    }

    @Transactional
    public ExamResponse createExamByFaculty(Long userId, ExamRequest request) {
        Faculty faculty = facultyRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty profile not found for user ID: " + userId));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + request.getCourseId()));

        if (course.getFaculty() == null || !course.getFaculty().getFacultyId().equals(faculty.getFacultyId())) {
            log.warn("Faculty ID {} attempted to create exam for course ID {} not taught by them",
                    faculty.getFacultyId(), course.getCourseId());
            throw new AccessDeniedException("You are not authorized to create exams for a course you do not teach");
        }

        validateMaxMarks(request.getMaxMarks());

        Exam exam = Exam.builder()
                .course(course)
                .examName(request.getExamName())
                .examType(request.getExamType())
                .examDate(request.getExamDate())
                .maxMarks(request.getMaxMarks())
                .build();

        Exam saved = examRepository.save(exam);
        log.info("Exam '{}' created by faculty {} for course '{}'", saved.getExamName(), faculty.getEmployeeCode(), course.getCourseCode());
        return ExamResponse.fromEntity(saved);
    }

    public PageResponse<ExamResponse> getExamsForAdmin(
            Long courseId,
            ExamType examType,
            LocalDate examDate,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {
        validateDateRange(startDate, endDate);
        Page<Exam> page = examRepository.findWithAdminFilters(courseId, examType, examDate, startDate, endDate, pageable);
        return PageResponse.from(page.map(ExamResponse::fromEntity));
    }

    public PageResponse<ExamResponse> getExamsForFaculty(
            Long userId,
            Long courseId,
            ExamType examType,
            LocalDate examDate,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {
        Faculty faculty = facultyRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty profile not found for user ID: " + userId));

        validateDateRange(startDate, endDate);
        Page<Exam> page = examRepository.findWithFacultyFilters(faculty.getFacultyId(), courseId, examType, examDate, startDate, endDate, pageable);
        return PageResponse.from(page.map(ExamResponse::fromEntity));
    }

    public PageResponse<ExamResponse> getExamsForStudent(
            Long userId,
            Long courseId,
            ExamType examType,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {
        Student student = studentRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user ID: " + userId));

        validateDateRange(startDate, endDate);
        Page<Exam> page = examRepository.findWithStudentFilters(student.getStudentId(), courseId, examType, startDate, endDate, pageable);
        return PageResponse.from(page.map(ExamResponse::fromEntity));
    }

    public ExamResponse getExamById(Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with ID: " + examId));
        return ExamResponse.fromEntity(exam);
    }

    @Transactional
    public ExamResponse updateExamByAdmin(Long examId, ExamRequest request) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with ID: " + examId));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + request.getCourseId()));

        validateMaxMarks(request.getMaxMarks());

        exam.setCourse(course);
        exam.setExamName(request.getExamName());
        exam.setExamType(request.getExamType());
        exam.setExamDate(request.getExamDate());
        exam.setMaxMarks(request.getMaxMarks());

        Exam updated = examRepository.save(exam);
        log.info("Exam ID {} updated by admin", examId);
        return ExamResponse.fromEntity(updated);
    }

    @Transactional
    public ExamResponse updateExamByFaculty(Long userId, Long examId, ExamRequest request) {
        Faculty faculty = facultyRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty profile not found for user ID: " + userId));

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with ID: " + examId));

        if (exam.getCourse().getFaculty() == null ||
                !exam.getCourse().getFaculty().getFacultyId().equals(faculty.getFacultyId())) {
            log.warn("Faculty ID {} attempted to update exam ID {} not taught by them",
                    faculty.getFacultyId(), examId);
            throw new AccessDeniedException("You are not authorized to update exams for a course you do not teach");
        }

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + request.getCourseId()));

        if (!course.getFaculty().getFacultyId().equals(faculty.getFacultyId())) {
            throw new AccessDeniedException("Cannot transfer exam to a course you do not teach");
        }

        validateMaxMarks(request.getMaxMarks());

        exam.setCourse(course);
        exam.setExamName(request.getExamName());
        exam.setExamType(request.getExamType());
        exam.setExamDate(request.getExamDate());
        exam.setMaxMarks(request.getMaxMarks());

        Exam updated = examRepository.save(exam);
        log.info("Exam ID {} updated by faculty {}", examId, faculty.getEmployeeCode());
        return ExamResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteExamByAdmin(Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with ID: " + examId));

        if (!markRepository.findByExam_ExamId(examId).isEmpty()) {
            throw new ResourceConflictException("Cannot delete exam because marks have already been recorded for it");
        }

        examRepository.delete(exam);
        log.info("Exam ID {} deleted by admin", examId);
    }

    @Transactional
    public void deleteExamByFaculty(Long userId, Long examId) {
        Faculty faculty = facultyRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty profile not found for user ID: " + userId));

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with ID: " + examId));

        if (exam.getCourse().getFaculty() == null ||
                !exam.getCourse().getFaculty().getFacultyId().equals(faculty.getFacultyId())) {
            log.warn("Faculty ID {} attempted to delete exam ID {} not taught by them",
                    faculty.getFacultyId(), examId);
            throw new AccessDeniedException("You are not authorized to delete exams for a course you do not teach");
        }

        if (!markRepository.findByExam_ExamId(examId).isEmpty()) {
            throw new ResourceConflictException("Cannot delete exam because marks have already been recorded for it");
        }

        examRepository.delete(exam);
        log.info("Exam ID {} deleted by faculty {}", examId, faculty.getEmployeeCode());
    }

    private void validateMaxMarks(BigDecimal maxMarks) {
        if (maxMarks == null || maxMarks.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("Maximum marks must be greater than zero");
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new InvalidOperationException("Start date cannot be after end date");
        }
    }
}
