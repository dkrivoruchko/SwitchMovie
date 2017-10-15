package info.dvkr.switchmovie.domain.model


data class Movie(val id: Int,
                 val posterPath: String,
                 val originalTitle: String,
                 val overview: String,
                 val releaseDate: String,
                 val voteAverage: String
)