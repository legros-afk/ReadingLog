package com.flo.readinglog.di

import com.flo.readinglog.data.remote.AnthropicService
import com.flo.readinglog.data.remote.GoogleBooksService
import com.flo.readinglog.data.repository.GoogleBooksRepositoryImpl
import com.flo.readinglog.data.repository.RecommendationsRepositoryImpl
import com.flo.readinglog.domain.repository.GoogleBooksRepository
import com.flo.readinglog.domain.repository.RecommendationsRepository
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GoogleBooksRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AnthropicRetrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    @Provides
    @Singleton
    @GoogleBooksRetrofit
    fun provideGoogleBooksRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/books/v1/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideGoogleBooksService(@GoogleBooksRetrofit retrofit: Retrofit): GoogleBooksService =
        retrofit.create(GoogleBooksService::class.java)

    @Provides
    @Singleton
    fun provideGoogleBooksRepository(impl: GoogleBooksRepositoryImpl): GoogleBooksRepository = impl

    @Provides
    @Singleton
    @AnthropicRetrofit
    fun provideAnthropicRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api.anthropic.com/v1/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideAnthropicService(@AnthropicRetrofit retrofit: Retrofit): AnthropicService =
        retrofit.create(AnthropicService::class.java)

    @Provides
    @Singleton
    fun provideRecommendationsRepository(impl: RecommendationsRepositoryImpl): RecommendationsRepository = impl
}
