package info.dvkr.switchmovie.ui.moviedetail

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import info.dvkr.switchmovie.R
import info.dvkr.switchmovie.data.presenter.moviedetail.MovieDetailViewModel
import info.dvkr.switchmovie.data.presenter.moviegrid.Result
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.ui.BaseActivity
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.activity_movie_detail.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class MovieDetailActivity : BaseActivity() {

    companion object {
        private const val MOVIE_ID = "MOVIE_ID"

        @JvmStatic
        fun getStartIntent(context: Context, id: Int): Intent {
            return Intent(context, MovieDetailActivity::class.java).putExtra(MOVIE_ID, id)
        }
    }

    private val viewModel: MovieDetailViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(MovieDetailViewModel::class.java)
    }

    private val dateParser = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    private val dateFormatter = SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.wtf("MovieDetailActivity", "[${Thread.currentThread().name}] onCreate: Start")
        setContentView(R.layout.activity_movie_detail)
        viewModel.movieData.observe(this, Observer<Result<Movie>> {
            if (it?.error != null) {
                showError(it.error)
            } else if (it?.result != null) {
                onMovieReceive(it.result)
            }
        })

        val movieId = intent.getIntExtra(MOVIE_ID, -1)
        viewModel.getMovie(movieId)

        Log.wtf("MovieDetailActivity", "[${Thread.currentThread().name}] onCreate: End")
    }

    private fun onMovieReceive(movie: Movie?) {
        if (movie == null) {
            return
        }

        title = movie.title

        Glide.with(applicationContext)
                .load(movie.posterPath)
                .apply(RequestOptions.bitmapTransform(BlurTransformation(20)))
                .into(movieDetailBackground)

        Glide.with(applicationContext)
                .load(movie.posterPath)
                .into(movieDetailImage)

        movieDetailScore.text = movie.voteAverage
        movieDetailRating.text = "Unkown"

        try {
            val date = dateParser.parse(movie.releaseDate)
            movieDetailReleaseDate.text = dateFormatter.format(date)
        } catch (ex: ParseException) {
            showError(ex)
            movieDetailReleaseDate.text = movie.releaseDate
        }

        movieDetailTitle.text = movie.title
        movieDetailOverview.text = movie.overview
    }

    private fun showError(error: Throwable?) {
        Toast.makeText(applicationContext, error?.message, Toast.LENGTH_LONG).show()
    }

}