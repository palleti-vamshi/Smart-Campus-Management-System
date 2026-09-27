package com.smartcampus.service;

import com.smartcampus.dto.request.AttendanceRequest;
import com.smartcampus.dto.request.AttendanceUpdateRequest;
import com.smartcampus.dto.response.AttendanceResponse;
import com.smartcampus.dto.response.AttendanceSummaryResponse;
import com.smartcampus.dto.response.PageResponse;
import com.smartcampus.entity.Attendance;
import com.smartcampus.entity.Course;
import com.smartcampus.entity.Faculty;
import com.smartcampus.entity.Student;
import com.smartcampus.entity.enums.AttendanceStatus;
import com.smartcampus.exception.InvalidOperationException;
import com.smartcampus.exception.ResourceConflictException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.AttendanceRepository;
import com.smartcampus.repository.CourseRepository;
import com.smartcampus.repository.EnrollmentRepository;
import com.smartcampus.repository.FacultyRepository;
import com.smartcampus.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final FacultyRepository facultyRepository;
    private final EnrollmentRepository enrollmentRepository;

    /**
     * Faculty records attendance for a course they teach.
     */
    @Transactional
    public AttendanceResponse recordAttendanceByFaculty(Long userId, AttendanceRequest request) {
        Faculty faculty = facultyRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty profile not found for user ID: " + userId));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + request.getCourseId()));

        if (course.getFaculty() == null || !course.getFaculty().getFacultyId().equals(faculty.getFacultyId())) {
            log.warn("Faculty ID {} attempted to record attendance for course ID {} not assigned to them",
                    faculty.getFacultyId(), course.getCourseId());
            throw new AccessDeniedException("You are not authorized to mark attendance for a course you do not teach");
        }

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + request.getStudentId()));

        validateEnrollmentAndDuplicate(student, course, request.getAttendanceDate());

        Attendance attendance = Attendance.builder()
                .student(student)
                .course(course)
                .attendanceDate(request.getAttendanceDate())
                .status(request.getStatus())
                .markedBy(faculty)
                .build();

        Attendance saved = attendanceRepository.save(attendance);
        log.info("Attendance marked by faculty {} for student {} in course {} on {}",
                faculty.getEmployeeCode(), student.getRollNumber(), course.getCourseCode(), request.getAttendanceDate());
        return AttendanceResponse.fromEntity(saved);
    }

    /**
     * Admin records attendance for any course.
     */
    @Transactional
    public AttendanceResponse recordAttendanceByAdmin(AttendanceRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + request.getStudentId()));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + request.getCourseId()));

        validateEnrollmentAndDuplicate(student, course, request.getAttendanceDate());

        Faculty markedBy;
        if (request.getMarkedByFacultyId() != null) {
            markedBy = facultyRepository.findById(request.getMarkedByFacultyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with ID: " + request.getMarkedByFacultyId()));
        } else if (course.getFaculty() != null) {
            markedBy = course.getFaculty();
        } else {
            throw new ResourceConflictException("Course has no assigned faculty; specify markedByFacultyId");
        }

        Attendance attendance = Attendance.builder()
                .student(student)
                .course(course)
                .attendanceDate(request.getAttendanceDate())
                .status(request.getStatus())
                .markedBy(markedBy)
                .build();

        Attendance saved = attendanceRepository.save(attendance);
        log.info("Attendance marked by Admin for student {} in course {} on {}",
                student.getRollNumber(), course.getCourseCode(), request.getAttendanceDate());
        return AttendanceResponse.fromEntity(saved);
    }

    /**
     * Faculty view: list attendance for assigned courses with optional filters.
     */
    public PageResponse<AttendanceResponse> getAttendanceForFaculty(
            Long userId,
            Long courseId,
            Long studentId,
            LocalDate attendanceDate,
            LocalDate startDate,
            LocalDate endDate,
            AttendanceStatus status,
            Pageable pageable) {
        Faculty faculty = facultyRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty profile not found for user ID: " + userId));

        validateDateRange(startDate, endDate);

        Page<Attendance> page = attendanceRepository.findWithFacultyFilters(
                faculty.getFacultyId(), courseId, studentId, attendanceDate, startDate, endDate, status, pageable);
        return PageResponse.from(page.map(AttendanceResponse::fromEntity));
    }

    /**
     * Admin view: list attendance across all courses.
     */
    public PageResponse<AttendanceResponse> getAttendanceForAdmin(
            Long courseId,
            Long studentId,
            Long facultyId,
            LocalDate attendanceDate,
            LocalDate startDate,
            LocalDate endDate,
            AttendanceStatus status,
            Pageable pageable) {
        validateDateRange(startDate, endDate);

        Page<Attendance> page = attendanceRepository.findWithAdminFilters(
                courseId, studentId, facultyId, attendanceDate, startDate, endDate, status, pageable);
        return PageResponse.from(page.map(AttendanceResponse::fromEntity));
    }

    /**
     * Get attendance record by ID.
     */
    public AttendanceResponse getAttendanceById(Long attendanceId) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found with ID: " + attendanceId));
        return AttendanceResponse.fromEntity(attendance);
    }

    /**
     * Faculty updates attendance for an assigned course.
     */
    @Transactional
    public AttendanceResponse updateAttendanceByFaculty(Long userId, Long attendanceId, AttendanceUpdateRequest request) {
        Faculty faculty = facultyRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty profile not found for user ID: " + userId));

        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found with ID: " + attendanceId));

        if (attendance.getCourse().getFaculty() == null ||
                !attendance.getCourse().getFaculty().getFacultyId().equals(faculty.getFacultyId())) {
            log.warn("Faculty ID {} attempted to update attendance ID {} for a course not taught by them",
                    faculty.getFacultyId(), attendanceId);
            throw new AccessDeniedException("You are not authorized to update attendance for a course you do not teach");
        }

        applyAttendanceUpdate(attendance, request);
        Attendance updated = attendanceRepository.save(attendance);
        return AttendanceResponse.fromEntity(updated);
    }

    /**
     * Admin updates attendance.
     */
    @Transactional
    public AttendanceResponse updateAttendanceByAdmin(Long attendanceId, AttendanceUpdateRequest request) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found with ID: " + attendanceId));

        applyAttendanceUpdate(attendance, request);
        Attendance updated = attendanceRepository.save(attendance);
        return AttendanceResponse.fromEntity(updated);
    }

    /**
     * Faculty deletes attendance for an assigned course.
     */
    @Transactional
    public void deleteAttendanceByFaculty(Long userId, Long attendanceId) {
        Faculty faculty = facultyRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty profile not found for user ID: " + userId));

        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found with ID: " + attendanceId));

        if (attendance.getCourse().getFaculty() == null ||
                !attendance.getCourse().getFaculty().getFacultyId().equals(faculty.getFacultyId())) {
            log.warn("Faculty ID {} attempted to delete attendance ID {} for a course not taught by them",
                    faculty.getFacultyId(), attendanceId);
            throw new AccessDeniedException("You are not authorized to delete attendance for a course you do not teach");
        }

        attendanceRepository.delete(attendance);
        log.info("Attendance ID {} deleted by faculty {}", attendanceId, faculty.getEmployeeCode());
    }

    /**
     * Admin deletes attendance.
     */
    @Transactional
    public void deleteAttendanceByAdmin(Long attendanceId) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found with ID: " + attendanceId));

        attendanceRepository.delete(attendance);
        log.info("Attendance ID {} deleted by admin", attendanceId);
    }

    /**
     * Student view: list own attendance.
     */
    public PageResponse<AttendanceResponse> getAttendanceForStudent(
            Long userId,
            Long courseId,
            LocalDate startDate,
            LocalDate endDate,
            AttendanceStatus status,
            Pageable pageable) {
        Student student = studentRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user ID: " + userId));

        validateDateRange(startDate, endDate);

        Page<Attendance> page = attendanceRepository.findWithStudentFilters(
                student.getStudentId(), courseId, null, startDate, endDate, status, pageable);
        return PageResponse.from(page.map(AttendanceResponse::fromEntity));
    }

    /**
     * Student view: attendance summary aggregated per course.
     */
    public List<AttendanceSummaryResponse> getStudentAttendanceSummary(Long userId) {
        Student student = studentRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user ID: " + userId));

        return attendanceRepository.getStudentAttendanceSummary(student.getStudentId());
    }

    private void validateEnrollmentAndDuplicate(Student student, Course course, LocalDate date) {
        if (!enrollmentRepository.existsByStudent_StudentIdAndCourse_CourseId(student.getStudentId(), course.getCourseId())) {
            throw new ResourceConflictException(String.format(
                    "Student '%s' is not enrolled in course '%s'",
                    student.getRollNumber(), course.getCourseCode()));
        }

        if (attendanceRepository.existsByStudent_StudentIdAndCourse_CourseIdAndAttendanceDate(
                student.getStudentId(), course.getCourseId(), date)) {
            throw new ResourceConflictException(String.format(
                    "Attendance already recorded for student '%s' in course '%s' on %s",
                    student.getRollNumber(), course.getCourseCode(), date));
        }
    }

    private void applyAttendanceUpdate(Attendance attendance, AttendanceUpdateRequest request) {
        if (request.getAttendanceDate() != null && !request.getAttendanceDate().equals(attendance.getAttendanceDate())) {
            if (attendanceRepository.existsByStudent_StudentIdAndCourse_CourseIdAndAttendanceDateAndAttendanceIdNot(
                    attendance.getStudent().getStudentId(),
                    attendance.getCourse().getCourseId(),
                    request.getAttendanceDate(),
                    attendance.getAttendanceId())) {
                throw new ResourceConflictException(String.format(
                        "Attendance already exists for student '%s' in course '%s' on %s",
                        attendance.getStudent().getRollNumber(),
                        attendance.getCourse().getCourseCode(),
                        request.getAttendanceDate()));
            }
            attendance.setAttendanceDate(request.getAttendanceDate());
        }

        if (request.getStatus() != null) {
            attendance.setStatus(request.getStatus());
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new InvalidOperationException("Start date cannot be after end date");
        }
    }
}
