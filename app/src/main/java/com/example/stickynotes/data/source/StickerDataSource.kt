package com.example.stickynotes.data.source

import android.content.Context
import com.example.stickynotes.R
import com.example.stickynotes.data.model.StickerCollection

class StickerDataSource(private val context: Context) {

    fun getCollections(): List<StickerCollection> {
        return listOf(
            createCollection(
                R.string.title_collection_takopi,
                R.drawable.thumb_takopi,
                R.array.array_sticker_takopi
            ),
            createCollection(
                R.string.title_collection_mitao_cat,
                R.drawable.thumb_mitao,
                R.array.array_sticker_mitao
            ),
            createCollection(
                R.string.title_collection_bugcat,
                R.drawable.thumb_bugcat,
                R.array.array_sticker_bugcat
            ),
            createCollection(
                R.string.title_collection_exploding_kittens,
                R.drawable.thumb_exploding_kittens,
                R.array.array_sticker_exploding_kittens
            ),
            createCollection(
                R.string.title_collection_kapibara_san,
                R.drawable.thumb_kapibara_san,
                R.array.array_sticker_kapibara_san
            ),
            createCollection(
                R.string.title_collection_cult_of_lamb,
                R.drawable.thumb_cult_of_lamb,
                R.array.array_sticker_cult_of_lamb
            )
        )
    }

    private fun createCollection(nameRes: Int, thumbRes: Int, arrayRes: Int): StickerCollection {
        val stickers = mutableListOf<Int>()
        val typedArray = context.resources.obtainTypedArray(arrayRes)

        try {
            for (i in 0 until typedArray.length()) {
                val resId = typedArray.getResourceId(i, 0)
                if (resId != 0) stickers.add(resId)
            }
        } finally {
            typedArray.recycle()
        }

        return StickerCollection(
            name = context.getString(nameRes),
            thumbnailRes = thumbRes,
            stickers = stickers
        )
    }
}