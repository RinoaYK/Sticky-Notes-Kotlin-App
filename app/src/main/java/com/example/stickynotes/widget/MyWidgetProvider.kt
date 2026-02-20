package com.example.stickynotes.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.TypedValue
import android.widget.RemoteViews
import androidx.core.graphics.scale
import com.example.stickynotes.R
import com.example.stickynotes.ui.main.MainActivity

open class MyWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {

        val prefs = context.getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE)

        for (appWidgetId in appWidgetIds) {

            val views = RemoteViews(context.packageName, R.layout.widget_sticky_note)

            val userText = prefs.getString("user_text", "Minha Nota")
            val bgColor = prefs.getInt("widget_bg_color", Color.WHITE)

            views.setTextViewText(R.id.widget_text_display, userText)
            views.setInt(R.id.widget_background_display, "setBackgroundColor", bgColor)

            prefs.getString("widget_image_uri", null)?.let { uriStr ->
                try {
                    val bitmap = uriToBitmap(context, Uri.parse(uriStr))
                    bitmap?.let {
                        views.setImageViewBitmap(R.id.widget_image_display, it)
                    }
                } catch (_: Exception) {
                }
            }

            val alignmentName =
                prefs.getString("image_alignment", "RIGHT_CENTER") ?: "RIGHT_CENTER"

            val layoutDirection =
                if (alignmentName == "RIGHT_CENTER")
                    android.view.View.LAYOUT_DIRECTION_LTR
                else
                    android.view.View.LAYOUT_DIRECTION_RTL


            views.setInt(
                R.id.widget_background_display,
                "setLayoutDirection",
                layoutDirection
            )

            val selectedSize = prefs.getString("widget_size", "4x1") ?: "4x1"
            val imgSizeDp = if (selectedSize == "4x1") 80f else 65f

            val imgPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                imgSizeDp,
                context.resources.displayMetrics
            ).toInt()

            views.setInt(R.id.widget_image_display, "setMaxWidth", imgPx)
            views.setInt(R.id.widget_image_display, "setMaxHeight", imgPx)

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    private fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(
                    ImageDecoder.createSource(context.contentResolver, uri)
                )
            } else {
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
            bitmap.scale(300, 300)
        } catch (_: Exception) {
            null
        }
    }
}
