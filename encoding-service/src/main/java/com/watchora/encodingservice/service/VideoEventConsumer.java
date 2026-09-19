package com.watchora.encodingservice.service;

import com.watchora.encodingservice.event.VideoUploadedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class VideoEventConsumer {
    private final EncodingService encodingService;


    /**
     * Listens to video.uplaoded kafka topic.
     * Triggered when video service uploads a raw video to s3.
     * FLOW :
     * Video Service -> s3 upload -> Kafka (video.uploaded)
     *                             -> This Consumer
     *                              -> EncodingService -> ffmpeg -> s3
     *                              -> kafka (video.encoded)
     */

    @KafkaListener(
            topics = "video.uploaded",
            groupId = "encoding-service-gorup"
    )
    public  void consumeVideoUploadedEvent(VideoUploadedEvent event ) {
        log.info("Consumed  video uploaded event : {}", event);
        try {
             encodingService.encodeVideo(event);

        }catch (Exception e) {
            log.warn("Failed to consume video uploaded event : {}", event);
        }
    }

}
