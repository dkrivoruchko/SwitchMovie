package info.dvkr.switchmovie.data.movie.repository.api

import com.google.gson.annotations.SerializedName
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    data class ServerMovie(
            @SerializedName("id") val id: Int,
            @SerializedName("poster_path") val posterPath: String,
            @SerializedName("title") val title: String,
            @SerializedName("overview") val overview: String,
            @SerializedName("release_date") val releaseDate: String,
            @SerializedName("vote_average") val voteAverage: String
    )

    data class ServerList(
            @SerializedName("page") val page: Int,
            @SerializedName("total_results") val totalResults: Int,
            @SerializedName("total_pages") val totalPages: Int,
            @SerializedName("results") val items: List<ServerMovie>
    )

    @GET("movie/now_playing")
    fun getNowPlaying(@Query("api_key") apiKey: String, @Query("page") page: Int = 1): Single<ServerList>
}