package info.dvkr.switchmovie.data.viewmodel

import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch

abstract class BaseViewStateViewModel : BaseViewModel() {

    inline fun runEffect(crossinline block: suspend () -> Unit) {
        launch(coroutineContext + Job(parentJob)) { block.invoke() }
    }
}