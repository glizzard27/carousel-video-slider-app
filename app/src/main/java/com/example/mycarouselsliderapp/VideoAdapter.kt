package com.example.mycarouselsliderapp

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.mycarouselsliderapp.databinding.VideoPlayerItemBinding

class VideoAdapter(
    private val videoList: List<Int>,
    private val context: Context,
    private val viewPager2: ViewPager2

) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {
    private var playingViewHolder: VideoViewHolder? = null
    private val activeViewHolders = mutableListOf<VideoViewHolder>()

    override fun onViewAttachedToWindow(holder: VideoViewHolder) {
        super.onViewAttachedToWindow(holder)
        activeViewHolders.add(holder)
    }

    override fun onViewDetachedFromWindow(holder: VideoViewHolder) {
        super.onViewDetachedFromWindow(holder)
        activeViewHolders.remove(holder)
        if (playingViewHolder == holder) {
            playingViewHolder = null // Reset jika ViewHolder yang sedang diputar keluar dari layar
        }
        holder.releasePlayer()
    }
    inner class VideoViewHolder(private val binding: VideoPlayerItemBinding) : RecyclerView.ViewHolder(binding.root) {
        private var player: ExoPlayer? = null

        @OptIn(UnstableApi::class)
        fun bind(videoResId: Int, position: Int, currentItem: Any) { // Tambahkan parameter position
            val videoUri = RawResourceDataSource.buildRawResourceUri(videoResId)
            val mediaItem = MediaItem.fromUri(videoUri)

            player = ExoPlayer.Builder(context).build().also { exoPlayer ->
                binding.exoPlayerView.player = exoPlayer
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()

                // Set listener untuk perubahan status pemutaran
                exoPlayer.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_READY) {
                            exoPlayer.playWhenReady = binding.exoPlayerView.isAttachedToWindow && position == currentItem
                        } else {
                            exoPlayer.playWhenReady = false
                        }
                    }
                })

                // Click listener untuk kontrol manual (jika diperlukan)
                binding.exoPlayerView.setOnClickListener {
                    if (player?.isPlaying == true) {
                        pauseVideo()
                        playingViewHolder = null
                    } else {
                        playVideo()
                    }
                    playingViewHolder = this
                }
            }
        }


        fun releasePlayer() {
            player?.release()
            player = null
        }

        @OptIn(UnstableApi::class)
        fun playVideo() {
            player?.play()
            binding.exoPlayerView.showController()
        }

        @OptIn(UnstableApi::class)
        fun pauseVideo() {
            player?.pause()
            binding.exoPlayerView.hideController()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = VideoPlayerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(videoList[position], position, viewPager2.currentItem)
    }

    override fun onViewRecycled(holder: VideoViewHolder) {
        super.onViewRecycled(holder)
        holder.releasePlayer()
    }

    override fun getItemCount(): Int = videoList.size

    fun releaseAllPlayers() {
        activeViewHolders.forEach { it.releasePlayer() }
        activeViewHolders.clear()
    }
}
