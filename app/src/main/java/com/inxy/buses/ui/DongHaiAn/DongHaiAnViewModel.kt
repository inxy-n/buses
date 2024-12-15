package com.inxy.buses.ui.DongHaiAn

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DongHaiAnViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "1.7更新说明\n1.校车时间调整(第6周以后了)。\n2.可以看不同时段的车。\n\n1.6更新说明\n1.增加了电子围栏功能。\n2.减少了窃取用户隐私的频率。\n3.修复了很多bug，增加稳定性。"
    }
    val text: LiveData<String> = _text
}