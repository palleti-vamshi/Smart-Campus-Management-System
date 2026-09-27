package com.smartcampus.service;

import com.smartcampus.dto.request.FacultyRequest;
import com.smartcampus.dto.response.FacultyResponse;
import com.smartcampus.dto.response.PageResponse;
import com.smartcampus.entity.Department;
import com.smartcampus.entity.Faculty;
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
 * Service handling Faculty master data and corresponding User identity operations.
 */
@Service
public class FacultyService {

    private static final String DEFAULT_DEV_PASSWORD = "Password@123";

    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final TimetableRepository timetableRepository;
    private final PasswordEncoder passwordEncoder;

    public FacultyService(FacultyRepository facultyRepository,
                          DepartmentRepository departmentRepository,
                          UserRepository userRepository,
                          CourseRepository courseRepository,
                          TimetableRepository timetableRepository,
                          PasswordEncoder passwordEncoder) {
        this.facultyRepository = facultyRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.timetableRepository = timetableRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public PageResponse<FacultyResponse> getFaculty(Long departmentId, String search, Pageable pageable) {
        String cleanSearch = StringUtils.hasText(search) ? search.trim() : null;
        Page<Faculty> page = facultyRepository.findWithFilters(departmentId, cleanSearch, pageable);
        return PageResponse.from(page.map(FacultyResponse::fromEntity));
    }

    @Transactional(readOnly = true)
    public FacultyResponse getFacultyById(Long id) {
        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty", "id", id));
        return FacultyResponse.fromEntity(faculty);
    }

    @Transactional(readOnly = true)
    public FacultyResponse getFacultyByUserId(Long userId) {
        Faculty faculty = facultyRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty profile", "userId", userId));
        return FacultyResponse.fromEntity(faculty);
    }

    @Transactional
    public FacultyResponse createFaculty(FacultyRequest request) {
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));

        String employeeCode = request.getEmployeeCode().trim().toUpperCase();
        if (facultyRepository.existsByEmployeeCode(employeeCode)) {
            throw new DuplicateResourceException("Faculty", "employeeCode", employeeCode);
        }

        String email = request.getEmail().trim().toLowerCase();
        int atIndex = email.indexOf('@');
        String username = atIndex > 0 ? email.substring(0, atIndex) : email;

        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("User", "username", username);
        }
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("User", "email", email);
        }

        // Transactional user creation
        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(DEFAULT_DEV_PASSWORD))
                .role(Role.FACULTY)
                .isActive(true)
                .build();
        User savedUser = userRepository.save(user);

        Faculty faculty = Faculty.builder()
                .user(savedUser)
                .department(department)
                .employeeCode(employeeCode)
                .firstName(request.getFirstName().trim())
                .lastName(StringUtils.hasText(request.getLastName()) ? request.getLastName().trim() : null)
                .designation(StringUtils.hasText(request.getDesignation()) ? request.getDesignation().trim() : null)
                .specialization(StringUtils.hasText(request.getSpecialization()) ? request.getSpecialization().trim() : null)
                .phone(request.getPhone())
                .build();

        Faculty savedFaculty = facultyRepository.save(faculty);
        return FacultyResponse.fromEntity(savedFaculty);
    }

    @Transactional
    public FacultyResponse updateFaculty(Long id, FacultyRequest request) {
        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty", "id", id));

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));

        String employeeCode = request.getEmployeeCode().trim().toUpperCase();
        if (facultyRepository.existsByEmployeeCodeAndFacultyIdNot(employeeCode, id)) {
            throw new DuplicateResourceException("Faculty", "employeeCode", employeeCode);
        }

        User user = faculty.getUser();
        if (user != null) {
            String newEmail = request.getEmail().trim().toLowerCase();
            int atIndex = newEmail.indexOf('@');
            String newUsername = atIndex > 0 ? newEmail.substring(0, atIndex) : newEmail;

            if (!user.getEmail().equalsIgnoreCase(newEmail) && userRepository.existsByEmail(newEmail)) {
                throw new DuplicateResourceException("User", "email", newEmail);
            }
            if (!user.getUsername().equalsIgnoreCase(newUsername) && userRepository.existsByUsername(newUsername)) {
                throw new DuplicateResourceException("User", "username", newUsername);
            }

            user.setUsername(newUsername);
            user.setEmail(newEmail);
            userRepository.save(user);
        }

        faculty.setDepartment(department);
        faculty.setEmployeeCode(employeeCode);
        faculty.setFirstName(request.getFirstName().trim());
        faculty.setLastName(StringUtils.hasText(request.getLastName()) ? request.getLastName().trim() : null);
        faculty.setDesignation(StringUtils.hasText(request.getDesignation()) ? request.getDesignation().trim() : null);
        faculty.setSpecialization(StringUtils.hasText(request.getSpecialization()) ? request.getSpecialization().trim() : null);
        faculty.setPhone(request.getPhone());

        Faculty updated = facultyRepository.save(faculty);
        return FacultyResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteFaculty(Long id) {
        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty", "id", id));

        if (courseRepository.existsByFaculty_FacultyId(id)) {
            throw new ResourceConflictException("Cannot delete faculty member because active course assignments exist");
        }
        if (timetableRepository.existsByFaculty_FacultyId(id)) {
            throw new ResourceConflictException("Cannot delete faculty member because timetable schedules exist");
        }

        User user = faculty.getUser();
        facultyRepository.delete(faculty);

        if (user != null) {
            userRepository.delete(user);
        }
    }
}
