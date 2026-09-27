package com.smartcampus.entity;

import com.smartcampus.entity.enums.EnrollmentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Junction table resolving the many-to-many relationship between students and courses.
 *
 * Composite unique constraint: (student_id, course_id, academic_year, semester)
 * prevents a student from enrolling in the same course twice in the same term.
 *
 * Maps to the 'enrollments' table.
 */
@Entity
@Table(name = "enrollments", indexes = {
        @Index(name = "idx_enrollments_student", columnList = "student_id"),
        @Index(name = "idx_enrollments_course", columnList = "course_id")
}, uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_enrollment_student_course_term",
                columnNames = {"student_id", "course_id", "academic_year", "semester"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enrollment_id")
    private Long enrollmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "academic_year", length = 20, nullable = false)
    private String academicYear;

    @Column(name = "semester", nullable = false)
    private Integer semester;

    @Column(name = "enrollment_date", nullable = false)
    private LocalDate enrollmentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private EnrollmentStatus status;
}
