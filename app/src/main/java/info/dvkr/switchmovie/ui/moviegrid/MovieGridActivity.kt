package info.dvkr.switchmovie.ui.moviegrid

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.res.ColorStateList
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.StaggeredGridLayoutManager
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import info.dvkr.switchmovie.R
import info.dvkr.switchmovie.data.presenter.moviegrid.MovieGridViewModel
import info.dvkr.switchmovie.data.presenter.moviegrid.MovieRange
import info.dvkr.switchmovie.data.presenter.moviegrid.Result
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.ui.BaseActivity
import info.dvkr.switchmovie.ui.moviedetail.MovieDetailActivity
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.activity_movie_detail.*
import kotlinx.android.synthetic.main.activity_movie_grid.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class MovieGridActivity : BaseActivity() {
    companion object {
        const val SELECTED_MOVIE_ID = "SELECTED_MOVIE_ID"
        const val NEED_REFRESH = "NEED_REFRESH"
    }

    private val viewModel: MovieGridViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(MovieGridViewModel::class.java)
    }

    private val staggeredLayoutManager by lazy {
        StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL)
    }
    private lateinit var movieGridRecyclerViewAdapter: MovieGridRecyclerViewAdapter

    private val dateParser = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    private val dateFormatter = SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH)

    private var selectedMovieId = -1
    private var needRefresh = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_grid)

        observeOnEvents()
        setupViews()

        needRefresh = savedInstanceState?.getBoolean(NEED_REFRESH) ?: true
        if (needRefresh) {
            viewModel.refreshItems()
        }

        selectedMovieId = savedInstanceState?.getInt(SELECTED_MOVIE_ID) ?: -1
        if (selectedMovieId > 0) {
            viewModel.getMovieById(selectedMovieId)
        }
    }

    private fun observeOnEvents() {
        viewModel.movies.observe(this, Observer<Result<MovieRange>> {
            if (it?.error != null) {
                showError(it.error?.message)
            } else if (it?.result != null) {
                needRefresh = false
                movieGridRecyclerViewAdapter.setMovieList(it.result!!.movies)
                if (selectedMovieId < 0) {
                    viewModel.getMovieById(it.result!!.movies.first().id)
                }
            }
        })

        viewModel.refreshing.observe(this, Observer<Boolean> {
            movieGridSwipeRefresh.isRefreshing = it ?: false
        })

        viewModel.currentMovie.observe(this, Observer<Result<Movie>> {
            if (it?.error != null) {
                showError(it.error?.message)
            } else if (it?.result != null) {
                setupSelectedMovie(it.result!!)
            }
        })

        viewModel.starMovie.observe(this, Observer<Result<Int>> {
            if (it?.error != null) {
                showError(it.error?.message)
            } else if (it?.result != null && selectedMovieId == it.result) {
                viewModel.getMovieById(it.result!!)
            }
        })
    }

    private fun setupViews() {
        movieGridRecyclerView.setHasFixedSize(true)
        movieGridRecyclerView.layoutManager = staggeredLayoutManager

        movieGridRecyclerViewAdapter = MovieGridRecyclerViewAdapter(
                { viewModel.starMovie(it.id) },
                ::onMovieClicked,
                { startActivity(MovieDetailActivity.getStartIntent(applicationContext, it.id)); true },
                viewModel::getNextPage)

        movieGridRecyclerViewAdapter.setHasStableIds(true)
        movieGridRecyclerView.adapter = movieGridRecyclerViewAdapter

        movieGridSwipeRefresh.setOnRefreshListener {
            viewModel.refreshItems()
        }
    }

    private fun onMovieClicked(movie: Movie) {
        selectedMovieId = movie.id
        viewModel.getMovieById(movie.id)
    }

    private fun setupSelectedMovie(movie: Movie) {
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
            showError(ex.message)
        }

        movieDetailTitle.text = movie.title
        movieDetailOverview.text = movie.overview

        if (movie.isStar)
            movieDetailStar.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorAccent))
        else
            movieDetailStar.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorWhite))
    }

    private fun showError(message: String?) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putInt(SELECTED_MOVIE_ID, selectedMovieId)
        outState?.putBoolean(NEED_REFRESH, needRefresh)
        super.onSaveInstanceState(outState)
    }

}