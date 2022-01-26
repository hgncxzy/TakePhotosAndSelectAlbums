package com.xzy.example.takephotosandselectalbums

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.Display
import android.widget.TextView
import android.widget.LinearLayout

import android.view.LayoutInflater
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import com.xzy.example.takephotosandselectalbums.databinding.BottomActionsheetDialogBinding
import java.util.ArrayList

/**
 * 底部弹出选择框
 * */
@Suppress("UNUSED", "DEPRECATION", "UNCHECKED_CAST")
class ActionSheetDialog(private val context: Context) {

    private var dialog: Dialog? = null
    private var showTitle = false
    private var sheetItemList: MutableList<SheetItem>? = null
    private val display: Display
    private lateinit var binding: BottomActionsheetDialogBinding

    fun builder(): ActionSheetDialog {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.bottom_actionsheet_dialog,
            null,
            false
        )
        binding.apply {
            binding.root.minimumWidth = display.width
            // 取消事件
            tvCancel.setOnClickListener { dialog?.dismiss() }
        }
        dialog = Dialog(context, R.style.ActionSheetDialogStyle)
        dialog?.setContentView(binding.root)
        val dialogWindow = dialog?.window
        dialogWindow?.setGravity(Gravity.LEFT or Gravity.BOTTOM)
        val lp = dialogWindow?.attributes
        lp?.x = 0
        lp?.y = 0
        dialogWindow?.attributes = lp
        return this
    }

    fun setTitle(title: String?): ActionSheetDialog {
        showTitle = true
        binding.apply {
            tvTitle.visibility = View.VISIBLE
            tvTitle.text = title
        }
        return this
    }

    fun setCancelable(cancel: Boolean): ActionSheetDialog {
        dialog?.setCancelable(cancel)
        return this
    }

    fun setCanceledOnTouchOutside(cancel: Boolean): ActionSheetDialog {
        dialog?.setCanceledOnTouchOutside(cancel)
        return this
    }

    /**
     * @param strItem  条目名称
     * @param color    条目字体颜色，设置null则默认蓝色
     * @param listener
     *
     * @return
     */
    fun addSheetItem(
        strItem: String?,
        color: SheetItemColor?,
        listener: OnSheetItemClickListener
    ): ActionSheetDialog {
        if (sheetItemList == null) {
            sheetItemList = ArrayList()
        }
        sheetItemList?.add(SheetItem(strItem, color, listener))
        return this
    }

    /**
     * 设置条目布局
     */
    private fun setSheetItems() {
        if (sheetItemList == null || sheetItemList!!.size <= 0) {
            return
        }
        val size = sheetItemList?.size ?: 0
        // 添加条目过多的时候控制高度
        if (size >= 7) {
            binding.apply {
                val params = layoutScrollView.layoutParams as LinearLayout.LayoutParams
                params.height = display.height / 2
                layoutScrollView.layoutParams = params
            }
        }

        // 循环添加条目
        for (i in 1..size) {
            val sheetItem = sheetItemList!![i - 1]
            val strItem = sheetItem.name
            val color = sheetItem.color
            val textView = TextView(context)
            textView.text = strItem
            textView.textSize = 15.0f
            textView.gravity = Gravity.CENTER

            // 背景图片
            if (size == 1) {
                if (showTitle) {
                    textView.setBackgroundResource(R.drawable.bg_txt_stroke_white_bottom)
                } else {
                    textView.setBackgroundResource(R.drawable.layer_action_sheet)
                }
            } else {
                if (showTitle) {
                    if (i in 1 until size) {
                        textView.setBackgroundResource(R.drawable.bg_txt_stroke_white_middle)
                    } else {
                        textView.setBackgroundResource(R.drawable.bg_txt_stroke_white_bottom)
                    }
                } else {
                    when {
                        i == 1 -> {
                            textView.setBackgroundResource(R.drawable.layer_action_sheet)
                        }
                        i < size -> {
                            textView.setBackgroundResource(R.drawable.bg_txt_stroke_white_middle)
                        }
                        else -> {
                            textView.setBackgroundResource(R.drawable.bg_txt_stroke_white_bottom)
                        }
                    }
                }
            }

            // 字体颜色
            if (color == null) {
                textView.setTextColor(Color.parseColor(SheetItemColor.BLACK.name))
            } else {
                textView.setTextColor(Color.parseColor(color.name))
            }

            // 高度
            val scale = context.resources.displayMetrics.density
            val height = (48 * scale + 0.5f).toInt()
            textView.layoutParams =
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height)

            // 点击事件
            textView.setOnClickListener {
                sheetItem.itemClickListener.onClick(i)
                dialog?.dismiss()
            }
            binding.layoutLinearLayout.addView(textView)
        }
    }

    fun show() {
        setSheetItems()
        dialog?.show()
    }

    interface OnSheetItemClickListener {
        fun onClick(which: Int)
    }

    inner class SheetItem(
        var name: String?,
        var color: SheetItemColor?,
        var itemClickListener: OnSheetItemClickListener
    )

    enum class SheetItemColor(name: String) {
        BLACK("#000000"), WHITE("#FFFFFF");
    }

    init {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        display = windowManager.defaultDisplay
    }
}
