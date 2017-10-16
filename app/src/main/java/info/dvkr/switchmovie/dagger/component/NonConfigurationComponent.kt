package info.dvkr.switchmovie.dagger.component

import dagger.Subcomponent
import info.dvkr.switchmovie.data.dagger.PersistentScope
import info.dvkr.switchmovie.ui.moviedetail.MovieDetailActivity
import info.dvkr.switchmovie.ui.moviegrid.MovieGridActivity

@PersistentScope
@Subcomponent
interface NonConfigurationComponent {

    fun inject(activity: MovieGridActivity)

    fun inject(activity: MovieDetailActivity)
}