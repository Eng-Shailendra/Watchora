package com.watchora.videos_service.service;

import com.watchora.videos_service.envet.VideoUploadedEvent;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class VideoService {
    private  final S3Client s3Client;
    private  final KafkaTemplate<String, VideoUploadedEvent> kafkaTemplate;

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    private  static  final String Video_Uploaded_Topic = "video.uploaded";

    /**
     * upload video to AWS s3 and public videoUploadedEvent to kafka
     * ------------------>
     * Flow
     * 1 Receive multipart video  file
     * 2 Generate unique s3 key
     * 3 Uploade to S3
     * 4 Public VideoUploadeEvent to Kafka
     * 5 Encoding Servic pick up start ffmpeg
     */

    public  String uploadVideo( String movieId,  MultipartFile file)throws IOException {
        log.info("Uploading video : {}", file.getOriginalFilename());

        // Generate unique S3 key for row video
        // Format : row/movId/uuid.fileName

        String videoKey = "row/" + movieId + "/" + UUID.randomUUID() +"_"+file.getOriginalFilename();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName).key(videoKey).contentType(file.getContentType()).contentLength(file.getSize()).build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        log.info("Uploaded video to S3 : {}", videoKey);

        // Public event to Kafka
        // Encoding Service will consume this and Start ffmpeg processing

        VideoUploadedEvent videoUploadedEvent = new VideoUploadedEvent(movieId, videoKey, bucketName, file.getOriginalFilename(),  file.getSize());

        kafkaTemplate.send(Video_Uploaded_Topic, movieId, videoUploadedEvent);
        log.info("Uploaded video to Kafka : {}", videoKey);
    }



}
