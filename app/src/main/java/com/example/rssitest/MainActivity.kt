package com.example.rssitest

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var wifiManager: WifiManager
    private lateinit var wifiListView: ListView
    private lateinit var wifiListAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        wifiListView = findViewById(R.id.wifiListView)
        wifiListAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        wifiListView.adapter = wifiListAdapter

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager



        if (!wifiManager.isWifiEnabled) {
            wifiManager.isWifiEnabled = true
        }

        // 브로드캐스트 리시버 등록 (스캔 결과 수신 시 filterBySignalStrength 호출)
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d("WiFiScan", "Received scan results broadcast")
                val scanResults = wifiManager.scanResults
                filterBySignalStrength(scanResults, -1000) // -70dBm 이상의 신호만 필터링
            }
        }, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))

        wifiManager.startScan()
        Log.d("WiFiScan", "Started Wi-Fi scan")
    }

    /*
    private fun updateWifiList(scanResults: List<ScanResult>, minRssi: Int) {
        val filteredResults = scanResults.filter { it.level >= minRssi }
        val wifiInfoList = if (filteredResults.isEmpty()) {
            listOf("Wi-Fi 네트워크를 찾을 수 없습니다.")
        } else {
            filteredResults.map { "${it.SSID} - ${it.level}dBm" }
        }

        runOnUiThread {
            wifiListAdapter.clear()
            wifiListAdapter.addAll(wifiInfoList)
            wifiListAdapter.notifyDataSetChanged()
        }
    }
    */

    private fun filterBySignalStrength(scanResults: List<ScanResult>, minRssi: Int) {
        Log.d("WiFiScan", "Scan result size: ${scanResults.size}") // 🔥 로그 추가
        val filteredResults = scanResults.filter { it.level >= minRssi }

        if (filteredResults.isEmpty()) {
            Log.d("WiFiScan", "No Wi-Fi networks found.") // 🔥 로그 추가
        }

        val wifiInfoList = if (filteredResults.isEmpty()) {
            listOf("Wi-Fi 네트워크를 찾을 수 없습니다.")
        } else {
            filteredResults.map { "${it.SSID} - ${it.level}dBm" }
        }

        runOnUiThread {
            Log.d("WiFiScan", "Updating ListView with ${wifiInfoList.size} items") // 🔥 로그 추가
            wifiListAdapter.clear()
            wifiListAdapter.addAll(wifiInfoList)
            wifiListAdapter.notifyDataSetChanged()
        }
    }
}
