package info.dvkr.switchmovie.ui.moviegrid

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions.fitCenterTransform
import info.dvkr.switchmovie.R
import info.dvkr.switchmovie.data.view.MovieGridView
import kotlinx.android.synthetic.main.movie_item.view.*


internal class MovieGridRecyclerViewAdapter(private val onItemClickListener: (MovieGridView.MovieGridItem) -> Unit,
                                            private val onBottomReachedListener: () -> Unit) :
        RecyclerView.Adapter<MovieGridRecyclerViewAdapter.ViewHolder>() {

    private var movieList: MutableList<MovieGridView.MovieGridItem> = mutableListOf<MovieGridView.MovieGridItem>()

    internal fun setMovieList(newMovieList: List<MovieGridView.MovieGridItem>) {
        movieList = newMovieList.toMutableList()
        notifyDataSetChanged()
    }

    internal fun addMovieList(addMovieList: List<MovieGridView.MovieGridItem>) {
        val sizeBefore = movieList.size
        movieList.addAll(addMovieList)
        notifyItemRangeInserted(sizeBefore, addMovieList.size)
    }

    override fun getItemCount() = movieList.size

    override fun getItemId(position: Int) = movieList[position].id.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.movie_item, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(movieList[position], onItemClickListener)
        if (position == movieList.size - 1) onBottomReachedListener()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: MovieGridView.MovieGridItem,
                 listener: (MovieGridView.MovieGridItem) -> Unit) = with(itemView) {

            Glide.with(itemView)
                    .load(item.posterPath)
                    .apply(fitCenterTransform())
                    .into(movieItemImage)

            movieItemHolder.setOnClickListener { listener(item) }
        }
    }
}