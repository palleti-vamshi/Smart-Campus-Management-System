package com.smartcampus.repository;

import com.smartcampus.entity.Timetable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for the Timetable entity.
 */
@Repository
public interface TimetableRepository extends JpaRepository<Timetable, Long> {

    List<Timetable> findByProgram_ProgramIdAndSemesterAndAcademicYear(
            Long programId, Integer semester, String academicYear);

    List<Timetable> findByFaculty_FacultyId(Long facultyId);

    List<Timetable> findByClassroom_ClassroomId(Long classroomId);

    List<Timetable> findByDayOfWeek(String dayOfWeek);

    boolean existsByFaculty_FacultyId(Long facultyId);

    boolean existsByCourse_CourseId(Long courseId);
}


