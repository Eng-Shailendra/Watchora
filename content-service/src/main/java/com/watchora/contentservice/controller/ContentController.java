package com.watchora.contentservice.controller;

import com.watchora.contentservice.dto.MovieRequest;
import com.watchora.contentservice.dto.MovieResponse;
import com.watchora.contentservice.models.Genre;
import com.watchora.contentservice.models.Language;
import com.watchora.contentservice.service.ContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/movies")
@Slf4j
@RequiredArgsConstructor
public class ContentController {
    private  final ContentService contentService;


//    add new movie to catalog
    @PostMapping("/")
    public ResponseEntity<MovieResponse> addMovie (
            @Valid @RequestBody MovieRequest movieRequest){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(contentService.addMovie(movieRequest));
    }


    // get all movie
    @GetMapping("/")
    public ResponseEntity<List<MovieResponse>> getAllMovies (){
        return ResponseEntity.ok(contentService.getAllMovies());
    }

    //get movies by Genre
    @GetMapping("/genre/{genre}")
    public ResponseEntity<List<MovieResponse>> getMoviesByGenre (@PathVariable Genre genre){
        return  ResponseEntity.ok(contentService.getMoviesByGenre(genre));
    }

    // Get all movie by id
    @GetMapping("/{id}")
    public ResponseEntity<MovieResponse> getMoviesById (@PathVariable String movieId){
        return  ResponseEntity.ok(contentService.getMoviesById(movieId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<MovieResponse>> searchMoviesByTitleAndLangauge (@RequestParam String title,
                                                              @RequestParam Language language){
        return ResponseEntity.ok(contentService.searchMoviesByTitleAndLangauge(title, language));
    }

    @GetMapping("/serach")
    public  ResponseEntity<List<MovieResponse>> searchMoviesByTitle( @RequestParam String title){
        return ResponseEntity.ok(contentService.searchMoviesByTitle(title));
    }

    @GetMapping("/serach")
    public  ResponseEntity<List<MovieResponse>> searchMoviesByLangauge(@RequestParam Language language){
        return ResponseEntity.ok(contentService.searchMoviesByLangauge(language));
    }

}
