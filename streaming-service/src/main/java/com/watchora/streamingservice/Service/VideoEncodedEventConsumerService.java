package com.watchora.streamingservice.Service;

import com.watchora.streamingservice.event.VideoEncodedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@RequiredArgsConstructor
public class VideoEncodedEventConsumerService {

    private final RedisTemplate<String, String> redisTemplate;
    private static  final  String MASTER_PLAYLIST_KEY_PREFIX = "streaming:playlist:";

    /**
     * Listens to video Encoded kafka  topic
     * Stores maser playList key in redis when encoding is complete.
     * This  allow  StreamingService to quickly find the playlist key by movie
     */

    @KafkaListener(
            topics = "video.encoded",
            groupId = "streaming-service-group"
    )
    public  void  consumeVideoEncodedEvent (VideoEncodedEvent event){
        log.info("Consumed video encoded event for movie : {} success : {}", event.getMovieId(), event.isSuccess());
        if(event.isSuccess()){
            //Store Master playlist key in redis
            String cacheKey = MASTER_PLAYLIST_KEY_PREFIX + event.getMovieId();
            redisTemplate.opsForList().rightPush( cacheKey, event.getMaserPlaylistKey());
            log.info("Master playlist key stored in Redis for movie : {} ", event.getMovieId());


        }else{
            log.error("Error while consuming video encoded event for movie : {}", event.getMovieId());
        }
    }

}
