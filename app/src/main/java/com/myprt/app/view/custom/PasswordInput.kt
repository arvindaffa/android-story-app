package com.myprt.app.view.custom

import android.content.Context
import android.graphics.Canvas
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputLayout

class PasswordInput : TextInputLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {}
            override fun onTextChanged(password: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (password.isNullOrEmpty()) {
                    isErrorEnabled = false
                } else {
                    if (password.length >= 8) {
                        isErrorEnabled = false
                    } else {
                        isErrorEnabled = true
                        error = "Password must be at least 8 characters."
                    }
                }
            }
        })
    }
}