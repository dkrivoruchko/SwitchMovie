package info.dvkr.switchmovie.data.repository.local

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import info.dvkr.switchmovie.domain.model.Movie

object MovieLocal {

    @Entity(tableName = "movie_table")
    data class MovieDb(
        @PrimaryKey(autoGenerate = false) @ColumnInfo(name = "id") var id: Int = 0,
        @ColumnInfo(name = "posterPath") var posterPath: String = "",
        @ColumnInfo(name = "title") var title: String = "",
        @ColumnInfo(name = "overview") var overview: String = "",
        @ColumnInfo(name = "releaseDate") var releaseDate: String = "",
        @ColumnInfo(name = "voteAverage") var voteAverage: String = "",
        @ColumnInfo(name = "popularity") var popularity: Float = 0.0F,
        @ColumnInfo(name = "isStar") var isStar: Boolean = false
    )

    object MovieConverter {
        @TypeConverter
        fun fromMovieToMovieDb(movie: Movie): MovieDb =
            with(movie) { MovieDb(id, posterPath, title, overview, releaseDate, voteAverage, popularity, isStar) }

        @TypeConverter
        fun fromMovieDbToMovie(movieDb: MovieDb): Movie =
            with(movieDb) { Movie(id, posterPath, title, overview, releaseDate, voteAverage, popularity, isStar) }
    }

    @Dao
    @TypeConverters(MovieConverter::class)
    interface MovieDao {
        @Query("SELECT * FROM movie_table where id = :movieId")
        fun getMovieById(movieId: Int): Movie?

        @Query("SELECT * FROM movie_table where id = :movieId")
        fun getMovieByIdLiveData(movieId: Int): LiveData<Movie>

        @Query("SELECT * FROM movie_table ORDER BY popularity DESC")
        fun getAll(): LiveData<List<Movie>>

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun insert(movie: MovieDb)

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun insertAll(movies: List<MovieDb>)

        @Query("DELETE FROM movie_table")
        fun deleteAll()
    }
}