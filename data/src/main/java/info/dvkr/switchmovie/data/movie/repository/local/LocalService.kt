package info.dvkr.switchmovie.data.movie.repository.local

import com.ironz.binaryprefs.serialization.serializer.persistable.Persistable
import com.ironz.binaryprefs.serialization.serializer.persistable.io.DataInput
import com.ironz.binaryprefs.serialization.serializer.persistable.io.DataOutput

interface LocalService {
    data class LocalMovie(internal var id: Int = 0,
                          internal var posterPath: String = "",
                          internal var originalTitle: String = "",
                          internal var overview: String = "",
                          internal var releaseDate: String = "",
                          internal var voteAverage: String = "") : Persistable {

        companion object {
            const val LOCAL_MOVIE_KEY = "LOCAL_MOVIE_KEY"
        }

        override fun writeExternal(dataOutput: DataOutput) {
            dataOutput.writeInt(id)
            dataOutput.writeString(posterPath)
            dataOutput.writeString(originalTitle)
            dataOutput.writeString(overview)
            dataOutput.writeString(releaseDate)
            dataOutput.writeString(voteAverage)
        }

        override fun readExternal(dataInput: DataInput) {
            id = dataInput.readInt()
            posterPath = dataInput.readString()
            originalTitle = dataInput.readString()
            overview = dataInput.readString()
            releaseDate = dataInput.readString()
            voteAverage = dataInput.readString()
        }

        override fun deepClone() = LocalMovie(id, posterPath, originalTitle, overview, releaseDate, voteAverage)
    }

    data class LocalList(internal val items: MutableList<LocalMovie> = mutableListOf()) : Persistable {

        companion object {
            const val LOCAL_LIST_KEY = "LOCAL_LIST_KEY"
        }

        override fun writeExternal(dataOutput: DataOutput) {
            val size = items.size
            dataOutput.writeInt(size)
            items.forEach { writeExternal(dataOutput) }
        }

        override fun readExternal(dataInput: DataInput) {
            val size = dataInput.readInt()
            for (i in 0..size) {
                items.add(LocalMovie().apply { readExternal(dataInput) })
            }

        }

        override fun deepClone(): LocalList {
            val newLocalList = LocalList()
            items.forEach {
                newLocalList.items.add(it.deepClone())
            }
            return newLocalList
        }
    }
}