package info.dvkr.switchmovie.di

import org.koin.core.qualifier.named

object KoinQualifier {

    val COMPUTATION_COROUTINE_SCOPE = named("COMPUTATION_COROUTINE_SCOPE")
    val IO_COROUTINE_SCOPE = named("IO_COROUTINE_SCOPE")
}