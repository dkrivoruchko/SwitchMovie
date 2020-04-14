package info.dvkr.switchmovie.ui.fragment

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.*
import coil.api.load
import com.elvishew.xlog.XLog
import info.dvkr.switchmovie.R
import info.dvkr.switchmovie.databinding.FragmentMovieGridBinding
import info.dvkr.switchmovie.databinding.ItemMovieBinding
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.utils.getLog
import info.dvkr.switchmovie.viewmodel.moviegrid.MovieGridViewEvent
import info.dvkr.switchmovie.viewmodel.moviegrid.MovieGridViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MovieGridFragment : Fragment() {

    data class MovieGridViewItem(val id: Long, val posterPath: String, val isStar: Boolean) {
        companion object {
            fun fromMovie(movie: Movie) = MovieGridViewItem(movie.id, movie.posterPath, movie.isStar)
        }

        override fun toString() = "Movie(id=$id)"
    }

    private val viewModel by viewModel<MovieGridViewModel>()
    private var movieAdapter: MovieAdapter? = null
    private var error: Throwable? = null

    private var _binding: FragmentMovieGridBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMovieGridBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        XLog.d(getLog("onViewCreated"))

        requireActivity().title = getString(R.string.movie_grid_activity_name)

        movieAdapter = MovieAdapter { movieGridViewItem ->
            if (movieGridViewItem.isStar) viewModel.onEvent(MovieGridViewEvent.UnsetMovieStar(movieGridViewItem.id))
            else viewModel.onEvent(MovieGridViewEvent.SetMovieStar(movieGridViewItem.id))
        }
            .apply { setHasStableIds(true) }

        with(binding.rvFragmentMovieGrid) {
            layoutManager = StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL)
            itemAnimator = DefaultItemAnimator()
            adapter = movieAdapter

            setHasFixedSize(true)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (canScrollVertically(1).not()) viewModel.onEvent(MovieGridViewEvent.LoadMore)
                }
            })
        }

        viewModel.stateLiveData().observe(viewLifecycleOwner, androidx.lifecycle.Observer { movieGridState ->
            XLog.d(getLog("stateLiveData", movieGridState.toString()))

            movieAdapter?.submitList(movieGridState.movies.map { MovieGridViewItem.fromMovie(it) })

            binding.srFragmentMovieGrid.isRefreshing = movieGridState.workInProgressCounter > 0

            if (error != movieGridState.error) {
                error = movieGridState.error
                movieGridState.error?.run {
                    Toast.makeText(requireActivity(), message, Toast.LENGTH_LONG).show()
                }
            }
        })

        binding.srFragmentMovieGrid.setOnRefreshListener {
            viewModel.onEvent(MovieGridViewEvent.Refresh)
        }

        viewModel.onEvent(MovieGridViewEvent.Update)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        movieAdapter = null
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
            ItemMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            onItemStartClick
        )

        override fun onBindViewHolder(holder: MovieViewItemHolder, position: Int) = holder.bind(getItem(position))

        override fun getItemId(position: Int): Long = getItem(position).id
    }

    private class MovieViewItemHolder(
        private val binding: ItemMovieBinding,
        private val onItemStartClick: (MovieGridViewItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val colorWhite by lazy { ContextCompat.getColor(binding.root.context, R.color.colorWhite) }
        private val colorAccent by lazy { ContextCompat.getColor(binding.root.context, R.color.colorAccent) }

        fun bind(item: MovieGridViewItem) {
            binding.ibItemMovie.load(item.posterPath)
            // TODO Add shared element transition
            binding.ibItemMovie.setOnClickListener {
                it.findNavController().navigate(
                    MovieGridFragmentDirections.actionMovieGridFragmentToMovieDetailFragment(item.id)
                )
            }
            binding.ivItemMoviewStar.imageTintList =
                ColorStateList.valueOf(if (item.isStar) colorAccent else colorWhite)
            binding.ivItemMoviewStar.setOnClickListener { onItemStartClick.invoke(item) }
        }
    }
}