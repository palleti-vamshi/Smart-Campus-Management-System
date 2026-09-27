package com.smartcampus.dto.request;

import com.smartcampus.entity.enums.RoomType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomRequest {

    @NotBlank(message = "Room number is required")
    @Size(max = 30, message = "Room number must not exceed 30 characters")
    private String roomNumber;

    @NotBlank(message = "Building is required")
    @Size(max = 100, message = "Building name must not exceed 100 characters")
    private String building;

    @NotNull(message = "Room type is required")
    private RoomType roomType;

    @NotNull(message = "Capacity is required")
    @Positive(message = "Capacity must be greater than 0")
    private Integer capacity;

    private Boolean isActive;
}
