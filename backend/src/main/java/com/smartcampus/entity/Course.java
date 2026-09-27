package com.smartcampus.entity;

import com.smartcampus.entity.enums.CourseType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a course offered within a program.
 *
 * Each course belongs to one program and may be assigned to one faculty member.
 * The faculty_id FK is nullable to allow courses that are not yet assigned.
 *
 * Maps to the 'courses' table.
 */
@Entity
@Table(name = "courses", indexes = {
        @Index(name = "idx_courses_program", columnList = "program_id"),
        @Index(name = "idx_courses_faculty", columnList = "faculty_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Long courseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id")
    private Faculty faculty;

    @Column(name = "course_code", length = 30, unique = true, nullable = false)
    private String courseCode;

    @Column(name = "course_name", length = 150, nullable = false)
    private String courseName;

    @Column(name = "credits", precision = 3, scale = 1, nullable = false)
    private BigDecimal credits;

    @Column(name = "semester", nullable = false)
    private Integer semester;

    @Enumerated(EnumType.STRING)
    @Column(name = "course_type", length = 30, nullable = false)
    private CourseType courseType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
