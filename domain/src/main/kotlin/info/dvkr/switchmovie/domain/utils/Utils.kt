package info.dvkr.switchmovie.domain.utils

object Utils {

    // TODO
    fun getLogPrefix(obj: Any): String = "${obj.javaClass.simpleName}@${Thread.currentThread().name}#${obj.hashCode()}"


    /**
     * Sets the value to the result of a function that is called when both `LiveData`s have data
     * or when they receive updates after that.
     */
//    fun <T, A, B> LiveData<A>.combineAndCompute(other: LiveData<B>, onChange: (A, B) -> T): MediatorLiveData<T> {
//
//        var source1emitted = false
//        var source2emitted = false
//
//        val result = MediatorLiveData<T>()
//
//        val mergeF = {
//            val source1Value = this.value
//            val source2Value = other.value
//
//            if (source1emitted && source2emitted) {
//                result.value = onChange.invoke(source1Value!!, source2Value!!)
//            }
//        }
//
//        result.addSource(this) { source1emitted = true; mergeF.invoke() }
//        result.addSource(other) { source2emitted = true; mergeF.invoke() }
//
//        return result
//    }
//
//    fun <T> LiveData<T>.getDistinct(): LiveData<T> {
//        val distinctLiveData = MediatorLiveData<T>()
//        distinctLiveData.addSource(this, object : Observer<T> {
//            private var initialized = false
//            private var lastObj: T? = null
//
//            override fun onChanged(obj: T?) {
//                if (!initialized) {
//                    initialized = true
//                    lastObj = obj
//                    distinctLiveData.postValue(lastObj)
//                } else if ((obj == null && lastObj != null) || obj != lastObj) {
//                    lastObj = obj
//                    distinctLiveData.postValue(lastObj)
//                }
//            }
//        })
//
//        return distinctLiveData
//    }
}

