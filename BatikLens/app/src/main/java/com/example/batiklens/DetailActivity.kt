package com.example.batiklens

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.batiklens.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {
    var imageUrl = ""
    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = intent.extras
        if (bundle != null) {
            binding.detailDesc.text = bundle.getString("Description")
            binding.detailTitle.text = bundle.getString("Title")
            imageUrl = bundle.getString("Image")!!
            Glide.with(this).load(bundle.getString("Image"))
                .into(binding.detailImage)
        }
    }
}