package com.zoomself.myapplication

import android.app.Application
import com.zoomself.exoplayer.ExoPlayerManager

/**
 * des :
 * @author : zoomself
 * 2021/3/29 14:34
 */
class App: Application() {
    override fun onCreate() {
        super.onCreate()
        ExoPlayerManager.instance.init(this,R.layout.styled_player_view_default)
    }
}