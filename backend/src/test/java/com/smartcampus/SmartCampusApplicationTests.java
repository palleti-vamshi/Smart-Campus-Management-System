package com.smartcampus;

import com.smartcampus.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class SmartCampusApplicationTests {

	@Autowired private UserRepository userRepository;
	@Autowired private DepartmentRepository departmentRepository;
	@Autowired private ProgramRepository programRepository;
	@Autowired private StudentRepository studentRepository;
	@Autowired private FacultyRepository facultyRepository;
	@Autowired private CourseRepository courseRepository;
	@Autowired private EnrollmentRepository enrollmentRepository;
	@Autowired private AttendanceRepository attendanceRepository;
	@Autowired private ExamRepository examRepository;
	@Autowired private MarkRepository markRepository;
	@Autowired private ClassroomRepository classroomRepository;
	@Autowired private TimetableRepository timetableRepository;
	@Autowired private NoticeRepository noticeRepository;
	@Autowired private DocumentTypeRepository documentTypeRepository;
	@Autowired private DocumentRequestRepository documentRequestRepository;
	@Autowired private DocumentRequestHistoryRepository documentRequestHistoryRepository;

	@Test
	void contextLoads() {
		assertNotNull(userRepository);
		assertNotNull(departmentRepository);
		assertNotNull(programRepository);
		assertNotNull(studentRepository);
		assertNotNull(facultyRepository);
		assertNotNull(courseRepository);
		assertNotNull(enrollmentRepository);
		assertNotNull(attendanceRepository);
		assertNotNull(examRepository);
		assertNotNull(markRepository);
		assertNotNull(classroomRepository);
		assertNotNull(timetableRepository);
		assertNotNull(noticeRepository);
		assertNotNull(documentTypeRepository);
		assertNotNull(documentRequestRepository);
		assertNotNull(documentRequestHistoryRepository);
	}

}
