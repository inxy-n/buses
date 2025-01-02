package com.inxy.buses.ui.notifications


import android.annotation.SuppressLint
import android.content.Context

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.amap.api.fence.GeoFence
import com.inxy.buses.R
import com.inxy.buses.databinding.FragmentNotificationsBinding


class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


//声明AMapLocationClient类对象


    private val uiHandler = Handler(Looper.getMainLooper())
    private var is_des = false;



    @SuppressLint("SuspiciousIndentation", "SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)


        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val webView: WebView = root.findViewById(R.id.webView)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true

        webView.webViewClient = WebViewClient()

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true) // API < 21
        cookieManager.setAcceptThirdPartyCookies(webView, true) // API >= 21
        val url = "https://inxy.xyz/"
        val sharedPreferences=requireActivity().getSharedPreferences("buses", Context.MODE_PRIVATE);
        val launchPosition=sharedPreferences.getString("LaunchPosition", "latitude=123;longitude=123").toString()
        val uid=sharedPreferences.getString("FirstLaunchTime", "122").toString()
        val cookie = "userid=${uid.substring(0,uid.length-5)};$launchPosition; Secure; HttpOnly"
        Log.e("TAG", "onCreateView: $cookie", )
        cookieManager.setCookie(url, cookie)

        // 确保cookies在WebView中生效
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.flush()
        } else {
            CookieSyncManager.createInstance(context)
            CookieSyncManager.getInstance().sync()
        }
        // 加载网页
        webView.loadUrl("https://inxy.xyz/buses/comment/")


        return root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        is_des = true;


        uiHandler.removeCallbacksAndMessages(null)
    }
}