package com.smartcampus.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

/**
 * Represents a scheduled class in the timetable.
 *
 * Links a course, faculty member, classroom, and program
 * to a specific day and time slot.
 *
 * Conflict detection (faculty double-booked, classroom double-booked,
 * program overlap) is enforced at the service layer since time-range
 * overlap checks cannot be expressed with simple unique constraints.
 *
 * Maps to the 'timetable' table.
 */
@Entity
@Table(name = "timetable", indexes = {
        @Index(name = "idx_timetable_program", columnList = "program_id"),
        @Index(name = "idx_timetable_faculty", columnList = "faculty_id"),
        @Index(name = "idx_timetable_classroom", columnList = "classroom_id"),
        @Index(name = "idx_timetable_day_time", columnList = "day_of_week, start_time")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Timetable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "timetable_id")
    private Long timetableId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    @Column(name = "day_of_week", length = 15, nullable = false)
    private String dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "semester", nullable = false)
    private Integer semester;

    @Column(name = "academic_year", length = 20, nullable = false)
    private String academicYear;
}
