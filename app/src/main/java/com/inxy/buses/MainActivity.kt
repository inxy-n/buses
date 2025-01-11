package com.inxy.buses

import android.Manifest
import android.app.Activity
import android.content.ClipData.Item
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Rect
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationClient.setApiKey
import com.amap.api.location.AMapLocationListener
import com.amap.apis.utils.core.api.AMapUtilCoreApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.inxy.buses.databinding.ActivityMainBinding
import com.inxy.buses.ui.dashboard.DashboardFragment
import com.inxy.buses.ui.home.HomeFragment
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.concurrent.thread
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    private lateinit var mLocationClient: AMapLocationClient
    private lateinit var binding: ActivityMainBinding
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
    private lateinit var sharedPreferences: SharedPreferences
    private fun saveFirstLaunchTime() {
        val currentTime = System.currentTimeMillis()
        val phoneBrand = Build.BRAND
        val phoneXinghao=Build.MODEL
        val encodedBytes = Base64.encode("$currentTime+$phoneBrand+$phoneXinghao".toByteArray(), Base64.DEFAULT)
        val encodedString = String(encodedBytes)
        println("Encoded String: $encodedString")
        val editor = sharedPreferences.edit()
        editor.putString("FirstLaunchTime", encodedString.toString())
        editor.apply()

    }
    private fun sendRequestWithHttpUrl(userID: String,lati: Double,longi:Double){
        thread{
            var userID2=userID;
            if(userID2=="")
            {
                userID2= sharedPreferences.getString("FirstLaunchTime", "").toString()
            }
            var connection: HttpURLConnection?=null
            try{
                val response=StringBuilder()
                val url= URL("https://inxy.xyz/buses/position.php")
                connection=url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.connectTimeout=8000
                connection.readTimeout=8000
                val requestBody = "userID=$userID2&latitude=$lati&longitude=$longi&activity=startup"
                DataOutputStream(connection.outputStream).writeBytes(requestBody)

                val editor = sharedPreferences.edit()
                editor.putString("LaunchPosition", "latitude=$lati;longitude=$longi")
                editor.apply()

                val input=connection.inputStream
                val reader= BufferedReader(InputStreamReader(input))
                reader.use{
                    reader.forEachLine {
                        response.append(it)
                    }
                }
                Log.e("TAG", "$response ", )


            }catch (e:Exception){
                e.printStackTrace()
            }finally {
                //断开连接
                connection?.disconnect()
            }
        }
    }private fun checkV( ){
        thread{
            var connection: HttpURLConnection?=null
            try{
                val response1=StringBuilder()
                val response2=StringBuilder()
                val url= URL("https://inxy.xyz/buses/v")
                Log.e("TAG", "checkV:", )
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
                var flag=true
                reader.use{
                    reader.forEachLine {
                        if(flag)
                        {
                            response1.append(it)
                            flag=false
                        }
                        else
                        {
                            response2.append(it)
                        }
                    }
                }
                val packageInfo = this.packageManager.getPackageInfo(this.packageName, 0)
                val versionName = packageInfo.versionName.toFloat()
                val v=response1.toString().toFloat()
                val remoteBusv=response2.toString().toFloat()

                Log.e("TAG", "checkV:$v $remoteBusv ", )
                if(v>versionName) {
                    val editor = sharedPreferences.edit()
                    editor.putString("NeedUpdate", "true")
                    editor.apply()
                    if (sharedPreferences.getString("ShowRedDot", "true")=="true")
                    {
                        runOnUiThread  {addRedDot(binding.navView, R.id.donghaian)}


                    }
                }else
                {
                    val editor = sharedPreferences.edit()
                    editor.putString("NeedUpdate", "false")
                    editor.apply()
                }


                val busV=sharedPreferences.getString("busV", "0").toString().toFloat()
                if(remoteBusv>busV)
                {
                    val response=StringBuilder()
                    var connection2: HttpURLConnection?=null
                    val url2= URL("https://inxy.xyz/buses/buslist.txt")

                    connection2=url2.openConnection() as HttpURLConnection
                    connection2.connectTimeout=8000
                    connection2.readTimeout=8000
                    val input2=connection2.inputStream
                    val reader2= BufferedReader(InputStreamReader(input2))
                    reader2.use{
                        reader2.forEachLine {
                            response.append(it)
                        }
                    }
                    var buslist=response.toString()
                    Log.e("TAG", "checkV:$buslist", )
                    val editor = sharedPreferences.edit()
                    editor.putString("busList",buslist)
                    editor.putString("busListUpdated","true")
                    editor.putString("busV",remoteBusv.toString())
                    editor.apply()
                    Log.e("TAG", "checkV:车表已经更新 ", )
                    Handler(Looper.getMainLooper()).post {
                    Toast.makeText(this, "车表已经更新", Toast.LENGTH_SHORT).show()
                    }
                    //binding.root.findViewById<TextView>(R.id.Shuttletitle).setText("Shuttle Bus (updated)")
                }else Log.e("TAG", "checkV:车表 &%$remoteBusv $busV 新 ", )

            }catch (e:Exception){
                e.printStackTrace()
            }finally {
                //断开连接
                connection?.disconnect()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        sharedPreferences = getSharedPreferences("buses", Context.MODE_PRIVATE)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_dashboard,
                R.id.navigation_notifications,
                R.id.donghaian
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        val contentView: View = findViewById(android.R.id.content)
        contentView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val r = Rect()
                contentView.getWindowVisibleDisplayFrame(r)
                val screenHeight = contentView.rootView.height
                val keypadHeight = screenHeight - r.bottom

                if (keypadHeight > screenHeight * 0.15) { // 如果键盘高度超过屏幕高度的15%，则认为键盘已打开
                    // 键盘已打开，隐藏底部导航栏
                    navView.visibility = View.GONE
                } else {
                    // 键盘已关闭，显示底部导航栏
                    navView.visibility = View.VISIBLE
                }
            }
        })
        // 显示手机品牌
        // 检查是否是首次启动
        if (!sharedPreferences.contains("FirstLaunchTime")) {
            saveFirstLaunchTime()
        } else {
            val firstLaunchTime = sharedPreferences.getString("FirstLaunchTime", "")
            Log.e("TAG", "onCreate:$firstLaunchTime ", )
        }

        if (!sharedPreferences.contains("ShowRedDot") ) {
            val editor = sharedPreferences.edit()
            editor.putString("ShowRedDot", "true")
            editor.apply()
            checkV();
        }else {
            if(sharedPreferences.getString("NeedUpdate", "false")=="false")
            {
            val showRedDot = sharedPreferences.getString("ShowRedDot", "")
            Log.e("TAG", "onCreate:$showRedDot ", )
            checkV();
            }else if (sharedPreferences.getString("ShowRedDot", "true")=="true")
            {

                addRedDot(navView, R.id.donghaian)
                checkV();
            }
        }




        AMapLocationClient.updatePrivacyShow(this, true, true);
        AMapLocationClient.updatePrivacyAgree(this, true)
        AMapUtilCoreApi.setCollectInfoEnable(true);




        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Send feedback to the author", Snackbar.LENGTH_LONG)
                .setAction("OPEN EMAIL APP", View.OnClickListener {

                    // 在这里添加你的监听器逻辑
                    val emailIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "message/rfc822"
                        putExtra(Intent.EXTRA_EMAIL, arrayOf("inxlll@outlook.com"))
                        putExtra(Intent.EXTRA_SUBJECT, "buses App反馈")
                        putExtra(Intent.EXTRA_TEXT, "")
                    }
                    try {val editor = sharedPreferences.edit()
                        editor.putString("busV","0")
                        editor.apply()
                        startActivity(Intent.createChooser(emailIntent, "Choose an email client"))
                    } catch (ex: android.content.ActivityNotFoundException) {
                        println("No email clients installed.")
                    }

                })
                .setAnchorView(R.id.fab).show()
        }



        setApiKey("5a280680d06e0867f0333ca92e3f573e");
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        } else {


            mLocationClient = AMapLocationClient(applicationContext)
            getNowKnownLocation()


            // Example condition, replace with actual logic

        }
    }


    private fun addRedDot(navView: BottomNavigationView, itemId: Int) {
        val menuItem: MenuItem = navView.menu.findItem(itemId)
        val itemView: View = navView.findViewById(menuItem.itemId)

        if (itemView is FrameLayout) {
            // 创建红点视图
            val redDot = View(this)
            redDot.setBackgroundResource(R.drawable.red_dot)
            val dotParams = FrameLayout.LayoutParams(
                resources.getDimensionPixelSize(R.dimen.red_dot_size),
                resources.getDimensionPixelSize(R.dimen.red_dot_size)
            )
            dotParams.gravity = android.view.Gravity.TOP or android.view.Gravity.END
            dotParams.topMargin = resources.getDimensionPixelSize(R.dimen.red_dot_margin_top)
            dotParams.marginEnd = resources.getDimensionPixelSize(R.dimen.red_dot_margin_end)
            itemView.addView(redDot, dotParams)
        }
    }
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }


    private fun getNowKnownLocation() {
        try {

                Log.w("MainActivity", "Last known location is null")
                requestNewLocationData()

        } catch (e: Exception) {
            Log.e("MainActivity", "Error getting last known location", e)
        }
    }

    private fun handleLocation(location: AMapLocation) {
        // 处理位置信息
        Log.d("MainActivity", "Latitude: ${location.latitude}, Longitude: ${location.longitude}")
        val latitude = location.latitude
        val longitude = location.longitude
        val address = location.address
        var door3 = haversine(latitude, longitude, 23.403859, 116.632668);
        var door9 = haversine(latitude, longitude, 23.399929, 116.632391);
        var north = haversine(latitude, longitude, 23.408053, 116.641935);
        var mind = min(min(door3, door9), north);

    // Passing each menu ID as a set of Ids because each
    // menu should be considered as top level destinations.

    //val navController = findNavController(R.id.nav_host_fragment_activity_main)
    val navInflater = findNavController(R.id.nav_host_fragment_activity_main).navInflater
    val navGraph = navInflater.inflate(R.navigation.mobile_navigation)
    //Toast.makeText(this, "定位成功", Toast.LENGTH_SHORT).show()
    if (mind== north) {
        navGraph.setStartDestination(R.id.navigation_dashboard)
        findNavController(R.id.nav_host_fragment_activity_main).graph = navGraph
    }
    }

    private fun requestNewLocationData() {
        // 创建定位参数
        val option = AMapLocationClientOption()
        option.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
        option.isOnceLocation = true

        // 设置定位监听
        mLocationClient.setLocationListener(object : AMapLocationListener {
            override fun onLocationChanged(location: AMapLocation?) {
                if (location != null && location.errorCode == 0) {

                    Log.e("TAG", "handleLocation:$location.toDouble() ", )
                    handleLocation(location)
                    sendRequestWithHttpUrl("",location.latitude ,location.longitude)
                } else {
                    // 定位失败
                    Log.e("MainActivity", "Location failed with error code: ${location?.errorCode} and error info: ${location?.errorInfo}")
                }
                // 停止定位
                mLocationClient.stopLocation()
            }
        })

        // 开始单次定位
        mLocationClient.startLocation()
    }

}