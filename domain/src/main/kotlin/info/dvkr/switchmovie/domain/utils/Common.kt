package info.dvkr.switchmovie.domain.utils

fun Any.getTag(name: String? = "") = "${this.javaClass.simpleName}.$name@${Thread.currentThread().name}"