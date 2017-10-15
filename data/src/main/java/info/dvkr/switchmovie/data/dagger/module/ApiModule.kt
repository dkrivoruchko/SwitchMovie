package info.dvkr.switchmovie.data.dagger.module

import dagger.Module
import dagger.Provides
import info.dvkr.switchmovie.data.movie.repository.api.ApiRepository
import info.dvkr.switchmovie.data.movie.repository.api.ApiService
import info.dvkr.switchmovie.domain.BuildConfig
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Singleton
@Module
class ApiModule {
    @Singleton
    @Provides
    fun provideApiService(): ApiService {
        val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BuildConfig.BASE_API_URL)
                .build()

        return retrofit.create(ApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideApiRepository(apiService: ApiService) = ApiRepository(apiService, BuildConfig.API_KEY)
}