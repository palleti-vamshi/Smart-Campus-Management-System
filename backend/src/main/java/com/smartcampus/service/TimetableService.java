package com.smartcampus.service;

import com.smartcampus.dto.request.TimetableRequest;
import com.smartcampus.dto.response.PageResponse;
import com.smartcampus.dto.response.TimetableResponse;
import com.smartcampus.entity.*;
import com.smartcampus.exception.InvalidOperationException;
import com.smartcampus.exception.ResourceConflictException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;

@Service
@Slf4j
public class TimetableService {

    private final TimetableRepository timetableRepository;
    private final ProgramRepository programRepository;
    private final CourseRepository courseRepository;
    private final FacultyRepository facultyRepository;
    private final ClassroomRepository classroomRepository;
    private final StudentRepository studentRepository;

    public TimetableService(TimetableRepository timetableRepository,
                            ProgramRepository programRepository,
                            CourseRepository courseRepository,
                            FacultyRepository facultyRepository,
                            ClassroomRepository classroomRepository,
                            StudentRepository studentRepository) {
        this.timetableRepository = timetableRepository;
        this.programRepository = programRepository;
        this.courseRepository = courseRepository;
        this.facultyRepository = facultyRepository;
        this.classroomRepository = classroomRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<TimetableResponse> getTimetableForAdmin(
            Long programId,
            Long courseId,
            Long facultyId,
            Long classroomId,
            String dayOfWeek,
            Integer semester,
            String academicYear,
            Pageable pageable) {
        Page<Timetable> page = timetableRepository.findWithAdminFilters(
                programId, courseId, facultyId, classroomId, dayOfWeek, semester, academicYear, pageable);
        return PageResponse.from(page.map(TimetableResponse::fromEntity));
    }

    @Transactional(readOnly = true)
    public PageResponse<TimetableResponse> getTimetableForFaculty(
            Long userId,
            String dayOfWeek,
            Integer semester,
            String academicYear,
            Pageable pageable) {
        Faculty faculty = facultyRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty profile not found for user ID: " + userId));

        Page<Timetable> page = timetableRepository.findWithFacultyFilters(
                faculty.getFacultyId(), dayOfWeek, semester, academicYear, pageable);
        return PageResponse.from(page.map(TimetableResponse::fromEntity));
    }

    @Transactional(readOnly = true)
    public PageResponse<TimetableResponse> getTimetableForStudent(
            Long userId,
            String dayOfWeek,
            Integer semester,
            String academicYear,
            Pageable pageable) {
        Student student = studentRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user ID: " + userId));

        Integer targetSemester = semester != null ? semester : student.getCurrentSemester();

        Page<Timetable> page = timetableRepository.findWithStudentFilters(
                student.getProgram().getProgramId(), dayOfWeek, targetSemester, academicYear, pageable);
        return PageResponse.from(page.map(TimetableResponse::fromEntity));
    }

    @Transactional(readOnly = true)
    public TimetableResponse getTimetableById(Long id) {
        Timetable timetable = timetableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Timetable entry not found with ID: " + id));
        return TimetableResponse.fromEntity(timetable);
    }

    @Transactional
    public TimetableResponse createTimetable(TimetableRequest request) {
        validateTimeRange(request);
        String dayOfWeek = normalizeAndValidateDayOfWeek(request.getDayOfWeek());

        Program program = programRepository.findById(request.getProgramId())
                .orElseThrow(() -> new ResourceNotFoundException("Program not found with ID: " + request.getProgramId()));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + request.getCourseId()));

        Faculty faculty = facultyRepository.findById(request.getFacultyId())
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with ID: " + request.getFacultyId()));

        Classroom classroom = classroomRepository.findById(request.getClassroomId())
                .orElseThrow(() -> new ResourceNotFoundException("Classroom not found with ID: " + request.getClassroomId()));

        if (!Boolean.TRUE.equals(classroom.getIsActive())) {
            throw new ResourceConflictException("Cannot schedule class in an inactive classroom: " + classroom.getRoomNumber());
        }

        if (!course.getProgram().getProgramId().equals(program.getProgramId())) {
            throw new ResourceConflictException(String.format(
                    "Course '%s' does not belong to program '%s'",
                    course.getCourseCode(), program.getProgramCode()));
        }

        validateConflicts(null, classroom, faculty, program, dayOfWeek, request);

        Timetable timetable = Timetable.builder()
                .program(program)
                .course(course)
                .faculty(faculty)
                .classroom(classroom)
                .dayOfWeek(dayOfWeek)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .semester(request.getSemester())
                .academicYear(request.getAcademicYear().trim())
                .build();

        Timetable saved = timetableRepository.save(timetable);
        log.info("Timetable entry created: ID {}, Course {}, Day {}, Time {}-{}",
                saved.getTimetableId(), course.getCourseCode(), dayOfWeek, request.getStartTime(), request.getEndTime());
        return TimetableResponse.fromEntity(saved);
    }

    @Transactional
    public TimetableResponse updateTimetable(Long id, TimetableRequest request) {
        Timetable timetable = timetableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Timetable entry not found with ID: " + id));

        validateTimeRange(request);
        String dayOfWeek = normalizeAndValidateDayOfWeek(request.getDayOfWeek());

        Program program = programRepository.findById(request.getProgramId())
                .orElseThrow(() -> new ResourceNotFoundException("Program not found with ID: " + request.getProgramId()));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + request.getCourseId()));

        Faculty faculty = facultyRepository.findById(request.getFacultyId())
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with ID: " + request.getFacultyId()));

        Classroom classroom = classroomRepository.findById(request.getClassroomId())
                .orElseThrow(() -> new ResourceNotFoundException("Classroom not found with ID: " + request.getClassroomId()));

        if (!Boolean.TRUE.equals(classroom.getIsActive())) {
            throw new ResourceConflictException("Cannot schedule class in an inactive classroom: " + classroom.getRoomNumber());
        }

        if (!course.getProgram().getProgramId().equals(program.getProgramId())) {
            throw new ResourceConflictException(String.format(
                    "Course '%s' does not belong to program '%s'",
                    course.getCourseCode(), program.getProgramCode()));
        }

        validateConflicts(id, classroom, faculty, program, dayOfWeek, request);

        timetable.setProgram(program);
        timetable.setCourse(course);
        timetable.setFaculty(faculty);
        timetable.setClassroom(classroom);
        timetable.setDayOfWeek(dayOfWeek);
        timetable.setStartTime(request.getStartTime());
        timetable.setEndTime(request.getEndTime());
        timetable.setSemester(request.getSemester());
        timetable.setAcademicYear(request.getAcademicYear().trim());

        Timetable updated = timetableRepository.save(timetable);
        log.info("Timetable entry updated: ID {}", updated.getTimetableId());
        return TimetableResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteTimetable(Long id) {
        Timetable timetable = timetableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Timetable entry not found with ID: " + id));

        timetableRepository.delete(timetable);
        log.info("Timetable entry deleted: ID {}", id);
    }

    private void validateTimeRange(TimetableRequest request) {
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new InvalidOperationException("Start time and end time are required");
        }
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new InvalidOperationException("Start time must be strictly before end time");
        }
    }

    private String normalizeAndValidateDayOfWeek(String dayOfWeek) {
        if (dayOfWeek == null || dayOfWeek.trim().isEmpty()) {
            throw new InvalidOperationException("Day of week is required");
        }
        String normalized = dayOfWeek.trim().toUpperCase();
        try {
            DayOfWeek.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new InvalidOperationException("Invalid day of week: '" + dayOfWeek + "'. Must be a valid day (e.g., MONDAY, TUESDAY)");
        }
        return normalized;
    }

    private void validateConflicts(Long excludeId,
                                   Classroom classroom,
                                   Faculty faculty,
                                   Program program,
                                   String dayOfWeek,
                                   TimetableRequest request) {
        String academicYear = request.getAcademicYear().trim();

        // Rule 2: Prevent classroom clashes
        boolean classroomConflict = timetableRepository.hasClassroomConflict(
                classroom.getClassroomId(),
                dayOfWeek,
                request.getSemester(),
                academicYear,
                request.getStartTime(),
                request.getEndTime(),
                excludeId
        );
        if (classroomConflict) {
            throw new ResourceConflictException(String.format(
                    "Classroom scheduling conflict: Room '%s' is already booked for an overlapping time slot on %s",
                    classroom.getRoomNumber(), dayOfWeek));
        }

        // Rule 3: Prevent faculty clashes
        boolean facultyConflict = timetableRepository.hasFacultyConflict(
                faculty.getFacultyId(),
                dayOfWeek,
                request.getSemester(),
                academicYear,
                request.getStartTime(),
                request.getEndTime(),
                excludeId
        );
        if (facultyConflict) {
            String facultyName = (faculty.getFirstName() + (faculty.getLastName() != null ? " " + faculty.getLastName() : "")).trim();
            throw new ResourceConflictException(String.format(
                    "Faculty scheduling conflict: Faculty member '%s' (%s) is already scheduled for an overlapping time slot on %s",
                    facultyName, faculty.getEmployeeCode(), dayOfWeek));
        }

        // Rule 4: Prevent program scheduling conflicts
        boolean programConflict = timetableRepository.hasProgramConflict(
                program.getProgramId(),
                dayOfWeek,
                request.getSemester(),
                academicYear,
                request.getStartTime(),
                request.getEndTime(),
                excludeId
        );
        if (programConflict) {
            throw new ResourceConflictException(String.format(
                    "Program scheduling conflict: Program '%s' already has an overlapping class scheduled on %s",
                    program.getProgramCode(), dayOfWeek));
        }
    }
}
