package com.watchora.contentservice.models;

/**
 * TRACK THE VIDEO PROCESSING LIFE_CYCLE
 *
 * FLOW
 * penging --> uploading -->encoding--> ready---> failed
 * */
public enum VideoStatus {
    PENDING,
    UPLOADING,
    UPLOADED,
    ENCODED,
    READY,
    FAILED,

}
