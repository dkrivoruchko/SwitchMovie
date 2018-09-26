package info.dvkr.switchmovie.ui.moviegrid

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.Transformations
import android.content.res.ColorStateList
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.github.andrewlord1990.snackbarbuilder.SnackbarBuilder
import info.dvkr.switchmovie.R
import info.dvkr.switchmovie.data.viewmodel.moviegrid.MovieGridEvent
import info.dvkr.switchmovie.data.viewmodel.moviegrid.MovieGridModel
import info.dvkr.switchmovie.data.viewmodel.moviegrid.MovieGridViewItem
import info.dvkr.switchmovie.data.viewmodel.moviegrid.MovieGridViewModel
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.utils.getTag
import info.dvkr.switchmovie.ui.BaseActivity
import info.dvkr.switchmovie.ui.moviedetail.MovieDetailActivity
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.activity_movie_grid.*
import kotlinx.android.synthetic.main.movie_item.*
import org.koin.android.viewmodel.ext.android.viewModel
import timber.log.Timber

class MovieGridActivity : BaseActivity() {

    private val viewModel by viewModel<MovieGridViewModel>()
    private lateinit var movieAdapter: MovieAdapter
    private var moviesLiveData: LiveData<List<Movie>>? = null
    private var movieGridViewItemLiveData: LiveData<List<MovieGridViewItem>>? = null

    private var currentInvertMovieStarById: Pair<Int, Snackbar>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_grid)

        with(movieGridRecyclerView) {
            val staggeredGridLayoutManager = StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL)

            movieAdapter = MovieAdapter(
                { startActivity(MovieDetailActivity.getStartIntent(this@MovieGridActivity, it)) },
                { movieId -> viewModel.onViewEvent(MovieGridEvent.ViewInvertMovieStarById(movieId)) }
            )//.apply { setHasStableIds(true) }

            layoutManager = staggeredGridLayoutManager
            itemAnimator = DefaultItemAnimator()
            adapter = movieAdapter

            setHasFixedSize(true)

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (canScrollVertically(1).not()) viewModel.onViewEvent(MovieGridEvent.ViewLoadMore)
                }
            })
        }

        viewModel.getMovieGridModelLiveData().observe(this, Observer<MovieGridModel> { movieGridModel ->
            Timber.tag(getTag("getMovieGridModelLiveData")).d(movieGridModel.toString())

            if (movieGridModel == null) return@Observer

            if (moviesLiveData != movieGridModel.moviesLiveData) {
                moviesLiveData = movieGridModel.moviesLiveData

                movieGridViewItemLiveData?.removeObservers(this)
                movieGridViewItemLiveData = Transformations.map(movieGridModel.moviesLiveData) { list ->
                    list.map { MovieGridViewItem(it.id, it.posterPath, it.isStar) }
                }
                movieGridViewItemLiveData?.observe(this, Observer {
                    Timber.tag(getTag("getMovieGridModelLiveData.submitList")).d(it.toString())
                    movieAdapter.submitList(it)
                })
            }

            val invertMovieStarById = movieGridModel.invertMovieStarByIdJob?.first
            if (invertMovieStarById != null) {
                if (invertMovieStarById != currentInvertMovieStarById?.first) {
                    // New movie to invertMovieStarById
                    currentInvertMovieStarById?.second?.let { if (it.isShownOrQueued) it.dismiss() }

                    val snackbar = SnackbarBuilder(this@MovieGridActivity)
                        .message("Star/unstar movie (id: $invertMovieStarById)")
                        .actionText("Cancel")
                        .duration(Snackbar.LENGTH_INDEFINITE)
                        .dismissCallback { _, dismissEvent ->
                            when (dismissEvent) {
                                Snackbar.Callback.DISMISS_EVENT_ACTION -> {
                                    Timber.e("DISMISS_EVENT_ACTION")
                                    viewModel.onViewEvent(
                                        MovieGridEvent.ViewCancelInvertMovieStarById(invertMovieStarById)
                                    )
                                }
                                Snackbar.Callback.DISMISS_EVENT_SWIPE -> Timber.e("DISMISS_EVENT_SWIPE")
                                Snackbar.Callback.DISMISS_EVENT_TIMEOUT -> Timber.e("DISMISS_EVENT_TIMEOUT")
                                Snackbar.Callback.DISMISS_EVENT_MANUAL -> Timber.e("DISMISS_EVENT_MANUAL")
                                Snackbar.Callback.DISMISS_EVENT_CONSECUTIVE -> Timber.e("DISMISS_EVENT_CONSECUTIVE")
                            }
                        }
                        .build()

                    currentInvertMovieStarById = Pair(invertMovieStarById, snackbar)
                    snackbar.show()
                }
            } else {
                if (currentInvertMovieStarById != null) {
                    currentInvertMovieStarById?.second?.let { if (it.isShownOrQueued) it.dismiss() }
                    currentInvertMovieStarById = null
                }
            }

            movieGridSwipeRefresh.isRefreshing = movieGridModel.workInProgressCounter > 0

            movieGridModel.error?.run {
                Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
            }
        })

        movieGridSwipeRefresh.setOnRefreshListener {
            viewModel.onViewEvent(MovieGridEvent.ViewRefresh)
        }

        viewModel.onViewEvent(MovieGridEvent.ViewUpdate)
    }


    private class MovieViewItemHolder(
        override val containerView: View,
        private val onItemClick: (Int) -> Unit,
        private val onItemStartClick: (Int) -> Unit
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(item: MovieGridViewItem) {
            Glide.with(containerView.context.applicationContext).load(item.posterPath).into(movieItemImage)
            movieItemImage.setOnClickListener { onItemClick.invoke(item.id) }

            movieGridItemViewStar.imageTintList = if (item.isStar)
                ColorStateList.valueOf(ContextCompat.getColor(containerView.context, R.color.colorAccent))
            else
                ColorStateList.valueOf(ContextCompat.getColor(containerView.context, R.color.colorWhite))
            movieGridItemViewStar.setOnClickListener { onItemStartClick.invoke(item.id) }
        }
    }

    private class MovieAdapter(
        private val onItemClick: (Int) -> Unit,
        private val onItemStartClick: (Int) -> Unit
    ) : ListAdapter<MovieGridViewItem, MovieViewItemHolder>(
        object : DiffUtil.ItemCallback<MovieGridViewItem>() {
            override fun areItemsTheSame(oldItem: MovieGridViewItem, newItem: MovieGridViewItem) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: MovieGridViewItem, newItem: MovieGridViewItem) =
                oldItem == newItem
        }
    ) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MovieViewItemHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.movie_item, parent, false),
            onItemClick, onItemStartClick
        )

        override fun onBindViewHolder(holder: MovieViewItemHolder, position: Int) = holder.bind(getItem(position))
    }
}