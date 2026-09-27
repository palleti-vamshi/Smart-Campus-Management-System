package com.smartcampus.service;

import com.smartcampus.dto.request.ClassroomRequest;
import com.smartcampus.dto.response.ClassroomResponse;
import com.smartcampus.dto.response.PageResponse;
import com.smartcampus.entity.Classroom;
import com.smartcampus.entity.enums.RoomType;
import com.smartcampus.exception.InvalidOperationException;
import com.smartcampus.exception.ResourceConflictException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.ClassroomRepository;
import com.smartcampus.repository.TimetableRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final TimetableRepository timetableRepository;

    public ClassroomService(ClassroomRepository classroomRepository,
                            TimetableRepository timetableRepository) {
        this.classroomRepository = classroomRepository;
        this.timetableRepository = timetableRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<ClassroomResponse> getClassrooms(
            String roomNumber,
            String building,
            RoomType roomType,
            Boolean isActive,
            Pageable pageable) {
        Page<Classroom> page = classroomRepository.findWithFilters(
                roomNumber, building, roomType, isActive, pageable);
        return PageResponse.from(page.map(ClassroomResponse::fromEntity));
    }

    @Transactional(readOnly = true)
    public ClassroomResponse getClassroomById(Long id) {
        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom not found with ID: " + id));
        return ClassroomResponse.fromEntity(classroom);
    }

    @Transactional
    public ClassroomResponse createClassroom(ClassroomRequest request) {
        if (classroomRepository.existsByRoomNumber(request.getRoomNumber())) {
            throw new ResourceConflictException("Classroom with room number '" + request.getRoomNumber() + "' already exists");
        }

        if (request.getCapacity() == null || request.getCapacity() <= 0) {
            throw new InvalidOperationException("Capacity must be greater than 0");
        }

        Boolean active = request.getIsActive() != null ? request.getIsActive() : true;

        Classroom classroom = Classroom.builder()
                .roomNumber(request.getRoomNumber().trim())
                .building(request.getBuilding().trim())
                .roomType(request.getRoomType())
                .capacity(request.getCapacity())
                .isActive(active)
                .build();

        Classroom saved = classroomRepository.save(classroom);
        log.info("Classroom created: {} in building {}", saved.getRoomNumber(), saved.getBuilding());
        return ClassroomResponse.fromEntity(saved);
    }

    @Transactional
    public ClassroomResponse updateClassroom(Long id, ClassroomRequest request) {
        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom not found with ID: " + id));

        if (classroomRepository.existsByRoomNumberAndClassroomIdNot(request.getRoomNumber(), id)) {
            throw new ResourceConflictException("Classroom with room number '" + request.getRoomNumber() + "' already exists");
        }

        if (request.getCapacity() == null || request.getCapacity() <= 0) {
            throw new InvalidOperationException("Capacity must be greater than 0");
        }

        classroom.setRoomNumber(request.getRoomNumber().trim());
        classroom.setBuilding(request.getBuilding().trim());
        classroom.setRoomType(request.getRoomType());
        classroom.setCapacity(request.getCapacity());
        if (request.getIsActive() != null) {
            classroom.setIsActive(request.getIsActive());
        }

        Classroom updated = classroomRepository.save(classroom);
        log.info("Classroom updated: {}", updated.getRoomNumber());
        return ClassroomResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteClassroom(Long id) {
        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom not found with ID: " + id));

        if (timetableRepository.existsByClassroom_ClassroomId(id)) {
            throw new ResourceConflictException("Cannot delete classroom with ID " + id + " because it is associated with existing timetable entries");
        }

        classroomRepository.delete(classroom);
        log.info("Classroom deleted: ID {}", id);
    }
}
