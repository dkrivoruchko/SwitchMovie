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
import androidx.navigation.findNavController
import androidx.recyclerview.widget.*
import coil.api.load
import com.elvishew.xlog.XLog
import info.dvkr.switchmovie.R
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.utils.getLog
import info.dvkr.switchmovie.viewmodel.moviegrid.MovieGridViewEvent
import info.dvkr.switchmovie.viewmodel.moviegrid.MovieGridViewModel
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

        viewModel.stateLiveData().observe(viewLifecycleOwner, androidx.lifecycle.Observer { movieGridState ->
            XLog.d(getLog("stateLiveData", movieGridState.toString()))

            movieAdapter?.submitList(movieGridState.movies.map { MovieGridViewItem(it.id, it.posterPath, it.isStar) })

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