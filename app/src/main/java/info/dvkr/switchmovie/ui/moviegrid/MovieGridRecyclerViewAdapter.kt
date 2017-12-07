package info.dvkr.switchmovie.ui.moviegrid

import android.content.res.ColorStateList
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import info.dvkr.switchmovie.R
import info.dvkr.switchmovie.domain.model.Movie
import kotlinx.android.synthetic.main.movie_item.view.*


internal class MovieGridRecyclerViewAdapter(private val onItemStarClickListener: (Movie) -> Unit,
                                            private val onItemClickListener: (Movie) -> Unit,
                                            private val onItemLongClickListener: (Movie) -> Boolean,
                                            private val onBottomReachedListener: () -> Unit) :
        RecyclerView.Adapter<MovieGridRecyclerViewAdapter.ViewHolder>() {

    private var movieList: MutableList<Movie> = mutableListOf()

    internal fun setMovieList(newMovieList: List<Movie>) {
        movieList = newMovieList.toMutableList()
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

        fun bind(item: Movie,
                 starListener: (Movie) -> Unit,
                 listener: (Movie) -> Unit,
                 longListener: (Movie) -> Boolean) = with(itemView) {

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