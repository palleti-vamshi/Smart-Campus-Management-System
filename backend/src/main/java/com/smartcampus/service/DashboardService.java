package com.smartcampus.service;

import com.smartcampus.dto.dashboard.*;
import com.smartcampus.dto.response.AttendanceSummaryResponse;
import com.smartcampus.dto.response.NoticeResponse;
import com.smartcampus.entity.*;
import com.smartcampus.entity.enums.DocumentStatus;
import com.smartcampus.entity.enums.EnrollmentStatus;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardService {

    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final ProgramRepository programRepository;
    private final CourseRepository courseRepository;
    private final ClassroomRepository classroomRepository;
    private final AttendanceRepository attendanceRepository;
    private final ExamRepository examRepository;
    private final MarkRepository markRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final TimetableRepository timetableRepository;
    private final NoticeRepository noticeRepository;
    private final DocumentRequestRepository documentRequestRepository;

    /**
     * Aggregates department-wide summary data for the administrator dashboard.
     */
    public AdminDashboardResponse getAdminDashboard() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();
        String todayDay = today.getDayOfWeek().name();

        // 1. Department Summary
        DepartmentSummary deptSummary = DepartmentSummary.builder()
                .totalStudents(studentRepository.count())
                .totalFaculty(facultyRepository.count())
                .totalPrograms(programRepository.count())
                .totalCourses(courseRepository.count())
                .totalClassrooms(classroomRepository.count())
                .build();

        // 2. Student Distribution
        List<ProgramStudentCount> studentsByProgram = new ArrayList<>();
        for (Object[] row : studentRepository.countStudentsByProgram()) {
            studentsByProgram.add(ProgramStudentCount.builder()
                    .programId((Long) row[0])
                    .programCode((String) row[1])
                    .programName((String) row[2])
                    .studentCount(((Number) row[3]).longValue())
                    .build());
        }

        List<SemesterStudentCount> studentsBySemester = new ArrayList<>();
        for (Object[] row : studentRepository.countStudentsBySemester()) {
            studentsBySemester.add(SemesterStudentCount.builder()
                    .semester((Integer) row[0])
                    .studentCount(((Number) row[1]).longValue())
                    .build());
        }

        // 3. Attendance Overview
        long totalAttendance = 0L;
        long presentAttendance = 0L;
        long absentAttendance = 0L;
        long lateAttendance = 0L;
        Double overallAttendancePct = 0.0;

        List<Object[]> overallStats = attendanceRepository.getOverallAttendanceStats();
        if (overallStats != null && !overallStats.isEmpty() && overallStats.get(0) != null) {
            Object[] row = overallStats.get(0);
            totalAttendance = row[0] != null ? ((Number) row[0]).longValue() : 0L;
            presentAttendance = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            absentAttendance = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            lateAttendance = row[3] != null ? ((Number) row[3]).longValue() : 0L;
            if (totalAttendance > 0) {
                overallAttendancePct = roundTwoDecimals(((double) presentAttendance / totalAttendance) * 100.0);
            }
        }

        List<ProgramAttendanceSummary> programAttendance = new ArrayList<>();
        for (Object[] row : attendanceRepository.getAttendanceStatsByProgram()) {
            long progTotal = row[3] != null ? ((Number) row[3]).longValue() : 0L;
            long progPresent = row[4] != null ? ((Number) row[4]).longValue() : 0L;
            Double progPct = progTotal > 0 ? roundTwoDecimals(((double) progPresent / progTotal) * 100.0) : 0.0;

            programAttendance.add(ProgramAttendanceSummary.builder()
                    .programId((Long) row[0])
                    .programCode((String) row[1])
                    .programName((String) row[2])
                    .totalRecords(progTotal)
                    .presentRecords(progPresent)
                    .attendancePercentage(progPct)
                    .build());
        }

        AttendanceSummary attendanceOverview = AttendanceSummary.builder()
                .totalClasses(totalAttendance)
                .presentClasses(presentAttendance)
                .absentClasses(absentAttendance)
                .lateClasses(lateAttendance)
                .overallPercentage(overallAttendancePct)
                .byProgram(programAttendance)
                .byCourse(new ArrayList<>())
                .build();

        // 4. Academic Performance
        long examCount = examRepository.count();
        long marksCount = markRepository.count();
        Double avgMarks = 0.0;

        List<Object[]> overallMarkStats = markRepository.getOverallMarkStats();
        if (overallMarkStats != null && !overallMarkStats.isEmpty() && overallMarkStats.get(0) != null) {
            Object[] row = overallMarkStats.get(0);
            if (row[1] != null) {
                avgMarks = roundTwoDecimals(((Number) row[1]).doubleValue());
            }
        }

        List<CoursePerformanceSummary> coursePerformance = new ArrayList<>();
        for (Object[] row : markRepository.getCoursePerformanceStats()) {
            coursePerformance.add(CoursePerformanceSummary.builder()
                    .courseId((Long) row[0])
                    .courseCode((String) row[1])
                    .courseName((String) row[2])
                    .marksCount(((Number) row[3]).longValue())
                    .averageMarks(row[4] != null ? roundTwoDecimals(((Number) row[4]).doubleValue()) : 0.0)
                    .highestMark(row[5] != null ? roundTwoDecimals(((Number) row[5]).doubleValue()) : 0.0)
                    .lowestMark(row[6] != null ? roundTwoDecimals(((Number) row[6]).doubleValue()) : 0.0)
                    .build());
        }

        AcademicPerformanceSummary academicSummary = AcademicPerformanceSummary.builder()
                .examCount(examCount)
                .marksCount(marksCount)
                .averageMarks(avgMarks)
                .coursePerformance(coursePerformance)
                .build();

        // 5. Enrollment Summary
        List<ProgramEnrollmentSummary> programEnrollments = new ArrayList<>();
        for (Object[] row : enrollmentRepository.countEnrollmentsByProgram()) {
            programEnrollments.add(ProgramEnrollmentSummary.builder()
                    .programId((Long) row[0])
                    .programCode((String) row[1])
                    .programName((String) row[2])
                    .enrollmentCount(((Number) row[3]).longValue())
                    .build());
        }

        EnrollmentSummary enrollmentSummary = EnrollmentSummary.builder()
                .totalEnrollments(enrollmentRepository.count())
                .activeEnrollments(enrollmentRepository.countByStatus(EnrollmentStatus.ACTIVE))
                .byProgram(programEnrollments)
                .byCourse(new ArrayList<>())
                .build();

        // 6. Timetable Summary
        TimetableSummary timetableSummary = TimetableSummary.builder()
                .totalEntries(timetableRepository.count())
                .todayClassesCount(timetableRepository.countByDayOfWeekIgnoreCase(todayDay))
                .todaySchedule(new ArrayList<>())
                .weeklySchedule(new ArrayList<>())
                .build();

        // 7. Notice Summary
        Map<String, Long> noticesByCategory = new HashMap<>();
        for (Object[] row : noticeRepository.countActiveNoticesByCategory(now)) {
            if (row[0] != null) {
                noticesByCategory.put(row[0].toString(), ((Number) row[1]).longValue());
            }
        }

        NoticeSummary noticeSummary = NoticeSummary.builder()
                .totalActive(noticeRepository.countActiveNotices(now))
                .urgentCount(noticeRepository.countActiveUrgentNotices(now))
                .byCategory(noticesByCategory)
                .recentNotices(new ArrayList<>())
                .build();

        // 8. Document Summary
        DocumentSummary docSummary = buildDocumentSummary(documentRequestRepository.countByStatusGrouped());

        // 9. Upcoming Academic Events (Exams)
        List<UpcomingExamSummary> upcomingExams = new ArrayList<>();
        for (Exam e : examRepository.findUpcomingExams(today, PageRequest.of(0, 5))) {
            upcomingExams.add(mapToUpcomingExam(e));
        }

        return AdminDashboardResponse.builder()
                .departmentSummary(deptSummary)
                .studentsByProgram(studentsByProgram)
                .studentsBySemester(studentsBySemester)
                .attendanceOverview(attendanceOverview)
                .academicPerformance(academicSummary)
                .enrollmentSummary(enrollmentSummary)
                .timetableSummary(timetableSummary)
                .noticeSummary(noticeSummary)
                .documentSummary(docSummary)
                .upcomingExams(upcomingExams)
                .build();
    }

    /**
     * Aggregates faculty-specific summary data restricted to assigned courses.
     */
    public FacultyDashboardResponse getFacultyDashboard(Long userId) {
        Faculty faculty = facultyRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty profile not found for user ID: " + userId));

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();
        String todayDay = today.getDayOfWeek().name();

        List<Object[]> timetableOfferings = timetableRepository.findDistinctCoursesAndSectionsByFaculty(faculty.getFacultyId());
        List<CourseSummary> courseSummaries = new ArrayList<>();
        List<CourseEnrollmentSummary> enrollmentsByCourse = new ArrayList<>();
        long totalStudents = 0L;
        long totalEnrollments = 0L;

        if (!timetableOfferings.isEmpty()) {
            totalStudents = enrollmentRepository.countDistinctStudentsForFacultyScope(faculty.getFacultyId());
            for (Object[] row : timetableOfferings) {
                Long cId = (Long) row[0];
                String cCode = (String) row[1];
                String cName = (String) row[2];
                com.smartcampus.entity.enums.CourseType cType = (com.smartcampus.entity.enums.CourseType) row[3];
                Double creds = row[4] != null ? ((Number) row[4]).doubleValue() : null;
                Integer sem = (Integer) row[5];
                String sec = (String) row[6];

                long count = enrollmentRepository.countActiveEnrollmentsForCourseAndSection(cId, sec);
                totalEnrollments += count;

                courseSummaries.add(CourseSummary.builder()
                        .courseId(cId)
                        .courseCode(cCode)
                        .courseName(cName)
                        .courseType(cType != null ? cType.name() : "THEORY")
                        .credits(creds)
                        .semester(sem)
                        .section(sec)
                        .enrolledStudents(count)
                        .build());

                enrollmentsByCourse.add(CourseEnrollmentSummary.builder()
                        .courseId(cId)
                        .courseCode(cCode)
                        .courseName(cName + " (Sec " + sec + ")")
                        .enrollmentCount(count)
                        .build());
            }
        } else {
            List<Course> assignedCourses = courseRepository.findByFaculty_FacultyId(faculty.getFacultyId());
            List<Long> courseIds = assignedCourses.stream().map(Course::getCourseId).toList();
            if (!courseIds.isEmpty()) {
                totalStudents = enrollmentRepository.countDistinctStudentsForCourseIds(courseIds);
                for (Course c : assignedCourses) {
                    CourseSummary s = this.mapToCourseSummary(c);
                    courseSummaries.add(s);
                }
            }
        }

        FacultyStudentSummary studentEnrollmentSummary = FacultyStudentSummary.builder()
                .totalStudents(totalStudents)
                .totalEnrollments(totalEnrollments)
                .enrollmentsByCourse(enrollmentsByCourse)
                .build();

        // 2. Attendance Overview (Scoped to Faculty's assigned sections)
        long totalAtt = 0L;
        long presentAtt = 0L;
        long absentAtt = 0L;
        List<CourseAttendanceSummary> courseAttendance = new ArrayList<>();

        for (Object[] row : attendanceRepository.getAttendanceStatsForFacultyScope(faculty.getFacultyId())) {
            long tot = ((Number) row[3]).longValue();
            long pres = row[4] != null ? ((Number) row[4]).longValue() : 0L;
            long abs = row[5] != null ? ((Number) row[5]).longValue() : 0L;

            totalAtt += tot;
            presentAtt += pres;
            absentAtt += abs;

            Double pct = tot > 0 ? roundTwoDecimals(((double) pres / tot) * 100.0) : 0.0;
            courseAttendance.add(CourseAttendanceSummary.builder()
                    .courseId((Long) row[0])
                    .courseCode((String) row[1])
                    .courseName((String) row[2])
                    .totalClasses(tot)
                    .presentClasses(pres)
                    .absentClasses(abs)
                    .lateClasses(0L)
                    .attendancePercentage(pct)
                    .percentage(pct)
                    .build());
        }

        Double overallAttPct = totalAtt > 0 ? roundTwoDecimals(((double) presentAtt / totalAtt) * 100.0) : 0.0;

        AttendanceSummary attendanceOverview = AttendanceSummary.builder()
                .totalClasses(totalAtt)
                .presentClasses(presentAtt)
                .absentClasses(absentAtt)
                .lateClasses(0L)
                .overallPercentage(overallAttPct)
                .byProgram(new ArrayList<>())
                .byCourse(courseAttendance)
                .build();

        // 3. Exams (Scoped to faculty teaching courses)
        long totalExams = examRepository.countByFacultyScope(faculty.getFacultyId());
        List<UpcomingExamSummary> upcomingExams = new ArrayList<>();
        for (Exam e : examRepository.findUpcomingExamsForFacultyScope(faculty.getFacultyId(), today)) {
            upcomingExams.add(mapToUpcomingExam(e));
        }

        FacultyExamSummary examSummary = FacultyExamSummary.builder()
                .totalExams(totalExams)
                .totalUpcomingExams(upcomingExams.size())
                .upcomingExams(upcomingExams)
                .build();

        // 4. Marks (Scoped to faculty teaching scope)
        long marksEntered = markRepository.countMarksForFacultyScope(faculty.getFacultyId());
        List<CoursePerformanceSummary> coursePerformance = new ArrayList<>();
        for (Object[] row : markRepository.getCoursePerformanceStatsForFacultyScope(faculty.getFacultyId())) {
            coursePerformance.add(CoursePerformanceSummary.builder()
                    .courseId((Long) row[0])
                    .courseCode((String) row[1])
                    .courseName((String) row[2])
                    .marksCount(((Number) row[3]).longValue())
                    .averageMarks(row[4] != null ? roundTwoDecimals(((Number) row[4]).doubleValue()) : 0.0)
                    .highestMark(row[5] != null ? roundTwoDecimals(((Number) row[5]).doubleValue()) : 0.0)
                    .lowestMark(row[6] != null ? roundTwoDecimals(((Number) row[6]).doubleValue()) : 0.0)
                    .build());
        }

        FacultyMarksSummary marksSummary = FacultyMarksSummary.builder()
                .marksEnteredCount(marksEntered)
                .coursePerformance(coursePerformance)
                .build();

        // 5. Timetable
        List<Timetable> allFacultyTimetable = timetableRepository.findByFaculty_FacultyId(faculty.getFacultyId());
        List<Timetable> todayFacultyTimetable = timetableRepository.findByFacultyAndDayOfWeek(faculty.getFacultyId(), todayDay);

        TimetableSummary timetableSummary = TimetableSummary.builder()
                .totalEntries(allFacultyTimetable.size())
                .todayClassesCount(todayFacultyTimetable.size())
                .todaySchedule(todayFacultyTimetable.stream().map(this::mapToTimetableClass).toList())
                .weeklySchedule(allFacultyTimetable.stream().map(this::mapToTimetableClass).toList())
                .build();

        // 6. Notices (department wide)
        List<Notice> deptNotices = noticeRepository.findActiveDepartmentWideNotices(now, PageRequest.of(0, 5));
        long urgentDeptNotices = noticeRepository.countActiveUrgentDepartmentWideNotices(now);

        NoticeSummary noticeSummary = NoticeSummary.builder()
                .totalActive(deptNotices.size())
                .urgentCount(urgentDeptNotices)
                .byCategory(new HashMap<>())
                .recentNotices(deptNotices.stream().map(this::mapToNoticeResponse).toList())
                .build();

        String deptName = faculty.getDepartment() != null ? faculty.getDepartment().getDepartmentName() : "N/A";
        String fullName = (faculty.getFirstName() + " " + (faculty.getLastName() != null ? faculty.getLastName() : "")).trim();

        return FacultyDashboardResponse.builder()
                .facultyId(faculty.getFacultyId())
                .facultyName(fullName)
                .employeeCode(faculty.getEmployeeCode())
                .designation(faculty.getDesignation())
                .departmentName(deptName)
                .totalAssignedCourses(courseSummaries.size())
                .assignedCourses(courseSummaries)
                .studentEnrollmentSummary(studentEnrollmentSummary)
                .attendanceOverview(attendanceOverview)
                .examSummary(examSummary)
                .marksSummary(marksSummary)
                .timetableSummary(timetableSummary)
                .noticeSummary(noticeSummary)
                .build();
    }

    /**
     * Aggregates student-specific academic summary data restricted to authenticated student identity.
     */
    public StudentDashboardResponse getStudentDashboard(Long userId) {
        Student student = studentRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user ID: " + userId));

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();
        String todayDay = today.getDayOfWeek().name();

        // 1. Profile Summary
        String progCode = student.getProgram() != null ? student.getProgram().getProgramCode() : "N/A";
        String progName = student.getProgram() != null ? student.getProgram().getProgramName() : "N/A";
        String deptName = student.getProgram() != null && student.getProgram().getDepartment() != null
                ? student.getProgram().getDepartment().getDepartmentName()
                : "N/A";
        String fullName = (student.getFirstName() + " " + (student.getLastName() != null ? student.getLastName() : "")).trim();

        StudentProfileSummary profile = StudentProfileSummary.builder()
                .studentId(student.getStudentId())
                .rollNumber(student.getRollNumber())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .fullName(fullName)
                .programId(student.getProgram() != null ? student.getProgram().getProgramId() : null)
                .programCode(progCode)
                .programName(progName)
                .departmentName(deptName)
                .currentSemester(student.getCurrentSemester())
                .section(student.getSection())
                .admissionYear(student.getAdmissionYear())
                .build();

        // 2. Enrolled Courses
        List<Enrollment> enrollments = enrollmentRepository.findByStudent_StudentId(student.getStudentId());
        List<CourseSummary> enrolledCourses = enrollments.stream()
                .map(e -> mapToCourseSummary(e.getCourse()))
                .toList();

        List<Long> enrolledCourseIds = enrollments.stream()
                .map(e -> e.getCourse().getCourseId())
                .toList();

        Map<Long, Enrollment> enrollmentMap = new HashMap<>();
        for (Enrollment e : enrollments) {
            if (e.getCourse() != null) {
                enrollmentMap.put(e.getCourse().getCourseId(), e);
            }
        }

        // 3. Attendance Summary
        List<AttendanceSummaryResponse> summaries = attendanceRepository.getStudentAttendanceSummary(student.getStudentId());
        long totalClasses = 0L;
        long presentClasses = 0L;
        long absentClasses = 0L;
        long lateClasses = 0L;
        List<CourseAttendanceSummary> courseAttendance = new ArrayList<>();

        for (AttendanceSummaryResponse s : summaries) {
            totalClasses += s.getTotalClasses();
            presentClasses += s.getPresentCount();
            absentClasses += s.getAbsentCount();
            lateClasses += s.getLateCount();

            Enrollment enr = enrollmentMap.get(s.getCourseId());
            Double creds = (enr != null && enr.getCourse() != null && enr.getCourse().getCredits() != null) 
                    ? enr.getCourse().getCredits().doubleValue() 
                    : null;
            String facName = (enr != null && enr.getCourse() != null && enr.getCourse().getFaculty() != null)
                    ? (enr.getCourse().getFaculty().getFirstName() + " " + (enr.getCourse().getFaculty().getLastName() != null ? enr.getCourse().getFaculty().getLastName() : "")).trim()
                    : null;

            courseAttendance.add(CourseAttendanceSummary.builder()
                    .courseId(s.getCourseId())
                    .courseCode(s.getCourseCode())
                    .courseName(s.getCourseName())
                    .courseType(s.getCourseType())
                    .credits(creds)
                    .facultyName(facName)
                    .totalClasses(s.getTotalClasses())
                    .presentClasses(s.getPresentCount())
                    .absentClasses(s.getAbsentCount())
                    .lateClasses(s.getLateCount())
                    .attendancePercentage(s.getAttendancePercentage())
                    .build());
        }

        Double overallAttendancePct = totalClasses > 0
                ? roundTwoDecimals(((double) presentClasses / totalClasses) * 100.0)
                : 0.0;

        AttendanceSummary attendanceSummary = AttendanceSummary.builder()
                .totalClasses(totalClasses)
                .presentClasses(presentClasses)
                .absentClasses(absentClasses)
                .lateClasses(lateClasses)
                .overallPercentage(overallAttendancePct)
                .byProgram(new ArrayList<>())
                .byCourse(courseAttendance)
                .build();

        // 4. Exams (Upcoming & Past)
        List<UpcomingExamSummary> upcomingExams = new ArrayList<>();
        List<UpcomingExamSummary> pastExams = new ArrayList<>();

        if (!enrolledCourseIds.isEmpty()) {
            for (Long cId : enrolledCourseIds) {
                for (Exam ex : examRepository.findByCourse_CourseId(cId)) {
                    if (ex.getExamDate() != null && !ex.getExamDate().isBefore(today)) {
                        upcomingExams.add(mapToUpcomingExam(ex));
                    } else {
                        pastExams.add(mapToUpcomingExam(ex));
                    }
                }
            }
            upcomingExams.sort(Comparator.comparing(UpcomingExamSummary::getExamDate));
            pastExams.sort(Comparator.comparing(UpcomingExamSummary::getExamDate).reversed());
        }

        // 5. Marks
        List<Mark> marks = markRepository.findByStudent_StudentId(student.getStudentId());
        List<StudentMarkItem> markItems = new ArrayList<>();
        double sumMarks = 0.0;
        for (Mark m : marks) {
            sumMarks += m.getMarksObtained().doubleValue();
            Course c = m.getExam().getCourse();
            markItems.add(StudentMarkItem.builder()
                    .markId(m.getMarkId())
                    .courseCode(c.getCourseCode())
                    .courseName(c.getCourseName())
                    .examName(m.getExam().getExamName())
                    .examType(m.getExam().getExamType().name())
                    .courseType(c.getCourseType() != null ? c.getCourseType().name() : null)
                    .marksObtained(m.getMarksObtained())
                    .maxMarks(m.getExam().getMaxMarks())
                    .grade(m.getGrade())
                    .build());
        }

        Double avgStudentMarks = !marks.isEmpty() ? roundTwoDecimals(sumMarks / marks.size()) : 0.0;
        StudentMarksSummary marksSummary = StudentMarksSummary.builder()
                .totalMarksRecorded(marks.size())
                .averageMarks(avgStudentMarks)
                .marks(markItems)
                .build();

        // 6. Timetable
        List<Timetable> weeklySchedule = new ArrayList<>();
        List<Timetable> todaySchedule = new ArrayList<>();
        if (student.getProgram() != null && student.getCurrentSemester() != null) {
            String section = student.getSection() != null && !student.getSection().trim().isEmpty()
                    ? student.getSection().trim().toUpperCase() : "A";
            weeklySchedule = timetableRepository.findByProgramAndSectionAndSemester(
                    student.getProgram().getProgramId(), section, student.getCurrentSemester());
            todaySchedule = timetableRepository.findByProgramAndSectionAndSemesterAndDayOfWeek(
                    student.getProgram().getProgramId(), section, student.getCurrentSemester(), todayDay);
        }

        TimetableSummary timetableSummary = TimetableSummary.builder()
                .totalEntries(weeklySchedule.size())
                .todayClassesCount(todaySchedule.size())
                .todaySchedule(todaySchedule.stream().map(this::mapToTimetableClass).toList())
                .weeklySchedule(weeklySchedule.stream().map(this::mapToTimetableClass).toList())
                .build();

        // 7. Notices (program + department wide)
        List<Notice> studentNotices = new ArrayList<>();
        long urgentNoticesCount = 0L;
        if (student.getProgram() != null) {
            studentNotices = noticeRepository.findActiveNoticesForProgramAndDept(
                    student.getProgram().getProgramId(), now, PageRequest.of(0, 5));
            urgentNoticesCount = noticeRepository.countActiveUrgentNoticesForProgramAndDept(
                    student.getProgram().getProgramId(), now);
        }

        NoticeSummary noticeSummary = NoticeSummary.builder()
                .totalActive(studentNotices.size())
                .urgentCount(urgentNoticesCount)
                .byCategory(new HashMap<>())
                .recentNotices(studentNotices.stream().map(this::mapToNoticeResponse).toList())
                .build();

        // 8. Document Summary
        DocumentSummary docSummary = buildDocumentSummary(
                documentRequestRepository.countByStatusGroupedForStudent(student.getStudentId()));

        // 9. Quick Summary
        StudentQuickSummary quickSummary = StudentQuickSummary.builder()
                .overallAttendancePercentage(overallAttendancePct)
                .upcomingExamsCount(upcomingExams.size())
                .pendingDocumentRequests(docSummary.getPendingRequests())
                .activeNoticesCount(studentNotices.size())
                .enrolledCoursesCount(enrolledCourses.size())
                .build();

        return StudentDashboardResponse.builder()
                .profile(profile)
                .quickSummary(quickSummary)
                .attendance(attendanceSummary)
                .upcomingExams(upcomingExams)
                .pastExams(pastExams)
                .marks(marksSummary)
                .enrollments(enrolledCourses)
                .timetable(timetableSummary)
                .notices(noticeSummary)
                .documentSummary(docSummary)
                .build();
    }

    private DocumentSummary buildDocumentSummary(List<Object[]> rows) {
        long submitted = 0L;
        long underReview = 0L;
        long approved = 0L;
        long issued = 0L;
        long rejected = 0L;

        if (rows != null) {
            for (Object[] row : rows) {
                if (row[0] != null) {
                    DocumentStatus st = (DocumentStatus) row[0];
                    long cnt = ((Number) row[1]).longValue();
                    switch (st) {
                        case SUBMITTED -> submitted += cnt;
                        case UNDER_REVIEW -> underReview += cnt;
                        case APPROVED -> approved += cnt;
                        case ISSUED -> issued += cnt;
                        case REJECTED -> rejected += cnt;
                    }
                }
            }
        }

        long total = submitted + underReview + approved + issued + rejected;
        long pending = submitted + underReview;

        return DocumentSummary.builder()
                .totalRequests(total)
                .pendingRequests(pending)
                .submittedRequests(submitted)
                .underReviewRequests(underReview)
                .approvedRequests(approved)
                .issuedRequests(issued)
                .rejectedRequests(rejected)
                .build();
    }

    private UpcomingExamSummary mapToUpcomingExam(Exam e) {
        return UpcomingExamSummary.builder()
                .examId(e.getExamId())
                .courseId(e.getCourse() != null ? e.getCourse().getCourseId() : null)
                .courseCode(e.getCourse() != null ? e.getCourse().getCourseCode() : "N/A")
                .courseName(e.getCourse() != null ? e.getCourse().getCourseName() : "N/A")
                .courseType(e.getCourse() != null && e.getCourse().getCourseType() != null ? e.getCourse().getCourseType().name() : null)
                .examName(e.getExamName())
                .examType(e.getExamType() != null ? e.getExamType().name() : "N/A")
                .examDate(e.getExamDate())
                .maxMarks(e.getMaxMarks())
                .build();
    }

    private CourseSummary mapToCourseSummary(Course c) {
        String facName = null;
        if (c.getFaculty() != null) {
            facName = (c.getFaculty().getFirstName() + " " + (c.getFaculty().getLastName() != null ? c.getFaculty().getLastName() : "")).trim();
        }
        String cType = c.getCourseType() != null ? c.getCourseType().name() : (c.getCourseName().toUpperCase().contains("LAB") ? "LABORATORY" : "THEORY");
        return CourseSummary.builder()
                .courseId(c.getCourseId())
                .courseCode(c.getCourseCode())
                .courseName(c.getCourseName())
                .courseType(cType)
                .credits(c.getCredits() != null ? c.getCredits().doubleValue() : 0.0)
                .semester(c.getSemester())
                .programCode(c.getProgram() != null ? c.getProgram().getProgramCode() : "N/A")
                .facultyName(facName)
                .build();
    }

    private TimetableClassSummary mapToTimetableClass(Timetable t) {
        String facName = null;
        if (t.getFaculty() != null) {
            facName = (t.getFaculty().getFirstName() + " " + (t.getFaculty().getLastName() != null ? t.getFaculty().getLastName() : "")).trim();
        }
        String room = t.getClassroom() != null ? t.getClassroom().getRoomNumber() : "N/A";
        String bld = t.getClassroom() != null ? t.getClassroom().getBuilding() : "N/A";

        String progCode = t.getProgram() != null ? t.getProgram().getProgramCode() : "N/A";

        return TimetableClassSummary.builder()
                .timetableId(t.getTimetableId())
                .courseCode(t.getCourse() != null ? t.getCourse().getCourseCode() : "N/A")
                .courseName(t.getCourse() != null ? t.getCourse().getCourseName() : "N/A")
                .roomNumber(room)
                .building(bld)
                .facultyName(facName)
                .startTime(t.getStartTime())
                .endTime(t.getEndTime())
                .dayOfWeek(t.getDayOfWeek())
                .semester(t.getSemester())
                .section(t.getSection())
                .programCode(progCode)
                .build();
    }

    private NoticeResponse mapToNoticeResponse(Notice notice) {
        if (notice == null) {
            return null;
        }
        Long targetProgramId = notice.getTargetProgram() != null ? notice.getTargetProgram().getProgramId() : null;
        String targetProgramCode = notice.getTargetProgram() != null ? notice.getTargetProgram().getProgramCode() : null;
        String targetProgramName = notice.getTargetProgram() != null ? notice.getTargetProgram().getProgramName() : null;
        Long publisherId = notice.getPublishedBy() != null ? notice.getPublishedBy().getUserId() : null;
        String publisherName = notice.getPublishedBy() != null ? notice.getPublishedBy().getUsername() : null;

        LocalDateTime now = LocalDateTime.now();
        boolean active = !notice.getPublishAt().isAfter(now)
                && (notice.getExpiresAt() == null || !notice.getExpiresAt().isBefore(now));

        return NoticeResponse.builder()
                .noticeId(notice.getNoticeId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .category(notice.getCategory())
                .priority(notice.getPriority())
                .targetProgramId(targetProgramId)
                .targetProgramCode(targetProgramCode)
                .targetProgramName(targetProgramName)
                .publishedByUserId(publisherId)
                .publishedByName(publisherName)
                .publishAt(notice.getPublishAt())
                .expiresAt(notice.getExpiresAt())
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .active(active)
                .build();
    }

    private Double roundTwoDecimals(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0.0;
        }
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
