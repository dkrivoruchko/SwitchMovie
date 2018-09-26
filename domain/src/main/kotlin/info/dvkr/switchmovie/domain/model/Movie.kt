package info.dvkr.switchmovie.domain.model


data class Movie(
    val id: Int,
    val posterPath: String,
    val title: String,
    val overview: String,
    val releaseDate: String,
    val voteAverage: String,
    val popularity: Float,
    val isStar: Boolean
) {
    override fun toString() = "Movie(id=$id)"
}