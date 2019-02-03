package info.dvkr.switchmovie.data.repository.api


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import info.dvkr.switchmovie.domain.model.Movie
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

object MovieApi {
    const val MOVIES_PER_PAGE = 20

    @JsonClass(generateAdapter = true)
    class MovieRaw(
        @Json(name = "id") val id: Int,
        @Json(name = "poster_path") val posterPath: String?,
        @Json(name = "title") val title: String,
        @Json(name = "overview") val overview: String,
        @Json(name = "release_date") val releaseDate: String, // "2018-09-13"
        @Json(name = "vote_average") val voteAverage: String, // 5.6
        @Json(name = "popularity") val popularity: Float      // 219.771
    ) {
        fun toMovie(apiBaseImageUrl: String): Movie = Movie(
            id,
            posterPath?.let { apiBaseImageUrl + it } ?: "",
            title,
            overview,
            releaseDate,
            voteAverage,
            popularity,
            false
        )

        override fun toString() = "MovieRaw(id=$id)"
    }

    @JsonClass(generateAdapter = true)
    class MovieListRaw(
        @Json(name = "page") val page: Int = 0,
        @Json(name = "total_results") val totalResults: Int = 0,
        @Json(name = "total_pages") val totalPages: Int = 0,
        @Json(name = "results") val items: List<MovieRaw> = emptyList()
    )

    interface Service {

        @GET("movie/now_playing")
//        @Headers("Cache-Control: private, max-age=600, max-stale=600")
        fun getNowPlaying(
            @Query("api_key") apiKey: String,
            @Query("page") page: Int
        ): Call<MovieListRaw>
    }
}