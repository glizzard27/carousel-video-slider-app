package com.example.mycarouselsliderapp

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.LoadControl
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.recyclerview.widget.RecyclerView
import com.example.mycarouselsliderapp.databinding.VideoPlayerItemBinding

class VideoAdapter(
    private val videoList: ArrayList<Int>,
    private val context: Context
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
            playingViewHolder = null
        }
        holder.releasePlayer()
    }

    inner class VideoViewHolder(private val binding: VideoPlayerItemBinding) : RecyclerView.ViewHolder(binding.root) {
        private var player: ExoPlayer? = null
        private var currentPosition = 0L
        private var isPlaying = false

        init {
            initializePlayer()
        }

        @OptIn(UnstableApi::class)
        private fun initializePlayer() {

            val loadControl: LoadControl = DefaultLoadControl.Builder()
                .setTargetBufferBytes(10 * 1024 * 1024) // 10 MB target buffer
                .setBufferDurationsMs(30000, 60000, 5000, 10000)
                .build()

            val trackSelector = DefaultTrackSelector(context).apply {
                setParameters(buildUponParameters().setMaxVideoSizeSd())
            }

            player = ExoPlayer.Builder(context.applicationContext)
                .setTrackSelector(trackSelector)
                .setLoadControl(loadControl)
                .build().also { exoPlayer ->
                binding.exoPlayerView.player = exoPlayer
                exoPlayer.playWhenReady = false

                exoPlayer.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        Log.d("VideoViewHolder", "Playback state changed: $playbackState")
                        if (playbackState == Player.STATE_READY) {
                            exoPlayer.seekTo(currentPosition)
                            exoPlayer.playWhenReady = isPlaying
                        } else if (playbackState == Player.STATE_ENDED) {
                            currentPosition = 0L
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        Log.e("VideoAdapter", "Player error: ${error.message}")
                        error.printStackTrace()
                    }
                })

                binding.exoPlayerView.setOnClickListener {
                    if (isPlaying) {
                        pauseVideo()
                    } else {
                        playingViewHolder?.pauseVideo()
                        playVideo()
                        playingViewHolder = this@VideoViewHolder
                    }
                }
            }
        }

        @OptIn(UnstableApi::class)
        fun bind(videoResId: Int) {
            val videoUri = RawResourceDataSource.buildRawResourceUri(videoResId)
            Log.d("VideoAdapter", "Binding video URI: $videoUri")

            // Gunakan DefaultDataSourceFactory untuk memuat video dari res/raw
            val dataSourceFactory = DefaultDataSource.Factory(context.applicationContext)
            val mediaItem = MediaItem.fromUri(videoUri)
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
            player?.setMediaSource(mediaSource)
            player?.prepare()
        }

        fun releasePlayer() {
            Log.d("VideoViewHolder", "Releasing player")
            player?.release()
            player = null
        }

        fun playVideo() {
            Log.d("VideoViewHolder", "Playing video")
            player?.play()
            isPlaying = true
            binding.exoPlayerView.useController = true
        }

        fun pauseVideo() {
            Log.d("VideoViewHolder", "Pausing video")
            player?.pause()
            currentPosition = player?.currentPosition ?: 0L
            isPlaying = false
            binding.exoPlayerView.useController = false
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = VideoPlayerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(videoList[position])
    }

    override fun onViewRecycled(holder: VideoViewHolder) {
        super.onViewRecycled(holder)
        holder.pauseVideo()
        holder.releasePlayer()
    }

    override fun getItemCount(): Int {
        return videoList.size
    }

    fun releaseAllPlayers() {
        activeViewHolders.forEach { it.releasePlayer() }
        activeViewHolders.clear()
    }
}
