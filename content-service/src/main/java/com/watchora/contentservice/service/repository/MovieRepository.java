package com.watchora.contentservice.service.repository;

import com.watchora.contentservice.models.Genre;
import com.watchora.contentservice.models.Language;
import com.watchora.contentservice.models.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, String> {

    List<Movie> findByGenre(Genre genre);
    List<Movie> findByTitle(String title);
    List<Movie> findByTitleAndLangauge(String title, Language language);
    List<Movie> findByLangauge(Language language);
}
