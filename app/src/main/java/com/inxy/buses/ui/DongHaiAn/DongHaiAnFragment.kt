package com.inxy.buses.ui.DongHaiAn

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.amap.api.fence.GeoFenceClient
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClient.setApiKey
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.google.android.material.snackbar.Snackbar
import com.inxy.buses.GeofenceService
import com.inxy.buses.R
import com.inxy.buses.databinding.FragmentDonghaianBinding
import com.inxy.buses.databinding.FragmentNotificationsBinding
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt


class DongHaiAnFragment : Fragment() {
    private lateinit var sharedPreferences: SharedPreferences
    private var _binding: FragmentDonghaianBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    private val uiHandler = Handler(Looper.getMainLooper())
    private var is_des = false;


    private fun sendRequestWithHttpUrl(userID: String, lati: Double, longi: Double) {
        thread {
            var userID2 = userID;
            if (userID2 == "") {
                userID2 = sharedPreferences.getString("FirstLaunchTime", "").toString()
            }
            var connection: HttpURLConnection? = null
            try {
                val response = StringBuilder()
                val url = URL("https://inxy.xyz/buses/position.php")
                connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.connectTimeout = 8000
                connection.readTimeout = 8000
                val requestBody = "userID=$userID2&latitude=$lati&longitude=$longi"
                DataOutputStream(connection.outputStream).writeBytes(requestBody)
                //指定请求方式
                // connection.requestMethod="Post"
                //网络输出，附带参数请求
                //val output=DataOutputStream(connection.outputStream)
                //output.writeBytes("username=admin&password=121231")
                //网络响应输入
                val input = connection.inputStream
                val reader = BufferedReader(InputStreamReader(input))
                reader.use {
                    reader.forEachLine {
                        response.append(it)
                    }
                }
                Log.e("TAG", "$response ")


            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                //断开连接
                connection?.disconnect()
            }
        }
    }

    fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        // 地球半径（单位：千米）
        val R = 6371.0

        // 将经纬度从度转换为弧度
        val lat1Rad = Math.toRadians(lat1)
        val lon1Rad = Math.toRadians(lon1)
        val lat2Rad = Math.toRadians(lat2)
        val lon2Rad = Math.toRadians(lon2)

        // 计算经纬度差值
        val deltaLat = lat2Rad - lat1Rad
        val deltaLon = lon2Rad - lon1Rad

        // 计算中间变量
        val a = sin(deltaLat / 2).pow(2) + cos(lat1Rad) * cos(lat2Rad) * sin(deltaLon / 2).pow(2)

        // 计算距离
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = R * c

        return distance
    }

    private lateinit var mLocationClient: AMapLocationClient
    private lateinit var mLocationOption: AMapLocationClientOption
    private val CHANNEL_ID = "position_mission"
    private val NOTIFICATION_ID = 1
    lateinit var notificationManager: NotificationManager

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    val GEOFENCE_BROADCAST_ACTION = "com.inxy.apis.geofencedemo.broadcast"
    private lateinit var mGeoFenceClient: GeoFenceClient
    private var mGeoFenceClientCreated=false
    private fun requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(requireContext(), ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(ACCESS_FINE_LOCATION, ACCESS_BACKGROUND_LOCATION),
                1
            )
        } else {
            // 权限已授予，启动 GeofenceService
            //startGeofenceService()
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val donghaian =
            ViewModelProvider(this).get(DongHaiAnViewModel::class.java)
        sharedPreferences = requireActivity().getSharedPreferences("buses", Context.MODE_PRIVATE)

        _binding = FragmentDonghaianBinding.inflate(inflater, container, false)
        val root: View = binding.root
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireContext() as Activity,
                arrayOf(ACCESS_FINE_LOCATION),
                1
            )
        }

        is_des = false;
        setApiKey("5a280680d06e0867f0333ca92e3f573e");
        AMapLocationClient.updatePrivacyShow(context, true, true);
        AMapLocationClient.updatePrivacyAgree(context, true);
        notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        Log.e("TAG", "初始化notificationManager: ", )
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                } else {
                    Toast.makeText(requireContext(), "您拒绝了权限🙂", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        if(sharedPreferences.getString("WeiLan", "false")=="true") {


        }else  {}

        binding.switch2.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                //sleep(3000)
                val editor = sharedPreferences.edit()
                editor.putString("WeiLan", "true")
                editor.apply()
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                Log.e("TAG", "启动围栏", )
                requestLocationPermissions()
                val serviceIntent = Intent(requireContext(), GeofenceService::class.java)
                requireContext().startForegroundService(serviceIntent)
                Log.e("TAG", "了", )
            } else {
                val editor = sharedPreferences.edit()
                editor.putString("WeiLan", "false")
                editor.apply()
                Toast.makeText(requireContext(), "关闭", Toast.LENGTH_SHORT).show()
            }
        }


        // 创建一个中心点坐标


        mLocationClient = AMapLocationClient(requireContext())
        mLocationOption = AMapLocationClientOption()
        mLocationOption.setInterval(3000)
        mLocationOption.setOnceLocation(true)
        mLocationOption.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
        //mLocationOption.isOnceLocation = true // 设置单次定位
        mLocationClient.setLocationOption(mLocationOption)
        mLocationClient.setLocationListener(object : AMapLocationListener {
            @SuppressLint("SetTextI18n")
            override fun onLocationChanged(location: AMapLocation?) {
                if (location != null && location.errorCode == 0) {
                    // 定位成功
                    val latitude = location.latitude
                    val longitude = location.longitude
                    val address = location.address
                    // 处理定位结果
                    if(!is_des)
                        binding.textNotifications2.text =
                            "Latitude: $latitude, Longitude: $longitude, Address: $address";
                    println("Latitude: $latitude, Longitude: $longitude, Address: $address")
                    var door3 = haversine(latitude, longitude, 23.403859, 116.632668);
                    var door9 = haversine(latitude, longitude, 23.399929, 116.632391);
                    var north = haversine(latitude, longitude, 23.408053, 116.641935);
                    var mind = min(min(door3, door9), north);
                    var minn = ""
                    if (mind == door9)
                        minn = "9号门"
                    if (mind == door3)
                        minn = "3号门"
                    if (mind == north)
                        minn = "北校"
                    if(!is_des)
                        binding.textNotifications22.text =
                            "3号门: $door3, \n9号门: $door9,\n 北校: $north\n\n最近距离是:$mind\n最近位置是:$minn";
                    sendRequestWithHttpUrl("", latitude, longitude)
                    mLocationClient.stopLocation()
                } else {
                    // 定位失败
                    if(!is_des)
                        binding.textNotifications2.text = "定位失败"
                    println("Location Error, Error code: ${location?.errorCode}, Error message: ${location?.errorInfo}")
                }
            }
        })


        // 开始定位
        mLocationClient.startLocation()


        val textView: TextView = binding.textDonghaian
        donghaian.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        val switch=binding.switch1
        audioManager = requireContext().getSystemService(AUDIO_SERVICE) as AudioManager

        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.never) // music 是文件名，无扩展名

        sharedPreferences = requireActivity().getSharedPreferences("buses", Context.MODE_PRIVATE)
        if(sharedPreferences.getString("NeedUpdate", "true")=="true") {
            binding.button.text = "有更新"
            binding.button.setBackgroundColor(Color.parseColor("#FF0000"))
        }
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
        is_des = true;
        if(mGeoFenceClientCreated){

            mGeoFenceClient.removeGeoFence()
            //requireContext().unregisterReceiver(mGeoFenceReceiver)
        }

        uiHandler.removeCallbacksAndMessages(null)
    }
}