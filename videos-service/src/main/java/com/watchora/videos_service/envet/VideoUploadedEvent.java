package com.watchora.videos_service.envet;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event published to kafka when a video is uploaded to S3.
 * Encoding Service consume this to start ffmpeg processing.
 *
 *
 * TOPIC : video.uploaded
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoUploadedEvent {
    private String movieId;
    private String videoKey;
    private String bucketName;
    private String originalFilename;
    private long fileSizeInBytes;

}
