package org.test.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.test.task.DocumentManager.Document;
import org.test.task.DocumentManager.Author;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for DocumentManager.
 * Tests the core functionality of the DocumentManager class including save, search, and findById methods.
 */
public class DocumentManagerTest {

    private DocumentManager documentManager;

    /**
     * Initializes the DocumentManager before each test.
     */
    @BeforeEach
    public void setup() {
        documentManager = new DocumentManager();
    }

    /**
     * Tests saving a new document.
     * Verifies that the document is saved correctly and an id is generated.
     */
    @Test
    public void testSaveNewDocument() {
        Document document = Document.builder()
                .title("Test Document")
                .content("This is a test document.")
                .author(new Author("1", "John Doe"))
                .build();

        Document savedDocument = documentManager.save(document);

        assertNotNull(savedDocument.getId(), "Document id should not be null");
        assertEquals("Test Document", savedDocument.getTitle());
        assertEquals("This is a test document.", savedDocument.getContent());
        assertNotNull(savedDocument.getCreated(), "Document created time should not be null");
    }

    /**
     * Tests updating an existing document.
     * Verifies that the document is updated correctly while preserving its original creation time.
     */
    @Test
    public void testUpdateExistingDocument() {
        Document document = Document.builder()
                .id("doc_1")
                .title("Test Document")
                .content("This is a test document.")
                .author(new Author("1", "John Doe"))
                .created(Instant.now())
                .build();

        documentManager.save(document);

        Document updatedDocument = Document.builder()
                .id("doc_1")
                .title("Updated Test Document")
                .content("Updated content.")
                .author(new Author("1", "John Doe"))
                .created(document.getCreated())
                .build();

        Document savedDocument = documentManager.save(updatedDocument);

        assertEquals("Updated Test Document", savedDocument.getTitle());
        assertEquals("Updated content.", savedDocument.getContent());
    }

    /**
     * Tests searching for documents by author.
     * Verifies that the correct document is found based on the provided author ID.
     */
    @Test
    public void testSearchByAuthor() {
        Document doc1 = Document.builder()
                .id("doc_1")
                .title("Document 1")
                .content("Content 1")
                .author(new Author("1", "John Doe"))
                .created(Instant.now())
                .build();

        Document doc2 = Document.builder()
                .id("doc_2")
                .title("Document 2")
                .content("Content 2")
                .author(new Author("2", "Jane Doe"))
                .created(Instant.now())
                .build();

        documentManager.save(doc1);
        documentManager.save(doc2);

        DocumentManager.SearchRequest searchRequest = DocumentManager.SearchRequest.builder()
                .authorIds(List.of("1"))
                .build();

        List<Document> result = documentManager.search(searchRequest);

        assertEquals(1, result.size(), "There should be 1 document for author John Doe");
        assertEquals("Document 1", result.get(0).getTitle());
    }

    /**
     * Tests searching for documents by creation time.
     * Verifies that the correct document is found based on the provided date range.
     */
    @Test
    public void testSearchByCreatedTime() {
        Instant now = Instant.now();
        Document doc1 = Document.builder()
                .id("doc_1")
                .title("Document 1")
                .content("Content 1")
                .author(new Author("1", "John Doe"))
                .created(now.minusSeconds(3600)) // 1 hour
                .build();

        Document doc2 = Document.builder()
                .id("doc_2")
                .title("Document 2")
                .content("Content 2")
                .author(new Author("2", "Jane Doe"))
                .created(now.minusSeconds(7200)) // 2 hours
                .build();

        documentManager.save(doc1);
        documentManager.save(doc2);

        DocumentManager.SearchRequest searchRequest = DocumentManager.SearchRequest.builder()
                .createdFrom(now.minusSeconds(5400)) // 1.5 hours
                .build();

        List<Document> result = documentManager.search(searchRequest);

        assertEquals(1, result.size(), "There should be 1 document created after 1.5 hours ago");
        assertEquals("Document 1", result.get(0).getTitle());
    }

    /**
     * Tests finding a document by its id when the document exists.
     * Verifies that the document is correctly found.
     */
    @Test
    public void testFindByIdFound() {
        Document document = Document.builder()
                .id("doc_1")
                .title("Test Document")
                .content("Content of the test document")
                .author(new Author("1", "John Doe"))
                .created(Instant.now())
                .build();

        documentManager.save(document);

        Optional<Document> foundDocument = documentManager.findById("doc_1");

        assertTrue(foundDocument.isPresent(), "Document should be found");
        assertEquals("Test Document", foundDocument.get().getTitle());
    }

    /**
     * Tests finding a document by its id when the document does not exist.
     * Verifies that the result is empty.
     */
    @Test
    public void testFindByIdNotFound() {
        Optional<Document> foundDocument = documentManager.findById("non_existent_id");

        assertFalse(foundDocument.isPresent(), "Document should not be found");
    }
}
