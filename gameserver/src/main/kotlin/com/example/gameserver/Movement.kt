package com.example.gameserver

enum class Movement(val value: Int) {
    UP(0), DOWN(1), LEFT(2), RIGHT(3);

    companion object {
        fun fromValue(value: Int) = Movement.entries.find { it.value == value }
    }
}