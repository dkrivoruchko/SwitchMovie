package info.dvkr.switchmovie.ui.moviedetail

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import info.dvkr.switchmovie.R
import info.dvkr.switchmovie.data.viewmodel.moviedetail.MovieDetailEvent
import info.dvkr.switchmovie.data.viewmodel.moviedetail.MovieDetailModel
import info.dvkr.switchmovie.data.viewmodel.moviedetail.MovieDetailViewModel
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.utils.Utils
import info.dvkr.switchmovie.ui.BaseActivity
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.activity_movie_detail.*
import org.koin.android.viewmodel.ext.android.viewModel
import timber.log.Timber
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

    private val viewModel by viewModel<MovieDetailViewModel>()
    private var movieLiveData: LiveData<Movie>? = null


    private val dateParser = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    private val dateFormatter = SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_detail)

        viewModel.getMovieDetailModelLiveData().observe(this, Observer<MovieDetailModel> { movieDetailModel ->
            Timber.e("[${Utils.getLogPrefix(this)}] getMovieDetailModelLiveData: $movieDetailModel")

            if (movieDetailModel == null) return@Observer

            if (movieLiveData != movieDetailModel.movieLiveData) {

                movieLiveData?.removeObservers(this)
                movieLiveData = movieDetailModel.movieLiveData

                movieLiveData?.observe(this, Observer MovieObserver@{ movie ->
                    Timber.e("[${Utils.getLogPrefix(this)}] movieLiveData: $movie")

                    if (movie == null) return@MovieObserver
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
                        movieDetailReleaseDate.text = movie.releaseDate
//                    toEvent(MovieDetailView.ToEvent.OnError(ex))
                    }

                    movieDetailTitle.text = movie.title
                    movieDetailOverview.text = movie.overview

                    if (movie.isStar)
                        movieDetailStar.imageTintList =
                                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorAccent))
                    else
                        movieDetailStar.imageTintList =
                                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorWhite))
                })
            }

//            movieGridSwipeRefresh.isRefreshing = movieGridModel.isWorkInProgress

            movieDetailModel.error?.run {
                Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
            }
        })


        val movieId = intent.getIntExtra(MOVIE_ID, -1)
        viewModel.onViewEvent(MovieDetailEvent.ViewGetMovieById(movieId))
    }
}