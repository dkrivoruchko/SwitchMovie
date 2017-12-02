package info.dvkr.switchmovie.ui.moviegrid

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.StaggeredGridLayoutManager
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import info.dvkr.switchmovie.R
import info.dvkr.switchmovie.data.presenter.moviegrid.MovieGridPresenter
import info.dvkr.switchmovie.data.presenter.moviegrid.MovieGridView
import info.dvkr.switchmovie.ui.BaseActivity
import info.dvkr.switchmovie.ui.moviedetail.MovieDetailActivity
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.activity_movie_detail.*
import kotlinx.android.synthetic.main.activity_movie_grid.*
import timber.log.Timber
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class MovieGridActivity : BaseActivity(), MovieGridView {
    companion object {
        const val SELECTED_MOVIE_ID = "SELECTED_MOVIE_ID"
    }

    private val presenter: MovieGridPresenter by lazy {
        ViewModelProviders.of(this, presenterFactory).get(MovieGridPresenter::class.java)
    }

    private var currentRange: Pair<Int, Int> = Pair(0, 0)
    private var selectedMovieId = -1
    private val staggeredLayoutManager by lazy {
        StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL)
    }
    private lateinit var movieGridRecyclerViewAdapter: MovieGridRecyclerViewAdapter

    private val dateParser = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    private val dateFormatter = SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH)

    override fun toEvent(toEvent: MovieGridView.ToEvent) {
        runOnUiThread {
            Timber.d("[${this.javaClass.simpleName}#${this.hashCode()}@${Thread.currentThread().name}] toEvent: $toEvent")

            when (toEvent) {
                is MovieGridView.ToEvent.OnRefresh -> {
                    movieGridSwipeRefresh.isRefreshing = toEvent.isRefreshing
                }

                is MovieGridView.ToEvent.OnMovieGridItemsRange -> {
                    currentRange = toEvent.range
                    movieGridRecyclerViewAdapter.updateMovieList(toEvent.range, toEvent.list)

                    if (selectedMovieId <= 0) {
                        presenter.offer(MovieGridView.FromEvent.GetMovieById(toEvent.list.first().id))
                    }
                }

                is MovieGridView.ToEvent.OnMovie -> {
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
                        toEvent(MovieGridView.ToEvent.OnError(ex))
                    }

                    movieDetailTitle.text = toEvent.movie.title
                    movieDetailOverview.text = toEvent.movie.overview
                }

                is MovieGridView.ToEvent.OnError -> {
                    Toast.makeText(applicationContext, toEvent.error.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_grid)

        movieGridRecyclerView.setHasFixedSize(true)
        movieGridRecyclerView.layoutManager = staggeredLayoutManager

        movieGridRecyclerViewAdapter = MovieGridRecyclerViewAdapter(
                { presenter.offer(MovieGridView.FromEvent.StarMovieById(it.id)) },
                {
                    selectedMovieId = it.id
                    presenter.offer(MovieGridView.FromEvent.GetMovieById(it.id))
                },
                { startActivity(MovieDetailActivity.getStartIntent(applicationContext, it.id)); true },
                { presenter.offer(MovieGridView.FromEvent.GetNext(currentRange.second)) })

        movieGridRecyclerViewAdapter.setHasStableIds(true)
        movieGridRecyclerView.adapter = movieGridRecyclerViewAdapter

        movieGridSwipeRefresh.setOnRefreshListener {
            presenter.offer(MovieGridView.FromEvent.RefreshItems)
        }

        presenter.attach(this)

        if (savedInstanceState == null) {
            presenter.offer(MovieGridView.FromEvent.GetCache)
            presenter.offer(MovieGridView.FromEvent.RefreshItems)
        } else {
            selectedMovieId = savedInstanceState.getInt(SELECTED_MOVIE_ID)
            presenter.offer(MovieGridView.FromEvent.GetCache)
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putInt(SELECTED_MOVIE_ID, selectedMovieId)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        presenter.detach()
        super.onDestroy()
    }
}