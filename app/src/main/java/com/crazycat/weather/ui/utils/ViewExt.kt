package com.crazycat.weather.ui.utils

import android.content.Context
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

fun View.show() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.changeVisibility(show: Boolean) {
    if (show) show() else gone()
}

fun View.hideKeyboard() {
    val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    inputManager?.hideSoftInputFromWindow(windowToken, 0)
}

fun EditText.isEmailValid(): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(this.text.toString()).matches()
}

fun Fragment.showToast(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}

fun Fragment.showSnackbar(message: String) {
    this.view?.let { Snackbar.make(requireContext(), it, message, Snackbar.LENGTH_LONG).show() }
}