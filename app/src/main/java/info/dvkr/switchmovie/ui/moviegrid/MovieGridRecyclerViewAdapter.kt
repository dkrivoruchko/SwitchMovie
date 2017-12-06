package info.dvkr.switchmovie.ui.moviegrid

import android.content.res.ColorStateList
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import info.dvkr.switchmovie.R
import info.dvkr.switchmovie.data.presenter.moviegrid.MovieGridView
import kotlinx.android.synthetic.main.movie_item.view.*


internal class MovieGridRecyclerViewAdapter(private val onItemStarClickListener: (MovieGridView.MovieGridItem) -> Unit,
                                            private val onItemClickListener: (MovieGridView.MovieGridItem) -> Unit,
                                            private val onItemLongClickListener: (MovieGridView.MovieGridItem) -> Boolean,
                                            private val onBottomReachedListener: () -> Unit) :
    RecyclerView.Adapter<MovieGridRecyclerViewAdapter.ViewHolder>() {

  private var movieList: MutableList<MovieGridView.MovieGridItem> = mutableListOf()

  internal fun setMovieList(newMovieList: List<MovieGridView.MovieGridItem>) {
    movieList = newMovieList.toMutableList()
    notifyDataSetChanged()
  }

  internal fun updateMovieList(range: Pair<Int, Int>, addMovieList: List<MovieGridView.MovieGridItem>) {
    if (movieList.size < range.first) throw IllegalStateException("Wrong Range")

    movieList = movieList.subList(0, range.first)
    movieList.addAll(addMovieList)
    notifyDataSetChanged()
  }

  override fun getItemCount() = movieList.size

  override fun getItemId(position: Int) = movieList[position].id.toLong()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
      ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.movie_item, parent, false))

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(movieList[position], onItemStarClickListener, onItemClickListener, onItemLongClickListener)
    if (position == movieList.size - 1) onBottomReachedListener()
  }

  inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(item: MovieGridView.MovieGridItem,
             starListener: (MovieGridView.MovieGridItem) -> Unit,
             listener: (MovieGridView.MovieGridItem) -> Unit,
             longListener: (MovieGridView.MovieGridItem) -> Boolean) = with(itemView) {

      Glide.with(context).load(item.posterPath).into(movieItemImage)
      if (item.isStar)
        movieGridItemViewStar.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorAccent))
      else
        movieGridItemViewStar.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorWhite))
      movieGridItemViewStar.setOnClickListener { starListener(item) }
      movieItemHolder.setOnClickListener { listener(item) }
      movieItemHolder.setOnLongClickListener { longListener(item) }

    }
  }
}