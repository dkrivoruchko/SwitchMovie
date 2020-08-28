package info.dvkr.switchmovie.data.repository.local

import androidx.room.*
import info.dvkr.switchmovie.domain.model.Movie
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate


@Entity(tableName = "movie_table")
data class MovieDb(
    @PrimaryKey(autoGenerate = false) @ColumnInfo(name = "id") val id: Long = 0,
    @ColumnInfo(name = "posterPath") val posterPath: String = "",
    @ColumnInfo(name = "title") val title: String = "",
    @ColumnInfo(name = "overview") val overview: String = "",
    @ColumnInfo(name = "releaseDate") val releaseDate: LocalDate = LocalDate.now(),
    @ColumnInfo(name = "voteAverage") val voteAverage: String = "",
    @ColumnInfo(name = "popularity") val popularity: Float = 0.0F,
    @ColumnInfo(name = "isStar") val isStar: Boolean = false
) {
    companion object {
        fun fromMovie(movie: Movie): MovieDb =
            movie.run { MovieDb(id, posterPath, title, overview, releaseDate, voteAverage, popularity, isStar) }
    }

    fun toMovie(): Movie = Movie(id, posterPath, title, overview, releaseDate, voteAverage, popularity, isStar)
}

class LocalDateConverter {
    @TypeConverter
    fun fromLocalDateToString(localDate: LocalDate): String = localDate.toString()

    @TypeConverter
    fun fromStringToLocalDate(localDateString: String): LocalDate = LocalDate.parse(localDateString)
}

@Dao
interface MovieDao {
    @Query("SELECT * FROM movie_table where id = :movieId")
    suspend fun getMovieById(movieId: Long): MovieDb?

    @Query("SELECT * FROM movie_table where id = :movieId")
    fun getMovieFlowById(movieId: Long): Flow<MovieDb>

    @Query("SELECT * FROM movie_table ORDER BY popularity DESC")
    fun getAll(): Flow<List<MovieDb>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(movie: MovieDb)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(movies: List<MovieDb>)

    @Query("DELETE FROM movie_table")
    suspend fun deleteAll()
}