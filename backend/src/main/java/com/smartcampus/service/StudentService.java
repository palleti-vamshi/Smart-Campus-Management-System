package com.smartcampus.service;

import com.smartcampus.dto.request.StudentRequest;
import com.smartcampus.dto.response.PageResponse;
import com.smartcampus.dto.response.StudentResponse;
import com.smartcampus.entity.Program;
import com.smartcampus.entity.Student;
import com.smartcampus.entity.User;
import com.smartcampus.entity.enums.Role;
import com.smartcampus.exception.DuplicateResourceException;
import com.smartcampus.exception.ResourceConflictException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Service handling Student master data and corresponding User identity operations.
 */
@Service
public class StudentService {

    private static final String DEFAULT_DEV_PASSWORD = "Password@123";
    private static final String EMAIL_DOMAIN = "@vnrvjiet.in";

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final ProgramRepository programRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final MarkRepository markRepository;
    private final DocumentRequestRepository documentRequestRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentService(StudentRepository studentRepository,
                          UserRepository userRepository,
                          ProgramRepository programRepository,
                          EnrollmentRepository enrollmentRepository,
                          AttendanceRepository attendanceRepository,
                          MarkRepository markRepository,
                          DocumentRequestRepository documentRequestRepository,
                          PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.programRepository = programRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.attendanceRepository = attendanceRepository;
        this.markRepository = markRepository;
        this.documentRequestRepository = documentRequestRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public PageResponse<StudentResponse> getStudents(Long programId,
                                                    Integer semester,
                                                    String section,
                                                    String search,
                                                    Pageable pageable) {
        String cleanSearch = StringUtils.hasText(search) ? search.trim() : null;
        String cleanSection = StringUtils.hasText(section) ? section.trim() : null;

        Page<Student> page = studentRepository.findWithFilters(programId, semester, cleanSection, cleanSearch, pageable);
        return PageResponse.from(page.map(StudentResponse::fromEntity));
    }

    @Transactional(readOnly = true)
    public StudentResponse getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));
        return StudentResponse.fromEntity(student);
    }

    @Transactional(readOnly = true)
    public StudentResponse getStudentByUserId(Long userId) {
        Student student = studentRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile", "userId", userId));
        return StudentResponse.fromEntity(student);
    }

    @Transactional
    public StudentResponse createStudent(StudentRequest request) {
        Program program = programRepository.findById(request.getProgramId())
                .orElseThrow(() -> new ResourceNotFoundException("Program", "id", request.getProgramId()));

        String rollNumber = request.getRollNumber().trim().toUpperCase();
        if (studentRepository.existsByRollNumber(rollNumber)) {
            throw new DuplicateResourceException("Student", "rollNumber", rollNumber);
        }

        String email = StringUtils.hasText(request.getEmail())
                ? request.getEmail().trim().toLowerCase()
                : rollNumber.toLowerCase() + EMAIL_DOMAIN;

        if (userRepository.existsByUsername(rollNumber)) {
            throw new DuplicateResourceException("User", "username", rollNumber);
        }
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("User", "email", email);
        }

        // Transactional user creation
        User user = User.builder()
                .username(rollNumber)
                .email(email)
                .passwordHash(passwordEncoder.encode(DEFAULT_DEV_PASSWORD))
                .role(Role.STUDENT)
                .isActive(true)
                .build();
        User savedUser = userRepository.save(user);

        Student student = Student.builder()
                .user(savedUser)
                .program(program)
                .rollNumber(rollNumber)
                .firstName(request.getFirstName().trim())
                .lastName(StringUtils.hasText(request.getLastName()) ? request.getLastName().trim() : null)
                .gender(request.getGender())
                .dateOfBirth(request.getDateOfBirth())
                .admissionYear(request.getAdmissionYear())
                .currentSemester(request.getCurrentSemester())
                .section(request.getSection().trim().toUpperCase())
                .phone(request.getPhone())
                .build();

        Student savedStudent = studentRepository.save(student);
        return StudentResponse.fromEntity(savedStudent);
    }

    @Transactional
    public StudentResponse updateStudent(Long id, StudentRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));

        Program program = programRepository.findById(request.getProgramId())
                .orElseThrow(() -> new ResourceNotFoundException("Program", "id", request.getProgramId()));

        String rollNumber = request.getRollNumber().trim().toUpperCase();
        if (studentRepository.existsByRollNumberAndStudentIdNot(rollNumber, id)) {
            throw new DuplicateResourceException("Student", "rollNumber", rollNumber);
        }

        User user = student.getUser();
        if (user != null) {
            String newEmail = StringUtils.hasText(request.getEmail())
                    ? request.getEmail().trim().toLowerCase()
                    : rollNumber.toLowerCase() + EMAIL_DOMAIN;

            if (!user.getEmail().equalsIgnoreCase(newEmail) && userRepository.existsByEmail(newEmail)) {
                throw new DuplicateResourceException("User", "email", newEmail);
            }
            if (!user.getUsername().equalsIgnoreCase(rollNumber) && userRepository.existsByUsername(rollNumber)) {
                throw new DuplicateResourceException("User", "username", rollNumber);
            }

            user.setUsername(rollNumber);
            user.setEmail(newEmail);
            userRepository.save(user);
        }

        student.setProgram(program);
        student.setRollNumber(rollNumber);
        student.setFirstName(request.getFirstName().trim());
        student.setLastName(StringUtils.hasText(request.getLastName()) ? request.getLastName().trim() : null);
        student.setGender(request.getGender());
        student.setDateOfBirth(request.getDateOfBirth());
        student.setAdmissionYear(request.getAdmissionYear());
        student.setCurrentSemester(request.getCurrentSemester());
        student.setSection(request.getSection().trim().toUpperCase());
        student.setPhone(request.getPhone());

        Student updated = studentRepository.save(student);
        return StudentResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));

        if (enrollmentRepository.existsByStudent_StudentId(id)) {
            throw new ResourceConflictException("Cannot delete student because course enrollments exist");
        }
        if (attendanceRepository.existsByStudent_StudentId(id)) {
            throw new ResourceConflictException("Cannot delete student because attendance records exist");
        }
        if (markRepository.existsByStudent_StudentId(id)) {
            throw new ResourceConflictException("Cannot delete student because exam marks exist");
        }
        if (documentRequestRepository.existsByStudent_StudentId(id)) {
            throw new ResourceConflictException("Cannot delete student because document requests exist");
        }

        User user = student.getUser();
        studentRepository.delete(student);

        if (user != null) {
            userRepository.delete(user);
        }
    }
}
