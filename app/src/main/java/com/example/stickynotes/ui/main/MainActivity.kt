package com.example.stickynotes.ui.main

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RemoteViews
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stickynotes.R
import com.example.stickynotes.data.source.StickerDataSource
import com.example.stickynotes.databinding.ActivityMainBinding
import com.example.stickynotes.domain.model.WidgetNote
import com.example.stickynotes.ui.widget.StickerAdapter
import com.example.stickynotes.ui.widget.WidgetGalleryAdapter
import com.example.stickynotes.ui.widget.WidgetViewModel
import com.example.stickynotes.util.getGreeting
import com.example.stickynotes.util.uriToBitmap
import com.example.stickynotes.widget.WidgetProvider4x1
import com.example.stickynotes.widget.WidgetProvider5x2
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import dagger.hilt.android.AndroidEntryPoint
import yuku.ambilwarna.AmbilWarnaDialog
import java.util.Locale

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: WidgetViewModel by viewModels()
    private var currentWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var binding: ActivityMainBinding
    private lateinit var hubGalleryAdapter: WidgetGalleryAdapter
    private lateinit var sheetGalleryAdapter: WidgetGalleryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )

        binding.btnHome.setOnClickListener {
            selectTab(binding.btnHome)
            showHubScreen()
        }

        binding.btnDesign.setOnClickListener {
            if (currentWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                selectTab(binding.btnDesign)
                showDesignScreen()
            } else {
                Toast.makeText(this, "Escolha uma nota na galeria para editar!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnInput.setOnClickListener {
            if (currentWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                selectTab(binding.btnInput)
                openInputSheet()
            } else {
                Toast.makeText(this, "Selecione um widget primeiro!", Toast.LENGTH_SHORT).show()
            }
        }

        if (currentWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            viewModel.loadData(currentWidgetId)
            selectTab(binding.btnDesign)
            showDesignScreen()
        } else {
            selectTab(binding.btnHome)
            showHubScreen()
        }
    }

    private fun showHubScreen() {
        binding.fragmentContainer.removeAllViews()
        val hubView = layoutInflater.inflate(R.layout.layout_hub_screen, binding.fragmentContainer, false)
        binding.fragmentContainer.addView(hubView)

        val tvGreeting = hubView.findViewById<TextView>(R.id.tv_greeting)
        val btnCreate = hubView.findViewById<Button>(R.id.btn_hub_create_new)
        val rvGallery = hubView.findViewById<RecyclerView>(R.id.rv_hub_gallery)
        val tvEmpty = hubView.findViewById<TextView>(R.id.tv_empty_state)

        tvGreeting.text = getGreeting()
        btnCreate.setOnClickListener { openFormatDialog() }

        hubGalleryAdapter = WidgetGalleryAdapter(
            items = emptyList(),
            onItemClick = { selectedNote ->
                currentWidgetId = selectedNote.id
                viewModel.loadData(selectedNote.id)
                selectTab(binding.btnDesign)
                showDesignScreen()
            },
            onDeleteClick = { noteToDelete ->
                showDeleteConfirmation(noteToDelete)
            }
        )

        rvGallery.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = hubGalleryAdapter
        }

        viewModel.allWidgets.removeObservers(this)

        viewModel.allWidgets.observe(this) { widgets ->
            if (widgets.isNullOrEmpty()) {
                tvEmpty.visibility = View.VISIBLE
                rvGallery.visibility = View.GONE
            } else {
                tvEmpty.visibility = View.GONE
                rvGallery.visibility = View.VISIBLE

                hubGalleryAdapter.updateData(widgets)
            }
        }
        viewModel.loadAllWidgets()
    }

    private fun showDesignScreen() {
        binding.fragmentContainer.removeAllViews()
        val view = layoutInflater.inflate(R.layout.fragment_design, binding.fragmentContainer, false)

        val cardPreview = view.findViewById<CardView>(R.id.card_preview)
        val previewBg = view.findViewById<View>(R.id.widget_preview_background)
        val previewText = view.findViewById<TextView>(R.id.widget_preview_text)
        val previewImage = view.findViewById<ImageView>(R.id.widget_preview_image)
        val tag4x1 = view.findViewById<Chip>(R.id.tag_4x1)
        val tag5x2 = view.findViewById<Chip>(R.id.tag_5x2)
        val btnInverter = view.findViewById<MaterialButton>(R.id.btn_switch_layout)
        val btnPin = view.findViewById<MaterialButton>(R.id.btn_pin_to_home)
        val chipGroupSize = view.findViewById<ChipGroup>(R.id.chipGroupSize)

        view.findViewById<ImageView>(R.id.btn_text_plus).setOnClickListener { viewModel.updateFontSize(true); forceAllWidgetsUpdate() }
        view.findViewById<ImageView>(R.id.btn_text_minus).setOnClickListener { viewModel.updateFontSize(false); forceAllWidgetsUpdate() }
        view.findViewById<ImageView>(R.id.btn_sticker_plus).setOnClickListener { viewModel.updateStickerSize(true); forceAllWidgetsUpdate() }
        view.findViewById<ImageView>(R.id.btn_sticker_minus).setOnClickListener { viewModel.updateStickerSize(false); forceAllWidgetsUpdate() }

        val btnMyWidgets = view.findViewById<MaterialButton>(R.id.btn_my_widgets)
        btnMyWidgets.setOnClickListener {
            openNotesGallerySheet()
        }

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
            state.imageUri?.let { previewImage.setImageURI(it.toUri()) }

            previewBg.layoutDirection = if (state.imageAlignment == "LEFT_CENTER") View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR

            val imgPx = (state.stickerSize * resources.displayMetrics.density).toInt()
            previewImage.layoutParams = (previewImage.layoutParams as LinearLayout.LayoutParams).apply {
                width = imgPx
                height = imgPx
            }

            val is4x1 = state.layoutSize == "4x1"
            val screenWidth = resources.displayMetrics.widthPixels
            val density = resources.displayMetrics.density

            val (widthFactor, heightDp, contentPadding) = if (is4x1) {
                Triple(0.80, 110, 6)
            } else {
                Triple(0.95, 240, 16)
            }

            val paddingPx = (contentPadding * density).toInt()
            previewBg.setPadding(paddingPx, paddingPx, paddingPx, paddingPx)

            cardPreview.layoutParams = cardPreview.layoutParams.apply {
                width = (screenWidth * widthFactor).toInt()
                height = (heightDp * density).toInt()
            }

            cardPreview.requestLayout()
            cardPreview.setOnClickListener {
                openInputSheet()
            }

            tag4x1.isChecked = is4x1
            tag5x2.isChecked = !is4x1

            val appWidgetManager = AppWidgetManager.getInstance(this)
            val info = appWidgetManager.getAppWidgetInfo(currentWidgetId)

            if (info == null) {
                btnPin.visibility = View.VISIBLE
                btnPin.text = "Fixar nota na Home"
            } else {
                val isPhysical5x2 = info.provider.className.contains("WidgetProvider5x2")
                val isPhysical4x1 = info.provider.className.contains("WidgetProvider4x1")

                val sizeMismatch = (state.layoutSize == "5x2" && !isPhysical5x2) ||
                        (state.layoutSize == "4x1" && !isPhysical4x1)

                if (sizeMismatch) {
                    btnPin.visibility = View.VISIBLE
                    btnPin.text = "Aplicar tamanho ${state.layoutSize} na Home"
                } else {
                    btnPin.visibility = View.GONE
                }
            }
        }

        chipGroupSize.setOnCheckedStateChangeListener { _, checkedIds ->
            val newSize = if (checkedIds.contains(R.id.tag_5x2)) "5x2" else "4x1"
            if (viewModel.widgetState.value?.layoutSize != newSize) {
                viewModel.updateSize(newSize)
                forceAllWidgetsUpdate()
            }
        }

        btnPin.setOnClickListener {
            val state = viewModel.widgetState.value ?: return@setOnClickListener
            val providerClass = if (state.layoutSize == "5x2") WidgetProvider5x2::class.java else WidgetProvider4x1::class.java

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requestWidgetPin(providerClass)
            }
        }

        btnInverter.setOnClickListener {
            viewModel.toggleAlignments()
            forceAllWidgetsUpdate()
        }
        view.findViewById<Button>(R.id.btn_change_image).setOnClickListener { openStickerPicker() }
        view.findViewById<Button>(R.id.btn_change_bg).setOnClickListener { openColorPicker(previewBg) }
        view.findViewById<Button>(R.id.btn_change_text_color).setOnClickListener { openTextColorPicker() }

        binding.fragmentContainer.addView(view)
    }

    private fun openNotesGallerySheet() {
        val dialog = BottomSheetDialog(this)
        val sheetView = layoutInflater.inflate(R.layout.layout_notes_gallery_sheet, null)
        dialog.setContentView(sheetView)

        val rvGrid = sheetView.findViewById<RecyclerView>(R.id.rv_gallery_grid)

        sheetGalleryAdapter = WidgetGalleryAdapter(
            items = emptyList(),
            onItemClick = { selectedNote ->
                viewModel.loadData(selectedNote.id)
                currentWidgetId = selectedNote.id
                val mainScroll = binding.fragmentContainer.findViewById<android.widget.ScrollView>(R.id.mainScrollView)
                mainScroll?.smoothScrollTo(0, 0)
                dialog.dismiss()
            },
            onDeleteClick = { noteToDelete ->
                showDeleteConfirmation(noteToDelete)
            }
        )

        rvGrid.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = sheetGalleryAdapter
        }

        viewModel.allWidgets.observe(this) { widgets ->
            if (!widgets.isNullOrEmpty()) {
                sheetGalleryAdapter.updateData(widgets)
            }
        }

        viewModel.loadAllWidgets()
        dialog.show()
    }

    private fun showDeleteConfirmation(note: WidgetNote) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Dados?")
            .setMessage("Isso apagará o conteúdo salvo. Para remover o widget da tela inicial, você deve arrastá-lo para a lixeira do seu celular.")
            .setPositiveButton("Excluir") { _, _ ->
                viewModel.deleteWidget(note.id)
                forceAllWidgetsUpdate()
            }
            .setNegativeButton("Cancelar", null)
            .show()
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
        val btnSave = view.findViewById<Button>(R.id.btn_save)
        val btnClear = view.findViewById<Button>(R.id.btn_clear)
        val btnClose = view.findViewById<Button>(R.id.btn_close)

        etText.setText(viewModel.widgetState.value?.text ?: "")

        btnSave.setOnClickListener {
            viewModel.updateText(etText.text.toString())
            forceAllWidgetsUpdate()
            dialog.dismiss()
        }

        btnClear.setOnClickListener {
            etText.setText("")
        }

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
    private fun openFormatDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_widget_size, null)
        val alert = AlertDialog.Builder(this).setView(view).create()

        view.findViewById<View>(R.id.card_widget_4x1).setOnClickListener {
            confirmWidgetSelection("4x1", WidgetProvider4x1::class.java, alert)
        }
        view.findViewById<View>(R.id.card_widget_5x2).setOnClickListener {
            confirmWidgetSelection("5x2", WidgetProvider5x2::class.java, alert)
        }
        alert.show()
    }

    private fun confirmWidgetSelection(size: String, providerClass: Class<*>, alert: AlertDialog) {

        val newId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        currentWidgetId = newId

        viewModel.loadData(newId)
        viewModel.updateSize(size)

        binding.root.postDelayed({
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val appWidgetManager = AppWidgetManager.getInstance(this)
                val myProvider = ComponentName(this, providerClass)

                val successIntent = Intent(this, providerClass).apply {
                    action = "ACTION_WIDGET_PINNED"
                    putExtra("source_note_id", newId)
                }

                val successPending = PendingIntent.getBroadcast(
                    this,
                    newId,
                    successIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )

                appWidgetManager.requestPinAppWidget(myProvider, null, successPending)
            }
            alert.dismiss()

            showDesignScreen()
        }, 200)
    }

    private fun openColorPicker(previewBackground: View) {
        val initialColor = viewModel.widgetState.value?.bgColor ?: Color.WHITE

        AmbilWarnaDialog(this, initialColor, object : AmbilWarnaDialog.OnAmbilWarnaListener {
            override fun onCancel(dialog: AmbilWarnaDialog?) {}
            override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                viewModel.updateColors(bgColor = color)
                forceAllWidgetsUpdate()
            }
        }).show()
    }

    private fun openTextColorPicker() {
        val initialColor = viewModel.widgetState.value?.textColor ?: Color.BLACK

        AmbilWarnaDialog(this, initialColor, object : AmbilWarnaDialog.OnAmbilWarnaListener {
            override fun onCancel(dialog: AmbilWarnaDialog?) {}
            override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                viewModel.updateColors(textColor = color)
                forceAllWidgetsUpdate()
            }
        }).show()
    }

    private fun forceAllWidgetsUpdate() {
        val providers = listOf(WidgetProvider4x1::class.java, WidgetProvider5x2::class.java)

        providers.forEach { providerClass ->
            val appWidgetManager = AppWidgetManager.getInstance(application)
            val ids = appWidgetManager.getAppWidgetIds(ComponentName(application, providerClass))

            if (ids.isNotEmpty()) {
                val intent = Intent(this, providerClass).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                sendBroadcast(intent)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestWidgetPin(providerClass: Class<*>) {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val provider = ComponentName(this, providerClass)

        if (!appWidgetManager.isRequestPinAppWidgetSupported) return

        val currentNote = viewModel.widgetState.value ?: return

        val layoutRes = if (currentNote.layoutSize == "5x2") R.layout.widget_sticky_5x2 else R.layout.widget_sticky_4x1
        val preview = RemoteViews(packageName, layoutRes).apply {

            setTextViewText(R.id.widget_text_display, currentNote.text)
            setInt(R.id.widget_background_display, "setBackgroundColor", currentNote.bgColor)
            setTextColor(R.id.widget_text_display, currentNote.textColor)

            val dir = if (currentNote.imageAlignment == "LEFT_CENTER")
                View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
            setInt(R.id.widget_background_display, "setLayoutDirection", dir)

            currentNote.imageUri?.let { uriStr ->
                try {
                    val density = resources.displayMetrics.density
                    val px = (currentNote.stickerSize * density).toInt()
                    val bmp = uriToBitmap(this@MainActivity, uriStr.toUri(), px)
                    bmp?.let { setImageViewBitmap(R.id.widget_image_display, it) }
                } catch (_: Exception) { }
            }
        }

        val successIntent = Intent(this, providerClass).apply {
            action = "ACTION_WIDGET_PINNED"
            putExtra("source_note_id", currentNote.id)
        }

        val successPending = PendingIntent.getBroadcast(
            this,
            currentNote.id,
            successIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val extras = Bundle().apply {
            putParcelable(AppWidgetManager.EXTRA_APPWIDGET_PREVIEW, preview)
        }

        appWidgetManager.requestPinAppWidget(provider, extras, successPending)
    }

    private fun openStickerPicker() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_sticker_picker, null)
        dialog.setContentView(view)

        val rv = view.findViewById<RecyclerView>(R.id.rv_stickers)
        val title = view.findViewById<TextView>(R.id.tv_picker_title)

        val stickerSource = StickerDataSource(this)
        val myCollections = stickerSource.getCollections()

        val allThumbIds = myCollections.map { it.thumbnailRes }

        var stickerAdapter: StickerAdapter? = null

        stickerAdapter = StickerAdapter(
            items = allThumbIds,
            thumbIds = allThumbIds
        ) { selectedRes ->

            val clickedCollection = myCollections.find { it.thumbnailRes == selectedRes }

            if (clickedCollection != null) {
                title.text = "← ${clickedCollection.name}"
                title.setTextColor(ContextCompat.getColor(this, R.color.sticky_text))

                stickerAdapter?.updateData(clickedCollection.stickers)

                title.setOnClickListener {
                    title.text = "Escolha uma Coleção"
                    title.setTextColor(ContextCompat.getColor(this, R.color.sticky_text))
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
        val uriPath = "android.resource://$packageName/$resId"
        viewModel.updateImage(uriPath)
        forceAllWidgetsUpdate()
        dialog.dismiss()
    }
}