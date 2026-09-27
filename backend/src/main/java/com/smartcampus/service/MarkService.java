package com.smartcampus.service;

import com.smartcampus.dto.request.MarkRequest;
import com.smartcampus.dto.request.MarkUpdateRequest;
import com.smartcampus.dto.response.MarkResponse;
import com.smartcampus.dto.response.PageResponse;
import com.smartcampus.dto.response.StudentResultResponse;
import com.smartcampus.entity.Course;
import com.smartcampus.entity.Exam;
import com.smartcampus.entity.Faculty;
import com.smartcampus.entity.Mark;
import com.smartcampus.entity.Student;
import com.smartcampus.entity.enums.ExamType;
import com.smartcampus.exception.InvalidOperationException;
import com.smartcampus.exception.ResourceConflictException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.EnrollmentRepository;
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
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MarkService {

    private final MarkRepository markRepository;
    private final ExamRepository examRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Transactional
    public MarkResponse createMarkByAdmin(MarkRequest request) {
        Exam exam = examRepository.findById(request.getExamId())
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with ID: " + request.getExamId()));

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + request.getStudentId()));

        validateEnrollmentAndDuplicate(student, exam);
        validateMarksRange(request.getMarksObtained(), exam.getMaxMarks());

        Faculty enteredBy;
        if (request.getEnteredByFacultyId() != null) {
            enteredBy = facultyRepository.findById(request.getEnteredByFacultyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with ID: " + request.getEnteredByFacultyId()));
        } else if (exam.getCourse().getFaculty() != null) {
            enteredBy = exam.getCourse().getFaculty();
        } else {
            throw new ResourceConflictException("Course has no assigned faculty to attribute marks entry to");
        }

        String grade = determineGrade(request.getGrade(), request.getMarksObtained(), exam.getMaxMarks());

        Mark mark = Mark.builder()
                .exam(exam)
                .student(student)
                .marksObtained(request.getMarksObtained())
                .grade(grade)
                .enteredBy(enteredBy)
                .build();

        Mark saved = markRepository.save(mark);
        log.info("Mark entered by admin for student {} in exam {}", student.getRollNumber(), exam.getExamName());
        return MarkResponse.fromEntity(saved);
    }

    @Transactional
    public MarkResponse createMarkByFaculty(Long userId, MarkRequest request) {
        Faculty faculty = facultyRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty profile not found for user ID: " + userId));

        Exam exam = examRepository.findById(request.getExamId())
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with ID: " + request.getExamId()));

        if (exam.getCourse().getFaculty() == null ||
                !exam.getCourse().getFaculty().getFacultyId().equals(faculty.getFacultyId())) {
            log.warn("Faculty ID {} attempted to record marks for exam ID {} in a course not taught by them",
                    faculty.getFacultyId(), exam.getExamId());
            throw new AccessDeniedException("You are not authorized to enter marks for an exam of a course you do not teach");
        }

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + request.getStudentId()));

        validateEnrollmentAndDuplicate(student, exam);
        validateMarksRange(request.getMarksObtained(), exam.getMaxMarks());

        String grade = determineGrade(request.getGrade(), request.getMarksObtained(), exam.getMaxMarks());

        Mark mark = Mark.builder()
                .exam(exam)
                .student(student)
                .marksObtained(request.getMarksObtained())
                .grade(grade)
                .enteredBy(faculty)
                .build();

        Mark saved = markRepository.save(mark);
        log.info("Mark entered by faculty {} for student {} in exam {}",
                faculty.getEmployeeCode(), student.getRollNumber(), exam.getExamName());
        return MarkResponse.fromEntity(saved);
    }

    public PageResponse<MarkResponse> getMarksForAdmin(
            Long examId,
            Long studentId,
            Long courseId,
            Pageable pageable) {
        Page<Mark> page = markRepository.findWithAdminFilters(examId, studentId, courseId, pageable);
        return PageResponse.from(page.map(MarkResponse::fromEntity));
    }

    public PageResponse<MarkResponse> getMarksForFaculty(
            Long userId,
            Long examId,
            Long studentId,
            Long courseId,
            Pageable pageable) {
        Faculty faculty = facultyRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty profile not found for user ID: " + userId));

        Page<Mark> page = markRepository.findWithFacultyFilters(faculty.getFacultyId(), examId, studentId, courseId, pageable);
        return PageResponse.from(page.map(MarkResponse::fromEntity));
    }

    public PageResponse<MarkResponse> getMarksForStudent(
            Long userId,
            Long courseId,
            Long examId,
            ExamType examType,
            Integer semester,
            Pageable pageable) {
        Student student = studentRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user ID: " + userId));

        Page<Mark> page = markRepository.findWithStudentFilters(student.getStudentId(), courseId, examId, examType, semester, pageable);
        return PageResponse.from(page.map(MarkResponse::fromEntity));
    }

    public MarkResponse getMarkById(Long markId) {
        Mark mark = markRepository.findById(markId)
                .orElseThrow(() -> new ResourceNotFoundException("Mark not found with ID: " + markId));
        return MarkResponse.fromEntity(mark);
    }

    @Transactional
    public MarkResponse updateMarkByAdmin(Long markId, MarkUpdateRequest request) {
        Mark mark = markRepository.findById(markId)
                .orElseThrow(() -> new ResourceNotFoundException("Mark not found with ID: " + markId));

        validateMarksRange(request.getMarksObtained(), mark.getExam().getMaxMarks());
        String grade = determineGrade(request.getGrade(), request.getMarksObtained(), mark.getExam().getMaxMarks());

        mark.setMarksObtained(request.getMarksObtained());
        mark.setGrade(grade);

        Mark updated = markRepository.save(mark);
        log.info("Mark ID {} updated by admin", markId);
        return MarkResponse.fromEntity(updated);
    }

    @Transactional
    public MarkResponse updateMarkByFaculty(Long userId, Long markId, MarkUpdateRequest request) {
        Faculty faculty = facultyRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty profile not found for user ID: " + userId));

        Mark mark = markRepository.findById(markId)
                .orElseThrow(() -> new ResourceNotFoundException("Mark not found with ID: " + markId));

        if (mark.getExam().getCourse().getFaculty() == null ||
                !mark.getExam().getCourse().getFaculty().getFacultyId().equals(faculty.getFacultyId())) {
            log.warn("Faculty ID {} attempted to update mark ID {} not taught by them",
                    faculty.getFacultyId(), markId);
            throw new AccessDeniedException("You are not authorized to update marks for an exam of a course you do not teach");
        }

        validateMarksRange(request.getMarksObtained(), mark.getExam().getMaxMarks());
        String grade = determineGrade(request.getGrade(), request.getMarksObtained(), mark.getExam().getMaxMarks());

        mark.setMarksObtained(request.getMarksObtained());
        mark.setGrade(grade);

        Mark updated = markRepository.save(mark);
        log.info("Mark ID {} updated by faculty {}", markId, faculty.getEmployeeCode());
        return MarkResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteMarkByAdmin(Long markId) {
        Mark mark = markRepository.findById(markId)
                .orElseThrow(() -> new ResourceNotFoundException("Mark not found with ID: " + markId));

        markRepository.delete(mark);
        log.info("Mark ID {} deleted by admin", markId);
    }

    @Transactional
    public void deleteMarkByFaculty(Long userId, Long markId) {
        Faculty faculty = facultyRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty profile not found for user ID: " + userId));

        Mark mark = markRepository.findById(markId)
                .orElseThrow(() -> new ResourceNotFoundException("Mark not found with ID: " + markId));

        if (mark.getExam().getCourse().getFaculty() == null ||
                !mark.getExam().getCourse().getFaculty().getFacultyId().equals(faculty.getFacultyId())) {
            log.warn("Faculty ID {} attempted to delete mark ID {} not taught by them",
                    faculty.getFacultyId(), markId);
            throw new AccessDeniedException("You are not authorized to delete marks for an exam of a course you do not teach");
        }

        markRepository.delete(mark);
        log.info("Mark ID {} deleted by faculty {}", markId, faculty.getEmployeeCode());
    }

    public List<StudentResultResponse> getStudentResults(Long userId) {
        Student student = studentRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user ID: " + userId));

        List<Mark> marks = markRepository.findByStudent_StudentId(student.getStudentId());

        Map<Long, List<Mark>> marksByCourse = new LinkedHashMap<>();
        for (Mark m : marks) {
            Course c = m.getExam().getCourse();
            marksByCourse.computeIfAbsent(c.getCourseId(), k -> new ArrayList<>()).add(m);
        }

        List<StudentResultResponse> results = new ArrayList<>();
        for (Map.Entry<Long, List<Mark>> entry : marksByCourse.entrySet()) {
            List<Mark> courseMarks = entry.getValue();
            Course course = courseMarks.get(0).getExam().getCourse();

            BigDecimal totalObtained = BigDecimal.ZERO;
            BigDecimal totalMax = BigDecimal.ZERO;

            List<StudentResultResponse.ExamResultItem> examItems = new ArrayList<>();
            for (Mark m : courseMarks) {
                totalObtained = totalObtained.add(m.getMarksObtained());
                totalMax = totalMax.add(m.getExam().getMaxMarks());

                examItems.add(StudentResultResponse.ExamResultItem.builder()
                        .examId(m.getExam().getExamId())
                        .examName(m.getExam().getExamName())
                        .examType(m.getExam().getExamType())
                        .examDate(m.getExam().getExamDate())
                        .maxMarks(m.getExam().getMaxMarks())
                        .marksObtained(m.getMarksObtained())
                        .grade(m.getGrade())
                        .build());
            }

            Double percentage = 0.0;
            if (totalMax.compareTo(BigDecimal.ZERO) > 0) {
                percentage = totalObtained.divide(totalMax, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue();
            }

            results.add(StudentResultResponse.builder()
                    .courseId(course.getCourseId())
                    .courseCode(course.getCourseCode())
                    .courseName(course.getCourseName())
                    .totalExams(courseMarks.size())
                    .totalMarksObtained(totalObtained)
                    .totalMaxMarks(totalMax)
                    .percentage(percentage)
                    .examResults(examItems)
                    .build());
        }

        return results;
    }

    private void validateEnrollmentAndDuplicate(Student student, Exam exam) {
        if (!enrollmentRepository.existsByStudent_StudentIdAndCourse_CourseId(
                student.getStudentId(), exam.getCourse().getCourseId())) {
            throw new ResourceConflictException(String.format(
                    "Student '%s' is not enrolled in course '%s'",
                    student.getRollNumber(), exam.getCourse().getCourseCode()));
        }

        if (markRepository.existsByExam_ExamIdAndStudent_StudentId(exam.getExamId(), student.getStudentId())) {
            throw new ResourceConflictException(String.format(
                    "Mark already recorded for student '%s' in exam '%s'",
                    student.getRollNumber(), exam.getExamName()));
        }
    }

    private void validateMarksRange(BigDecimal marksObtained, BigDecimal maxMarks) {
        if (marksObtained == null || marksObtained.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidOperationException("Marks obtained must be non-negative");
        }
        if (marksObtained.compareTo(maxMarks) > 0) {
            throw new InvalidOperationException(String.format(
                    "Marks obtained (%.2f) cannot exceed maximum marks (%.2f)",
                    marksObtained, maxMarks));
        }
    }

    private String determineGrade(String grade, BigDecimal marksObtained, BigDecimal maxMarks) {
        if (grade != null && !grade.trim().isEmpty()) {
            return grade.trim().toUpperCase();
        }

        if (maxMarks == null || maxMarks.compareTo(BigDecimal.ZERO) <= 0 || marksObtained == null) {
            return "F";
        }

        double percentage = marksObtained.divide(maxMarks, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();

        if (percentage >= 90.0) return "A+";
        if (percentage >= 80.0) return "A";
        if (percentage >= 70.0) return "B+";
        if (percentage >= 60.0) return "B";
        if (percentage >= 50.0) return "C";
        if (percentage >= 40.0) return "D";
        return "F";
    }
}
