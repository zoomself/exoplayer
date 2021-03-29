package com.zoomself.exoplayer

import android.content.Context
import android.net.ConnectivityManager
import android.os.Environment
import android.util.Log
import android.view.ViewGroup
import androidx.core.view.children
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import java.io.File

/**
 * des :
 * @author : zoomself
 * 2021/3/29 14:02
 */
object ExoPlayerUtils {

    /**
     * 无缝切换容器
     * @param newContainer 新播放器容器
     */
    @JvmStatic
    fun switchSeamless(
        styledPlayerView: StyledPlayerView,
        newContainer: ViewGroup,
    ) {
        val parent = styledPlayerView.parent
        if (parent != null) {
            //防止把播放异常消息带入另一个播放容器内
            styledPlayerView.setCustomErrorMessage(null)
            val oldContainer = parent as ViewGroup
            if (oldContainer == newContainer) {
                return
            }

            oldContainer.removeView(styledPlayerView)
        }
        newContainer.addView(styledPlayerView)
    }

    /**
     * 创建轨道选择
     */
    @JvmStatic
    fun createTrackSelector(context: Context): DefaultTrackSelector {
        return DefaultTrackSelector(context)
    }

    /**
     * 创建媒体源工厂
     * 实现边播放边缓存
     */
    @JvmStatic
    fun createMediaSourceFactory(context: Context): MediaSourceFactory {
        val defaultDataSourceFactory = DefaultDataSourceFactory(context)
        val labelRes = context.applicationInfo.labelRes
        val appName = context.resources.getString(labelRes)
        var cacheFile = File(context.cacheDir, "exo_$appName")
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            context.externalCacheDir?.let {
                cacheFile = File(it, "exo_$appName")
            }
        }
        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(
                SimpleCache(
                    cacheFile,
                    LeastRecentlyUsedCacheEvictor(ExoPlayerManager.MAX_CACHE_SIZE),
                )
            )
            .setUpstreamDataSourceFactory(defaultDataSourceFactory)
        return DefaultMediaSourceFactory(cacheDataSourceFactory)
    }

    @JvmStatic
    fun isNetConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val infoList = connectivityManager.allNetworkInfo
        for (ni in infoList) {
            if (ni.isConnected) {
                return true
            }
        }
        return false
    }

}