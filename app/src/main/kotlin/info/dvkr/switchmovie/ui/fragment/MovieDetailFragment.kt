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
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.elvishew.xlog.XLog
import info.dvkr.switchmovie.R
import info.dvkr.switchmovie.data.viewmodel.BaseViewModel
import info.dvkr.switchmovie.data.viewmodel.moviedetail.MovieDetailViewEvent
import info.dvkr.switchmovie.data.viewmodel.moviedetail.MovieDetailViewModel
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.utils.getLog
import kotlinx.android.synthetic.main.fragment_movie_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class MovieDetailFragment : Fragment() {

    private val args: MovieDetailFragmentArgs by navArgs()
    private val viewModel by viewModel<MovieDetailViewModel>()
    private var movieLiveData: LiveData<Movie>? = null
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
            val movieDetailState = state as? MovieDetailViewModel.MovieDetailSate ?: return@Observer
            XLog.d(getLog("stateLiveData", movieDetailState.toString()))

            movieLiveData?.removeObservers(viewLifecycleOwner)
            movieLiveData = movieDetailState.movieLiveData

            movieLiveData?.observe(viewLifecycleOwner, androidx.lifecycle.Observer MovieObserver@{ movie ->
                XLog.d(getLog("movieLiveData", movie.toString()))

                movie != null || return@MovieObserver
                requireActivity().title = movie.title

                Glide.with(requireContext())
                    .load(movie.posterPath)
                    .into(iv_fragment_movie_detail_image)

                tv_fragment_movie_detail_score.text = movie.voteAverage
                tv_fragment_movie_detail_rating.text = movie.popularity.toString()

                try {
                    val date = dateParser.parse(movie.releaseDate)
                    tv_fragment_movie_detail_date.text = dateFormatter.format(date)
                } catch (ex: ParseException) {
                    tv_fragment_movie_detail_date.text = movie.releaseDate
                    viewModel.onEvent(BaseViewModel.Error(ex))
                }

                tv_fragment_movie_detail_title.text = movie.title
                tv_fragment_movie_detail_overview.text = movie.overview
                iv_fragment_movie_detail_start.imageTintList =
                    ColorStateList.valueOf(if (movie.isStar) colorAccent else colorWhite)
            })

            sr_fragment_movie_detail.isRefreshing = movieDetailState.workInProgressCounter > 0

            if (error != movieDetailState.error) {
                movieDetailState.error?.run {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                }
            }
        })

        viewModel.onEvent(MovieDetailViewEvent.GetMovieById(args.movieId))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        movieLiveData = null
        error = null
    }
}