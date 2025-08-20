package com.example.streamease.helper

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitClient {
    private val builder = OkHttpClient.Builder()

    private val interceptor: HttpLoggingInterceptor = HttpLoggingInterceptor()


    init {

        interceptor.level = HttpLoggingInterceptor.Level.BODY
        builder.addInterceptor(interceptor)


        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(builder.build())
            .build()
    }


    val api: Api
        get() = retrofit.create(Api::class.java)

    companion object {
        private const val BASE_URL = "http://10.0.2.2:8000/"  //for virtual android devices replace everything before:8000 to PC ip adress for real phones and connect the phone to same wifi and also change xml network security config file

        private var retrofitClient: RetrofitClient? = null
        private lateinit var retrofit: Retrofit

        @get:Synchronized
        val instance: RetrofitClient?
            get() {
                if (retrofitClient == null) {
                    retrofitClient = RetrofitClient()
                }
                return retrofitClient
            }
    }
}
