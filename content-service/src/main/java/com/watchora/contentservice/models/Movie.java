package com.watchora.contentservice.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "movies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Movie{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private  String id ;

    @Column(nullable = false)
    private  String title;

    private  Language language;
    @Column(length = 1000)
    private  String description;

    @Enumerated(EnumType.STRING)
    private  Genre genre;

    private  String director;
    private  String actor;
    private String cast;
    private  String releaseYear;
    private  double rating;
    private  String ThumbnailUrl;
    private  int durationMinutes;

// s3 key for the videos file
    private String videoKey;

//    HLS master play list  url for streaming
    private  String hlsUrl;

    //! status of video processing
    @Enumerated(EnumType.STRING)
    private VideoStatus videoStatus;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;




}
