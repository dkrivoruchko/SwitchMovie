package info.dvkr.switchmovie.data.movie.repository.api


import com.squareup.moshi.Json
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

object MovieApi {

    class ServerMovie(
            @Json(name = "id") var id: Int,
            @Json(name = "poster_path") var posterPath: String,
            @Json(name = "title") var title: String,
            @Json(name = "overview") var overview: String,
            @Json(name = "release_date") var releaseDate: String,
            @Json(name = "vote_average") var voteAverage: String
    ) {
        override fun toString() = "ServerMovie(id=$id)"
    }

    class ServerList(
            @Json(name = "page") var page: Int,
            @Json(name = "total_results") var totalResults: Int,
            @Json(name = "total_pages") var totalPages: Int,
            @Json(name = "results") var items: List<ServerMovie>
    )

    const val MOVIES_PER_PAGE = 20

    interface Service {
        @GET("movie/now_playing")
        fun getNowPlaying(@Query("api_key") apiKey: String,
                          @Query("page") page: Int = 1): Call<ServerList>
    }

}