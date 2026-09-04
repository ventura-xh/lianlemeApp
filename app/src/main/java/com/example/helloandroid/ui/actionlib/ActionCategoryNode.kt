package com.example.helloandroid.ui.actionlib

import com.example.helloandroid.entity.ActionLibEntity

data class ActionCategoryNode (
    val category: String,
    val subCategories: List<String>,
    val actions: Map<String, List<ActionLibEntity>>  // subCategory -> actions
)