package me.fetsh.geekbrains.weather.ui.utils

import android.content.Context
import android.provider.Settings.Global.getString
import android.view.View
import com.google.android.material.snackbar.Snackbar

fun View.showSnackBar(
    text: String,
    actionText: String,
    action: (View) -> Unit,
    length: Int = Snackbar.LENGTH_INDEFINITE
) {
    Snackbar.make(this, text, length).setAction(actionText, action).show()
}

fun View.showSnackBar(
    context: Context,
    text: Int,
    length: Int = Snackbar.LENGTH_SHORT,
    actionText: Int?,
    action: (View) -> Unit = {},
) {
    Snackbar
        .make(this, context.getString(text), length)
        .also { snackbar ->
            actionText?.let {
                snackbar.setAction(context.getString(it), action)
            }
        }.show()
}
