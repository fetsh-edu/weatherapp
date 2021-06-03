package me.fetsh.geekbrains.weather

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.android.material.snackbar.Snackbar
import me.fetsh.geekbrains.weather.ui.main.MainFragment

class MainActivity : AppCompatActivity() {

    private val br: BroadcastReceiver = object : BroadcastReceiver() {

        private var snackbar : Snackbar? = null

        override fun onReceive(context: Context, intent: Intent) {
            val hasConnection = !intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)
            if (hasConnection) {
                snackbar?.dismiss()
            } else {
                val view : View? = (context as Activity).findViewById(R.id.container)
                view?.let {
                    snackbar = Snackbar.make(it, context.getString(R.string.no_connection), Snackbar.LENGTH_INDEFINITE)
                    snackbar?.show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(br, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }
    override fun onStop() {
        super.onStop()
        unregisterReceiver(br)
    }
}