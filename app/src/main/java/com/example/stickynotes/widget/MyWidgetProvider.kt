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
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import androidx.core.graphics.scale
import androidx.core.net.toUri
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
            val defaultTextColor = ContextCompat.getColor(context, R.color.sticky_text)
            val textColor = prefs.getInt("widget_text_color", defaultTextColor)
            val fontSize = prefs.getFloat("widget_font_size", 13f)

            views.setTextViewText(R.id.widget_text_display, userText)
            views.setInt(R.id.widget_background_display, "setBackgroundColor", bgColor)
            views.setTextColor(R.id.widget_text_display, textColor)
            views.setTextViewTextSize(
                R.id.widget_text_display,
                TypedValue.COMPLEX_UNIT_SP,
                fontSize
            )

            var stickerSizeDp = prefs.getInt("widget_sticker_size", 110).toFloat()

            val selectedSize = prefs.getString("widget_size", "4x1") ?: "4x1"

            if (selectedSize == "4x1" && stickerSizeDp > 75f) {
                stickerSizeDp = 75f
            }

            val imgPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                stickerSizeDp,
                context.resources.displayMetrics
            ).toInt()

            prefs.getString("widget_image_uri", null)?.let { uriStr ->
                try {
                    val bitmap = uriToBitmap(context, uriStr.toUri(), imgPx)
                    bitmap?.let {
                        views.setImageViewBitmap(R.id.widget_image_display, it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val alignmentName = prefs.getString("image_alignment", "RIGHT_CENTER") ?: "RIGHT_CENTER"
            val layoutDirection = if (alignmentName == "RIGHT_CENTER")
                View.LAYOUT_DIRECTION_LTR
            else
                View.LAYOUT_DIRECTION_RTL

            views.setInt(R.id.widget_background_display, "setLayoutDirection", layoutDirection)

            val intent = Intent(context, MainActivity::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }

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

    private fun uriToBitmap(context: Context, uri: Uri, targetSizePx: Int): Bitmap? {
        return try {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(
                    ImageDecoder.createSource(context.contentResolver, uri)
                ) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                }
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }

            bitmap.scale(targetSizePx, targetSizePx)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}