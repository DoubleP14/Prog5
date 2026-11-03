package hu.mik.pte.prog5.filmtar.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Movie {

    private Long id;

    // 6-1
    @NotBlank(message = "{validation.notBlank}")
    private String title;

    // 6-1
    @NotBlank(message = "{validation.notBlank}")
    private String director;

    // 6-1
    @NotNull(message = "{validation.notNull}")
    @Min(value = 1888, message = "{validation.year}")
    @Max(value = 2100, message = "{validation.year}")
    private Integer releaseYear;

    private String owner;

    public Movie(String title, String director, Integer releaseYear, String owner) {
        this.title = title;
        this.director = director;
        this.releaseYear = releaseYear;
        this.owner = owner;
    }
}
