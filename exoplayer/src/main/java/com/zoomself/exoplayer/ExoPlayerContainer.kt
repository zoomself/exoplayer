package com.zoomself.exoplayer

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import com.bumptech.glide.Glide

/**
 * 可以设置图片背景的FrameLayout
 */
class ExoPlayerContainer(context: Context, attributeSet: AttributeSet) :
    FrameLayout(context, attributeSet) {
    init {
        setBackgroundColor(Color.BLACK)
    }

    /**
     * 设置背景缩略图（建议提前计算出缩略图宽高，避免异步计算宽高出现瞬间的闪跳感）
     * @param thumbnailUrl 缩略图路径
     * @param thumbnailWidth 缩略图宽 （0 默认填满容器宽度）
     * @param thumbnailHeight 缩略图高 （0 默认填满容器高度）
     */
    fun setThumbnail(thumbnailUrl: String?, thumbnailWidth: Int = 0, thumbnailHeight: Int = 0) {
        thumbnailUrl?.let {
            if (thumbnailWidth == 0 || thumbnailHeight == 0) {
                //必须异步，要不然计算不出当前容器的宽高
                post {
                    handInternal(it, thumbnailWidth, thumbnailHeight)
                }
            } else {
                handInternal(it, thumbnailWidth, thumbnailHeight)
            }
        }
    }

    private fun handInternal(
        thumbnailUrl: String,
        thumbnailWidth: Int = 0,
        thumbnailHeight: Int = 0
    ) {
        var w = measuredWidth
        var h = height
        if (thumbnailWidth > 0) {
            w = thumbnailWidth
        }
        if (thumbnailHeight > 0) {
            h = thumbnailHeight
        }
        removeAllViews()
        val imageView = ImageView(context)
        addView(imageView)
        configImageView(imageView, thumbnailUrl, w, h)
    }

    private fun configImageView(
        imageView: ImageView, thumbnailUrl: String,
        w: Int,
        h: Int
    ) {
        imageView.scaleType = ImageView.ScaleType.FIT_XY
        val layoutParams = imageView.layoutParams as LayoutParams
        layoutParams.width = w
        layoutParams.height = h
        layoutParams.gravity = Gravity.CENTER
        imageView.layoutParams = layoutParams
        loadImage(imageView = imageView, thumbnailUrl = thumbnailUrl, w = w, h = h)
    }

    private fun loadImage(
        thumbnailUrl: String, imageView: ImageView, w: Int,
        h: Int
    ) {
        Glide.with(imageView)
            .load(thumbnailUrl)
            .override(w, h)
            //.placeholder(R.mipmap.a)
            .into(imageView)
    }

}