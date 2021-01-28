package com.mat.tracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mat.tracker.databinding.ActivityTrackerBinding

class TrackerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTrackerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackerBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    enum class State {
        TRACING,
        NOT_TRACING
    }
}