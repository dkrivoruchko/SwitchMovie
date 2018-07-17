package info.dvkr.switchmovie.data.movie.repository.api

import info.dvkr.switchmovie.domain.BuildConfig
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.utils.Utils
import timber.log.Timber


class MovieApiService(
    private val movieApi: MovieApi.Service,
    private val apiKey: String
) {

    suspend fun getMovies(page: Int): List<Movie> {
        Timber.d("[${Utils.getLogPrefix(this)}] getMovies.page: $page")

        return movieApi.getNowPlaying(apiKey, page).await().items
            .map {
                Movie(
                    it.id,
                    BuildConfig.BASE_IMAGE_URL + it.posterPath,
                    it.title,
                    it.overview,
                    it.releaseDate,
                    it.voteAverage,
                    false
                )
            }
    }
}