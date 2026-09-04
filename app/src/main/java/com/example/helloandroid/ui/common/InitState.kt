package com.example.helloandroid.ui.common

sealed class InitState {
    object Idle : InitState()
    object Loading : InitState()
    data class Error(val message: String) : InitState()
    object Success : InitState()
}