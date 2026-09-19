package com.watchora.encodingservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Consume from kafka video.uplaoded
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoEncodedEvent {

    private String movieId;
    private  String hlsUrl; // Master playlist URL for streaming
    private String masterPlaylistKey; // S3 key if master.m3u8
    private  boolean success;
    private  String errorMessage; // id encoding failed


}
