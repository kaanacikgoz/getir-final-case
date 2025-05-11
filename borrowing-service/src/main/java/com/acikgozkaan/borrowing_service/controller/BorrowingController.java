package com.acikgozkaan.borrowing_service.controller;

import com.acikgozkaan.borrowing_service.dto.request.BorrowingRequest;
import com.acikgozkaan.borrowing_service.dto.response.BorrowingResponse;
import com.acikgozkaan.borrowing_service.service.BorrowingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/borrowings")
@RequiredArgsConstructor
public class BorrowingController {

    private final BorrowingService borrowingService;

    @PostMapping
    @PreAuthorize("hasRole('PATRON')")
    public ResponseEntity<BorrowingResponse> borrowBook(@Valid @RequestBody BorrowingRequest request) {
        return ResponseEntity.ok(borrowingService.borrowBook(request));
    }

    @PutMapping("/{id}/return")
    @PreAuthorize("hasRole('PATRON')")
    public ResponseEntity<BorrowingResponse> returnBook(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(borrowingService.returnBook(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('LIBRARIAN')")
    public ResponseEntity<List<BorrowingResponse>> getAll() {
        return ResponseEntity.ok(borrowingService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'PATRON')")
    public ResponseEntity<BorrowingResponse> getById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(borrowingService.getById(id));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'PATRON')")
    public ResponseEntity<List<BorrowingResponse>> getByUserId(@PathVariable("userId") UUID userId) {
        return ResponseEntity.ok(borrowingService.getByUserId(userId));
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public ResponseEntity<List<BorrowingResponse>> getOverdueBorrowings() {
        return ResponseEntity.ok(borrowingService.getOverdueBorrowings());
    }

    @GetMapping("/overdue/report")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public ResponseEntity<String> generateOverdueReport() {
        return ResponseEntity.ok().body(borrowingService.generateOverdueReport());
    }

}