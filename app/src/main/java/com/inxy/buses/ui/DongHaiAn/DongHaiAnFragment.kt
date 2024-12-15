package com.inxy.buses.ui.DongHaiAn

import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
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
    private lateinit var sharedPreferences: SharedPreferences
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
        val switch=binding.switch1
        audioManager = requireContext().getSystemService(AUDIO_SERVICE) as AudioManager

        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.never) // music 是文件名，无扩展名

        sharedPreferences = requireActivity().getSharedPreferences("buses", Context.MODE_PRIVATE)
        if(sharedPreferences.getString("NeedUpdate", "true")=="true")
            binding.button.text="有更新"
        else
            binding.button.text="无更新"
        if(sharedPreferences.getString("ShowRedDot", "true")=="true")
        switch.isChecked=true
else switch.isChecked=false
        binding.button.setOnClickListener {
            run {
                sendRequestWithHttpUrl(1)
            }
        }
        switch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val editor = sharedPreferences.edit()
                editor.putString("ShowRedDot", "true")
                editor.apply()
                Toast.makeText(requireContext(), "Now show", Toast.LENGTH_SHORT).show()
            } else {
                val editor = sharedPreferences.edit()
                editor.putString("ShowRedDot", "false")
                editor.apply()
                Toast.makeText(requireContext(), "Restart app to apply", Toast.LENGTH_SHORT).show()
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
    }
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var audioManager: AudioManager
    var click_cishu=0;
    private fun showResponse(response:String){
        //此方法可以进行异步的ui界面更新
        activity?.runOnUiThread  {
            val packageInfo = requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0)
            val versionName = packageInfo.versionName.toFloat()
            val versionCode = packageInfo.getLongVersionCode()
            //="Version Name: "+versionName.toString()
            var v=response.toFloat()
            if(v>versionName)
            {val editor = sharedPreferences.edit()
                editor.putString("NeedUpdate", "false")
                editor.apply()
                binding.textDonghaian.text="当前版本:"+versionName+"\n最新版本:"+response;
                val url = "https://inxy.xyz/buses/a.apk"
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(url)
                }

                startActivity(intent)}
            else {
                click_cishu++;
                if(click_cishu>=3)
                {        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

                    //val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume / 2, 0) // 将音量设置为最大音量的一半

                    mediaPlayer.start()
                }
            }
            println("Version Code: $versionCode")

        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayer.setOnCompletionListener {
            it.release()
        }
        _binding = null
    }
}