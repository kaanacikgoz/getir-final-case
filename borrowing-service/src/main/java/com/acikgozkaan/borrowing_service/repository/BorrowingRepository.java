package com.acikgozkaan.borrowing_service.repository;

import com.acikgozkaan.borrowing_service.entity.Borrowing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface BorrowingRepository extends JpaRepository<Borrowing, UUID> {

    List<Borrowing> findByUserId(UUID userId);
    List<Borrowing> findByDueDateBeforeAndReturnDateIsNull(LocalDate date);
}