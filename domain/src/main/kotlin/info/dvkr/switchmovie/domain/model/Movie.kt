package info.dvkr.switchmovie.domain.model

import java.time.LocalDate


data class Movie(
    val id: Long,
    val posterPath: String,
    val title: String,
    val overview: String,
    val releaseDate: LocalDate,
    val voteAverage: String,
    val popularity: Float,
    val isStar: Boolean
) {
    override fun toString() = "Movie(id=$id)"

    companion object {
        val EMPTY = Movie(-1, "", "", "", LocalDate.now(), "", 0f, false)
    }
}