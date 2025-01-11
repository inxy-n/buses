package com.inxy.buses.ui.home
import com.inxy.buses.PublicLib
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock.sleep
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.TranslateAnimation
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import com.google.gson.Gson
import com.inxy.buses.R
import com.inxy.buses.WebViewActivity
import com.inxy.buses.databinding.FragmentHomeBinding
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.concurrent.thread


class HomeFragment : Fragment() {

    var bus1:MutableList<LocalTime> = mutableListOf()
    var bus2_Mon:MutableList<LocalTime> = mutableListOf()
    var bus2_Tue:MutableList<LocalTime> = mutableListOf()
    var bus2_Wed:MutableList<LocalTime> = mutableListOf()

    var bus2_Thu:MutableList<LocalTime> = mutableListOf()

    var bus2_Fri:MutableList<LocalTime> = mutableListOf()
    var bus2_Sat:MutableList<LocalTime> = mutableListOf()
    var bus2_Sun:MutableList<LocalTime> = mutableListOf()
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    fun getCurrentTime(): String {

        val now= LocalDateTime.now().plus(pianyi)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return now.format(formatter)
    }
    val no_car=LocalTime.of(0, 0, 1);
    private val handler = Handler(Looper.getMainLooper())
    fun get_nearest_bus(nowtime:LocalTime,bus:List<LocalTime>):Int
    {
        var next=-1
        for (i in 0..<bus.size)
        {
            if(nowtime.minusMinutes(4)<=bus[i])
            {
                next=i;
                break;
            }

        }
        return next;
    }
    data class JsonrResponse(
        val jsonr: JsonrData
    )

    data class JsonrData(
        val data: Data
    )

    data class Data(
        val depDesc: String,
        val depIntervalM:Int
    )
    private fun sendRequestWithHttpUrl( ){
        thread{

            var connection: HttpURLConnection?=null
            try{
                val response=StringBuilder()
                val url= URL("https://web.chelaile.net.cn/api/bus/line!busesDetail.action?s=h5&wxs=wx_app&sign=1&h5RealData=1&v=3.10.69&src=weixinapp_cx&ctm_mp=mp_wx&vc=2&cityId=345&favoriteGray=1&userId=okBHq0KkxDoQ91pbqi5t6Cf3xWgY&h5Id=okBHq0KkxDoQ91pbqi5t6Cf3xWgY&unionId=oSpTTjvuhaS8-o1fTvB1FWfHdtUE&accountId=&secret=&lat=23.365249633789062&lng=116.70539855957031&geo_lat=23.365249633789062&geo_lng=116.70539855957031&gpstype=wgs&geo_type=wgs&scene=1256&lineId=754169935834&targetOrder=1&specialTargetOrder=1&specail=0&stationId=0754-2590&specialType=undefined&cshow=busDetail")
                connection=url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout=8000
                connection.readTimeout=8000
                connection.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36 MicroMessenger/7.0.20.1781(0x6700143B) NetType/WIFI MiniProgramEnv/Windows WindowsWechat/WMPF WindowsWechat(0x63090c11)XWEB/11275")

                connection.setRequestProperty("Referer","https://servicewechat.com/wx71d589ea01ce3321/744/page-frame.html") //指定请求方式
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
                Log.e("TAG", "$response ", )
                var r=response.toString()
                val gson = Gson()
                val response2 = gson.fromJson(r.subSequence(6,r.length-6 ).toString(), JsonrResponse::class.java)

                // 获取 depDesc 字段
                val depDesc = response2.jsonr.data.depDesc
                val depIntervalM= response2.jsonr.data.depIntervalM
                val spannableString = SpannableString("39路    点击刷新\n广东以色列理工学院南校区站 上车\n汕头大学站 下车\n\n"+depDesc+"\n"+"大约$depIntervalM 分钟一趟")
                spannableString.setSpan(StyleSpan(Typeface.BOLD), 0, 3, 0)
                if(!isDestroyed)
                activity?.runOnUiThread  {binding.cont.shantoubus.text=spannableString;}
                Log.e("TAG", "depDesc: $depDesc", )

            }catch (e:Exception){
                e.printStackTrace()
            }finally {
                //断开连接
                connection?.disconnect()
            }
        }
    }

    private val updateTimeTask = object : Runnable {
        @SuppressLint("SetTextI18n")
        override fun run() {
            // 获取当前时间并格式化
            if(sharedPreferences.getString("busListUpdated","false").toString()=="true")
            {
                updateCars();
                val editor = sharedPreferences.edit()
                editor.putString("busListUpdated","gotit")
                editor.apply()
            }
            val now_time: TextView = binding.cont.nowTime
            now_time.text = getCurrentTime();
            var nowtime=LocalTime.now();
            //var nowtime=LocalTime.of(7, 45, 10);
            var localdate=LocalDate.now();
            localdate=localdate.plusDays(pianyi.toDaysPart())
            nowtime=nowtime.plus(pianyi)

            var next=-1;
            var total_list = bus1.toMutableList();
            when (localdate.dayOfWeek) {
                DayOfWeek.MONDAY -> {
                    total_list += bus2_Mon
                    total_list.sort()
                }
                DayOfWeek.TUESDAY -> {
                    total_list += bus2_Tue
                    total_list.sort()
                }
                DayOfWeek.WEDNESDAY -> {
                    total_list += bus2_Wed
                    total_list.sort()
                }
                DayOfWeek.THURSDAY -> {
                    total_list += bus2_Thu
                    total_list.sort()
                }
                DayOfWeek.FRIDAY -> {
                    total_list += bus2_Fri
                    total_list.sort()
                }
                DayOfWeek.SATURDAY -> {
                    total_list += bus2_Sat
                    total_list.sort()
                }
                DayOfWeek.SUNDAY -> {
                    total_list += bus2_Sun
                    total_list.sort()
                }
            }
                next=get_nearest_bus(nowtime,total_list);

            binding.cont.nearestBus.text=localdate.dayOfWeek.toString();
            var SS="";
            if( next==-1)
            SS="No Car!"
            else {for (i in next..<total_list.size){
                SS+=total_list[i].format(DateTimeFormatter.ofPattern("HH:mm:ss"))+"\n";

            }}
            binding.cont.nearestBus.text=binding.cont.nearestBus.text.toString()+"\n"+SS;
            // 每秒更新一次
            handler.postDelayed(this, 1000)
        }
    }


    var pianyi=Duration.ofHours(0);
    var posi=false;
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var buttonMain: Button
    private lateinit var button11: Button
    private lateinit var button22: Button
    private lateinit var button33: Button
    private var areButtonsVisible = false
    private fun showButtons() {
        animateButton(button11, 0)
        animateButton(button22, 50)
        animateButton(button33, 100)
        areButtonsVisible = true
    }

    private fun hideButtons() {
        button11.visibility = View.GONE
        button22.visibility = View.GONE
        button33.visibility = View.GONE
        areButtonsVisible = false
    }

    private fun animateButton(button: Button, delay: Int) {
        button.visibility = View.VISIBLE
        button.post {
            val animation = TranslateAnimation((-300.0).toFloat(), 0f, 0f, 0f).apply {
                duration = 50
                startOffset = delay.toLong()
            }
            button.startAnimation(animation)
        }
    }
fun updateCars()
{
    val busListJson=sharedPreferences.getString("busList", "{\"sn\":{},\"ns\":{}}").toString()


    val jsonObject = JSONObject(busListJson)

    if (jsonObject.has("sn")) {
        bus1= mutableListOf()
        bus2_Mon = mutableListOf()
        bus2_Tue = mutableListOf()
        bus2_Wed = mutableListOf()

        bus2_Thu = mutableListOf()

        bus2_Fri = mutableListOf()
        bus2_Sat = mutableListOf()
        bus2_Sun = mutableListOf()
        val snObject = jsonObject.getJSONObject("sn")

        // 添加 bus1 的数据
        if (snObject.has("bus1")) {
            val array = snObject.getJSONArray("bus1")
            for (i in 0 until array.length()) {
                bus1.add(LocalTime.parse(array.getString(i)))
            }
        }

        // 添加 bus2_Wed 的数据
        if (snObject.has("bus2_Mon")) {
            val array = snObject.getJSONArray("bus2_Mon")
            for (i in 0 until array.length()) {
                bus2_Mon.add(LocalTime.parse(array.getString(i)))
            }
        }
        if (snObject.has("bus2_Tue")) {
            val array = snObject.getJSONArray("bus2_Tue")
            for (i in 0 until array.length()) {
                bus2_Tue.add(LocalTime.parse(array.getString(i)))
            }
        }
        if (snObject.has("bus2_Wed")) {
            val array = snObject.getJSONArray("bus2_Wed")
            for (i in 0 until array.length()) {
                bus2_Wed.add(LocalTime.parse(array.getString(i)))
            }
        }

        // 添加 bus2_Thu 的数据
        if (snObject.has("bus2_Thu")) {
            val array = snObject.getJSONArray("bus2_Thu")
            for (i in 0 until array.length()) {
                bus2_Thu.add(LocalTime.parse(array.getString(i)))
            }
        }

        // 添加 bus2_Fri 的数据
        if (snObject.has("bus2_Fri")) {
            val array = snObject.getJSONArray("bus2_Fri")
            for (i in 0 until array.length()) {
                bus2_Fri.add(LocalTime.parse(array.getString(i)))
            }
        }
        if (snObject.has("bus2_Sat")) {
            val array = snObject.getJSONArray("bus2_Sat")
            for (i in 0 until array.length()) {
                bus2_Sat.add(LocalTime.parse(array.getString(i)))
            }
        }
        if (snObject.has("bus2_Sun")) {
            val array = snObject.getJSONArray("bus2_Sun")
            for (i in 0 until array.length()) {
                bus2_Sun.add(LocalTime.parse(array.getString(i)))
            }
        }
    }
}
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        isDestroyed=false
        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        };binding.cont.shantoubus.setOnClickListener { sendRequestWithHttpUrl() ;Toast.makeText(
            requireContext(),
            "refreshed",
            Toast.LENGTH_SHORT
        ).show()}

        // 设置按钮点击事件
        val button = binding.cont.qingju
        button.setOnClickListener {
            // 加载网页
            val intent = Intent(requireContext(), WebViewActivity::class.java)
            startActivity(intent)
        }
        buttonMain=binding.cont.button2
        button11=binding.cont.jianyitian
        button22=binding.cont.rest
        button33=binding.cont.jiayitian
        buttonMain.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                buttonMain.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
        buttonMain.setOnClickListener {
            if (!areButtonsVisible) {
                showButtons()
            } else {
                hideButtons()
            }
        };
        binding.cont.rest.setOnClickListener {
            // 加载网页
            if(posi==false) {
                pianyi = pianyi.minusSeconds(LocalTime.now().toSecondOfDay().toLong());
                pianyi = pianyi.plusMinutes(5);
                posi = true;
                binding.cont.rest.text="恢复"
            }
            else
            {
                pianyi = Duration.ofSeconds(0);
                posi = false;
                binding.cont.rest.text="时间归零"
            }
            handler.removeCallbacks(updateTimeTask)
            handler.post(updateTimeTask)
        }
        button11.setOnClickListener {
            // 加载网页
            pianyi = pianyi.plusDays(-1);
            handler.removeCallbacks(updateTimeTask)
            handler.post(updateTimeTask)

        }
        button33.setOnClickListener {
            // 加载网页
            pianyi = pianyi.plusDays(1);
            handler.removeCallbacks(updateTimeTask)
            handler.post(updateTimeTask)

        }
        sharedPreferences = requireActivity().getSharedPreferences("buses", Context.MODE_PRIVATE)
        if(bus1.size==0){
            updateCars()


        }
        // 开始更新时间任务
        handler.post(updateTimeTask)
        sendRequestWithHttpUrl()
        return root
    }
var isDestroyed=false;
    override fun onDestroyView() {
        isDestroyed=true;
        handler.removeCallbacks(updateTimeTask)// 正确使用 Toast.makeText

        super.onDestroyView()
        _binding = null
    }
}