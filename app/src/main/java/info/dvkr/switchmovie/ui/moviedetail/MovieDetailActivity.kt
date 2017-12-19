package info.dvkr.switchmovie.ui.moviedetail

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import info.dvkr.switchmovie.R
import info.dvkr.switchmovie.data.presenter.BaseView
import info.dvkr.switchmovie.data.presenter.moviedetail.MovieDetailPresenter
import info.dvkr.switchmovie.data.presenter.moviedetail.MovieDetailView
import info.dvkr.switchmovie.ui.BaseActivity
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.activity_movie_detail.*
import timber.log.Timber
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class MovieDetailActivity : BaseActivity(), MovieDetailView {

  companion object {
    private const val MOVIE_ID = "MOVIE_ID"

    @JvmStatic
    fun getStartIntent(context: Context, id: Int): Intent {
      return Intent(context, MovieDetailActivity::class.java).putExtra(MOVIE_ID, id)
    }
  }

  private val presenter: MovieDetailPresenter by lazy {
    ViewModelProviders.of(this, presenterFactory).get(MovieDetailPresenter::class.java)
  }

  private val dateParser = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
  private val dateFormatter = SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH)

  override fun toEvent(toEvent: BaseView.BaseToEvent) {
    Timber.d("[${Thread.currentThread().name} @${this.hashCode()}] toEvent: $toEvent")

    when (toEvent) {
      is MovieDetailView.ToEvent.OnMovie -> {
        title = toEvent.movie.title

        Glide.with(applicationContext)
            .load(toEvent.movie.posterPath)
            .apply(RequestOptions.bitmapTransform(BlurTransformation(20)))
            .into(movieDetailBackground)

        Glide.with(applicationContext)
            .load(toEvent.movie.posterPath)
            .into(movieDetailImage)

        movieDetailScore.text = toEvent.movie.voteAverage
        movieDetailRating.text = "Unkown"

        try {
          val date = dateParser.parse(toEvent.movie.releaseDate)
          movieDetailReleaseDate.text = dateFormatter.format(date)
        } catch (ex: ParseException) {
          movieDetailReleaseDate.text = toEvent.movie.releaseDate
          toEvent(MovieDetailView.ToEvent.OnError(ex))
        }

        movieDetailTitle.text = toEvent.movie.title
        movieDetailOverview.text = toEvent.movie.overview
      }

      is MovieDetailView.ToEvent.OnError -> {
        Toast.makeText(applicationContext, toEvent.error.message, Toast.LENGTH_LONG).show()
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_movie_detail)
    presenter.attach(this)

    val movieId = intent.getIntExtra(MOVIE_ID, -1)
    presenter.offer(MovieDetailView.FromEvent.GetMovieById(movieId))

  }

  override fun onDestroy() {
    presenter.detach()
    super.onDestroy()
  }
}