package com.watchora.streamingservice.controller;


import com.watchora.streamingservice.Service.StreamingService;
import com.watchora.streamingservice.dto.StreamingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stream")
@Slf4j
@RequiredArgsConstructor
public class StreamingController {

    private  final StreamingService streamingService;
    private  final RedisTemplate<String, String> redisTemplate;

    private  static  final  String MASTER_PLAYLIST_kEY_PREFIX = "streaming:playlist:";



    @GetMapping("/{movieId}")
    public ResponseEntity<StreamingResponse> getStreamingUrl(
            @PathVariable String movieId
    ) {
        log.info("Getting streaming url for movieId {}", movieId);

        // GET master playlist Key from redis

        String playlistKey = redisTemplate.opsForValue().get(MASTER_PLAYLIST_kEY_PREFIX + movieId);
        if(playlistKey == null){
            return ResponseEntity.notFound().build();
        }

       StreamingResponse response=  streamingService.getStreamingUrl(movieId, playlistKey);

        return  ResponseEntity.ok(response);
    }

    /**
     * Server signed s3u8 playlist content
     * called by MLS player for each Quality playlist.     *
     * @param movieId @description
     * @param path @description
     * @return @description
     */
    @GetMapping("/{movieId}/playlist")
    public ResponseEntity<String> getSignedPlaylist(
            @PathVariable String movieId,
            @RequestParam String path
    ) {
        String signedPlaylist = streamingService.getSignedPlaylist(movieId, path);

        return  ResponseEntity.ok().header(
                "Content-Type", "application/x.megURl"
        ).body(signedPlaylist);
    }

}
