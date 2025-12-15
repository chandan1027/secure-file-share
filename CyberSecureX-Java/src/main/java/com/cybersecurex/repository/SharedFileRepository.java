package com.cybersecurex.repository;

import com.cybersecurex.model.SharedFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SharedFileRepository extends JpaRepository<SharedFile, Long> {

    Optional<SharedFile> findByShareToken(String shareToken);

    List<SharedFile> findByUploaderIPOrderByUploadTimeDesc(String uploaderIP);

    @Query("SELECT f FROM SharedFile f WHERE f.expiryTime < :now AND f.active = true")
    List<SharedFile> findExpiredFiles(LocalDateTime now);

    @Query("SELECT f FROM SharedFile f WHERE f.active = true ORDER BY f.uploadTime DESC")
    List<SharedFile> findActiveFiles();

    long countByUploaderIP(String uploaderIP);
}
