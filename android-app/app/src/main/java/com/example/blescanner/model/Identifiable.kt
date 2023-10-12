package com.example.blescanner.model

interface Identifiable<out T> {
    val id: T
}