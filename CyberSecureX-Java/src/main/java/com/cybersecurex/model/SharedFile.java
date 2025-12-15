package com.cybersecurex.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "shared_files")
public class SharedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private String storedFileName;

    @Column(nullable = false)
    private String shareToken;

    private String password;

    @Column(nullable = false)
    private long fileSize;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private LocalDateTime uploadTime;

    private LocalDateTime expiryTime;

    private int maxDownloads;

    private int currentDownloads = 0;

    @Column(nullable = false)
    private String uploaderIP;

    private String description;

    @Column(nullable = false)
    private boolean active = true;

    // Constructors
    public SharedFile() {
    }

    public SharedFile(String originalFileName, String storedFileName, String shareToken,
            String password, long fileSize, String contentType, String uploaderIP) {
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
        this.shareToken = shareToken;
        this.password = password;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.uploaderIP = uploaderIP;
        this.uploadTime = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getStoredFileName() {
        return storedFileName;
    }

    public void setStoredFileName(String storedFileName) {
        this.storedFileName = storedFileName;
    }

    public String getShareToken() {
        return shareToken;
    }

    public void setShareToken(String shareToken) {
        this.shareToken = shareToken;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public LocalDateTime getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }

    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(LocalDateTime expiryTime) {
        this.expiryTime = expiryTime;
    }

    public int getMaxDownloads() {
        return maxDownloads;
    }

    public void setMaxDownloads(int maxDownloads) {
        this.maxDownloads = maxDownloads;
    }

    public int getCurrentDownloads() {
        return currentDownloads;
    }

    public void setCurrentDownloads(int currentDownloads) {
        this.currentDownloads = currentDownloads;
    }

    public String getUploaderIP() {
        return uploaderIP;
    }

    public void setUploaderIP(String uploaderIP) {
        this.uploaderIP = uploaderIP;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // Helper methods
    public boolean isExpired() {
        return expiryTime != null && LocalDateTime.now().isAfter(expiryTime);
    }

    public boolean isDownloadLimitReached() {
        return maxDownloads > 0 && currentDownloads >= maxDownloads;
    }

    public boolean isAccessible() {
        return active && !isExpired() && !isDownloadLimitReached();
    }

    public String getFormattedFileSize() {
        if (fileSize < 1024)
            return fileSize + " B";
        if (fileSize < 1024 * 1024)
            return String.format("%.1f KB", fileSize / 1024.0);
        if (fileSize < 1024 * 1024 * 1024)
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        return String.format("%.1f GB", fileSize / (1024.0 * 1024.0 * 1024.0));
    }
}
