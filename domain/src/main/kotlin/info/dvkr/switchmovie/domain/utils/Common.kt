package info.dvkr.switchmovie.domain.utils

fun Any.getLog(tag: String? = "", msg: String? = "Invoked") =
    "${this.javaClass.simpleName}#${this.hashCode()}.$tag@${Thread.currentThread().name}: $msg"