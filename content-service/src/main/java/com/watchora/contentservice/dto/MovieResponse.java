package com.watchora.contentservice.dto;


import com.watchora.contentservice.models.Genre;
import com.watchora.contentservice.models.VideoStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieResponse {


    private  String title;
    private  String description;


    private Genre genre;
    private  String director;

    private  String cast;
    private  String releaseYear;
    private  String rating;
    private  String language;
    private  String thumbnailUrl;
    private  int durationMinutes;
    private  String videoKey;
    private  String hlsUrl;
    private VideoStatus VideoStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
