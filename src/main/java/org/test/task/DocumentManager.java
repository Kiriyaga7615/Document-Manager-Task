package org.test.task;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for store data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {
    private final Map<String, Document> storage = new ConcurrentHashMap<>();
    private static final String ID_PREFIX = "doc_";

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        Objects.requireNonNull(document, "Document cannot be null");

        if (document.getId() == null) {
            document = generateNewDocument(document);
        } else {
            document = updateExistingDocument(document);
        }

        storage.put(document.getId(), document);
        return document;
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        if (request == null) {
            return new ArrayList<>(storage.values());
        }

        return storage.values().stream()
                .filter(doc -> matchesTitle(doc, request.getTitlePrefixes()))
                .filter(doc -> matchesContent(doc, request.getContainsContents()))
                .filter(doc -> matchesAuthor(doc, request.getAuthorIds()))
                .filter(doc -> matchesCreationTime(doc, request.getCreatedFrom(), request.getCreatedTo()))
                .collect(Collectors.toList());
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    /**
     * Generates a new document with a unique id and current timestamp.
     * This method should be used only when the document does not have an existing id.
     *
     * @param document - document to save
     * @return new document with assigned id and created timestamp
     */
    private Document generateNewDocument(Document document) {
        return document.toBuilder()
                .id(ID_PREFIX + UUID.randomUUID())
                .created(Instant.now())
                .build();
    }

    /**
     * Updates an existing document in the storage while preserving its creation timestamp.
     * If the document does not exist in storage, it returns the provided documen.
     *
     * @param document - document with updated information
     * @return updated document with preserved created timestamp
     */
    private Document updateExistingDocument(Document document) {
        Document existing = storage.get(document.getId());
        if (existing == null) return document;

        return document.toBuilder().created(existing.getCreated()).build();
    }

    /**
     * Checks if the document title starts with any of the provided prefixes.
     * If no prefixes are provided, the document matches by default.
     *
     * @param doc - document to check
     * @param prefixes - list of title prefixes to match against
     * @return true if document matches the title prefix filter, false otherwise
     */
    private boolean matchesTitle(Document doc, List<String> prefixes) {
        if (prefixes == null || prefixes.isEmpty()) return true;
        String title = doc.getTitle();
        return title != null && prefixes.stream().anyMatch(title::startsWith);
    }

    /**
     * Checks if the document content contains any of the search strings.
     * If no search strings are provided, the document matches by default.
     *
     * @param doc - document to check
     * @param contents - list of substrings to search within document content
     * @return true if document matches content filter, false otherwise
     */
    private boolean matchesContent(Document doc, List<String> contents) {
        String content = Optional.ofNullable(doc.getContent()).orElse("");
        return contents == null || contents.isEmpty() ||
                contents.stream().anyMatch(content::contains);
    }

    /**
     * Checks if the document was authored by one of the given author id`s.
     * If no author id`s are provided, the document matches by default.
     *
     * @param doc - document to check
     * @param authorIds - list of allowed author id`s
     * @return true if document matches author filter, false otherwise
     */
    private boolean matchesAuthor(Document doc, List<String> authorIds) {
        return authorIds == null || authorIds.isEmpty() ||
                Optional.ofNullable(doc.getAuthor())
                        .map(Author::getId)
                        .map(authorIds::contains)
                        .orElse(false);
    }

    /**
     * Checks if the document was created within the specified date range.
     * If no date range is provided, the document matches by default.
     *
     * @param doc - document to check
     * @param from - start of the creation date range (inclusive)
     * @param to - end of the creation date range (inclusive)
     * @return true if document matches the creation date filter, false otherwise
     */
    private boolean matchesCreationTime(Document doc, Instant from, Instant to) {
        Instant created = doc.getCreated();
        if (created == null) return false;
        return (from == null || !created.isBefore(from)) && (to == null || !created.isAfter(to));
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder(toBuilder = true)
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}
