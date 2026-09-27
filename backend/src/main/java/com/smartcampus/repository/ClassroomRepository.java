package com.smartcampus.repository;

import com.smartcampus.entity.Classroom;
import com.smartcampus.entity.enums.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
