package com.watchora.contentservice.dto;

import com.watchora.contentservice.models.VideoStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.message.Message;
import org.hibernate.validator.constraints.UniqueElements;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieRequest {

    @NotNull @UniqueElements
    private String id;
    @NotBlank(message =  "Title is required ")
    private  String title;
    private  String description;

    @NotNull(message = " genre is required ")
    private  String genre;
    private  String director;
    private  String actor;


    private  String cast;
    private  String releaseYear;
    private  String rating;
    private  String language;
    private  String thumbnailUrl;

    private  String videoKey;
    private VideoStatus videoStatus;
    private  int durationMinutes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;



}
