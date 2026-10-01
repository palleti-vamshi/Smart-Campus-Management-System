package com.smartcampus.service;

import com.smartcampus.dto.request.LoginRequest;
import com.smartcampus.dto.response.AuthResponse;
import com.smartcampus.security.CustomUserDetails;
import com.smartcampus.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Service handling user authentication and token issuance.
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final com.smartcampus.repository.FacultyRepository facultyRepository;
    private final com.smartcampus.repository.StudentRepository studentRepository;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtTokenProvider jwtTokenProvider,
                       com.smartcampus.repository.FacultyRepository facultyRepository,
                       com.smartcampus.repository.StudentRepository studentRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.facultyRepository = facultyRepository;
        this.studentRepository = studentRepository;
    }

    /**
     * Authenticates user credentials and generates a signed JWT token.
     */
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(userDetails);

        AuthResponse.AuthResponseBuilder respBuilder = AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(userDetails.getUserId())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .role(userDetails.getRole().name());

        if (userDetails.getRole() == com.smartcampus.entity.enums.Role.FACULTY) {
            facultyRepository.findByUser_UserId(userDetails.getUserId()).ifPresent(f -> {
                respBuilder.firstName(f.getFirstName())
                        .lastName(f.getLastName())
                        .fullName((f.getFirstName() + (f.getLastName() != null ? " " + f.getLastName() : "")).trim())
                        .employeeCode(f.getEmployeeCode());
            });
        } else if (userDetails.getRole() == com.smartcampus.entity.enums.Role.STUDENT) {
            studentRepository.findByUser_UserId(userDetails.getUserId()).ifPresent(s -> {
                respBuilder.firstName(s.getFirstName())
                        .lastName(s.getLastName())
                        .fullName((s.getFirstName() + (s.getLastName() != null ? " " + s.getLastName() : "")).trim())
                        .rollNumber(s.getRollNumber());
            });
        } else if (userDetails.getRole() == com.smartcampus.entity.enums.Role.ADMIN) {
            respBuilder.firstName("System")
                    .lastName("Administrator")
                    .fullName("Administrator");
        }

        return respBuilder.build();
    }
}
