package com.inxy.buses.ui.DongHaiAn

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DongHaiAnViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Dong Hai An Fragment"
    }
    val text: LiveData<String> = _text
}