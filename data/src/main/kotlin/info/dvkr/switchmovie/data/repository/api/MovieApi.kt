package info.dvkr.switchmovie.data.repository.api


import info.dvkr.switchmovie.domain.model.Movie
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.SkipCallbackExecutor
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import java.time.LocalDate

object MovieApi {
    const val MOVIES_PER_PAGE = 20

    @Serializable
    data class MovieRaw(
        @SerialName("id") val id: Long,
        @SerialName("poster_path") val posterPath: String?,
        @SerialName("title") val title: String,
        @SerialName("overview") val overview: String,
        @SerialName("release_date") val releaseDate: String, // "2018-09-13"
        @SerialName("vote_average") val voteAverage: String, // 5.6
        @SerialName("popularity") val popularity: Float      // 219.771
    ) {
        fun toMovie(apiBaseImageUrl: String): Movie = Movie(
            id,
            posterPath?.let { apiBaseImageUrl + it } ?: "",
            title,
            overview,
            LocalDate.parse(releaseDate),
            voteAverage,
            popularity,
            false
        )

        override fun toString() = "MovieRaw(id=$id)"
    }

    @Serializable
    data class MovieListRaw(
        @SerialName("page") val page: Int = 0,
        @SerialName("total_results") val totalResults: Int = 0,
        @SerialName("total_pages") val totalPages: Int = 0,
        @SerialName("results") val items: List<MovieRaw> = emptyList()
    )

    interface Service {

        @SkipCallbackExecutor
        @Headers("Cache-Control: no-cache")
        @GET("movie/now_playing")
        suspend fun getNowPlaying(
            @Query("api_key") apiKey: String,
            @Query("page") page: Int
        ): MovieListRaw
    }
}