package com.permitioexample.LibraryPermitIO;

import io.permit.sdk.api.PermitApiError;
import io.permit.sdk.api.PermitContextError;
import io.permit.sdk.openapi.models.UserRead;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final PermitService permitService;

    @PostMapping("/sync-user")
    public ResponseEntity<String> syncUser(Principal principal) {
        // Example user data
        String userId = principal.getName();
        String email = userId + "@example.com";
        String firstName = "John";
        String lastName = "Doe";
        String role = "admin";  // Example role

        // Sync user with Permit.io
        UserRead syncedUser = permitService.syncUser(userId, email, firstName, lastName, role);

        // Retrieve the first name from the attributes map
        String syncedFirstName = (String) syncedUser.getAttributes().get("firstName");

        return ResponseEntity.ok("User " + syncedFirstName + " synced successfully with role: " + role);
    }

    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks(Principal principal) throws PermitContextError, PermitApiError, IOException {
        // Check permission using Permit.io
        if (!permitService.checkPermission(principal.getName(), "view", "book")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        List<Book> books = bookService.getAllBooks();
        return ResponseEntity.ok(books);
    }

    @PostMapping
    public ResponseEntity<Book> addBook(@RequestBody Book book, Principal principal) throws PermitContextError, PermitApiError, IOException {
        // Check permission using Permit.io
        if (!permitService.checkPermission(principal.getName(), "create", "book")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        Book createdBook = bookService.addBook(book);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @RequestBody Book bookDetails, Principal principal) throws PermitContextError, PermitApiError, IOException {
        // Check permission using Permit.io
        if (!permitService.checkPermission(principal.getName(), "edit", "book")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        Book updatedBook = bookService.updateBook(id, bookDetails);
        return ResponseEntity.ok(updatedBook);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id, Principal principal) {
        // Check permission using Permit.io
        if (!permitService.checkPermission(principal.getName(), "delete", "book")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        bookService.deleteBook(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
