package com.smartcampus.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Represents an academic program within a department.
 *
 * Initial programs: AIML, IoT, RAI.
 * Relationship: Department 1 ──< Programs (many-to-one).
 *
 * Maps to the 'programs' table.
 */
@Entity
@Table(name = "programs", indexes = {
        @Index(name = "idx_programs_department", columnList = "department_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Program {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "program_id")
    private Long programId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "program_code", length = 20, unique = true, nullable = false)
    private String programCode;

    @Column(name = "program_name", length = 150, nullable = false)
    private String programName;

    @Column(name = "duration_years", nullable = false)
    private Integer durationYears;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
