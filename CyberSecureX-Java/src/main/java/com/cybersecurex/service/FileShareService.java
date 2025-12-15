package com.cybersecurex.service;

import com.cybersecurex.model.SharedFile;
import com.cybersecurex.repository.SharedFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class FileShareService {

    @Autowired
    private SharedFileRepository fileRepository;

    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom random = new SecureRandom();

    public Map<String, Object> uploadFile(MultipartFile file, String password,
            Integer maxDownloads, Integer expiryHours,
            String description, String uploaderIP) {
        Map<String, Object> result = new HashMap<>();

        try {
            // Validate file
            if (file.isEmpty()) {
                result.put("status", "error");
                result.put("message", "File is empty");
                return result;
            }

            // Check file size (100MB limit)
            if (file.getSize() > 100 * 1024 * 1024) {
                result.put("status", "error");
                result.put("message", "File size exceeds 100MB limit");
                return result;
            }

            // Create upload directory if it doesn't exist
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Generate unique file names and tokens
            String shareToken = generateShareToken();
            String storedFileName = generateStoredFileName(file.getOriginalFilename());

            // Hash password if provided
            String hashedPassword = null;
            if (password != null && !password.trim().isEmpty()) {
                hashedPassword = hashPassword(password.trim());
            }

            // Create SharedFile entity
            SharedFile sharedFile = new SharedFile(
                    file.getOriginalFilename(),
                    storedFileName,
                    shareToken,
                    hashedPassword,
                    file.getSize(),
                    file.getContentType(),
                    uploaderIP);

            // Set optional parameters
            if (maxDownloads != null && maxDownloads > 0) {
                sharedFile.setMaxDownloads(maxDownloads);
            }

            if (expiryHours != null && expiryHours > 0) {
                sharedFile.setExpiryTime(LocalDateTime.now().plusHours(expiryHours));
            }

            if (description != null && !description.trim().isEmpty()) {
                sharedFile.setDescription(description.trim());
            }

            // Store file on disk
            Path filePath = uploadDir.resolve(storedFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Save to database
            sharedFile = fileRepository.save(sharedFile);

            // Prepare success response
            result.put("status", "success");
            result.put("message", "File uploaded successfully!");
            result.put("shareToken", shareToken);
            result.put("shareUrl", "/download/" + shareToken);
            result.put("fileName", file.getOriginalFilename());
            result.put("fileSize", sharedFile.getFormattedFileSize());
            result.put("uploadTime", sharedFile.getUploadTime().toString());
            result.put("hasPassword", hashedPassword != null);
            result.put("maxDownloads", sharedFile.getMaxDownloads());
            result.put("expiryTime", sharedFile.getExpiryTime() != null ? sharedFile.getExpiryTime().toString() : null);

        } catch (IOException e) {
            result.put("status", "error");
            result.put("message", "Failed to store file: " + e.getMessage());
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "Upload failed: " + e.getMessage());
        }

        return result;
    }

    public Map<String, Object> getFileInfo(String shareToken) {
        Map<String, Object> result = new HashMap<>();

        try {
            Optional<SharedFile> fileOpt = fileRepository.findByShareToken(shareToken);

            if (!fileOpt.isPresent()) {
                result.put("status", "error");
                result.put("message", "File not found");
                return result;
            }

            SharedFile sharedFile = fileOpt.get();

            // Check if file is accessible
            if (!sharedFile.isAccessible()) {
                result.put("status", "error");
                if (sharedFile.isExpired()) {
                    result.put("message", "File has expired");
                } else if (sharedFile.isDownloadLimitReached()) {
                    result.put("message", "Download limit reached");
                } else {
                    result.put("message", "File is no longer available");
                }
                return result;
            }

            result.put("status", "success");
            result.put("fileName", sharedFile.getOriginalFileName());
            result.put("fileSize", sharedFile.getFormattedFileSize());
            result.put("contentType", sharedFile.getContentType());
            result.put("uploadTime", sharedFile.getUploadTime().toString());
            result.put("hasPassword", sharedFile.getPassword() != null);
            result.put("description", sharedFile.getDescription());
            result.put("currentDownloads", sharedFile.getCurrentDownloads());
            result.put("maxDownloads", sharedFile.getMaxDownloads());
            result.put("expiryTime", sharedFile.getExpiryTime() != null ? sharedFile.getExpiryTime().toString() : null);

        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "Error retrieving file info: " + e.getMessage());
        }

        return result;
    }

    public Map<String, Object> downloadFile(String shareToken, String password) {
        Map<String, Object> result = new HashMap<>();

        try {
            Optional<SharedFile> fileOpt = fileRepository.findByShareToken(shareToken);

            if (!fileOpt.isPresent()) {
                result.put("status", "error");
                result.put("message", "File not found");
                return result;
            }

            SharedFile sharedFile = fileOpt.get();

            // Check if file is accessible
            if (!sharedFile.isAccessible()) {
                result.put("status", "error");
                if (sharedFile.isExpired()) {
                    result.put("message", "File has expired");
                } else if (sharedFile.isDownloadLimitReached()) {
                    result.put("message", "Download limit reached");
                } else {
                    result.put("message", "File is no longer available");
                }
                return result;
            }

            // Check password if required
            if (sharedFile.getPassword() != null) {
                if (password == null || password.trim().isEmpty()) {
                    result.put("status", "error");
                    result.put("message", "Password required");
                    return result;
                }

                if (!verifyPassword(password.trim(), sharedFile.getPassword())) {
                    result.put("status", "error");
                    result.put("message", "Invalid password");
                    return result;
                }
            }

            // Get file resource
            Path filePath = Paths.get(uploadPath).resolve(sharedFile.getStoredFileName());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                result.put("status", "error");
                result.put("message", "File not found on disk");
                return result;
            }

            // Update download count
            sharedFile.setCurrentDownloads(sharedFile.getCurrentDownloads() + 1);
            fileRepository.save(sharedFile);

            result.put("status", "success");
            result.put("resource", resource);
            result.put("fileName", sharedFile.getOriginalFileName());
            result.put("contentType", sharedFile.getContentType());

        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "Download failed: " + e.getMessage());
        }

        return result;
    }

    public List<Map<String, Object>> getUserFiles(String uploaderIP) {
        List<Map<String, Object>> userFiles = new ArrayList<>();

        try {
            List<SharedFile> files = fileRepository.findByUploaderIPOrderByUploadTimeDesc(uploaderIP);

            for (SharedFile file : files) {
                Map<String, Object> fileInfo = new HashMap<>();
                fileInfo.put("shareToken", file.getShareToken());
                fileInfo.put("fileName", file.getOriginalFileName());
                fileInfo.put("fileSize", file.getFormattedFileSize());
                fileInfo.put("uploadTime", file.getUploadTime().toString());
                fileInfo.put("currentDownloads", file.getCurrentDownloads());
                fileInfo.put("maxDownloads", file.getMaxDownloads());
                fileInfo.put("expiryTime", file.getExpiryTime() != null ? file.getExpiryTime().toString() : null);
                fileInfo.put("isExpired", file.isExpired());
                fileInfo.put("isActive", file.isAccessible());
                fileInfo.put("hasPassword", file.getPassword() != null);
                fileInfo.put("description", file.getDescription());

                userFiles.add(fileInfo);
            }

        } catch (Exception e) {
            // Log error but return empty list
        }

        return userFiles;
    }

    private String generateShareToken() {
        StringBuilder token = new StringBuilder(32);
        for (int i = 0; i < 32; i++) {
            token.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return token.toString();
    }

    private String generateStoredFileName(String originalFileName) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String randomSuffix = generateShareToken().substring(0, 8);
        String extension = getFileExtension(originalFileName);
        return timestamp + "_" + randomSuffix + extension;
    }

    private String getFileExtension(String fileName) {
        if (fileName != null && fileName.lastIndexOf('.') > 0) {
            return fileName.substring(fileName.lastIndexOf('.'));
        }
        return "";
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    private boolean verifyPassword(String inputPassword, String storedHash) {
        return hashPassword(inputPassword).equals(storedHash);
    }
}
