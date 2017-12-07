package info.dvkr.switchmovie.data.presenter.moviedetail

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import info.dvkr.switchmovie.data.presenter.moviegrid.Result
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.repository.MovieRepository
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

class MovieDetailViewModel constructor(private val movieRepository: MovieRepository)
    : ViewModel() {

    var movieData = MutableLiveData<Result<Movie>>()
        private set

    fun getMovie(id: Int) {
        launch {
            try {
                val response = CompletableDeferred<Movie>()
                movieRepository.send(MovieRepository.Request.GetMovieById(id, response))
                val movie = response.await()
                launch(UI) {
                    movieData.value = Result(null, movie)
                }
            } catch (t : Throwable) {
                launch(UI) {
                    movieData.value = Result(t, null)
                }
            }
        }
    }

}