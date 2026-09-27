package com.smartcampus.repository;

import com.smartcampus.entity.Classroom;
import com.smartcampus.entity.enums.RoomType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the Classroom entity.
 */
@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {

    Optional<Classroom> findByRoomNumber(String roomNumber);

    List<Classroom> findByRoomType(RoomType roomType);

    List<Classroom> findByIsActiveTrue();

    boolean existsByRoomNumber(String roomNumber);

    boolean existsByRoomNumberAndClassroomIdNot(String roomNumber, Long classroomId);

    @Query("SELECT c FROM Classroom c WHERE " +
            "(:roomNumber IS NULL OR LOWER(c.roomNumber) LIKE LOWER(CONCAT('%', :roomNumber, '%'))) AND " +
            "(:building IS NULL OR LOWER(c.building) LIKE LOWER(CONCAT('%', :building, '%'))) AND " +
            "(:roomType IS NULL OR c.roomType = :roomType) AND " +
            "(:isActive IS NULL OR c.isActive = :isActive)")
    Page<Classroom> findWithFilters(
            @Param("roomNumber") String roomNumber,
            @Param("building") String building,
            @Param("roomType") RoomType roomType,
            @Param("isActive") Boolean isActive,
            Pageable pageable);
}
