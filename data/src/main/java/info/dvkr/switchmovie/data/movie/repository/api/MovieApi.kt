package info.dvkr.switchmovie.data.movie.repository.api


import android.support.annotation.Keep
import com.squareup.moshi.Json
import kotlinx.coroutines.experimental.Deferred
import retrofit2.http.GET
import retrofit2.http.Query

object MovieApi {

    @Keep data class ServerMovie(
        @Json(name = "id") val id: Int,
        @Json(name = "poster_path") val posterPath: String,
        @Json(name = "title") val title: String,
        @Json(name = "overview") val overview: String,
        @Json(name = "release_date") val releaseDate: String,
        @Json(name = "vote_average") val voteAverage: String
    ) {
        override fun toString() = "ServerMovie(id=$id)"
    }

    @Keep data class ServerList(
        @Json(name = "page") val page: Int,
        @Json(name = "total_results") val totalResults: Int,
        @Json(name = "total_pages") val totalPages: Int,
        @Json(name = "results") val items: List<ServerMovie>
    )

    const val MOVIES_PER_PAGE = 20

    @Keep interface Service {
        @GET("movie/now_playing")
        @Keep fun getNowPlaying(
            @Query("api_key") apiKey: String, @Query("page") page: Int = 1
        ): Deferred<ServerList>
    }

}