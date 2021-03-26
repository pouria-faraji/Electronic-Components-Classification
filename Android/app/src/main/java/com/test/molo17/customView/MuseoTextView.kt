package com.test.molo17.customView

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.TextView

class MuseoTextView : androidx.appcompat.widget.AppCompatTextView {

    constructor(context: Context) : super(context) {
        this.typeface = Typeface.createFromAsset(context.assets, "fonts/Museo Sans W01 Rounded 300.ttf")
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        this.typeface = Typeface.createFromAsset(context.assets, "fonts/Museo Sans W01 Rounded 300.ttf")
    }
}