package com.example.rssitest

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.net.wifi.rtt.RangingRequest
import android.net.wifi.rtt.RangingResult
import android.net.wifi.rtt.RangingResultCallback
import android.net.wifi.rtt.WifiRttManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var wifiManager: WifiManager
    private lateinit var wifiRTTManager: WifiRttManager
    private lateinit var wifiListView: ListView
    private lateinit var wifiListAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        wifiListView = findViewById(R.id.wifiListView)
        wifiListAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        wifiListView.adapter = wifiListAdapter

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiRTTManager =
            applicationContext.getSystemService(Context.WIFI_RTT_RANGING_SERVICE) as? WifiRttManager
                ?: run {
                    Log.e("MainActivity", "WifiRttManager is not available on this device.")
                    return
                }

        if (wifiRTTManager == null) {
            Log.e("WiFiRTT", "Wi-Fi RTT 기능이 지원되지 않는 기기입니다.")
        }

        if (wifiRTTManager == null) {
            Log.e("WiFiRTT", "Wi-Fi RTT 기능이 지원되지 않는 기기입니다.")
        }

        if (!wifiManager.isWifiEnabled) {
            wifiManager.isWifiEnabled = true
        }

        // 브로드캐스트 리시버 등록 (스캔 결과 수신 시 filterBySignalStrength 호출)
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d("WiFiScan", "Received scan results broadcast")
                val scanResults = wifiManager.scanResults
                filterBySignalStrength(scanResults, -70) // -70dBm 이상의 신호만 필터링
                Log.d("11", "11111111111111111111111111111111")
                rttFunction(scanResults, -70, wifiRTTManager)
                Log.d("33", "3333333333333333333333")
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

    //    @RequiresPermission(Manifest.permission.NEARBY_WIFI_DEVICES)
    private fun rttFunction(
        scanResults: List<ScanResult>,
        minRssi: Int,
        wifiRttManager: WifiRttManager
    ) {
        Log.d("22", "222222222222222222")
//        val filteredResults = scanResults.filter { it.level >= minRssi }

        val filteredResults = scanResults.filter { it.is80211mcResponder }
        if (filteredResults.isEmpty()) {
            Log.e("WifiRttHelper", "No RTT-capable APs found")
            return
        }
        val rangingRequest = RangingRequest.Builder().apply {
            filteredResults.filter { it.is80211mcResponder }.forEach { addAccessPoint(it) }
        }.build()
        val executor = ContextCompat.getMainExecutor(applicationContext)
        wifiRTTManager.startRanging(
            rangingRequest,
            executor,
            object : RangingResultCallback() {
                override fun onRangingResults(results: List<RangingResult>) {
                    for (result in results) {
                        if (result.status == RangingResult.STATUS_SUCCESS) {
                            val distanceMm = result.distanceMm
                            val macAddress = result.macAddress
                            // distanceMm를 활용하여 AP와의 거리 정보를 처리합니다.
                            Log.d(
                                "WifiRttHelper",
                                "AP MAC: $macAddress, 거리: ${distanceMm / 1000.0} m"
                            )
                        } else {
                            // 거리 측정 실패 처리
                            Log.e("WifiRttHelper", "거리 측정 실패: ${result.status}")
                        }
                    }
                }

                override fun onRangingFailure(code: Int) {
                    Log.e("WifiRttHelper", "Wi-Fi RTT 실패: 코드 $code")
                }
            }

        )
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
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
        filteredResults.map {
            Log.d(
                "WiFiScan",
                "SSID: ${it.wifiSsid}, Level: ${it.level}, ${it.BSSID} // ${it.capabilities} // ${it.channelWidth} // ${it.frequency}"
            )
        }

        runOnUiThread {
            Log.d("WiFiScan", "Updating ListView with ${wifiInfoList.size} items") // 🔥 로그 추가
            wifiListAdapter.clear()
            wifiListAdapter.addAll(wifiInfoList)
            wifiListAdapter.notifyDataSetChanged()
        }
    }
}
