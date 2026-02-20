package com.example.stickynotes.ui.widget

import android.view.Gravity

enum class WidgetAlignment(val gravity: Int) {
    LEFT_CENTER(Gravity.START or Gravity.CENTER_VERTICAL),
    RIGHT_CENTER(Gravity.END or Gravity.CENTER_VERTICAL),
    TOP_CENTER(Gravity.CENTER_HORIZONTAL or Gravity.TOP),
    BOTTOM_CENTER(Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM)
}