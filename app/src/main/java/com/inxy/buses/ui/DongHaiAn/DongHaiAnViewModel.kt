package com.inxy.buses.ui.DongHaiAn

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DongHaiAnViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "1.9更新说明\n1.车表可以网络更新了。2.更新了车表。\n\n1.8更新说明\n临时更新，新年快乐！\n把电子围栏功能放在第四页了，增加了讨论区功能。\n"
    }
    val text: LiveData<String> = _text
}