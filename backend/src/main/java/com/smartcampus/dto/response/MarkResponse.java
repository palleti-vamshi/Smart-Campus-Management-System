package com.smartcampus.dto.response;

import com.smartcampus.entity.Mark;
import com.smartcampus.entity.enums.ExamType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarkResponse {

    private Long markId;
    private Long examId;
    private String examName;
    private ExamType examType;
    private Long studentId;
    private String studentRollNumber;
    private String studentName;
    private Long courseId;
    private String courseCode;
    private String courseName;
    private String courseType;
    private BigDecimal credits;
    private Integer semester;
    private BigDecimal marksObtained;
    private BigDecimal maxMarks;
    private String grade;
    private Long enteredByFacultyId;
    private String enteredByFacultyName;
    private LocalDate examDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MarkResponse fromEntity(Mark mark) {
        if (mark == null) {
            return null;
        }

        Long eId = null;
        String eName = null;
        ExamType eType = null;
        Long cId = null;
        String cCode = null;
        String cName = null;
        String cType = null;
        BigDecimal creds = null;
        Integer sem = null;
        BigDecimal max = null;
        LocalDate eDate = null;
        if (mark.getExam() != null) {
            eId = mark.getExam().getExamId();
            eName = mark.getExam().getExamName();
            eType = mark.getExam().getExamType();
            max = mark.getExam().getMaxMarks();
            eDate = mark.getExam().getExamDate();
            if (mark.getExam().getCourse() != null) {
                cId = mark.getExam().getCourse().getCourseId();
                cCode = mark.getExam().getCourse().getCourseCode();
                cName = mark.getExam().getCourse().getCourseName();
                cType = mark.getExam().getCourse().getCourseType() != null ? mark.getExam().getCourse().getCourseType().name() : null;
                creds = mark.getExam().getCourse().getCredits();
                sem = mark.getExam().getCourse().getSemester();
            }
        }

        Long sId = null;
        String roll = null;
        String sName = null;
        if (mark.getStudent() != null) {
            sId = mark.getStudent().getStudentId();
            roll = mark.getStudent().getRollNumber();
            sName = (mark.getStudent().getFirstName() != null ? mark.getStudent().getFirstName() : "")
                    + (mark.getStudent().getLastName() != null ? " " + mark.getStudent().getLastName() : "");
            sName = sName.trim();
        }

        Long fId = null;
        String fName = null;
        if (mark.getEnteredBy() != null) {
            fId = mark.getEnteredBy().getFacultyId();
            fName = (mark.getEnteredBy().getFirstName() != null ? mark.getEnteredBy().getFirstName() : "")
                    + (mark.getEnteredBy().getLastName() != null ? " " + mark.getEnteredBy().getLastName() : "");
            fName = fName.trim();
        }

        return MarkResponse.builder()
                .markId(mark.getMarkId())
                .examId(eId)
                .examName(eName)
                .examType(eType)
                .studentId(sId)
                .studentRollNumber(roll)
                .studentName(sName)
                .courseId(cId)
                .courseCode(cCode)
                .courseName(cName)
                .courseType(cType)
                .credits(creds)
                .semester(sem)
                .marksObtained(mark.getMarksObtained())
                .maxMarks(max)
                .grade(mark.getGrade())
                .enteredByFacultyId(fId)
                .enteredByFacultyName(fName)
                .examDate(eDate)
                .createdAt(mark.getCreatedAt())
                .updatedAt(mark.getUpdatedAt())
                .build();
    }
}
