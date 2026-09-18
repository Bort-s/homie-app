package com.example.homieapp.model

import android.graphics.Color

data class Guides(
    val title: String,
    val number: Int,
    val chapter: String,
    val blocks: List<Block>
)

data class Block(
    val title: String,
    val content: String,
)