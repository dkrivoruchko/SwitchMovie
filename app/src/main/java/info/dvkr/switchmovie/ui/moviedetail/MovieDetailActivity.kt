package info.dvkr.switchmovie.ui.moviedetail

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Log
import android.widget.Toast
import com.jakewharton.rxrelay2.PublishRelay
import info.dvkr.switchmovie.R
import info.dvkr.switchmovie.dagger.component.NonConfigurationComponent
import info.dvkr.switchmovie.dagger.module.GlideApp
import info.dvkr.switchmovie.data.presenter.MovieDetailPresenter
import info.dvkr.switchmovie.data.view.MovieDetailView
import info.dvkr.switchmovie.ui.BaseActivity
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_movie_detail.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class MovieDetailActivity : BaseActivity(), MovieDetailView {

    companion object {
        private const val MOVIE_ID = "MOVIE_ID"

        fun getStartIntent(context: Context, id: Int): Intent {
            return Intent(context, MovieDetailActivity::class.java).putExtra(MOVIE_ID, id)
        }
    }

    @Inject internal lateinit var presenter: MovieDetailPresenter
    private val fromEvents = PublishRelay.create<MovieDetailView.FromEvent>()

    private val dateParser = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    private val dateFormatter = SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH)

    override fun inject(injector: NonConfigurationComponent) = injector.inject(this)

    override fun fromEvent(): Observable<MovieDetailView.FromEvent> = fromEvents

    override fun toEvent(toEvent: MovieDetailView.ToEvent) {
        Single.just(toEvent).subscribeOn(AndroidSchedulers.mainThread()).subscribe { event ->
            Log.wtf("MovieDetailActivity", "[${Thread.currentThread().name}] toEvent: $event")

            when (event) {
                is MovieDetailView.ToEvent.OnMovie -> {
//                    Single.just("").observeOn(Schedulers.io())
//                            .map {
//                                Glide.with(applicationContext).asBitmap()
//                                        .load(event.movie.posterPath)
//                                        .submit()
//                                        .get()
//                            }
//                            .observeOn(AndroidSchedulers.mainThread())
//                            .subscribe { bitmap -> Blurry.with(applicationContext).radius(20).color(Color.argb(66, 255, 255, 0)).from(bitmap).into(movieDetailBackground) }

                    title = event.movie.originalTitle

                    GlideApp.with(movieDetailImage)
                            .load(event.movie.posterPath)
                            .fitCenter()
                            .into(movieDetailImage)

                    movieDetailScore.text = event.movie.voteAverage
                    movieDetailRating.text = "Unkown"

                    try {
                        val date = dateParser.parse(event.movie.releaseDate)
                        movieDetailReleaseDate.text = dateFormatter.format(date)
                    } catch (ex: ParseException) {
                        movieDetailReleaseDate.text = event.movie.releaseDate
                        toEvent(MovieDetailView.ToEvent.OnError(ex))
                    }

                    movieDetailTitle.text = event.movie.originalTitle
                    movieDetailOverview.text = event.movie.overview
                }

                is MovieDetailView.ToEvent.OnError -> {
                    Toast.makeText(applicationContext, event.error.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.wtf("MovieDetailActivity", "[${Thread.currentThread().name}] onCreate: Start")
        setContentView(R.layout.activity_movie_detail)
        presenter.attach(this)

        val movieId = intent.getIntExtra(MOVIE_ID, -1)
        fromEvents.accept(MovieDetailView.FromEvent.GetMovieById(movieId))

        Log.wtf("MovieDetailActivity", "[${Thread.currentThread().name}] onCreate: End")
    }

    override fun onDestroy() {
        Log.wtf("MovieDetailActivity", "[${Thread.currentThread().name}] onDestroy")
        presenter.detach()
        super.onDestroy()
    }

    class BlurBuilder {
        private val BITMAP_SCALE = 0.4f
        private val BLUR_RADIUS = 7.5f

        fun blur(context: Context, image: Bitmap): Bitmap {
            val width = Math.round(image.width * BITMAP_SCALE)
            val height = Math.round(image.height * BITMAP_SCALE)

            val inputBitmap = Bitmap.createScaledBitmap(image, width, height, false)
            val outputBitmap = Bitmap.createBitmap(inputBitmap)

            val rs = RenderScript.create(context)
            val theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
            val tmpIn = Allocation.createFromBitmap(rs, inputBitmap)
            val tmpOut = Allocation.createFromBitmap(rs, outputBitmap)
            theIntrinsic.setRadius(BLUR_RADIUS)
            theIntrinsic.setInput(tmpIn)
            theIntrinsic.forEach(tmpOut)
            tmpOut.copyTo(outputBitmap)

            return outputBitmap
        }
    }
}