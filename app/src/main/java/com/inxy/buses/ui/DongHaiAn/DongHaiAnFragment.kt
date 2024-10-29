package com.inxy.buses.ui.DongHaiAn

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.inxy.buses.R
import com.inxy.buses.databinding.FragmentDonghaianBinding
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread


class DongHaiAnFragment : Fragment() {

    private var _binding: FragmentDonghaianBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val donghaian =
            ViewModelProvider(this).get(DongHaiAnViewModel::class.java)

        _binding = FragmentDonghaianBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textDonghaian
        donghaian.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        textView.setOnClickListener { view ->
            run {
                sendRequestWithHttpUrl(1)
            }
        }
        return root
    }
    private fun sendRequestWithHttpUrl(mode:Int){
        thread{
            var connection: HttpURLConnection?=null
            try{
                val response=StringBuilder()
                val url= URL("https://inxy.xyz/buses/v")
                connection=url.openConnection() as HttpURLConnection
                connection.connectTimeout=8000
                connection.readTimeout=8000
                //指定请求方式
                // connection.requestMethod="Post"
                //网络输出，附带参数请求
                //val output=DataOutputStream(connection.outputStream)
                //output.writeBytes("username=admin&password=121231")
                //网络响应输入
                val input=connection.inputStream
                val reader= BufferedReader(InputStreamReader(input))
                reader.use{
                    reader.forEachLine {
                        response.append(it)
                    }
                }
                showResponse(response.toString())



            }catch (e:Exception){
                e.printStackTrace()
            }finally {
                //断开连接
                connection?.disconnect()
            }
        }
    } private fun showResponse(response:String){
        //此方法可以进行异步的ui界面更新
        activity?.runOnUiThread  {
            val packageInfo = requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0)
            val versionName = packageInfo.versionName.toFloat()
            val versionCode = packageInfo.getLongVersionCode()
            //="Version Name: "+versionName.toString()
            var v=response.toFloat()
            if(v>versionName)
            {binding.textDonghaian.text="当前版本:"+versionName+"\n最新版本:"+response;
                val url = "https://inxy.xyz/buses/a.apk"
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(url)
                }
                startActivity(intent)}
            println("Version Code: $versionCode")

        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}