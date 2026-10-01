package com.smartcampus.security;

import com.smartcampus.entity.Faculty;
import com.smartcampus.entity.User;
import com.smartcampus.repository.FacultyRepository;
import com.smartcampus.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Custom UserDetailsService implementation that loads users from the database
 * by username, email, or faculty employee code.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final FacultyRepository facultyRepository;

    public CustomUserDetailsService(UserRepository userRepository, FacultyRepository facultyRepository) {
        this.userRepository = userRepository;
        this.facultyRepository = facultyRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        // 1. Try finding by username
        Optional<User> userOpt = userRepository.findByUsername(usernameOrEmail)
                // 2. Try finding by email
                .or(() -> userRepository.findByEmail(usernameOrEmail));

        // 3. Try finding by faculty employee code (e.g., FAC-0001, FAC0001, FAC001)
        if (userOpt.isEmpty()) {
            userOpt = facultyRepository.findByEmployeeCode(usernameOrEmail)
                    .map(Faculty::getUser);

            if (userOpt.isEmpty() && usernameOrEmail.toUpperCase().startsWith("FAC")) {
                String numPart = usernameOrEmail.replaceAll("[^0-9]", "");
                if (!numPart.isEmpty()) {
                    try {
                        int num = Integer.parseInt(numPart);
                        String normalizedCode = String.format("FAC-%04d", num);
                        userOpt = facultyRepository.findByEmployeeCode(normalizedCode)
                                .map(Faculty::getUser);
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        User user = userOpt.orElseThrow(() -> new UsernameNotFoundException(
                "User not found with username, email, or employee code: " + usernameOrEmail));

        return CustomUserDetails.build(user);
    }
}
