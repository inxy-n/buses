package com.inxy.buses.ui.dashboard

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.inxy.buses.databinding.FragmentDashboardBinding
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class DashboardFragment : Fragment() {

     val bus1 = listOf(
         LocalTime.of(7, 35, 0),
         LocalTime.of(8, 0, 0),
         LocalTime.of(8, 20, 0),
         LocalTime.of(8, 40, 0),
         LocalTime.of(9, 55, 0),
         LocalTime.of(10, 20, 0),
         LocalTime.of(11, 0, 0),
         LocalTime.of(12, 5, 0),
         LocalTime.of(12, 30, 0),
         LocalTime.of(13, 0, 0),
         LocalTime.of(13, 25, 0),
         LocalTime.of(13, 45, 0),
         LocalTime.of(15, 40, 0),
         LocalTime.of(16, 0, 0),
         LocalTime.of(16, 45, 0),
         LocalTime.of(17, 15, 0),
         LocalTime.of(18, 15, 0),
         LocalTime.of(18, 45, 0),
         LocalTime.of(19, 15, 0),
         LocalTime.of(20, 0, 0),
         LocalTime.of(20, 40, 0),
         LocalTime.of(22, 0, 0)
     )
    val bus2_Wed= listOf(
        LocalTime.of(10, 0, 0),
        LocalTime.of(16, 10, 0),
        LocalTime.of(18, 20, 0),
    )

    val bus2_Thu= listOf(
        LocalTime.of(10, 0, 0),
        LocalTime.of(16, 0, 0),
        LocalTime.of(16, 10, 0),
        LocalTime.of(18, 20, 0),
        LocalTime.of(18, 35, 0),
    )

    val bus2_Fri= listOf(
        LocalTime.of(10, 0, 0),
        LocalTime.of(18, 20, 0)
    )
    private var _binding: FragmentDashboardBinding? = null
    fun getCurrentTime(): String {
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return now.format(formatter)
    }val no_car=LocalTime.of(0, 0, 1);
    private val handler = Handler(Looper.getMainLooper())
    fun get_nearest_bus(nowtime:LocalTime,bus:List<LocalTime>):Int
    {
        var next=-1
        for (i in 0..<bus.size)
        {
            if(nowtime<=bus[i])
            {
                next=i;
                break;
            }

        }
        return next;
    }
    private val updateTimeTask = object : Runnable {
        @SuppressLint("SetTextI18n")
        override fun run() {
            // 获取当前时间并格式化

            val now_time: TextView = binding.nowTime
            now_time.text = getCurrentTime();
            var nowtime=LocalTime.now();
            //var nowtime=LocalTime.of(23, 0, 0);
            var localdate= LocalDate.now();

            var next=-1;
            var total_list = bus1;
            if(localdate.dayOfWeek== DayOfWeek.WEDNESDAY)
            {
                total_list+=bus2_Wed;
                total_list=total_list.sorted();
            }else if(localdate.dayOfWeek== DayOfWeek.THURSDAY)
            {
                total_list+=bus2_Thu;
                total_list=total_list.sorted();
            }else if(localdate.dayOfWeek== DayOfWeek.FRIDAY)
            {
                total_list+=bus2_Fri;
                total_list=total_list.sorted();
            }
            next=get_nearest_bus(nowtime,total_list);

            binding.nearestBus.text= LocalDate.now().dayOfWeek.toString();
            var SS="";
            if( next==-1)
                SS="No Car!"
            else {for (i in next..<total_list.size){
                SS+=total_list[i].format(DateTimeFormatter.ofPattern("HH:mm:ss"))+"\n";

            }}
            binding.nearestBus.text=binding.nearestBus.text.toString()+"\n"+SS;
            // 每秒更新一次
            handler.postDelayed(this, 1000)
        }
    }
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textDashboard
        dashboardViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        // 开始更新时间任务
        handler.post(updateTimeTask)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()

        handler.removeCallbacks(updateTimeTask)// 正确使用 Toast.makeText

        _binding = null
    }
}