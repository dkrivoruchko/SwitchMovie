package info.dvkr.switchmovie.data.repository.local

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import info.dvkr.switchmovie.domain.model.Movie

object MovieLocal {

    @Entity(tableName = "movie_table")
    data class MovieDb(
        @PrimaryKey(autoGenerate = false) @ColumnInfo(name = "id") var id: Int = 0,
        @ColumnInfo(name = "poster_path") var posterPath: String = "",
        @ColumnInfo(name = "title") var title: String = "",
        @ColumnInfo(name = "overview") var overview: String = "",
        @ColumnInfo(name = "release_date") var releaseDate: String = "",
        @ColumnInfo(name = "vote_average") var voteAverage: String = "",
        @ColumnInfo(name = "popularity") var popularity: Float = 0.0F,
        @ColumnInfo(name = "is_star") var isStar: Boolean = false
    )

    object MovieConverter {
        @TypeConverter // TODO
        fun fromMovieToMovieDb(movie: Movie) =
            with(movie) { MovieDb(id, posterPath, title, overview, releaseDate, voteAverage, popularity, isStar) }

        @TypeConverter // TODO
        fun fromMovieDbToMovie(movieDb: MovieDb) =
            with(movieDb) { Movie(id, posterPath, title, overview, releaseDate, voteAverage, popularity, isStar) }
    }

    @Dao
    interface MovieDao {
        @Query("SELECT * FROM movie_table where id = :arg0")
        fun getMovieById(movieId: Int): MovieDb?

        @Query("SELECT * FROM movie_table where id = :movieId")
        fun getMovieByIdLiveData(movieId: Int): LiveData<MovieDb>

        @Query("SELECT * FROM movie_table ORDER BY popularity DESC")
        fun getAll(): LiveData<List<MovieDb>> // TODO Not Lvedata

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun insert(movie: MovieDb)

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun insertAll(movies: List<MovieDb>)

        @Query("DELETE FROM movie_table")
        fun deleteAll()
    }
}