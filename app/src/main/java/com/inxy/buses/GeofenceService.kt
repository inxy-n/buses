package com.inxy.buses;

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.amap.api.fence.GeoFence
import com.amap.api.fence.GeoFenceClient
import com.amap.api.fence.GeoFenceClient.GEOFENCE_IN
import com.amap.api.fence.GeoFenceClient.GEOFENCE_OUT
import com.amap.api.fence.GeoFenceClient.GEOFENCE_STAYED
import com.amap.api.fence.GeoFenceListener
import com.amap.api.location.DPoint
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.absoluteValue


class GeofenceService : Service() {
    private val CHANNEL_ID = "position_mission"
    private val IN_ID = 1
    private val OUT_ID = 2
    lateinit var notificationManager: NotificationManager

    private lateinit var geofencingClient: GeofencingClient
    val mGeoFenceReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == GEOFENCE_BROADCAST_ACTION) {
//获取Bundle
                val bundle = intent.extras

//获取围栏行为：
                val status = bundle!!.getInt(GeoFence.BUNDLE_KEY_FENCESTATUS)

//获取自定义的围栏标识：
                val customId = bundle.getString(GeoFence.BUNDLE_KEY_CUSTOMID)

//获取围栏ID:
                val fenceId = bundle.getString(GeoFence.BUNDLE_KEY_FENCEID)

//获取当前有触发的围栏对象：
                val fence = bundle.getParcelable<GeoFence>(GeoFence.BUNDLE_KEY_FENCE)
                Log.e(
                    "TAG",
                    "电子围栏: status:$status ,fenceId:$fenceId,fence:$customId",
                )
if(Duration.between(LocalDateTime.now(),firstTime).toSeconds().absoluteValue>1L) {
    if (status == GEOFENCE_IN)
        sendNotification(IN_ID, "进入($customId)区域", "请记得取快递")
    else if (status == GEOFENCE_OUT)
        sendNotification(OUT_ID, "出($customId)区域", "🤗")
    else if (status == GEOFENCE_STAYED)
        sendNotification(IN_ID, "stay in ($customId) 10 min", "请记得取快递")
}           }
        }
    }
    override fun onDestroy(){
        super.onDestroy();
        if (mGeoFenceReceiver != null) {
            unregisterReceiver(mGeoFenceReceiver);
            //mGeoFenceReceiver = null;
        }
    }
    lateinit var firstTime:LocalDateTime;
    private fun sendNotification(notifiid:Int,title: String,data:String) {
        // 创建通知渠道
        //createNotificationChannel()
        createNotification()
        // 创建通知构建器
        val notificationBuilder = createNotificationBuilder(title, data)

        // 发送通知

        notificationManager.notify(notifiid, notificationBuilder.build())
    }



    private fun createNotificationBuilder(title: String, content: String): NotificationCompat.Builder {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("FROM_NOTIFICATION", true)
        }


        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE
            } else {
                0
            }
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
    }
    val GEOFENCE_BROADCAST_ACTION = "com.inxy.apis.geofencedemo.broadcast"
    private lateinit var mGeoFenceClient: GeoFenceClient
    @SuppressLint("ForegroundServiceType")
    override fun onCreate() {

        super.onCreate()
        try {
            // 尽快调用 startForeground() 方法
            startForeground(IN_ID,createNotification() )
        } catch (e: Exception) {
            Log.e("TAG", "Error starting foreground service", e)
        }
        // 初始化 GeofencingClient
        geofencingClient = LocationServices.getGeofencingClient(this)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        Log.e("TAG", "onCreate: onCreate")
        // 启动前台服务



    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 处理 Geofencing 相关的逻辑
        // 例如：添加或移除地理围栏
   //     Timber.tag("GeofenceService").d("onCreate called")
        mGeoFenceClient = GeoFenceClient(this);
        //mGeoFenceClientCreated=true
        mGeoFenceClient.setActivateAction(GEOFENCE_IN or GEOFENCE_OUT or GEOFENCE_STAYED)
        //Toast.makeText(requireContext(), "Now show", Toast.LENGTH_SHORT).show()
        val centerPoint = DPoint()

        centerPoint.latitude = 39.123

        centerPoint.longitude = 116.123

        mGeoFenceClient.addGeoFence(centerPoint, 500F, "寝室")
        val points = ArrayList<DPoint>()

// 添加多边形的顶点坐标
        points.add(DPoint(23.399346, 116.633095))
        points.add(DPoint(23.400903, 116.633539))
        points.add(DPoint(23.400078, 116.636203))
        points.add(DPoint(23.398754, 116.635537))

// 添加多边形地理围栏
        mGeoFenceClient.addGeoFence(points, "寝室")

        val fenceListener =
            GeoFenceListener { geoFenceList, errorCode, errorMessage ->
                if (errorCode == GeoFence.ADDGEOFENCE_SUCCESS) { // 判断围栏是否创建成功

                    firstTime = LocalDateTime.now()
                    Log.e("TAG", "添加围栏成功!\uD83E\uDD17")
                    Toast.makeText(this@GeofenceService, "添加围栏成功!🤗", Toast.LENGTH_SHORT)
                        .show()
                    // geoFenceList 是已经添加的围栏列表，可据此查看创建的围栏
                } else {
                    Toast.makeText(
                        this@GeofenceService,
                        "添加围栏失败!错误信息: $errorMessage",
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }

        mGeoFenceClient.setGeoFenceListener(fenceListener)
        mGeoFenceClient.createPendingIntent (GEOFENCE_BROADCAST_ACTION);


        val filter = IntentFilter(
            ConnectivityManager.CONNECTIVITY_ACTION
        )
        filter.addAction(GEOFENCE_BROADCAST_ACTION)
        ContextCompat.registerReceiver(
            this,
            mGeoFenceReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        Log.e("TAG", "man!\uD83E\uDD17")
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE
            } else {
                0
            }
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "进出指定区域",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("创建了围栏")
            .setContentText("")
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setContentIntent(pendingIntent)
            .build()

        return notification
    }

}