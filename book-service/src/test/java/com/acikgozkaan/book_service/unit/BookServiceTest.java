package com.acikgozkaan.book_service.unit;

import com.acikgozkaan.book_service.config.StockUpdatePublisher;
import com.acikgozkaan.book_service.dto.request.BookRequest;
import com.acikgozkaan.book_service.dto.response.BookResponse;
import com.acikgozkaan.book_service.entity.Book;
import com.acikgozkaan.book_service.entity.Genre;
import com.acikgozkaan.book_service.exception.BookNotFoundException;
import com.acikgozkaan.book_service.exception.IsbnAlreadyExistsException;
import com.acikgozkaan.book_service.exception.OutOfStockException;
import com.acikgozkaan.book_service.mapper.BookMapper;
import com.acikgozkaan.book_service.repository.BookRepository;
import com.acikgozkaan.book_service.service.impl.BookServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    @InjectMocks
    private BookServiceImpl bookService;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;

    @Mock
    private StockUpdatePublisher stockUpdatePublisher;

    private Book book;
    private BookRequest request;
    private BookResponse response;
    private UUID bookId;

    @BeforeEach
    void setUp() {
        bookId = UUID.randomUUID();

        book = Book.builder()
                .id(bookId)
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn("1234567890123")
                .publicationYear(2008)
                .genre(Genre.TECHNOLOGY)
                .stock(5)
                .build();

        request = new BookRequest(
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getPublicationYear(),
                book.getGenre(),
                book.getStock()
        );

        response = new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getPublicationYear(),
                book.getGenre(),
                book.getStock()
        );
    }

    @Test
    @DisplayName("Should create book successfully")
    void shouldCreateBook() {
        when(bookRepository.existsByIsbn(request.isbn())).thenReturn(false);
        when(bookMapper.toBook(request)).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toResponse(book)).thenReturn(response);

        BookResponse result = bookService.create(request);

        assertThat(result).isEqualTo(response);
        verify(bookRepository).save(book);
    }

    @Test
    @DisplayName("Should throw if ISBN already exists on create")
    void shouldThrowIfIsbnExistsOnCreate() {
        when(bookRepository.existsByIsbn(request.isbn())).thenReturn(true);

        assertThatThrownBy(() -> bookService.create(request))
                .isInstanceOf(IsbnAlreadyExistsException.class);
    }

    @Test
    @DisplayName("Should get book by id")
    void shouldGetBookById() {
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(bookMapper.toResponse(book)).thenReturn(response);

        BookResponse result = bookService.getById(bookId);

        assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("Should throw when book not found by id")
    void shouldThrowWhenBookNotFound() {
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getById(bookId))
                .isInstanceOf(BookNotFoundException.class);
    }

    @Test
    @DisplayName("Should return all books")
    void shouldReturnAllBooks() {
        when(bookRepository.findAll()).thenReturn(List.of(book));
        when(bookMapper.toResponse(book)).thenReturn(response);

        List<BookResponse> result = bookService.getAll();

        assertThat(result).containsExactly(response);
    }

    @Test
    @DisplayName("Should update book successfully")
    void shouldUpdateBookSuccessfully() {
        when(bookRepository.existsByIsbnAndIdNot(request.isbn(), bookId)).thenReturn(false);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        doAnswer(invocation -> {
            Book b = invocation.getArgument(0);
            BookRequest r = invocation.getArgument(1);
            b.setTitle(r.title());
            b.setAuthor(r.author());
            b.setIsbn(r.isbn());
            b.setPublicationYear(r.publicationYear());
            b.setGenre(r.genre());
            b.setStock(r.stock());
            return null;
        }).when(bookMapper).updateBookFromRequest(book, request);

        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toResponse(book)).thenReturn(response);

        BookResponse result = bookService.update(bookId, request);

        assertThat(result).isEqualTo(response);
        verify(bookRepository).save(book);
    }

    @Test
    @DisplayName("Should throw if ISBN exists on another book during update")
    void shouldThrowIfIsbnExistsOnUpdate() {
        when(bookRepository.existsByIsbnAndIdNot(request.isbn(), bookId)).thenReturn(true);

        assertThatThrownBy(() -> bookService.update(bookId, request))
                .isInstanceOf(IsbnAlreadyExistsException.class);
    }

    @Test
    @DisplayName("Should throw if book not found during update")
    void shouldThrowIfBookNotFoundOnUpdate() {
        when(bookRepository.existsByIsbnAndIdNot(request.isbn(), bookId)).thenReturn(false);
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.update(bookId, request))
                .isInstanceOf(BookNotFoundException.class);
    }

    @Test
    @DisplayName("Should delete book when it exists")
    void shouldDeleteBookWhenExists() {
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        bookService.delete(bookId);

        verify(bookRepository).delete(book);
    }

    @Test
    @DisplayName("Should throw BookNotFoundException if book not found during delete")
    void shouldThrowExceptionWhenBookNotFoundOnDelete() {
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.delete(bookId))
                .isInstanceOf(BookNotFoundException.class);
    }

    @Test
    @DisplayName("Should decrease stock when book is available")
    void shouldDecreaseStock() {
        book.setStock(3);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        bookService.decreaseStock(bookId);

        assertThat(book.getStock()).isEqualTo(2);
        verify(bookRepository).save(book);
        verify(stockUpdatePublisher).publish(any());
    }

    @Test
    @DisplayName("Should throw OutOfStockException when stock is 0")
    void shouldThrowOutOfStockWhenStockIsZero() {
        book.setStock(0);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> bookService.decreaseStock(bookId))
                .isInstanceOf(OutOfStockException.class);
    }

    @Test
    @DisplayName("Should throw BookNotFoundException when book not found")
    void shouldThrowBookNotFoundWhenDecreasingStock() {
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.decreaseStock(bookId))
                .isInstanceOf(BookNotFoundException.class);
    }

    @Test
    @DisplayName("Should increase stock when book exists")
    void shouldIncreaseStock() {
        book.setStock(2);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        bookService.increaseStock(bookId);

        assertThat(book.getStock()).isEqualTo(3);
        verify(bookRepository).save(book);
        verify(stockUpdatePublisher).publish(any());
    }

    @Test
    @DisplayName("Should throw BookNotFoundException when increasing stock for non-existing book")
    void shouldThrowBookNotFoundWhenIncreasingStock() {
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.increaseStock(bookId))
                .isInstanceOf(BookNotFoundException.class);
    }

    @Test
    @DisplayName("Should return books matching all search criteria")
    void shouldReturnMatchingBooks() {
        Pageable pageable = PageRequest.of(0, 5);
        Book book = buildSampleBook();
        Page<Book> bookPage = new PageImpl<>(List.of(book));

        @SuppressWarnings("unchecked")
        Specification<Book> spec = any(Specification.class);

        when(bookRepository.findAll(spec, eq(pageable))).thenReturn(bookPage);
        when(bookMapper.toResponse(book)).thenReturn(buildSampleResponse(book));

        Page<BookResponse> result = bookService.searchBooks(
                "Clean Code", "Robert", "9780132350884", Genre.TECHNOLOGY, pageable
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().title()).isEqualTo("Clean Code");
    }

    @Test
    @DisplayName("Should return empty page when no books match criteria")
    void shouldReturnEmptyWhenNoMatch() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Book> emptyPage = Page.empty();

        @SuppressWarnings("unchecked")
        Specification<Book> spec = any(Specification.class);

        when(bookRepository.findAll(spec, eq(pageable))).thenReturn(emptyPage);

        Page<BookResponse> result = bookService.searchBooks("NoTitle", null, null, null, pageable);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Should handle null parameters and return all results paged")
    void shouldHandleNullParams() {
        Pageable pageable = PageRequest.of(0, 10);
        Book book1 = buildSampleBook();
        Book book2 = Book.builder()
                .id(UUID.randomUUID())
                .title("Another Book")
                .author("Another Author")
                .isbn("1234567890123")
                .genre(Genre.HISTORY)
                .publicationYear(2010)
                .stock(5)
                .build();
        Page<Book> books = new PageImpl<>(List.of(book1, book2));

        @SuppressWarnings("unchecked")
        Specification<Book> spec = any(Specification.class);

        when(bookRepository.findAll(spec, eq(pageable))).thenReturn(books);
        when(bookMapper.toResponse(book1)).thenReturn(buildSampleResponse(book1));
        when(bookMapper.toResponse(book2)).thenReturn(buildSampleResponse(book2));

        Page<BookResponse> result = bookService.searchBooks(null, null, null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
    }


    private Book buildSampleBook() {
        return Book.builder()
                .id(UUID.randomUUID())
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn("9780132350884")
                .genre(Genre.TECHNOLOGY)
                .publicationYear(2008)
                .stock(10)
                .build();
    }

    private BookResponse buildSampleResponse(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getPublicationYear(),
                book.getGenre(),
                book.getStock()
        );
    }

}