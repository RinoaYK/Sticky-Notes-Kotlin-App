package com.example.stickynotes.ui.main

import android.animation.ObjectAnimator
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stickynotes.R
import com.example.stickynotes.data.model.StickerCollection
import com.example.stickynotes.databinding.ActivityMainBinding
import com.example.stickynotes.ui.widget.StickerAdapter
import com.example.stickynotes.ui.widget.WidgetAlignment
import com.example.stickynotes.ui.widget.WidgetViewModel
import com.example.stickynotes.widget.WidgetProvider4x1
import com.example.stickynotes.widget.WidgetProvider4x2
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import yuku.ambilwarna.AmbilWarnaDialog
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val viewModel: WidgetViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnDesign.setOnClickListener {
            selectTab(binding.btnDesign)
            showDesignScreen()
        }

        binding.btnInput.setOnClickListener {
            selectTab(binding.btnInput)
            openInputSheet()
        }

        binding.btnHome.setOnClickListener {
            selectTab(binding.btnHome)
            openFormatDialog()
        }

        selectTab(binding.btnDesign)
        showDesignScreen()
    }

    private fun showDesignScreen() {
        binding.fragmentContainer.removeAllViews()
        val view =
            layoutInflater.inflate(R.layout.fragment_design, binding.fragmentContainer, false)

        val cardPreview = view.findViewById<CardView>(R.id.card_preview)
        val previewBg = view.findViewById<View>(R.id.widget_preview_background)
        val previewText = view.findViewById<TextView>(R.id.widget_preview_text)
        val previewImage = view.findViewById<ImageView>(R.id.widget_preview_image)
        val tag4x1 = view.findViewById<Chip>(R.id.tag_4x1)
        val tag4x2 = view.findViewById<Chip>(R.id.tag_4x2)
        val btnInverter = view.findViewById<MaterialButton>(R.id.btn_switch_layout)

        val btnTextPlus = view.findViewById<ImageView>(R.id.btn_text_plus)
        val btnTextMinus = view.findViewById<ImageView>(R.id.btn_text_minus)
        val btnStickerPlus = view.findViewById<ImageView>(R.id.btn_sticker_plus)
        val btnStickerMinus = view.findViewById<ImageView>(R.id.btn_sticker_minus)

        btnTextPlus.setOnClickListener { viewModel.updateFontSize(true); updateWidget() }
        btnTextMinus.setOnClickListener { viewModel.updateFontSize(false); updateWidget() }
        btnStickerPlus.setOnClickListener { viewModel.updateStickerSize(true); updateWidget() }
        btnStickerMinus.setOnClickListener { viewModel.updateStickerSize(false); updateWidget() }

        listOf(tag4x1, tag4x2).forEach { chip ->
            chip.setOnCheckedChangeListener { buttonView, isChecked ->
                buttonView.setTypeface(null, if (isChecked) Typeface.BOLD else Typeface.NORMAL)
            }
        }

        cardPreview.setOnClickListener { openInputSheet() }

        viewModel.showLimitToast.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.toastShown()
            }
        }


        viewModel.widgetState.observe(this) { state ->
            previewBg.setBackgroundColor(state.bgColor)

            previewText.text = state.text
            previewText.setTextColor(state.textColor)
            previewText.textSize = state.fontSize

            state.imageUri?.let {
                previewImage.setImageURI(it.toUri())
                previewImage.alpha = 1.0f
            }

            val is4x1 = state.size == "4x1"

            val maxSticker = if (state.size == "4x1") 80 else 160
            val minSticker = 40

            view.findViewById<ImageView>(R.id.btn_sticker_plus).alpha =
                if (state.stickerSize >= maxSticker) 0.3f else 1.0f

            view.findViewById<ImageView>(R.id.btn_sticker_minus).alpha =
                if (state.stickerSize <= minSticker) 0.3f else 1.0f

            view.findViewById<ImageView>(R.id.btn_text_plus).alpha =
                if (state.fontSize >= 25f) 0.3f else 1.0f

            view.findViewById<ImageView>(R.id.btn_text_minus).alpha =
                if (state.fontSize <= 8f) 0.3f else 1.0f

            val imgParams = previewImage.layoutParams as LinearLayout.LayoutParams
            val imgPx = (state.stickerSize * resources.displayMetrics.density).toInt()
            imgParams.width = imgPx
            imgParams.height = imgPx
            previewImage.layoutParams = imgParams

            if (state.imageAlignment == WidgetAlignment.RIGHT_CENTER) {
                previewBg.layoutDirection = View.LAYOUT_DIRECTION_LTR
            } else if (state.imageAlignment == WidgetAlignment.LEFT_CENTER) {
                previewBg.layoutDirection = View.LAYOUT_DIRECTION_RTL
            }

            cardPreview.layoutParams.height = if (is4x1) 350 else 600
            cardPreview.requestLayout()

            tag4x1.isChecked = is4x1
            tag4x2.isChecked = !is4x1
        }

        btnInverter.setOnClickListener {
            viewModel.toggleAlignments()
            val currentAlignment =
                viewModel.widgetState.value?.imageAlignment ?: WidgetAlignment.RIGHT_CENTER

            val prefs = getSharedPreferences("WidgetPrefs", MODE_PRIVATE)
            prefs.edit {
                putString("image_alignment", currentAlignment.name)
            }

            val drawable = btnInverter.icon ?: return@setOnClickListener

            val animator = ObjectAnimator.ofInt(
                drawable,
                "level",
                0,
                10000
            )

            animator.duration = 600
            animator.interpolator =
                AccelerateDecelerateInterpolator()

            animator.start()

            updateWidget()
        }


        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupSize)

        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            when (checkedIds.firstOrNull()) {
                R.id.tag_4x1 -> viewModel.updateSize("4x1")
                R.id.tag_4x2 -> viewModel.updateSize("4x2")
            }
            updateWidget()
        }

        view.findViewById<Button>(R.id.btn_change_image).setOnClickListener {
            openStickerPicker()
        }

        view.findViewById<Button>(R.id.btn_change_bg)
            .setOnClickListener { openColorPicker(previewBg) }

        view.findViewById<Button>(R.id.btn_change_text_color).setOnClickListener {
            openTextColorPicker()
        }

        binding.fragmentContainer.addView(view)
    }

    private fun selectTab(selected: View) {
        binding.btnDesign.isSelected = false
        binding.btnInput.isSelected = false
        binding.btnHome.isSelected = false

        selected.isSelected = true
    }


    private fun openInputSheet() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_input_bottom_sheet, null)
        dialog.setContentView(view)

        val etText = view.findViewById<EditText>(R.id.et_widget_text)
        val prefs = getSharedPreferences("WidgetPrefs", MODE_PRIVATE)
        etText.setText(prefs.getString("user_text", ""))

        view.findViewById<Button>(R.id.btn_clear).setOnClickListener {
            etText.setText("")
            etText.requestFocus()
        }

        view.findViewById<Button>(R.id.btn_save).setOnClickListener {
            prefs.edit { putString("user_text", etText.text.toString()) }
            viewModel.loadData()
            updateWidget()
            dialog.dismiss()
        }
        view.findViewById<Button>(R.id.btn_close).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun openFormatDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_widget_size, null)
        val alert = AlertDialog.Builder(this).setView(view).create()

        view.findViewById<View>(R.id.card_widget_4x1).setOnClickListener {
            confirmWidgetSelection("4x1", WidgetProvider4x1::class.java, alert)
        }
        view.findViewById<View>(R.id.card_widget_4x2).setOnClickListener {
            confirmWidgetSelection("4x2", WidgetProvider4x2::class.java, alert)
        }
        alert.show()
    }

    private fun confirmWidgetSelection(size: String, provider: Class<*>, alert: AlertDialog) {
        viewModel.updateSize(size)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requestWidgetPin(provider)
        }
        updateWidget()
        alert.dismiss()
    }

    private fun openColorPicker(previewBackground: View) {
        val prefs = getSharedPreferences("WidgetPrefs", MODE_PRIVATE)
        val initialColor = prefs.getInt("widget_bg_color", Color.WHITE)

        AmbilWarnaDialog(this, initialColor, object : AmbilWarnaDialog.OnAmbilWarnaListener {
            override fun onCancel(dialog: AmbilWarnaDialog?) {}
            override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                prefs.edit { putInt("widget_bg_color", color) }
                viewModel.loadData()
                updateWidget()
            }
        }).show()
    }

    private fun openTextColorPicker() {
        val prefs = getSharedPreferences("WidgetPrefs", MODE_PRIVATE)
        val defaultTextColor = ContextCompat.getColor(this, R.color.sticky_text)
        val initialColor = prefs.getInt("widget_text_color", defaultTextColor)

        AmbilWarnaDialog(this, initialColor, object : AmbilWarnaDialog.OnAmbilWarnaListener {
            override fun onCancel(dialog: AmbilWarnaDialog?) {}
            override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                prefs.edit { putInt("widget_text_color", color) }
                viewModel.loadData()
                updateWidget()
            }
        }).show()
    }

    private fun updateWidget() {
        val providers = listOf(WidgetProvider4x1::class.java, WidgetProvider4x2::class.java)
        providers.forEach { providerClass ->
            val intent = Intent(this, providerClass).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(
                ComponentName(
                    application,
                    providerClass
                )
            )
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            sendBroadcast(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestWidgetPin(providerClass: Class<*>) {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val myProvider = ComponentName(this, providerClass)
        if (appWidgetManager.isRequestPinAppWidgetSupported) {
            val successCallback = PendingIntent.getBroadcast(
                this, 0, Intent(this, providerClass),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            appWidgetManager.requestPinAppWidget(myProvider, null, successCallback)
        }
    }

    private fun openStickerPicker() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_sticker_picker, null)
        dialog.setContentView(view)

        val rv = view.findViewById<RecyclerView>(R.id.rv_stickers)
        val title = view.findViewById<TextView>(R.id.tv_picker_title)

        val myCollections = listOf(
            StickerCollection(
                "Takopí",
                R.drawable.thumb_takopi,
                getStickersFromFolder("sticker_takopi")
            ),
            StickerCollection(
                "Mitao Cat",
                R.drawable.thumb_mitao,
                getStickersFromFolder("sticker_mitao")
            )
        )

        val allThumbIds = myCollections.map { it.thumbnailRes }

        var stickerAdapter: StickerAdapter? = null

        stickerAdapter = StickerAdapter(
            items = allThumbIds,
            thumbIds = allThumbIds
        ) { selectedRes ->

            val clickedCollection = myCollections.find { it.thumbnailRes == selectedRes }

            if (clickedCollection != null) {

                title.text = "← Voltar: ${clickedCollection.name}"
                title.setTextColor(Color.BLUE)

                stickerAdapter?.updateData(clickedCollection.stickers)

                title.setOnClickListener {
                    title.text = "Escolha uma Coleção"
                    title.setTextColor(Color.BLACK)

                    stickerAdapter?.updateData(allThumbIds)
                    title.setOnClickListener(null)
                }
            } else {
                saveStickerAndClose(selectedRes, dialog)
            }
        }

        rv.layoutManager = GridLayoutManager(this, 3)
        rv.adapter = stickerAdapter

        dialog.show()
    }

    private fun saveStickerAndClose(resId: Int, dialog: BottomSheetDialog) {
        val prefs = getSharedPreferences("WidgetPrefs", MODE_PRIVATE)
        val uriPath = "android.resource://$packageName/$resId"
        prefs.edit { putString("widget_image_uri", uriPath) }
        viewModel.loadData()
        updateWidget()
        dialog.dismiss()
    }

    private fun getStickersFromFolder(prefix: String): List<Int> {
        val stickers = mutableListOf<Int>()
        var index = 1
        while (true) {
            val name = "${prefix}_%02d".format(Locale.ROOT, index)
            val resId = resources.getIdentifier(name, "drawable", packageName)
            if (resId != 0) {
                stickers.add(resId)
                index++
            } else break
        }
        return stickers
    }
}