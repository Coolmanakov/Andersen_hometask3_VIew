package com.example.andersen_hometask3_view.task2

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.andersen_hometask3_view.R
import java.lang.Exception

class LoadImage : AppCompatActivity() {

    private var mEditText : EditText? = null
    private var mImage : ImageView? = null

    companion object{
        private var LOG_TAG = LoadImage::class.java.canonicalName
        private var ERROR_TOAST = "Such Image not found \n Check Internet Connection"
        private var NO_INTERNET_TOAST = "No Internet Connection"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load_image)

        mEditText = findViewById(R.id.mEditText)
        mImage = findViewById(R.id.imageView)
    }

    override fun onResume() {
        super.onResume()

        initListener(mEditText!!)
    }


    /**
     * Загружаем картинки из интеренета с помощью Glide
     * */
    private fun loadImageWithGlide(link : String){
        Glide
            .with(applicationContext)
            .load(link)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.e(LOG_TAG, "$ERROR_TOAST ${e.toString()}" )
                    Toast.makeText(applicationContext, ERROR_TOAST, Toast.LENGTH_LONG).show()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    // Image already loaded
                    return false
                }
            })
            .into(mImage!!)
    }
    /**
     * Load image by url without using external libraries
     * Start Thread, which responsible for getting bitmap
     * Then run ui thread, checking exception
     *
     * if exception is not null -> make toast
     * else load bitmap into image
     * */
    private fun loadImageWithoutExternalLibraries(url : String){
        var bitmap : Bitmap? = null
        var exception : String? = null
        Thread {
            try {
                val openStream =
                    java.net.URL(url).openStream()
                bitmap = BitmapFactory.decodeStream(openStream)
            } catch (e: Exception) {
                exception = e.message.toString()
                Log.e(LOG_TAG, exception!!)
            }

            runOnUiThread {
                if (exception == null) mImage?.setImageBitmap(bitmap)
                else Toast.makeText(applicationContext, ERROR_TOAST, Toast.LENGTH_SHORT).show()
            }
        }.start()
    }


    /**
    * Init listener for editText
     * !!!Important!!!
     * LOADING IMAGE function called inside listener
     * */
    private fun initListener(textView: TextView){
        textView.setOnEditorActionListener { textView, id, keyEvent ->
            //Если пользователь нажал на кнопку DONE, NEXT или ENTER
            if (id == EditorInfo.IME_ACTION_DONE ||
                id == EditorInfo.IME_ACTION_SEARCH ||
                keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_DOWN &&
                keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER
            ) {

                if (keyEvent == null || !keyEvent.isShiftPressed) {
                    val link = textView.text.toString()

                    if(isOnline()){
                        /**
                         * CHOOSE WAY TO LOAD IMAGE
                         * */
//                    loadImageWithGlide(link)
                        loadImageWithoutExternalLibraries(link)
                    }
                    else Toast.makeText(applicationContext, NO_INTERNET_TOAST, Toast.LENGTH_SHORT).show()

                    true
                }
            }
            false
        }
    }

    private fun isOnline() : Boolean{
        val connectManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connectManager.getNetworkCapabilities(connectManager.activeNetwork)

        if(capabilities != null){
            if(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)){
                return true
            }
        }
        return false
    }


}