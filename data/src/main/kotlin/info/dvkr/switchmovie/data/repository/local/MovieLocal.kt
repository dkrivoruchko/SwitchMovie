package info.dvkr.switchmovie.data.repository.local

import androidx.room.*
import info.dvkr.switchmovie.domain.model.Movie
import kotlinx.coroutines.flow.Flow

object MovieLocal {

    @Entity(tableName = "movie_table")
    data class MovieDb(
        @PrimaryKey(autoGenerate = false) @ColumnInfo(name = "id") val id: Int = 0,
        @ColumnInfo(name = "posterPath") val posterPath: String = "",
        @ColumnInfo(name = "title") val title: String = "",
        @ColumnInfo(name = "overview") val overview: String = "",
        @ColumnInfo(name = "releaseDate") val releaseDate: String = "",
        @ColumnInfo(name = "voteAverage") val voteAverage: String = "",
        @ColumnInfo(name = "popularity") val popularity: Float = 0.0F,
        @ColumnInfo(name = "isStar") val isStar: Boolean = false
    )

    object MovieConverter {
        @TypeConverter
        fun fromMovieToMovieDb(movie: Movie): MovieDb =
            movie.run {
                MovieDb(
                    id, posterPath, title, overview, releaseDate, voteAverage, popularity, isStar
                )
            }

        @TypeConverter
        fun fromMovieDbToMovie(movieDb: MovieDb): Movie =
            movieDb.run {
                Movie(
                    id, posterPath, title, overview, releaseDate, voteAverage, popularity, isStar
                )
            }
    }

    @Dao
    interface MovieDao {
        @Query("SELECT * FROM movie_table where id = :movieId")
        suspend fun getMovieById(movieId: Int): Movie?

        @Query("SELECT * FROM movie_table where id = :movieId")
        fun getMovieFlowById(movieId: Int): Flow<Movie>

        @Query("SELECT * FROM movie_table ORDER BY popularity DESC")
        fun getAll(): Flow<List<Movie>>

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insert(movie: MovieDb)

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertAll(movies: List<MovieDb>)

        @Query("DELETE FROM movie_table")
        suspend fun deleteAll()
    }
}