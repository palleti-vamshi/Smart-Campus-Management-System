package com.smartcampus.service;

import com.smartcampus.dto.request.CourseRequest;
import com.smartcampus.dto.response.CourseResponse;
import com.smartcampus.dto.response.PageResponse;
import com.smartcampus.entity.Course;
import com.smartcampus.entity.Faculty;
import com.smartcampus.entity.Program;
import com.smartcampus.exception.DuplicateResourceException;
import com.smartcampus.exception.ResourceConflictException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Service handling Course master data operations.
 */
@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final ProgramRepository programRepository;
    private final FacultyRepository facultyRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final ExamRepository examRepository;
    private final TimetableRepository timetableRepository;

    public CourseService(CourseRepository courseRepository,
                         ProgramRepository programRepository,
                         FacultyRepository facultyRepository,
                         EnrollmentRepository enrollmentRepository,
                         AttendanceRepository attendanceRepository,
                         ExamRepository examRepository,
                         TimetableRepository timetableRepository) {
        this.courseRepository = courseRepository;
        this.programRepository = programRepository;
        this.facultyRepository = facultyRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.attendanceRepository = attendanceRepository;
        this.examRepository = examRepository;
        this.timetableRepository = timetableRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<CourseResponse> getCourses(Long programId,
                                                  Integer semester,
                                                  Long facultyId,
                                                  String search,
                                                  Pageable pageable) {
        String cleanSearch = StringUtils.hasText(search) ? search.trim() : null;
        Page<Course> page = courseRepository.findWithFilters(programId, semester, facultyId, cleanSearch, pageable);
        return PageResponse.from(page.map(CourseResponse::fromEntity));
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
        return CourseResponse.fromEntity(course);
    }

    @Transactional
    public CourseResponse createCourse(CourseRequest request) {
        Program program = programRepository.findById(request.getProgramId())
                .orElseThrow(() -> new ResourceNotFoundException("Program", "id", request.getProgramId()));

        Faculty faculty = null;
        if (request.getFacultyId() != null) {
            faculty = facultyRepository.findById(request.getFacultyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Faculty", "id", request.getFacultyId()));
        }

        String courseCode = request.getCourseCode().trim().toUpperCase();
        if (courseRepository.existsByCourseCode(courseCode)) {
            throw new DuplicateResourceException("Course", "courseCode", courseCode);
        }

        Course course = Course.builder()
                .program(program)
                .faculty(faculty)
                .courseCode(courseCode)
                .courseName(request.getCourseName().trim())
                .credits(request.getCredits())
                .semester(request.getSemester())
                .courseType(request.getCourseType())
                .build();

        Course saved = courseRepository.save(course);
        return CourseResponse.fromEntity(saved);
    }

    @Transactional
    public CourseResponse updateCourse(Long id, CourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));

        Program program = programRepository.findById(request.getProgramId())
                .orElseThrow(() -> new ResourceNotFoundException("Program", "id", request.getProgramId()));

        Faculty faculty = null;
        if (request.getFacultyId() != null) {
            faculty = facultyRepository.findById(request.getFacultyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Faculty", "id", request.getFacultyId()));
        }

        String courseCode = request.getCourseCode().trim().toUpperCase();
        if (courseRepository.existsByCourseCodeAndCourseIdNot(courseCode, id)) {
            throw new DuplicateResourceException("Course", "courseCode", courseCode);
        }

        course.setProgram(program);
        course.setFaculty(faculty);
        course.setCourseCode(courseCode);
        course.setCourseName(request.getCourseName().trim());
        course.setCredits(request.getCredits());
        course.setSemester(request.getSemester());
        course.setCourseType(request.getCourseType());

        Course updated = courseRepository.save(course);
        return CourseResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));

        if (enrollmentRepository.existsByCourse_CourseId(id)) {
            throw new ResourceConflictException("Cannot delete course because student enrollments exist");
        }
        if (attendanceRepository.existsByCourse_CourseId(id)) {
            throw new ResourceConflictException("Cannot delete course because attendance records exist");
        }
        if (examRepository.existsByCourse_CourseId(id)) {
            throw new ResourceConflictException("Cannot delete course because exams are scheduled for it");
        }
        if (timetableRepository.existsByCourse_CourseId(id)) {
            throw new ResourceConflictException("Cannot delete course because timetable entries exist");
        }

        courseRepository.delete(course);
    }
}
