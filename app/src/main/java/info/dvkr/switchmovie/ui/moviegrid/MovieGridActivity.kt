package info.dvkr.switchmovie.ui.moviegrid

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.StaggeredGridLayoutManager
import android.widget.Toast
import info.dvkr.switchmovie.R
import info.dvkr.switchmovie.data.presenter.moviegrid.MovieGridPresenter
import info.dvkr.switchmovie.data.presenter.moviegrid.MovieGridView
import info.dvkr.switchmovie.ui.BaseActivity
import info.dvkr.switchmovie.ui.moviedetail.MovieDetailActivity
import kotlinx.android.synthetic.main.activity_movie_grid.*
import timber.log.Timber

class MovieGridActivity : BaseActivity(), MovieGridView {
    private val presenter: MovieGridPresenter by lazy {
        ViewModelProviders.of(this, presenterFactory).get(MovieGridPresenter::class.java)
    }

    private var currentPage: Int = 1
    private val staggeredLayoutManager by lazy {
        StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
    }
    private lateinit var movieGridRecyclerViewAdapter: MovieGridRecyclerViewAdapter

    override fun toEvent(toEvent: MovieGridView.ToEvent) = runOnUiThread {
        Timber.d("[${this.javaClass.simpleName}#${this.hashCode()}@${Thread.currentThread().name}] toEvent: $toEvent")

        when (toEvent) {
            is MovieGridView.ToEvent.OnRefresh -> {
                movieGridSwipeRefresh.isRefreshing = toEvent.isRefreshing
            }

            is MovieGridView.ToEvent.OnMovieGridItemsRefresh -> {
                movieGridRecyclerViewAdapter.setMovieList(toEvent.list)
            }

            is MovieGridView.ToEvent.OnMovieGridItemsPage -> {
                currentPage++
                movieGridRecyclerViewAdapter.addMovieList(toEvent.list)
            }

            is MovieGridView.ToEvent.OnError -> {
                Toast.makeText(applicationContext, toEvent.error.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_grid)

        movieGridRecyclerView.setHasFixedSize(true)
        movieGridRecyclerView.layoutManager = staggeredLayoutManager

        movieGridRecyclerViewAdapter = MovieGridRecyclerViewAdapter(
                { startActivity(MovieDetailActivity.getStartIntent(applicationContext, it.id)) },
                { presenter.offer(MovieGridView.FromEvent.GetPage(currentPage + 1)) })

        movieGridRecyclerViewAdapter.setHasStableIds(true)
        movieGridRecyclerView.adapter = movieGridRecyclerViewAdapter

        movieGridSwipeRefresh.setOnRefreshListener {
            currentPage = 1
            presenter.offer(MovieGridView.FromEvent.RefreshItems)
        }

        presenter.attach(this)

        presenter.offer(MovieGridView.FromEvent.RefreshItems)
    }

    override fun onDestroy() {
        presenter.detach()
        super.onDestroy()
    }
}