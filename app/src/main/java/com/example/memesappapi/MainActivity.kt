package com.example.memesappapi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.memesappapi.databinding.ActivityMainBinding
import okhttp3.*
import java.io.IOException
import android.graphics.BitmapFactory
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import org.jsoup.Jsoup
import java.io.BufferedInputStream

class MainActivity : AppCompatActivity(), Callback {

    private lateinit var binding: ActivityMainBinding
    private val client = OkHttpClient()
    private lateinit var memeName: List<String>
    private var TAG_INITIAL = 0
    private lateinit var nameMemeSelected: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getResponse("", "", "")
        binding.btnFetch.setOnClickListener {
            TAG_INITIAL = 1
            val textTop = binding.txtTopMeme.editText?.text.toString().replace(" ", "-")
            val textBottom = binding.txtBottomMeme.editText?.text.toString().replace(" ", "-")
            getResponse(nameMemeSelected, textTop, textBottom)
        }
    }

    private fun makeRequest(meme: String, top: String, bottom: String): Request {
        val pathName = when (TAG_INITIAL) {
            0 -> ""
            else -> "meme"
        }
        val url = HttpUrl.Builder()
            .scheme("https")
            .host("apimeme.com")
            .addPathSegment(pathName)
            .addQueryParameter("meme", meme)
            .addQueryParameter("top", top)
            .addQueryParameter("bottom", bottom)
            .build()

        return Request.Builder().url(url).build()
    }

    private fun getResponse(meme: String, top: String, bottom: String) {
        val request = makeRequest(meme, top, bottom)
        client.newCall(request).enqueue(this)
    }

    override fun onFailure(call: Call, e: IOException) {
        Log.i("MAIN_ACTIVITY", e.message.toString())
    }

    override fun onResponse(call: Call, response: Response) {
        runOnUiThread {
            when (TAG_INITIAL) {
                0 -> {
                    memeName = Jsoup.parse(response.body?.string().toString())
                        .getElementById("meme")?.children()?.map { it.text() }!!.toList()
                    initSpinner(memeName)
                }
                1 -> {
                    val bufferedInputStream = BufferedInputStream(response.body?.byteStream())
                    val bmp = BitmapFactory.decodeStream(bufferedInputStream)
                    binding.imgMeme.setImageBitmap(bmp)
                }
            }
        }
    }

    private fun initSpinner(memeName: List<String>) {
        val mAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, memeName)
        binding.spinnerMemes.apply {
            adapter = mAdapter
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    nameMemeSelected = memeName[p2]
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }
        }
    }
}