package com.example.mycarouselsliderapp

import android.os.Bundle
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.example.mycarouselsliderapp.databinding.ActivityMainBinding
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private lateinit var adapter: VideoAdapter
    private val videoList = arrayListOf(
        R.raw.video1,
        R.raw.video2,
        R.raw.video3
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        setUpTransformer()
    }

    private fun setUpTransformer() {
        val transformer = CompositePageTransformer()
        transformer.addTransformer(MarginPageTransformer(40))
        transformer.addTransformer { page, position ->
            val r = 1 - abs(position)
            page.scaleY = 0.85f + r * 0.14f
        }

        binding.viewPager2.setPageTransformer(transformer)
    }

    private fun init() {
        adapter = VideoAdapter(videoList, this)
        binding.viewPager2.adapter = adapter
        binding.viewPager2.offscreenPageLimit = 3
        binding.viewPager2.clipToPadding = false
        binding.viewPager2.clipChildren = false
        binding.viewPager2.getChildAt(0)?.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
    }

    @OptIn(UnstableApi::class)
    override fun onDestroy() {
        super.onDestroy()
        binding.viewPager2.adapter = null
        try {
            adapter.releaseAllPlayers()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error releasing players: ${e.message}")
        }
    }
}