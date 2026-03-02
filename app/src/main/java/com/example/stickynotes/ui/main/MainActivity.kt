package com.example.stickynotes.ui.main

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RemoteViews
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.GridLayoutManager
import com.example.stickynotes.R
import com.example.stickynotes.data.source.StickerDataSource
import com.example.stickynotes.databinding.ActivityMainBinding
import com.example.stickynotes.domain.model.WidgetNote
import com.example.stickynotes.ui.widget.StickerAdapter
import com.example.stickynotes.ui.widget.WidgetGalleryAdapter
import com.example.stickynotes.ui.widget.WidgetViewModel
import com.example.stickynotes.util.WidgetConstants
import com.example.stickynotes.util.getGreeting
import com.example.stickynotes.util.uriToBitmap
import com.example.stickynotes.widget.WidgetProvider4x1
import com.example.stickynotes.widget.WidgetProvider5x2
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import yuku.ambilwarna.AmbilWarnaDialog

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

        setupNavigation()

        if (currentWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            viewModel.loadData(currentWidgetId)
            selectTab(binding.btnDesign)
            showDesignScreen()
        } else {
            selectTab(binding.btnHome)
            showHubScreen()
        }
    }

    private fun setupNavigation() {
        binding.btnHome.setOnClickListener {
            selectTab(binding.btnHome)
            showHubScreen()
        }

        binding.btnDesign.setOnClickListener {
            if (currentWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                selectTab(binding.btnDesign)
                showDesignScreen()
            } else {
                Toast.makeText(this, getString(R.string.toast_select_note), Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.btnInput.setOnClickListener {
            if (currentWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                selectTab(binding.btnInput)
                openInputSheet()
            } else {
                Toast.makeText(this, getString(R.string.toast_select_widget), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun showHubScreen() {
        binding.fragmentContainer.removeAllViews()

        val hubBinding = com.example.stickynotes.databinding.LayoutHubScreenBinding.inflate(
            layoutInflater,
            binding.fragmentContainer,
            true
        )

        hubBinding.tvGreeting.text = getGreeting(this)
        hubBinding.btnHubCreateNew.setOnClickListener { openFormatDialog() }

        hubGalleryAdapter = WidgetGalleryAdapter(
            onItemClick = { selectedNote ->
                currentWidgetId = selectedNote.id
                viewModel.loadData(selectedNote.id)
                selectTab(binding.btnDesign)
                showDesignScreen()
            },
            onDeleteClick = { noteToDelete -> showDeleteConfirmation(noteToDelete) }
        )

        hubBinding.rvHubGallery.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = hubGalleryAdapter
        }

        viewModel.allWidgets.observe(this) { widgets ->
            if (widgets.isNullOrEmpty()) {
                hubBinding.tvEmptyState.visibility = View.VISIBLE
                hubBinding.rvHubGallery.visibility = View.GONE
            } else {
                hubBinding.tvEmptyState.visibility = View.GONE
                hubBinding.rvHubGallery.visibility = View.VISIBLE
                hubGalleryAdapter.updateData(widgets)
            }
        }
        viewModel.loadAllWidgets()
    }

    private fun showDesignScreen() {
        binding.fragmentContainer.removeAllViews()
        val designBinding = com.example.stickynotes.databinding.FragmentDesignBinding.inflate(
            layoutInflater,
            binding.fragmentContainer,
            true
        )

        designBinding.apply {
            btnTextPlus.setOnClickListener { viewModel.updateFontSize(true); forceAllWidgetsUpdate() }
            btnTextMinus.setOnClickListener { viewModel.updateFontSize(false); forceAllWidgetsUpdate() }
            btnStickerPlus.setOnClickListener { viewModel.updateStickerSize(true); forceAllWidgetsUpdate() }
            btnStickerMinus.setOnClickListener { viewModel.updateStickerSize(false); forceAllWidgetsUpdate() }

            btnMyWidgets.setOnClickListener { openNotesGallerySheet() }
            btnChangeImage.setOnClickListener { openStickerPicker() }
            btnChangeBg.setOnClickListener { openColorPicker(widgetPreviewBackground) }
            btnChangeTextColor.setOnClickListener { openTextColorPicker() }
            btnSwitchLayout.setOnClickListener {
                viewModel.toggleAlignments()
                forceAllWidgetsUpdate()
            }

            chipGroupSize.setOnCheckedStateChangeListener { _, checkedIds ->
                val newSize =
                    if (checkedIds.contains(R.id.tag_5x2)) WidgetConstants.SIZE_5X2 else WidgetConstants.SIZE_4X1
                if (viewModel.widgetState.value?.layoutSize != newSize) {
                    viewModel.updateSize(newSize)
                    forceAllWidgetsUpdate()
                }
            }

            btnPinToHome.setOnClickListener {
                val state = viewModel.widgetState.value ?: return@setOnClickListener
                val providerClass =
                    if (state.layoutSize == WidgetConstants.SIZE_5X2) WidgetProvider5x2::class.java else WidgetProvider4x1::class.java
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    requestWidgetPin(providerClass)
                }
            }
        }

        viewModel.showLimitToast.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.toastShown()
            }
        }

        viewModel.widgetState.observe(this) { state ->
            designBinding.apply {
                widgetPreviewBackground.setBackgroundColor(state.bgColor)
                widgetPreviewText.text = state.text
                widgetPreviewText.setTextColor(state.textColor)
                widgetPreviewText.textSize = state.fontSize
                state.imageUri?.let { widgetPreviewImage.setImageURI(it.toUri()) }

                widgetPreviewImage.scaleType = ImageView.ScaleType.FIT_CENTER
                widgetPreviewBackground.layoutDirection = if (state.imageAlignment == "LEFT_CENTER")
                    View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR

                val density = resources.displayMetrics.density
                val imgPx = (state.stickerSize * density).toInt()
                widgetPreviewImage.layoutParams =
                    (widgetPreviewImage.layoutParams as LinearLayout.LayoutParams).apply {
                        width = imgPx
                        height = imgPx
                    }

                val is4x1 = state.layoutSize == WidgetConstants.SIZE_4X1
                val screenWidth = resources.displayMetrics.widthPixels
                val (widthFactor, heightDp, contentPadding) = if (is4x1) {
                    Triple(
                        WidgetConstants.WIDTH_FACTOR_4X1,
                        WidgetConstants.HEIGHT_DP_4X1,
                        WidgetConstants.PADDING_4X1
                    )
                } else {
                    Triple(
                        WidgetConstants.WIDTH_FACTOR_5X2,
                        WidgetConstants.HEIGHT_DP_5X2,
                        WidgetConstants.PADDING_5X2
                    )
                }

                val paddingPx = (contentPadding * density).toInt()
                widgetPreviewBackground.setPadding(paddingPx, paddingPx, paddingPx, paddingPx)

                cardPreview.layoutParams = cardPreview.layoutParams.apply {
                    width = (screenWidth * widthFactor).toInt()
                    height = (heightDp * density).toInt()
                }
                cardPreview.requestLayout()
                cardPreview.setOnClickListener { openInputSheet() }

                tag4x1.isChecked = is4x1
                tag5x2.isChecked = !is4x1

                val appWidgetManager = AppWidgetManager.getInstance(this@MainActivity)
                val info = appWidgetManager.getAppWidgetInfo(currentWidgetId)

                if (info == null) {
                    btnPinToHome.visibility = View.VISIBLE
                    btnPinToHome.text = getString(R.string.btn_pin_new)
                } else {
                    val isPhysical5x2 = info.provider.className.contains("WidgetProvider5x2")
                    val isPhysical4x1 = info.provider.className.contains("WidgetProvider4x1")
                    val sizeMismatch =
                        (state.layoutSize == WidgetConstants.SIZE_5X2 && !isPhysical5x2) || (state.layoutSize == WidgetConstants.SIZE_4X1 && !isPhysical4x1)

                    if (sizeMismatch) {
                        btnPinToHome.visibility = View.VISIBLE
                        btnPinToHome.text = getString(R.string.btn_pin_apply_size, state.layoutSize)
                    } else {
                        btnPinToHome.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun openNotesGallerySheet() {
        val dialog = BottomSheetDialog(this)
        val sheetBinding =
            com.example.stickynotes.databinding.LayoutNotesGallerySheetBinding.inflate(
                layoutInflater
            )
        dialog.setContentView(sheetBinding.root)

        sheetGalleryAdapter = WidgetGalleryAdapter(
            onItemClick = { selectedNote ->
                viewModel.loadData(selectedNote.id)
                currentWidgetId = selectedNote.id

                val mainScroll =
                    binding.fragmentContainer.findViewById<android.widget.ScrollView>(R.id.mainScrollView)
                mainScroll?.smoothScrollTo(0, 0)

                dialog.dismiss()
            },
            onDeleteClick = { noteToDelete ->
                showDeleteConfirmation(noteToDelete)
            }
        )

        sheetBinding.rvGalleryGrid.apply {
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
            .setTitle(getString(R.string.dialog_delete_title))
            .setMessage(getString(R.string.dialog_delete_message))
            .setPositiveButton(getString(R.string.action_delete)) { _, _ ->
                viewModel.deleteWidget(note.id)
                forceAllWidgetsUpdate()
            }
            .setNegativeButton(getString(R.string.action_cancel), null)
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
        val inputBinding =
            com.example.stickynotes.databinding.LayoutInputBottomSheetBinding.inflate(layoutInflater)
        dialog.setContentView(inputBinding.root)

        inputBinding.etWidgetText.setText(viewModel.widgetState.value?.text ?: "")

        inputBinding.apply {
            btnSave.setOnClickListener {
                val newText = etWidgetText.text.toString()
                viewModel.updateText(newText)
                forceAllWidgetsUpdate()
                dialog.dismiss()
            }

            btnClear.setOnClickListener {
                etWidgetText.setText("")
            }

            btnClose.setOnClickListener {
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun openFormatDialog() {

        val dialogBinding =
            com.example.stickynotes.databinding.DialogWidgetSizeBinding.inflate(layoutInflater)

        val alert = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.apply {
            cardWidget4x1.setOnClickListener {
                confirmWidgetSelection(
                    WidgetConstants.SIZE_4X1,
                    WidgetProvider4x1::class.java,
                    alert
                )
            }

            cardWidget5x2.setOnClickListener {
                confirmWidgetSelection(
                    WidgetConstants.SIZE_5X2,
                    WidgetProvider5x2::class.java,
                    alert
                )
            }
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

        val layoutRes = if (currentNote.layoutSize == WidgetConstants.SIZE_5X2) {
            R.layout.widget_sticky_5x2
        } else {
            R.layout.widget_sticky_4x1
        }

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
                } catch (e: Exception) {
                    e.printStackTrace()
                }
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
        val pickerBinding =
            com.example.stickynotes.databinding.LayoutStickerPickerBinding.inflate(layoutInflater)
        dialog.setContentView(pickerBinding.root)

        val stickerSource = StickerDataSource(this)
        val myCollections = stickerSource.getCollections()
        val allThumbIds = myCollections.map { it.thumbnailRes }

        val stickerAdapter = StickerAdapter(thumbIds = allThumbIds) { selectedRes ->
            val clickedCollection = myCollections.find { it.thumbnailRes == selectedRes }

            pickerBinding.apply {
                if (clickedCollection != null) {
                    tvPickerTitle.text =
                        getString(R.string.picker_title_collection, clickedCollection.name)
                    tvPickerTitle.setTextColor(
                        ContextCompat.getColor(
                            this@MainActivity,
                            R.color.sticky_text
                        )
                    )

                    (rvStickers.adapter as? StickerAdapter)?.updateData(clickedCollection.stickers)

                    tvPickerTitle.setOnClickListener {
                        tvPickerTitle.text = getString(R.string.picker_title_default)
                        tvPickerTitle.setTextColor(
                            ContextCompat.getColor(
                                this@MainActivity,
                                R.color.sticky_text
                            )
                        )
                        (rvStickers.adapter as? StickerAdapter)?.updateData(allThumbIds)
                        tvPickerTitle.setOnClickListener(null)
                    }
                } else {
                    saveStickerAndClose(selectedRes, dialog)
                }
            }
        }

        pickerBinding.rvStickers.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 3)
            adapter = stickerAdapter
        }

        stickerAdapter.updateData(allThumbIds)
        dialog.show()
    }


    private fun saveStickerAndClose(resId: Int, dialog: BottomSheetDialog) {
        val uriPath = "android.resource://$packageName/$resId"
        viewModel.updateImage(uriPath)
        forceAllWidgetsUpdate()
        dialog.dismiss()
    }
}