package info.dvkr.switchmovie.ui.fragment

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.navigation.findNavController
import androidx.recyclerview.widget.*
import coil.api.load
import com.elvishew.xlog.XLog
import info.dvkr.switchmovie.R
import info.dvkr.switchmovie.data.viewmodel.moviegrid.MovieGridViewEvent
import info.dvkr.switchmovie.data.viewmodel.moviegrid.MovieGridViewModel
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.utils.getLog
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_movie_grid.*
import kotlinx.android.synthetic.main.item_movie.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class MovieGridFragment : Fragment() {

    data class MovieGridViewItem(val id: Int, val posterPath: String, val isStar: Boolean) {
        override fun toString() = "Movie(id=$id)"
    }

    private val viewModel by viewModel<MovieGridViewModel>()
    private var movieAdapter: MovieAdapter? = null
    private var moviesLiveData: LiveData<List<Movie>>? = null
    private var movieGridViewItemLiveData: LiveData<List<MovieGridViewItem>>? = null
    private var error: Throwable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_movie_grid, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        XLog.d(getLog("onViewCreated", "Invoked"))

        requireActivity().title = getString(R.string.movie_grid_activity_name)
        with(rv_fragment_movie_grid) {
            layoutManager = StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL)
            itemAnimator = DefaultItemAnimator()

            movieAdapter = MovieAdapter { movieGridViewItem ->
                if (movieGridViewItem.isStar) viewModel.onEvent(MovieGridViewEvent.UnsetMovieStar(movieGridViewItem.id))
                else viewModel.onEvent(MovieGridViewEvent.SetMovieStar(movieGridViewItem.id))
            }
                .apply {
                    //  setHasStableIds(true) TODO For some reason it's cause error
                    adapter = this
                }

            setHasFixedSize(true)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (canScrollVertically(1).not()) viewModel.onEvent(MovieGridViewEvent.LoadMore)
                }
            })
        }

        viewModel.stateLiveData().observe(viewLifecycleOwner, androidx.lifecycle.Observer { state ->
            val movieGridState = state as? MovieGridViewModel.MovieGridState ?: return@Observer
            XLog.d(getLog("stateLiveData", movieGridState.toString()))

            if (moviesLiveData != movieGridState.moviesLiveData) {
                moviesLiveData = movieGridState.moviesLiveData

                movieGridViewItemLiveData?.removeObservers(viewLifecycleOwner)
                movieGridViewItemLiveData = Transformations.map(movieGridState.moviesLiveData) { list ->
                    list.map { MovieGridViewItem(it.id, it.posterPath, it.isStar) }
                }
                movieGridViewItemLiveData?.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                    XLog.d(getLog("getMovieGridModelLiveData.submitList", it.toString()))
                    movieAdapter?.submitList(it)
                })
            }

//            val invertMovieStarById = movieGridState.invertMovieStarByIdJob?.first
//            if (invertMovieStarById != null) {
//                if (invertMovieStarById != currentInvertMovieStarById?.first) {
            // New movie to invertMovieStarById
//                    currentInvertMovieStarById?.second?.let { if (it.isShownOrQueued) it.dismiss() }

//                    val snackbar = SnackbarBuilder(this@MovieGridActivity)
//                        .message("Star/unstar movie (id: $invertMovieStarById)")
//                        .actionText("Cancel")
//                        .duration(Snackbar.LENGTH_INDEFINITE)
//                        .dismissCallback { _, dismissEvent ->
//                            when (dismissEvent) {
//                                Snackbar.Callback.DISMISS_EVENT_ACTION -> {
//                                    Timber.e("DISMISS_EVENT_ACTION")
//                                    viewModel.onViewEvent(
//                                        MovieGridViewEvent.ViewCancelInvertMovieStarById(invertMovieStarById)
//                                    )
//                                }
//                                Snackbar.Callback.DISMISS_EVENT_SWIPE -> Timber.e("DISMISS_EVENT_SWIPE")
//                                Snackbar.Callback.DISMISS_EVENT_TIMEOUT -> Timber.e("DISMISS_EVENT_TIMEOUT")
//                                Snackbar.Callback.DISMISS_EVENT_MANUAL -> Timber.e("DISMISS_EVENT_MANUAL")
//                                Snackbar.Callback.DISMISS_EVENT_CONSECUTIVE -> Timber.e("DISMISS_EVENT_CONSECUTIVE")
//                            }
//                        }
//                        .build()

//                    currentInvertMovieStarById = Pair(invertMovieStarById, snackbar)
//                    snackbar.show()
//                }
//            } else {
//                if (currentInvertMovieStarById != null) {
//                    currentInvertMovieStarById?.second?.let { if (it.isShownOrQueued) it.dismiss() }
//                    currentInvertMovieStarById = null
//                }
//            }

            sr_fragment_movie_grid.isRefreshing = movieGridState.workInProgressCounter > 0

            if (error != movieGridState.error) {
                error = movieGridState.error
                movieGridState.error?.run {
                    Toast.makeText(requireActivity(), message, Toast.LENGTH_LONG).show()
                }
            }
        })

        sr_fragment_movie_grid.setOnRefreshListener {
            viewModel.onEvent(MovieGridViewEvent.Refresh)
        }

        viewModel.onEvent(MovieGridViewEvent.Update)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        movieAdapter = null
        moviesLiveData = null
        error = null
    }

    private class MovieAdapter(
        private val onItemStartClick: (MovieGridViewItem) -> Unit
    ) : ListAdapter<MovieGridViewItem, MovieViewItemHolder>(
        object : DiffUtil.ItemCallback<MovieGridViewItem>() {
            override fun areItemsTheSame(oldItem: MovieGridViewItem, newItem: MovieGridViewItem) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: MovieGridViewItem, newItem: MovieGridViewItem) =
                oldItem == newItem
        }
    ) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MovieViewItemHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_movie, parent, false),
            onItemStartClick
        )

        override fun onBindViewHolder(holder: MovieViewItemHolder, position: Int) = holder.bind(getItem(position))
    }

    private class MovieViewItemHolder(
        override val containerView: View,
        private val onItemStartClick: (MovieGridViewItem) -> Unit
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        private val colorWhite by lazy { ContextCompat.getColor(containerView.context, R.color.colorWhite) }
        private val colorAccent by lazy { ContextCompat.getColor(containerView.context, R.color.colorAccent) }

        fun bind(item: MovieGridViewItem) {
            ib_item_movie.load(item.posterPath)
            // TODO Add shared element transition
            ib_item_movie.setOnClickListener {
                it.findNavController().navigate(
                    MovieGridFragmentDirections.actionMovieGridFragmentToMovieDetailFragment(item.id)
                )
            }
            iv_item_moview_star.imageTintList = ColorStateList.valueOf(if (item.isStar) colorAccent else colorWhite)
            iv_item_moview_star.setOnClickListener { onItemStartClick.invoke(item) }
        }
    }
}