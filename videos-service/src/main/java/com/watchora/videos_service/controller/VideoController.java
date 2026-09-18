package com.watchora.videos_service.controller;

import com.watchora.videos_service.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping
@Slf4j
@RequiredArgsConstructor

public class VideoController {
    private final VideoService videoService;

    /**
     * uploade video file for movie
     * Accepts multipart file uploade
     * POST /api/v1/videos/upload/{movieId}
     *
     */
    @PostMapping("/upload/{movieId}")
    public ResponseEntity<String> uploadeVideo(@PathVariable String movieId,
                                               @RequestParam ("file") MultipartFile file) throws IOException {
        log.info("Video request for movie : {} file size : {}MB", movieId, file.getSize() / (1024 * 1024));
        if(file.isEmpty()){
            return  ResponseEntity.badRequest().body("File is empty");

        }
        String videoKey = videoService.uploadVideo(movieId, file);

        return  ResponseEntity.ok("Video uploaded successfully! key :  "+videoKey + " - Encoding started " +
                "automatically by kafka ");

    }

}
