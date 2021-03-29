package com.zoomself.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.zoomself.exoplayer.ExoPlayerContainer
import com.zoomself.exoplayer.ExoPlayerManager

class MainActivity : AppCompatActivity() {
    private lateinit var epc1: ExoPlayerContainer
    private lateinit var epc2: ExoPlayerContainer
    private lateinit var btChange: Button

    companion object {
        const val video_url_1 = "http://vfx.mtime.cn/Video/2019/02/04/mp4/190204084208765161.mp4"
        const val video_url_2 = "http://vfx.mtime.cn/Video/2019/03/19/mp4/190319212559089721.mp4"

        const val pic1 = "https://t7.baidu.com/it/u=2040182115,3142469309&fm=193&f=GIF"
        const val pic2 = "https://t7.baidu.com/it/u=2515140172,193092332&fm=193&f=GIF"
    }

    private var mCurrentPosition = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        epc1 = findViewById(R.id.epc1)
        epc2 = findViewById(R.id.epc2)
        epc1.setThumbnail(pic1)
        epc2.setThumbnail(pic2)

        btChange = findViewById(R.id.bt_change)
        btChange.setOnClickListener {
            mCurrentPosition++
            play()
        }


    }

    private fun play() {
        val videoUrl = if (mCurrentPosition % 2 == 0) {
            video_url_1
        } else {
            video_url_2
        }
        val newContainer = if (mCurrentPosition % 2 == 0) {
            epc1
        } else {
            epc2
        }

        ExoPlayerManager.instance.play(
            videoUrl,
            newContainer,
            true,
            0,
            null
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        ExoPlayerManager.instance.release()
    }
}