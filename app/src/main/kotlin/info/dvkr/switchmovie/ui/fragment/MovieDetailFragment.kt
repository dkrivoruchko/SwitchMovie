package info.dvkr.switchmovie.ui.fragment

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import coil.api.load
import com.elvishew.xlog.XLog
import info.dvkr.switchmovie.R
import info.dvkr.switchmovie.databinding.FragmentMovieDetailBinding
import info.dvkr.switchmovie.domain.utils.getLog
import info.dvkr.switchmovie.viewmodel.BaseViewModel
import info.dvkr.switchmovie.viewmodel.moviedetail.MovieDetailViewEvent
import info.dvkr.switchmovie.viewmodel.moviedetail.MovieDetailViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class MovieDetailFragment : Fragment() {

    private val args: MovieDetailFragmentArgs by navArgs()
    private val viewModel by viewModel<MovieDetailViewModel>()
    private var error: Throwable? = null

    private val dateParser = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    private val dateFormatter = SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH)

    private val colorAccent by lazy { ContextCompat.getColor(requireContext(), R.color.colorAccent) }
    private val colorWhite by lazy { ContextCompat.getColor(requireContext(), R.color.colorWhite) }

    private var _binding: FragmentMovieDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMovieDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        XLog.d(getLog("onViewCreated"))

        viewModel.stateLiveData().observe(viewLifecycleOwner, androidx.lifecycle.Observer { state ->
            XLog.d(getLog("stateLiveData", state.toString()))

            requireActivity().title = state.movie.title

            binding.ivFragmentMovieDetailImage.load(state.movie.posterPath)

            binding.tvFragmentMovieDetailScore.text = state.movie.voteAverage
            binding.tvFragmentMovieDetailRating.text = state.movie.popularity.toString()

            try {
                val date = dateParser.parse(state.movie.releaseDate)
                binding.tvFragmentMovieDetailDate.text = dateFormatter.format(date)
            } catch (ex: ParseException) {
                binding.tvFragmentMovieDetailDate.text = state.movie.releaseDate
                viewModel.onEvent(BaseViewModel.Error(ex))
            }

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
        _binding = null
        error = null
    }
}