package com.smartcampus.service;

import com.smartcampus.dto.request.EnrollmentRequest;
import com.smartcampus.dto.response.EnrollmentResponse;
import com.smartcampus.dto.response.PageResponse;
import com.smartcampus.entity.Course;
import com.smartcampus.entity.Enrollment;
import com.smartcampus.entity.Faculty;
import com.smartcampus.entity.Student;
import com.smartcampus.entity.enums.EnrollmentStatus;
import com.smartcampus.exception.ResourceConflictException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.CourseRepository;
import com.smartcampus.repository.EnrollmentRepository;
import com.smartcampus.repository.FacultyRepository;
import com.smartcampus.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final FacultyRepository facultyRepository;

    /**
     * Admin view: get paginated enrollments with optional multi-attribute filters.
     */
    public PageResponse<EnrollmentResponse> getEnrollmentsForAdmin(
            Long studentId,
            Long courseId,
            String academicYear,
            Integer semester,
            EnrollmentStatus status,
            Pageable pageable) {
        log.debug("Admin fetching enrollments with filters - studentId: {}, courseId: {}, academicYear: {}, semester: {}, status: {}",
                studentId, courseId, academicYear, semester, status);

        Page<Enrollment> page = enrollmentRepository.findWithAdminFilters(
                studentId, courseId, academicYear, semester, status, pageable);
        return PageResponse.from(page.map(EnrollmentResponse::fromEntity));
    }

    /**
     * Get single enrollment details by ID.
     */
    public EnrollmentResponse getEnrollmentById(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with ID: " + enrollmentId));
        return EnrollmentResponse.fromEntity(enrollment);
    }

    /**
     * Admin create enrollment.
     */
    @Transactional
    public EnrollmentResponse createEnrollment(EnrollmentRequest request) {
        log.info("Creating enrollment for student ID: {} in course ID: {}", request.getStudentId(), request.getCourseId());

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + request.getStudentId()));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + request.getCourseId()));

        if (enrollmentRepository.existsByStudent_StudentIdAndCourse_CourseIdAndAcademicYearAndSemester(
                request.getStudentId(), request.getCourseId(), request.getAcademicYear(), request.getSemester())) {
            throw new ResourceConflictException(String.format(
                    "Student '%s' is already enrolled in course '%s' for academic year '%s' and semester %d",
                    student.getRollNumber(), course.getCourseCode(), request.getAcademicYear(), request.getSemester()));
        }

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .academicYear(request.getAcademicYear())
                .semester(request.getSemester())
                .enrollmentDate(request.getEnrollmentDate() != null ? request.getEnrollmentDate() : LocalDate.now())
                .status(request.getStatus() != null ? request.getStatus() : EnrollmentStatus.ACTIVE)
                .build();

        Enrollment saved = enrollmentRepository.save(enrollment);
        log.info("Enrollment created successfully with ID: {}", saved.getEnrollmentId());
        return EnrollmentResponse.fromEntity(saved);
    }

    /**
     * Admin update enrollment.
     */
    @Transactional
    public EnrollmentResponse updateEnrollment(Long enrollmentId, EnrollmentRequest request) {
        log.info("Updating enrollment with ID: {}", enrollmentId);

        Enrollment existing = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with ID: " + enrollmentId));

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + request.getStudentId()));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + request.getCourseId()));

        if (enrollmentRepository.existsByStudent_StudentIdAndCourse_CourseIdAndAcademicYearAndSemesterAndEnrollmentIdNot(
                request.getStudentId(), request.getCourseId(), request.getAcademicYear(), request.getSemester(), enrollmentId)) {
            throw new ResourceConflictException(String.format(
                    "Another enrollment already exists for student '%s' in course '%s' for academic year '%s' and semester %d",
                    student.getRollNumber(), course.getCourseCode(), request.getAcademicYear(), request.getSemester()));
        }

        existing.setStudent(student);
        existing.setCourse(course);
        existing.setAcademicYear(request.getAcademicYear());
        existing.setSemester(request.getSemester());
        if (request.getEnrollmentDate() != null) {
            existing.setEnrollmentDate(request.getEnrollmentDate());
        }
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }

        Enrollment updated = enrollmentRepository.save(existing);
        log.info("Enrollment updated successfully with ID: {}", updated.getEnrollmentId());
        return EnrollmentResponse.fromEntity(updated);
    }

    /**
     * Admin delete enrollment.
     */
    @Transactional
    public void deleteEnrollment(Long enrollmentId) {
        log.info("Deleting enrollment with ID: {}", enrollmentId);

        Enrollment existing = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with ID: " + enrollmentId));

        enrollmentRepository.delete(existing);
        log.info("Enrollment deleted successfully with ID: {}", enrollmentId);
    }

    /**
     * Faculty view: read-only access to enrollments for courses taught by the authenticated faculty member,
     * strictly scoped by timetable course and section assignments.
     */
    public PageResponse<EnrollmentResponse> getEnrollmentsForFaculty(
            Long userId,
            Long courseId,
            String section,
            String academicYear,
            Integer semester,
            EnrollmentStatus status,
            Pageable pageable) {
        Faculty faculty = facultyRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty profile not found for authenticated user ID: " + userId));

        log.debug("Faculty {} fetching enrollments for assigned courses (section: {})", faculty.getEmployeeCode(), section);

        Page<Enrollment> page = enrollmentRepository.findWithFacultyAndSectionFilters(
                faculty.getFacultyId(), courseId, section, academicYear, semester, status, pageable);
        return PageResponse.from(page.map(EnrollmentResponse::fromEntity));
    }

    /**
     * Student view: read-only access to the authenticated student's own enrollments.
     */
    public PageResponse<EnrollmentResponse> getEnrollmentsForStudent(
            Long userId,
            String academicYear,
            Integer semester,
            EnrollmentStatus status,
            Pageable pageable) {
        Student student = studentRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for authenticated user ID: " + userId));

        log.debug("Student {} fetching own enrollments", student.getRollNumber());

        Page<Enrollment> page = enrollmentRepository.findWithStudentFilters(
                student.getStudentId(), academicYear, semester, status, pageable);
        return PageResponse.from(page.map(EnrollmentResponse::fromEntity));
    }
}
