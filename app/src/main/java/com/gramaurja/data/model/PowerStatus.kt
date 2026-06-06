package com.gramaurja.data.model

/** Power status enum */
enum class PowerStatus {
    ON, OFF, UNKNOWN;

    fun displayName(): String = when (this) {
        ON -> "Power ON"
        OFF -> "Power OFF"
        UNKNOWN -> "Unknown"
    }

    fun isOn(): Boolean = this == ON
}
