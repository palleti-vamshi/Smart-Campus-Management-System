package com.smartcampus.repository;

import com.smartcampus.entity.Timetable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
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

    boolean existsByClassroom_ClassroomId(Long classroomId);

    boolean existsByProgram_ProgramId(Long programId);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Timetable t WHERE " +
            "t.classroom.classroomId = :classroomId AND " +
            "UPPER(t.dayOfWeek) = UPPER(:dayOfWeek) AND " +
            "t.semester = :semester AND " +
            "t.academicYear = :academicYear AND " +
            "(:excludeTimetableId IS NULL OR t.timetableId <> :excludeTimetableId) AND " +
            "(t.startTime < :endTime AND t.endTime > :startTime)")
    boolean hasClassroomConflict(
            @Param("classroomId") Long classroomId,
            @Param("dayOfWeek") String dayOfWeek,
            @Param("semester") Integer semester,
            @Param("academicYear") String academicYear,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeTimetableId") Long excludeTimetableId);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Timetable t WHERE " +
            "t.faculty.facultyId = :facultyId AND " +
            "UPPER(t.dayOfWeek) = UPPER(:dayOfWeek) AND " +
            "t.semester = :semester AND " +
            "t.academicYear = :academicYear AND " +
            "(:excludeTimetableId IS NULL OR t.timetableId <> :excludeTimetableId) AND " +
            "(t.startTime < :endTime AND t.endTime > :startTime)")
    boolean hasFacultyConflict(
            @Param("facultyId") Long facultyId,
            @Param("dayOfWeek") String dayOfWeek,
            @Param("semester") Integer semester,
            @Param("academicYear") String academicYear,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeTimetableId") Long excludeTimetableId);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Timetable t WHERE " +
            "t.program.programId = :programId AND " +
            "UPPER(t.dayOfWeek) = UPPER(:dayOfWeek) AND " +
            "t.semester = :semester AND " +
            "t.academicYear = :academicYear AND " +
            "(:excludeTimetableId IS NULL OR t.timetableId <> :excludeTimetableId) AND " +
            "(t.startTime < :endTime AND t.endTime > :startTime)")
    boolean hasProgramConflict(
            @Param("programId") Long programId,
            @Param("dayOfWeek") String dayOfWeek,
            @Param("semester") Integer semester,
            @Param("academicYear") String academicYear,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeTimetableId") Long excludeTimetableId);

    @Query("SELECT t FROM Timetable t WHERE " +
            "(:programId IS NULL OR t.program.programId = :programId) AND " +
            "(:courseId IS NULL OR t.course.courseId = :courseId) AND " +
            "(:facultyId IS NULL OR t.faculty.facultyId = :facultyId) AND " +
            "(:classroomId IS NULL OR t.classroom.classroomId = :classroomId) AND " +
            "(:dayOfWeek IS NULL OR UPPER(t.dayOfWeek) = UPPER(:dayOfWeek)) AND " +
            "(:semester IS NULL OR t.semester = :semester) AND " +
            "(:academicYear IS NULL OR t.academicYear = :academicYear)")
    Page<Timetable> findWithAdminFilters(
            @Param("programId") Long programId,
            @Param("courseId") Long courseId,
            @Param("facultyId") Long facultyId,
            @Param("classroomId") Long classroomId,
            @Param("dayOfWeek") String dayOfWeek,
            @Param("semester") Integer semester,
            @Param("academicYear") String academicYear,
            Pageable pageable);

    @Query("SELECT t FROM Timetable t WHERE " +
            "t.faculty.facultyId = :facultyId AND " +
            "(:dayOfWeek IS NULL OR UPPER(t.dayOfWeek) = UPPER(:dayOfWeek)) AND " +
            "(:semester IS NULL OR t.semester = :semester) AND " +
            "(:academicYear IS NULL OR t.academicYear = :academicYear)")
    Page<Timetable> findWithFacultyFilters(
            @Param("facultyId") Long facultyId,
            @Param("dayOfWeek") String dayOfWeek,
            @Param("semester") Integer semester,
            @Param("academicYear") String academicYear,
            Pageable pageable);

    @Query("SELECT t FROM Timetable t WHERE " +
            "t.program.programId = :programId AND " +
            "(:dayOfWeek IS NULL OR UPPER(t.dayOfWeek) = UPPER(:dayOfWeek)) AND " +
            "(:semester IS NULL OR t.semester = :semester) AND " +
            "(:academicYear IS NULL OR t.academicYear = :academicYear)")
    Page<Timetable> findWithStudentFilters(
            @Param("programId") Long programId,
            @Param("dayOfWeek") String dayOfWeek,
            @Param("semester") Integer semester,
            @Param("academicYear") String academicYear,
            Pageable pageable);

    @Query("SELECT COUNT(t) FROM Timetable t WHERE UPPER(t.dayOfWeek) = UPPER(:dayOfWeek)")
    long countByDayOfWeekIgnoreCase(@Param("dayOfWeek") String dayOfWeek);

    @Query("SELECT t FROM Timetable t WHERE t.faculty.facultyId = :facultyId AND UPPER(t.dayOfWeek) = UPPER(:dayOfWeek) ORDER BY t.startTime ASC")
    List<Timetable> findByFacultyAndDayOfWeek(@Param("facultyId") Long facultyId, @Param("dayOfWeek") String dayOfWeek);

    @Query("SELECT t FROM Timetable t WHERE t.program.programId = :programId AND t.semester = :semester AND UPPER(t.dayOfWeek) = UPPER(:dayOfWeek) ORDER BY t.startTime ASC")
    List<Timetable> findByProgramAndSemesterAndDayOfWeek(
            @Param("programId") Long programId,
            @Param("semester") Integer semester,
            @Param("dayOfWeek") String dayOfWeek);

    List<Timetable> findByProgram_ProgramIdAndSemester(Long programId, Integer semester);
}


