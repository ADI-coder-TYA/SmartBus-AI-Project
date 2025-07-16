package com.example.smartbusai.helperscripts

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.streamease.helper.RetrofitClient

    fun OnProceedButtonPressed(context: Context) {
        val call = RetrofitClient.instance?.api?.getSeats()

        call?.enqueue(object : retrofit2.Callback<String> {
            override fun onResponse(call: retrofit2.Call<String>, response: retrofit2.Response<String>) {
                if (response.isSuccessful) {
                    val serverResponse = response.body()
                    Toast.makeText(context, serverResponse ?: "No response", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Error: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<String>, t: Throwable) {
                Toast.makeText(context, "Failure: ${t.message}", Toast.LENGTH_LONG).show()
                t.message?.let { Log.d("Seats",it) }
            }
        })
    }

