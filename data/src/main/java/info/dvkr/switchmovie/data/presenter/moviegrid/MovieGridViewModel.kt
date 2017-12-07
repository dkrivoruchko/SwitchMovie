package info.dvkr.switchmovie.data.presenter.moviegrid

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.repository.MovieRepository
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch

data class Result<T> constructor(val error: Throwable?, val result: T?)
data class MovieRange constructor(var range: Pair<Int, Int>, var movies: MutableList<Movie>)
class MovieGridViewModel internal constructor(private val movieRepository: MovieRepository)
    : ViewModel() {

    var refreshing = MutableLiveData<Boolean>()
        private set
    var movies = MutableLiveData<Result<MovieRange>>()
        private set
    var starMovie = MutableLiveData<Result<Int>>()
        private set
    var currentMovie = MutableLiveData<Result<Movie>>()
        private set

    private var currentRange: Pair<Int, Int> = Pair(0, 0)

    fun refreshItems() {
        getMoviesFromIndex(0)
    }

    fun getNextPage() {
        getMoviesFromIndex(currentRange.second)
    }

    private fun getMoviesFromIndex(index: Int) {
        launch(UI) {
            refreshing.value = true
        }
        launch {
            try {
                val response = CompletableDeferred<MovieRepository.MoviesOnRange>()
                movieRepository.send(MovieRepository.Request.GetMoviesFromIndex(index, response))
                val moviesOnRange = response.await()
                val moviesList = moviesOnRange.moviesList
                currentRange = moviesOnRange.range
                if (index == 0) {
                    setValueOnUiThread(movies, Result(null,
                            MovieRange(moviesOnRange.range, moviesOnRange.moviesList.toMutableList())))
                } else {
                    val allMovies = movies.value?.result?.movies
                    if (allMovies != null && (allMovies.size < currentRange.first)) {
                        throw IllegalStateException("Wrong Range")
                    }
                    if (allMovies != null) {
                        allMovies.addAll(moviesList)
                        setValueOnUiThread(movies, Result(null,
                                MovieRange(moviesOnRange.range, allMovies)))
                    }
                }
            } catch (e: Throwable) {
                setValueOnUiThread(movies, Result<MovieRange>(e, null))
            }
        }
    }

    fun getMovieById(id: Int) {
        refreshing.value = true
        launch {
            try {
                val response = CompletableDeferred<Movie>()
                movieRepository.send(MovieRepository.Request.GetMovieById(id, response))
                val movie = response.await()
                setValueOnUiThread(currentMovie, Result(null, movie))
            } catch (t: Throwable) {
                setValueOnUiThread(currentMovie, Result<Movie>(t, null))
            }
        }
    }

    fun starMovie(id: Int) {
        refreshing.value = true
        launch {
            try {
                delay(5000)
                val response = CompletableDeferred<Int>()
                movieRepository.send(MovieRepository.Request.StarMovieById(id, response))
                val updatedMovieIndex = response.await()
                getMoviesFromIndex(updatedMovieIndex)
                setValueOnUiThread(starMovie, Result(null, id))
            } catch (e: Throwable) {
                setValueOnUiThread(starMovie, Result<Int>(e, null))
            }
        }
    }

    private fun <T> setValueOnUiThread(field: MutableLiveData<Result<T>>, result: Result<T>) {
        launch(UI) {
            refreshing.value = false
            field.value = result
        }
    }

}