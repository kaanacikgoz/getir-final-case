package com.acikgozkaan.borrowing_service.unit;

import com.acikgozkaan.borrowing_service.client.BookClient;
import com.acikgozkaan.borrowing_service.client.UserClient;
import com.acikgozkaan.borrowing_service.dto.external.BookResponse;
import com.acikgozkaan.borrowing_service.dto.request.BorrowingRequest;
import com.acikgozkaan.borrowing_service.dto.response.BorrowingResponse;
import com.acikgozkaan.borrowing_service.entity.Borrowing;
import com.acikgozkaan.borrowing_service.exception.OutOfStockException;
import com.acikgozkaan.borrowing_service.mapper.BorrowingMapper;
import com.acikgozkaan.borrowing_service.repository.BorrowingRepository;
import com.acikgozkaan.borrowing_service.service.impl.BorrowingServiceImpl;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowingServiceTest {

    @Mock private BorrowingRepository borrowingRepository;
    @Mock private BorrowingMapper borrowingMapper;
    @Mock private UserClient userClient;
    @Mock private BookClient bookClient;

    @InjectMocks
    private BorrowingServiceImpl borrowingService;

    @Test
    void borrowBook_successful() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        BorrowingRequest request = new BorrowingRequest(userId, bookId);

        BookResponse bookResponse = new BookResponse(bookId, "Book Name", 1);
        Borrowing borrowing = new Borrowing();
        Borrowing saved = new Borrowing();
        BorrowingResponse response = new BorrowingResponse(
                UUID.randomUUID(), userId, bookId,
                LocalDate.now(), LocalDate.now().plusDays(14), null
        );

        when(bookClient.getBookById(bookId)).thenReturn(bookResponse);
        when(borrowingMapper.toEntity(request)).thenReturn(borrowing);
        when(borrowingRepository.save(borrowing)).thenReturn(saved);
        when(borrowingMapper.toResponse(saved)).thenReturn(response);

        BorrowingResponse result = borrowingService.borrowBook(request);

        assertEquals(response, result);
        verify(userClient).checkUserExists(userId);
        verify(bookClient).decreaseStock(bookId);
    }

    @Test
    void borrowBook_outOfStock_throwsException() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        BorrowingRequest request = new BorrowingRequest(userId, bookId);

        BookResponse bookResponse = new BookResponse(bookId, "Book", 0);
        when(bookClient.getBookById(bookId)).thenReturn(bookResponse);

        FeignException.Conflict conflict = mock(FeignException.Conflict.class);
        doThrow(conflict).when(bookClient).decreaseStock(bookId);

        assertThrows(OutOfStockException.class, () -> borrowingService.borrowBook(request));
    }

    @Test
    void getAll_returnsMappedList() {
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Borrowing borrowing = new Borrowing();
        BorrowingResponse response = new BorrowingResponse(
                UUID.randomUUID(), userId, bookId,
                LocalDate.now(), LocalDate.now().plusDays(14), null
        );

        when(borrowingRepository.findAll()).thenReturn(List.of(borrowing));
        when(borrowingMapper.toResponse(borrowing)).thenReturn(response);

        List<BorrowingResponse> result = borrowingService.getAll();

        assertEquals(1, result.size());
        assertEquals(response, result.get(0));
    }

    @Test
    void returnBook_successful() {
        UUID borrowingId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Borrowing borrowing = new Borrowing();
        borrowing.setBookId(bookId);
        borrowing.setUserId(userId);
        borrowing.setReturnDate(null);

        Borrowing updated = new Borrowing();
        BorrowingResponse response = new BorrowingResponse(
                borrowingId, userId, bookId,
                LocalDate.now().minusDays(10), LocalDate.now(), LocalDate.now()
        );

        when(borrowingRepository.findById(borrowingId)).thenReturn(Optional.of(borrowing));
        when(borrowingRepository.save(any())).thenReturn(updated);
        when(borrowingMapper.toResponse(updated)).thenReturn(response);

        BorrowingResponse result = borrowingService.returnBook(borrowingId);

        assertEquals(response, result);
        verify(bookClient).increaseStock(bookId);
    }

    @Test
    void returnBook_alreadyReturned_throwsException() {
        UUID borrowingId = UUID.randomUUID();
        Borrowing borrowing = new Borrowing();
        borrowing.setReturnDate(LocalDate.now());

        when(borrowingRepository.findById(borrowingId)).thenReturn(Optional.of(borrowing));

        Exception ex = assertThrows(RuntimeException.class, () -> borrowingService.returnBook(borrowingId));
        assertTrue(ex.getMessage().contains("already returned"));
    }

    @Test
    void getById_found_returnsMapped() {
        UUID id = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Borrowing borrowing = new Borrowing();
        BorrowingResponse response = new BorrowingResponse(
                id, userId, bookId,
                LocalDate.now(), LocalDate.now().plusDays(14), null
        );

        when(borrowingRepository.findById(id)).thenReturn(Optional.of(borrowing));
        when(borrowingMapper.toResponse(borrowing)).thenReturn(response);

        BorrowingResponse result = borrowingService.getById(id);

        assertEquals(response, result);
    }

    @Test
    void getByUserId_returnsList() {
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Borrowing borrowing = new Borrowing();
        BorrowingResponse response = new BorrowingResponse(
                UUID.randomUUID(), userId, bookId,
                LocalDate.now(), LocalDate.now().plusDays(14), null
        );

        when(borrowingRepository.findByUserId(userId)).thenReturn(List.of(borrowing));
        when(borrowingMapper.toResponse(borrowing)).thenReturn(response);

        List<BorrowingResponse> result = borrowingService.getByUserId(userId);

        assertEquals(1, result.size());
        assertEquals(response, result.get(0));
    }

    @Test
    void getOverdueBorrowings_returnsList() {
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Borrowing borrowing = new Borrowing();
        BorrowingResponse response = new BorrowingResponse(
                UUID.randomUUID(), userId, bookId,
                LocalDate.now().minusDays(20), LocalDate.now().minusDays(5), null
        );

        when(borrowingRepository.findByDueDateBeforeAndReturnDateIsNull(any())).thenReturn(List.of(borrowing));
        when(borrowingMapper.toResponse(borrowing)).thenReturn(response);

        List<BorrowingResponse> result = borrowingService.getOverdueBorrowings();

        assertEquals(1, result.size());
        assertEquals(response, result.get(0));
    }

    @Test
    void generateOverdueReport_returnsFormattedString() {
        UUID userId = UUID.randomUUID();
        Borrowing borrowing = new Borrowing();
        borrowing.setUserId(userId);

        when(borrowingRepository.findByDueDateBeforeAndReturnDateIsNull(any())).thenReturn(List.of(borrowing));

        String report = borrowingService.generateOverdueReport();

        assertTrue(report.contains("Total Overdue Borrowings: 1"));
        assertTrue(report.contains(userId.toString()));
    }
}