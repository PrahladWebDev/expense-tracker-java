package com.expense.tracker.ocr.service;

import com.expense.tracker.ocr.dto.OcrResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CONCEPT: "Receipt OCR" without training our own model
 * Building real OCR from scratch is its own multi-month project, so
 * instead this calls OCR.space's hosted API (a free tier exists, using
 * their public demo key by default - see application.yml's `app.ocr.api-key`,
 * which you should replace with your own key from ocr.space/ocrapi for
 * production use / higher rate limits). We only depend on the JDK's own
 * HttpClient, so no new library was added to the project for this.
 *
 * Once we have the raw recognized text, we do simple regex/keyword
 * heuristics locally to guess:
 *  - the total amount (the largest currency-looking number on the receipt,
 *    since totals are usually the biggest figure and often the last one)
 *  - a category, by matching common merchant/item keywords
 * These are SUGGESTIONS ONLY - the frontend pre-fills the expense form
 * with them but the user reviews and can edit every field before saving.
 */
@Service
@Slf4j
public class OcrService {

    private static final String OCR_ENDPOINT = "https://api.ocr.space/parse/image";
    private static final Pattern AMOUNT_PATTERN =
            Pattern.compile("(?:₹|rs\\.?|inr)?\\s*([0-9]{1,3}(?:[,\\.][0-9]{3})*(?:\\.[0-9]{1,2})?)", Pattern.CASE_INSENSITIVE);

    private static final Map<String, String> CATEGORY_KEYWORDS = new LinkedHashMap<>();
    static {
        CATEGORY_KEYWORDS.put("restaurant|cafe|food|kitchen|diner|pizza|biryani|dhaba", "Food & Dining");
        CATEGORY_KEYWORDS.put("uber|ola|taxi|cab|fuel|petrol|diesel|metro|bus fare", "Transportation");
        CATEGORY_KEYWORDS.put("mart|store|supermarket|grocery|bazaar", "Groceries");
        CATEGORY_KEYWORDS.put("pharmacy|medical|hospital|clinic|chemist", "Health");
        CATEGORY_KEYWORDS.put("hotel|resort|inn|lodging", "Travel");
        CATEGORY_KEYWORDS.put("movie|cinema|theatre|entertainment|netflix", "Entertainment");
    }

    @Value("${app.ocr.api-key:helloworld}")
    private String apiKey;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OcrResponse extract(MultipartFile file) {
        String rawText = callOcrApi(file);
        BigDecimal amount = guessAmount(rawText);
        String category = guessCategory(rawText);
        String description = guessDescription(rawText);
        return new OcrResponse(rawText, amount, category, description);
    }

    private String callOcrApi(MultipartFile file) {
        try {
            String boundary = "----ExpenseTrackerBoundary" + UUID.randomUUID();
            byte[] body = buildMultipartBody(file, boundary);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OCR_ENDPOINT))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                    .timeout(Duration.ofSeconds(20))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode results = root.path("ParsedResults");
            if (results.isArray() && !results.isEmpty()) {
                return results.get(0).path("ParsedText").asText("");
            }
            log.warn("OCR API returned no parsed results: {}", root.path("ErrorMessage"));
            return "";
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            log.warn("OCR API call failed, returning empty text so the form still opens for manual entry", e);
            return "";
        }
    }

    private byte[] buildMultipartBody(MultipartFile file, String boundary) throws IOException {
        var out = new java.io.ByteArrayOutputStream();
        var writer = new java.io.PrintWriter(new java.io.OutputStreamWriter(out, java.nio.charset.StandardCharsets.UTF_8), true);

        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"apikey\"\r\n\r\n").append(apiKey).append("\r\n");

        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"OCREngine\"\r\n\r\n2\r\n");

        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                .append(file.getOriginalFilename() != null ? file.getOriginalFilename() : "receipt.jpg")
                .append("\"\r\n");
        writer.append("Content-Type: ").append(file.getContentType() != null ? file.getContentType() : "image/jpeg").append("\r\n\r\n");
        writer.flush();
        out.write(file.getBytes());
        writer.append("\r\n--").append(boundary).append("--\r\n");
        writer.flush();
        return out.toByteArray();
    }

    /** Picks the largest currency-looking number in the text - receipt totals are usually the biggest figure. */
    private BigDecimal guessAmount(String text) {
        if (text == null || text.isBlank()) return null;
        Matcher matcher = AMOUNT_PATTERN.matcher(text);
        BigDecimal largest = null;
        while (matcher.find()) {
            try {
                String normalized = matcher.group(1).replace(",", "");
                BigDecimal value = new BigDecimal(normalized);
                if (largest == null || value.compareTo(largest) > 0) {
                    largest = value;
                }
            } catch (NumberFormatException ignored) {
                // not a parseable number, skip it
            }
        }
        return largest;
    }

    private String guessCategory(String text) {
        if (text == null || text.isBlank()) return null;
        String lower = text.toLowerCase(Locale.ROOT);
        for (Map.Entry<String, String> entry : CATEGORY_KEYWORDS.entrySet()) {
            if (Pattern.compile(entry.getKey()).matcher(lower).find()) {
                return entry.getValue();
            }
        }
        return null;
    }

    /** First non-blank line is usually the merchant/business name on a receipt. */
    private String guessDescription(String text) {
        if (text == null || text.isBlank()) return null;
        for (String line : text.split("\\r?\\n")) {
            String trimmed = line.trim();
            if (trimmed.length() >= 3) {
                return trimmed;
            }
        }
        return null;
    }
}
