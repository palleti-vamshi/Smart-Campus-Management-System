package com.smartcampus.dto.response;

import com.smartcampus.entity.Classroom;
import com.smartcampus.entity.enums.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomResponse {

    private Long classroomId;
    private String roomNumber;
    private String building;
    private RoomType roomType;
    private Integer capacity;
    private Boolean isActive;

    public static ClassroomResponse fromEntity(Classroom classroom) {
        if (classroom == null) {
            return null;
        }
        return ClassroomResponse.builder()
                .classroomId(classroom.getClassroomId())
                .roomNumber(classroom.getRoomNumber())
                .building(classroom.getBuilding())
                .roomType(classroom.getRoomType())
                .capacity(classroom.getCapacity())
                .isActive(classroom.getIsActive())
                .build();
    }
}
