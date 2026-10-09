package com.example.helloandroid.ui.common

enum class WeightUnit(val label: String) {
    KG("kg"),
    LB("lb");

    companion object {
        fun fromLabel(label: String): WeightUnit =
            entries.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: KG
    }
}