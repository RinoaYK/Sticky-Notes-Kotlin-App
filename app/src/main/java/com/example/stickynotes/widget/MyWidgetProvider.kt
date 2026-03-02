package com.example.stickynotes.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import androidx.core.net.toUri
import androidx.room.Room
import com.example.stickynotes.R
import com.example.stickynotes.data.local.WidgetDatabase
import com.example.stickynotes.data.mapper.toDomain
import com.example.stickynotes.domain.model.WidgetNote
import com.example.stickynotes.ui.main.MainActivity
import com.example.stickynotes.util.uriToBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class MyWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val db = Room.databaseBuilder(
                context.applicationContext,
                WidgetDatabase::class.java,
                "sticky_notes.db"
            ).fallbackToDestructiveMigration().build()

            try {
                appWidgetIds.forEach { appWidgetId ->
                    val entity = db.widgetDao().getWidgetById(appWidgetId)

                    val views = if (entity != null) {
                        createRemoteViews(context, entity.toDomain())
                    } else {
                        createEmptyViews(context, appWidgetId)
                    }

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            } finally {
                db.close()
            }
        }
    }

    private fun createRemoteViews(context: Context, note: WidgetNote): RemoteViews {
        val layout = if (note.layoutSize == "5x2") R.layout.widget_sticky_5x2
        else R.layout.widget_sticky_4x1

        return RemoteViews(context.packageName, layout).apply {
            setTextViewText(R.id.widget_text_display, note.text)
            setInt(R.id.widget_background_display, "setBackgroundColor", note.bgColor)
            setTextColor(R.id.widget_text_display, note.textColor)
            setTextViewTextSize(R.id.widget_text_display, TypedValue.COMPLEX_UNIT_SP, note.fontSize)

            val dir = if (note.imageAlignment == "LEFT_CENTER")
                View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
            setInt(R.id.widget_background_display, "setLayoutDirection", dir)

            note.imageUri?.let { uriStr ->
                try {
                    val px = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, note.stickerSize.toFloat(),
                        context.resources.displayMetrics
                    ).toInt()
                    val bmp = uriToBitmap(context, uriStr.toUri(), px)
                    bmp?.let { setImageViewBitmap(R.id.widget_image_display, it) }
                } catch (_: Exception) {
                }
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, note.id)
                data = toUri(Intent.URI_INTENT_SCHEME).toUri()
            }
            val pi = PendingIntent.getActivity(
                context, note.id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            setOnClickPendingIntent(R.id.widget_root, pi)
        }
    }

    private fun createEmptyViews(context: Context, appWidgetId: Int): RemoteViews {
        return RemoteViews(context.packageName, R.layout.widget_sticky_4x1).apply {

            val emptyMessage = context.getString(R.string.widget_empty_state)
            setTextViewText(R.id.widget_text_display, emptyMessage)
            setInt(R.id.widget_background_display, "setBackgroundColor", Color.WHITE)

            val intent = Intent(context, MainActivity::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = toUri(Intent.URI_INTENT_SCHEME).toUri()
            }
            val pi = PendingIntent.getActivity(
                context, appWidgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            setOnClickPendingIntent(R.id.widget_root, pi)
        }
    }

    override fun onReceive(context: Context, intent: Intent?) {
        super.onReceive(context, intent)
        if (intent?.action == "ACTION_WIDGET_PINNED") {
            val newWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
            val sourceNoteId = intent.getIntExtra("source_note_id", -1)

            if (newWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID && sourceNoteId != -1) {
                CoroutineScope(Dispatchers.IO).launch {
                    val db = Room.databaseBuilder(
                        context.applicationContext,
                        WidgetDatabase::class.java,
                        "sticky_notes.db"
                    )
                        .fallbackToDestructiveMigration().build()
                    try {
                        val sourceEntity = db.widgetDao().getWidgetById(sourceNoteId)
                        if (sourceEntity != null) {
                            val finalNote = sourceEntity.copy(id = newWidgetId)
                            db.widgetDao().saveWidget(finalNote)
                            val manager = AppWidgetManager.getInstance(context)
                            onUpdate(context, manager, intArrayOf(newWidgetId))
                        }
                    } finally {
                        db.close()
                    }
                }
            }
        }
    }
}