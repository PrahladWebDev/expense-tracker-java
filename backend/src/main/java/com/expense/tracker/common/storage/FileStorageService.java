package com.expense.tracker.common.storage;

import com.expense.tracker.common.exception.ResourceNotFoundException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * CONCEPT: local disk storage, kept behind auth
 * Uploaded files (currently just expense receipts) are written to a single
 * directory on disk, each under a random UUID filename so nobody can guess
 * another file's name. We deliberately do NOT expose this directory as a
 * public static resource - every read goes through an authenticated
 * controller endpoint (see GroupExpenseController) that first checks group
 * membership, then streams the bytes back. That keeps receipts as private
 * as the rest of a group's data, consistent with the existing PDF-export
 * pattern (blob response, not a public URL).
 *
 * In a production deployment you'd swap this for S3/GCS - everything else
 * in the app only depends on this class's two methods, not on "it's a local
 * file", so that swap wouldn't touch any other file.
 */
@Service
@RequiredArgsConstructor
public class FileStorageService {

    @Value("${app.uploads.dir:./uploads}")
    private String uploadsDir;

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(rootPath());
    }

    /** Saves the file under a fresh random name and returns that stored name (not the original filename). */
    public String store(MultipartFile file) {
        String extension = "";
        String original = file.getOriginalFilename();
        if (StringUtils.hasText(original) && original.contains(".")) {
            extension = original.substring(original.lastIndexOf('.'));
        }
        String storedName = UUID.randomUUID() + extension;
        try {
            Files.copy(file.getInputStream(), rootPath().resolve(storedName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Could not store file", e);
        }
        return storedName;
    }

    public InputStream read(String storedName) {
        try {
            Path path = rootPath().resolve(storedName).normalize();
            if (!path.startsWith(rootPath())) {
                throw new ResourceNotFoundException("File not found"); // path traversal guard
            }
            return Files.newInputStream(path);
        } catch (IOException e) {
            throw new ResourceNotFoundException("File not found");
        }
    }

    public void delete(String storedName) {
        try {
            Files.deleteIfExists(rootPath().resolve(storedName).normalize());
        } catch (IOException ignored) {
            // best-effort cleanup; an orphaned file on disk isn't worth failing the request over
        }
    }

    private Path rootPath() {
        return Paths.get(uploadsDir).toAbsolutePath().normalize();
    }
}
