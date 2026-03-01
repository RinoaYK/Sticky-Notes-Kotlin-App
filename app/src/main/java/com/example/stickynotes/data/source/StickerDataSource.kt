package com.example.stickynotes.data.source

import android.content.Context
import com.example.stickynotes.R
import com.example.stickynotes.data.model.StickerCollection
import java.util.Locale

class StickerDataSource(private val context: Context) {

    fun getCollections(): List<StickerCollection> {
        return listOf(
            StickerCollection(
                "Takopi",
                R.drawable.thumb_takopi,
                getStickersFromFolder("sticker_takopi")
            ),
            StickerCollection(
                "Mitao Cat",
                R.drawable.thumb_mitao,
                getStickersFromFolder("sticker_mitao")
            )
        )
    }

    private fun getStickersFromFolder(prefix: String): List<Int> {
        val stickers = mutableListOf<Int>()
        var index = 1
        while (true) {
            val name = "${prefix}_%02d".format(Locale.ROOT, index)
            val resId = context.resources.getIdentifier(name, "drawable", context.packageName)
            if (resId != 0) {
                stickers.add(resId)
                index++
            } else break
        }
        return stickers
    }
}