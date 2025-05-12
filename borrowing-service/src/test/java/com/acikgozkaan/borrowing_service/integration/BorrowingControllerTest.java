package com.acikgozkaan.borrowing_service.integration;

import com.acikgozkaan.borrowing_service.client.BookClient;
import com.acikgozkaan.borrowing_service.client.UserClient;
import com.acikgozkaan.borrowing_service.dto.external.BookResponse;
import com.acikgozkaan.borrowing_service.dto.request.BorrowingRequest;
import com.acikgozkaan.borrowing_service.entity.Borrowing;
import com.acikgozkaan.borrowing_service.repository.BorrowingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BorrowingControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private BorrowingRepository borrowingRepository;

    @MockitoBean
    private UserClient userClient;

    @MockitoBean
    private BookClient bookClient;


    private UUID userId;
    private UUID bookId;

    @BeforeEach
    void setUp() {
        borrowingRepository.deleteAll();
        userId = UUID.randomUUID();
        bookId = UUID.randomUUID();
    }

    @Test
    @WithMockUser(roles = "PATRON")
    void shouldBorrowBook() throws Exception {
        doNothing().when(userClient).checkUserExists(userId);
        when(bookClient.getBookById(bookId)).thenReturn(new BookResponse(bookId, "Book", 1));
        doNothing().when(bookClient).decreaseStock(bookId);

        BorrowingRequest request = new BorrowingRequest(userId, bookId);

        mockMvc.perform(post("/api/v1/borrowings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void shouldGetAllBorrowings() throws Exception {
        Borrowing borrowing = Borrowing.builder()
                .userId(userId)
                .bookId(bookId)
                .borrowDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(14))
                .returnDate(null)
                .build();

        borrowingRepository.save(borrowing);

        mockMvc.perform(get("/api/v1/borrowings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(userId.toString()));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void shouldGetOverdueBorrowings() throws Exception {
        Borrowing overdue = Borrowing.builder()
                .userId(userId)
                .bookId(bookId)
                .borrowDate(LocalDate.now().minusDays(20))
                .dueDate(LocalDate.now().minusDays(5))
                .returnDate(null)
                .build();

        borrowingRepository.save(overdue);

        mockMvc.perform(get("/api/v1/borrowings/overdue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(userId.toString()));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void shouldGenerateOverdueReport() throws Exception {
        Borrowing overdue = Borrowing.builder()
                .userId(userId)
                .bookId(bookId)
                .borrowDate(LocalDate.now().minusDays(20))
                .dueDate(LocalDate.now().minusDays(5))
                .returnDate(null)
                .build();

        borrowingRepository.save(overdue);

        mockMvc.perform(get("/api/v1/borrowings/overdue/report"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("OVERDUE BOOK REPORT")));
    }

    @Test
    @WithMockUser(roles = "PATRON")
    void shouldReturnBookSuccessfully() throws Exception {
        Borrowing borrowing = Borrowing.builder()
                .userId(userId)
                .bookId(bookId)
                .borrowDate(LocalDate.now().minusDays(10))
                .dueDate(LocalDate.now().plusDays(4))
                .returnDate(null)
                .build();
        borrowing = borrowingRepository.save(borrowing);

        doNothing().when(bookClient).increaseStock(bookId);

        mockMvc.perform(put("/api/v1/borrowings/" + borrowing.getId() + "/return"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(borrowing.getId().toString()))
                .andExpect(jsonPath("$.returnDate").exists());
    }

    @Test
    @WithMockUser(roles = "PATRON")
    void shouldNotReturnBook_WhenAlreadyReturned() throws Exception {
        Borrowing borrowing = Borrowing.builder()
                .userId(userId)
                .bookId(bookId)
                .borrowDate(LocalDate.now().minusDays(20))
                .dueDate(LocalDate.now().minusDays(5))
                .returnDate(LocalDate.now().minusDays(1))
                .build();
        borrowing = borrowingRepository.save(borrowing);

        mockMvc.perform(put("/api/v1/borrowings/" + borrowing.getId() + "/return"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("already returned")));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void shouldGetBorrowingById() throws Exception {
        Borrowing borrowing = Borrowing.builder()
                .userId(userId)
                .bookId(bookId)
                .borrowDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(14))
                .returnDate(null)
                .build();

        borrowing = borrowingRepository.save(borrowing);

        mockMvc.perform(get("/api/v1/borrowings/" + borrowing.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(borrowing.getId().toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.bookId").value(bookId.toString()));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void shouldGetBorrowingsByUserId() throws Exception {
        Borrowing borrowing = Borrowing.builder()
                .userId(userId)
                .bookId(bookId)
                .borrowDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(14))
                .returnDate(null)
                .build();

        borrowingRepository.save(borrowing);

        mockMvc.perform(get("/api/v1/borrowings/user/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(userId.toString()));
    }

}