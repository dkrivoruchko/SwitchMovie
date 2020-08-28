package info.dvkr.switchmovie.ui.fragment

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.navArgs
import coil.load
import com.elvishew.xlog.XLog
import info.dvkr.switchmovie.R
import info.dvkr.switchmovie.databinding.FragmentMovieDetailBinding
import info.dvkr.switchmovie.domain.utils.getLog
import info.dvkr.switchmovie.helpers.viewBinding
import info.dvkr.switchmovie.viewmodel.moviedetail.MovieDetailViewEvent
import info.dvkr.switchmovie.viewmodel.moviedetail.MovieDetailViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MovieDetailFragment : BaseFragment(R.layout.fragment_movie_detail) {

    private val args: MovieDetailFragmentArgs by navArgs()
    private val viewModel by viewModel<MovieDetailViewModel>()
    private var error: Throwable? = null

    private val colorAccent by lazy(LazyThreadSafetyMode.NONE) {
        ContextCompat.getColor(requireContext(), R.color.colorAccent)
    }
    private val colorWhite by lazy(LazyThreadSafetyMode.NONE) {
        ContextCompat.getColor(requireContext(), R.color.colorWhite)
    }

    private val binding by viewBinding { fragment -> FragmentMovieDetailBinding.bind(fragment.requireView()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getStateLiveData().observe(viewLifecycleOwner, { state ->
            XLog.d(getLog("stateLiveData", state.toString()))

            requireActivity().title = state.movie.title

            binding.ivFragmentMovieDetailImage.load(state.movie.posterPath)

            binding.tvFragmentMovieDetailScore.text = state.movie.voteAverage
            binding.tvFragmentMovieDetailRating.text = state.movie.popularity.toString()

            binding.tvFragmentMovieDetailDate.text = state.movie.releaseDate.toString()

            binding.tvFragmentMovieDetailTitle.text = state.movie.title
            binding.tvFragmentMovieDetailOverview.text = state.movie.overview
            binding.ivFragmentMovieDetailStart.imageTintList =
                ColorStateList.valueOf(if (state.movie.isStar) colorAccent else colorWhite)

            binding.srFragmentMovieDetail.isRefreshing = state.workInProgressCounter > 0

            if (error != state.error) {
                state.error?.run {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                }
            }
        })

        viewModel.onEvent(MovieDetailViewEvent.GetMovieById(args.movieId))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        error = null
    }
}