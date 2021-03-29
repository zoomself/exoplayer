package com.zoomself.exoplayer

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.util.EventLogger
import com.google.android.exoplayer2.video.VideoListener


class ExoPlayerManager private constructor() {


    companion object {
        const val TAG = "ExoPlayerManager"

        /**
         * 最大1G缓存
         */
        const val MAX_CACHE_SIZE = 1024 * 1024 * 1024L

        val instance: ExoPlayerManager by lazy {
            return@lazy ExoPlayerManager()
        }
    }

    private lateinit var mContext: Context
    private lateinit var mPlayer: SimpleExoPlayer
    private var mDefaultStyledPlayerViewId: Int = 0
    private lateinit var mStyledPlayerView: StyledPlayerView

    private var mCoverLayout: ViewGroup? = null //提供自定义时使用

    private lateinit var mAnalyticsListener: AnalyticsListener

    private val mEventListener: Player.EventListener = object : Player.EventListener {
        override fun onPlayerError(error: ExoPlaybackException) {
            super.onPlayerError(error)
            handException(error)
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            if (isPlaying) {
                mStyledPlayerView.setCustomErrorMessage(null)
            }
        }
    }


    /**
     * 初始化
     * @param context 使用applicationContext初始化
     * @param defaultStyledPlayerViewLayoutId 默认视频播放ui
     */
    fun init(context: Context, @LayoutRes defaultStyledPlayerViewLayoutId: Int) {
        if (defaultStyledPlayerViewLayoutId == 0) {
            throw IllegalArgumentException("defaultStyledPlayerViewLayoutId:$defaultStyledPlayerViewLayoutId can not be 0")
        }
        if (defaultStyledPlayerViewLayoutId == mDefaultStyledPlayerViewId) {
            Log.e(TAG, "init method has bean called")
            return
        }

        try {
            mContext = context.applicationContext
            mDefaultStyledPlayerViewId = defaultStyledPlayerViewLayoutId
            val trackSelector = ExoPlayerUtils.createTrackSelector(mContext)
            mPlayer = SimpleExoPlayer.Builder(mContext)
                .setMediaSourceFactory(ExoPlayerUtils.createMediaSourceFactory(mContext))
                .setTrackSelector(trackSelector)
                .build()
            mAnalyticsListener = EventLogger(trackSelector)
            mPlayer.addAnalyticsListener(mAnalyticsListener)
            mPlayer.addListener(mEventListener)
            mStyledPlayerView = LayoutInflater.from(mContext)
                .inflate(mDefaultStyledPlayerViewId, null) as StyledPlayerView
            mStyledPlayerView.player = mPlayer
        } catch (e: Exception) {
            Log.e(TAG, "maybe init method has bean called:" + e.message)
        }

    }

    public fun getPlayer(): SimpleExoPlayer {

        return mPlayer
    }


    /**
     * 播放视频
     * @param videoUrl 视频路径
     * @param newContainer 视频播放容器（ExoPlayerContainer）

     * @param playWhenReady 是否播放（默认true,播放）
     * @param styledPlayerViewLayoutId exo播放器布局id，（0，使用默认布局）
     * @param coverLayout 方便配置不同的覆盖层 （默认null,没有任何覆盖布局）
     */
    fun play(
        videoUrl: String,
        newContainer: ViewGroup,
        playWhenReady: Boolean = true,
        @LayoutRes styledPlayerViewLayoutId: Int = 0, coverLayout: ViewGroup?
    ) {
        configUi(styledPlayerViewLayoutId, coverLayout)
        play(videoUrl, newContainer, playWhenReady)
    }

    private fun play(
        videoUrl: String,
        newContainer: ViewGroup,
        playWhenReady: Boolean = true
    ) {
        ExoPlayerUtils.switchSeamless(mStyledPlayerView, newContainer)
        val mediaItemCount = mPlayer.mediaItemCount
        if (mediaItemCount == 0) {
            mPlayer.setMediaItem(MediaItem.fromUri(videoUrl))
            mPlayer.prepare()
        } else if (mediaItemCount > 0) {
            val mediaItemAt = mPlayer.getMediaItemAt(0)
            if (videoUrl != mediaItemAt.mediaId) {
                mPlayer.setMediaItem(MediaItem.fromUri(videoUrl))
                mPlayer.prepare()
            }
        }
        mPlayer.playWhenReady = playWhenReady

    }

    /**
     * 配置播放器ui
     * @param styledPlayerViewLayoutId 方便配置不同的播放器视图 （0,默认就是Init中的视图）
     * @param coverLayout 方便配置不同的覆盖层 （默认没有）
     */
    private fun configUi(@LayoutRes styledPlayerViewLayoutId: Int, coverLayout: ViewGroup?) {
        if (styledPlayerViewLayoutId != 0 && styledPlayerViewLayoutId != mDefaultStyledPlayerViewId) {
            try {
                val styledPlayerView = LayoutInflater.from(mContext)
                    .inflate(styledPlayerViewLayoutId, null) as StyledPlayerView
                StyledPlayerView.switchTargetView(mPlayer, mStyledPlayerView, styledPlayerView)
                mStyledPlayerView = styledPlayerView
                mDefaultStyledPlayerViewId = styledPlayerViewLayoutId
            } catch (e: Exception) {
                Log.e(TAG, "configUi error: " + e.message)
            }
        }
        if (coverLayout != null) {
            val parent = coverLayout.parent
            if (parent != null) {
                val oldViewGroup = parent as ViewGroup
                oldViewGroup.removeView(coverLayout)
            }
            val overlayFrameLayout = mStyledPlayerView.overlayFrameLayout
            overlayFrameLayout?.apply {
                removeAllViews()
                addView(coverLayout)
            }
        }
        mCoverLayout = coverLayout
    }


    private fun handException(error: ExoPlaybackException) {
        if (!ExoPlayerUtils.isNetConnected(mContext)) {
            mStyledPlayerView.setCustomErrorMessage("网络连接异常")
        } else {
            when (error.type) {
                ExoPlaybackException.TYPE_SOURCE -> {
                    mStyledPlayerView.setCustomErrorMessage("视频地址出现异常")
                }
                /*ExoPlaybackException.TYPE_OUT_OF_MEMORY -> {
                    mStyledPlayerView.setCustomErrorMessage("内存溢出异常")
                }
                ExoPlaybackException.TYPE_TIMEOUT -> {
                    mStyledPlayerView.setCustomErrorMessage("连接超时")
                }*/
                ExoPlaybackException.TYPE_RENDERER -> {
                    mStyledPlayerView.setCustomErrorMessage("渲染出现异常")
                }
                else -> {
                    mStyledPlayerView.setCustomErrorMessage("${error.message}")
                }
            }


        }
    }


    fun release() {
        mStyledPlayerView.player = null
        mCoverLayout = null
        mPlayer.removeListener(mEventListener)
        mPlayer.removeAnalyticsListener(mAnalyticsListener)
        mPlayer.release()
    }

}