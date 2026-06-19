package com.firefly.controller.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.firefly.controller.data.api.FireflyApi
import com.firefly.controller.data.model.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class FireflyRepository(context: Context) {
    
    companion object {
        const val DEFAULT_SERVER = "http://192.168.1.215:5000"
        const val DEFAULT_SERVER_REMOTE = "http://120.48.26.76:25000"
        const val PREFS_NAME = "firefly_prefs"
        const val KEY_SERVER = "server_url"
        const val KEY_TOKEN = "auth_token"
    }
    
    private var serverUrl: String
    private var authToken: String
    private val prefs: SharedPreferences
    private var api: FireflyApi? = null
    
    init {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        
        prefs = EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        
        serverUrl = prefs.getString(KEY_SERVER, DEFAULT_SERVER_REMOTE) ?: DEFAULT_SERVER_REMOTE
        authToken = prefs.getString(KEY_TOKEN, "firefly_token_2024") ?: "firefly_token_2024"
        
        buildApi()
    }
    
    private fun buildApi() {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $authToken")
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
        
        val retrofit = Retrofit.Builder()
            .baseUrl(serverUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        api = retrofit.create(FireflyApi::class.java)
    }
    
    fun updateServer(url: String) {
        serverUrl = if (url.endsWith("/")) url.dropLast(1) else url
        prefs.edit().putString(KEY_SERVER, serverUrl).apply()
        buildApi()
    }
    
    fun getServerUrl() = serverUrl
    fun getToken() = authToken
    
    suspend fun checkHealth(): Boolean {
        return try {
            api?.health() != null
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun getStatus(): Result<StatusResponse> {
        return try {
            val response = api?.getStatus()
            if (response?.success == true) {
                Result.success(response)
            } else {
                Result.failure(Exception(response?.error ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
