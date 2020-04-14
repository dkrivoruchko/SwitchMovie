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
import info.dvkr.switchmovie.domain.utils.getLog
import info.dvkr.switchmovie.viewmodel.BaseViewModel
import info.dvkr.switchmovie.viewmodel.moviedetail.MovieDetailViewEvent
import info.dvkr.switchmovie.viewmodel.moviedetail.MovieDetailViewModel
import kotlinx.android.synthetic.main.fragment_movie_detail.*
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_movie_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        XLog.d(getLog("onViewCreated", "Invoked"))

        viewModel.stateLiveData().observe(viewLifecycleOwner, androidx.lifecycle.Observer { state ->
            XLog.d(getLog("stateLiveData", state.toString()))

            requireActivity().title = state.movie.title

            iv_fragment_movie_detail_image.load(state.movie.posterPath)

            tv_fragment_movie_detail_score.text = state.movie.voteAverage
            tv_fragment_movie_detail_rating.text = state.movie.popularity.toString()

            try {
                val date = dateParser.parse(state.movie.releaseDate)
                tv_fragment_movie_detail_date.text = dateFormatter.format(date)
            } catch (ex: ParseException) {
                tv_fragment_movie_detail_date.text = state.movie.releaseDate
                viewModel.onEvent(BaseViewModel.Error(ex))
            }

            tv_fragment_movie_detail_title.text = state.movie.title
            tv_fragment_movie_detail_overview.text = state.movie.overview
            iv_fragment_movie_detail_start.imageTintList =
                ColorStateList.valueOf(if (state.movie.isStar) colorAccent else colorWhite)


            sr_fragment_movie_detail.isRefreshing = state.workInProgressCounter > 0

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