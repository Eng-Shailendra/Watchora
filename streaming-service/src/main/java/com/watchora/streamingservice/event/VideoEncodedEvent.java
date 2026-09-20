package com.watchora.streamingservice.event;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;


/**
 * consume from kafka  topic.encoded
 */
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class VideoEncodedEvent {

    private  String movieId;
    private  String hlsUrl;
    private String maserPlaylistKey;
    private boolean success;
    private String errorMessage;

}
