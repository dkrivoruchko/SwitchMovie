package info.dvkr.switchmovie.ui.moviegrid

import android.os.Bundle
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.Log
import android.widget.Toast
import com.jakewharton.rxrelay2.PublishRelay
import info.dvkr.switchmovie.R
import info.dvkr.switchmovie.dagger.component.NonConfigurationComponent
import info.dvkr.switchmovie.data.presenter.MovieGridPresenter
import info.dvkr.switchmovie.data.view.MovieGridView
import info.dvkr.switchmovie.ui.BaseActivity
import info.dvkr.switchmovie.ui.moviedetail.MovieDetailActivity
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_movie_grid.*
import javax.inject.Inject

class MovieGridActivity : BaseActivity(), MovieGridView {
    @Inject internal lateinit var presenter: MovieGridPresenter
    private val fromEvents = PublishRelay.create<MovieGridView.FromEvent>()
    private var currentPage: Int = 1

    private lateinit var staggeredLayoutManager: StaggeredGridLayoutManager
    private lateinit var movieGridRecyclerViewAdapter: MovieGridRecyclerViewAdapter

    override fun inject(injector: NonConfigurationComponent) = injector.inject(this)

    override fun fromEvent(): Observable<MovieGridView.FromEvent> = fromEvents

    override fun toEvent(toEvent: MovieGridView.ToEvent) {
        Single.just(toEvent).subscribeOn(AndroidSchedulers.mainThread()).subscribe { event ->
            Log.wtf("MovieGridActivity", "[${Thread.currentThread().name}] toEvent: $event")

            when (event) {
                is MovieGridView.ToEvent.OnRefresh -> {
                    movieGridSwipeRefresh.isRefreshing = event.isRefreshing
                }

                is MovieGridView.ToEvent.OnMovieGridItemsRefresh -> {
                    movieGridRecyclerViewAdapter.setMovieList(event.list)
                }

                is MovieGridView.ToEvent.OnMovieGridItemsPage -> {
                    currentPage++
                    movieGridRecyclerViewAdapter.addMovieList(event.list)
                }

                is MovieGridView.ToEvent.OnError -> {
                    Toast.makeText(applicationContext, event.error.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.wtf("MovieGridActivity", "[${Thread.currentThread().name}] onCreate: Start")
        setContentView(R.layout.activity_movie_grid)

        movieGridRecyclerView.setHasFixedSize(true)
        staggeredLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        movieGridRecyclerView.layoutManager = staggeredLayoutManager

        movieGridRecyclerViewAdapter = MovieGridRecyclerViewAdapter(
                { startActivity(MovieDetailActivity.getStartIntent(applicationContext, it.id)) },
                { fromEvents.accept(MovieGridView.FromEvent.GetPage(currentPage + 1)) })

        movieGridRecyclerViewAdapter.setHasStableIds(true)
        movieGridRecyclerView.adapter = movieGridRecyclerViewAdapter

        movieGridSwipeRefresh.setOnRefreshListener {
            currentPage = 1
            fromEvents.accept(MovieGridView.FromEvent.RefreshItems())
        }

        presenter.attach(this)

        fromEvents.accept(MovieGridView.FromEvent.RefreshItems())
        Log.wtf("MovieGridActivity", "[${Thread.currentThread().name}] onCreate: End")
    }

    override fun onDestroy() {
        Log.wtf("MovieGridActivity", "[${Thread.currentThread().name}] onDestroy")
        presenter.detach()
        super.onDestroy()
    }
}