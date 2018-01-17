package info.dvkr.switchmovie.data.movie.repository.local

import com.ironz.binaryprefs.serialization.serializer.persistable.Persistable
import com.ironz.binaryprefs.serialization.serializer.persistable.io.DataInput
import com.ironz.binaryprefs.serialization.serializer.persistable.io.DataOutput

object MovieLocal {
    data class LocalMovie(internal var id: Int = 0,
                          internal var posterPath: String = "",
                          internal var title: String = "",
                          internal var overview: String = "",
                          internal var releaseDate: String = "",
                          internal var voteAverage: String = "",
                          internal var isStar: Boolean = false) : Persistable {

        companion object {
            const val LOCAL_MOVIE_KEY = "LOCAL_MOVIE_KEY"
        }

        override fun writeExternal(dataOutput: DataOutput) {
            dataOutput.writeInt(id)
            dataOutput.writeString(posterPath)
            dataOutput.writeString(title)
            dataOutput.writeString(overview)
            dataOutput.writeString(releaseDate)
            dataOutput.writeString(voteAverage)
            dataOutput.writeBoolean(isStar)
        }

        override fun readExternal(dataInput: DataInput) {
            id = dataInput.readInt()
            posterPath = dataInput.readString()
            title = dataInput.readString()
            overview = dataInput.readString()
            releaseDate = dataInput.readString()
            voteAverage = dataInput.readString()
            isStar = dataInput.readBoolean()
        }

        override fun deepClone() = LocalMovie(id, posterPath, title, overview, releaseDate, voteAverage, isStar)

        override fun toString() = "LocalMovie(id=$id)"
    }

    data class LocalList(internal var items: List<LocalMovie> = emptyList()) : Persistable {

        companion object {
            const val LOCAL_LIST_KEY = "LOCAL_LIST_KEY"
        }

        override fun writeExternal(dataOutput: DataOutput) {
            dataOutput.writeInt(items.size)
            items.forEach { it.writeExternal(dataOutput) }
        }

        override fun readExternal(dataInput: DataInput) {
            val size = dataInput.readInt()
            items = (0 until size).map { LocalMovie().apply { readExternal(dataInput) } }
        }

        override fun deepClone() = LocalList(items.map { it.deepClone() })
    }
}