package com.cybersecurex.controller;

import com.cybersecurex.service.FileShareService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Controller
public class FileShareController {

    @Autowired
    private FileShareService fileShareService;

    // Upload API
    @PostMapping("/api/files/upload")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "maxDownloads", required = false) Integer maxDownloads,
            @RequestParam(value = "expiryHours", required = false) Integer expiryHours,
            @RequestParam(value = "description", required = false) String description,
            HttpServletRequest request) {

        String uploaderIP = getClientIP(request);
        Map<String, Object> result = fileShareService.uploadFile(file, password, maxDownloads, expiryHours, description,
                uploaderIP);
        return ResponseEntity.ok(result);
    }

    // File info API
    @GetMapping("/api/files/info/{shareToken}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getFileInfo(@PathVariable String shareToken) {
        Map<String, Object> result = fileShareService.getFileInfo(shareToken);
        return ResponseEntity.ok(result);
    }

    // User files API
    @GetMapping("/api/files/my-files")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getUserFiles(HttpServletRequest request) {
        String uploaderIP = getClientIP(request);
        List<Map<String, Object>> files = fileShareService.getUserFiles(uploaderIP);
        return ResponseEntity.ok(files);
    }

    // Download page
    @GetMapping("/download/{shareToken}")
    public String downloadPage(@PathVariable String shareToken, Model model) {
        model.addAttribute("shareToken", shareToken);
        return "download";
    }

    // Download file
    @PostMapping("/api/files/download/{shareToken}")
    @ResponseBody
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String shareToken,
            @RequestParam(value = "password", required = false) String password) {

        Map<String, Object> result = fileShareService.downloadFile(shareToken, password);

        if ("success".equals(result.get("status"))) {
            Resource resource = (Resource) result.get("resource");
            String fileName = (String) result.get("fileName");
            String contentType = (String) result.get("contentType");

            return ResponseEntity.ok()
                    .contentType(
                            MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0];
        }

        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }

        return request.getRemoteAddr();
    }
}
