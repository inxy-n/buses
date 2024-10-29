package com.inxy.buses

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.inxy.buses.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
fun send_email(){

}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
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
                    try {
                        startActivity(Intent.createChooser(emailIntent, "Choose an email client"))
                    } catch (ex: android.content.ActivityNotFoundException) {
                        println("No email clients installed.")
                    }

                })
                .setAnchorView(R.id.fab).show()
        }
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications,R.id.donghaian
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }
}