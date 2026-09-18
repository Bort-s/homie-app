package com.example.homieapp.bluetooth

import android.Manifest
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import java.util.UUID

class BTManager(
    private val context: Context,
    private val onDataReceived: (String, String, Int) -> Unit
) {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter = bluetoothManager.adapter
    private var bluetoothGatt: BluetoothGatt? = null

    // UUIDs from your main.cpp
    private val SERVICE_UUID = UUID.fromString("12345678-1234-1234-1234-1234567890ab")
    private val TX_CHAR_UUID = UUID.fromString("12345678-1234-1234-1234-1234567890ac")

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            // CHECK: Connect permission needed to read the device name
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.e("BT_LOG", "Cannot read device name: BLUETOOTH_CONNECT permission rejected.")
                return
            }

            val deviceName = result.device.name
            Log.d("BT_LOG", "Found device: ${deviceName ?: "Unknown"}")

            if (deviceName == "HMMB000001") {
                Log.i("BT_LOG", "Target found! Stopping scan and connecting...")

                try {
                    // STOP SCAN: Requires BLUETOOTH_SCAN
                    adapter?.bluetoothLeScanner?.stopScan(this)
                    // CONNECT: Requires BLUETOOTH_CONNECT
                    bluetoothGatt = result.device.connectGatt(context, false, gattCallback)
                } catch (e: SecurityException) {
                    Log.e("BT_LOG", "SecurityException: Permission lost during operation. ${e.message}")
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("BT_LOG", "Scan failed with error code: $errorCode")
        }
    }

    fun startScanning() {
        if (adapter == null) {
            Log.e("BT_LOG", "Bluetooth not supported on this device.")
            return
        }

        if (!adapter.isEnabled) {
            Log.e("BT_LOG", "Bluetooth is disabled.")
            return
        }

        val scanner = adapter.bluetoothLeScanner
        if (scanner == null) {
            Log.e("BT_LOG", "Bluetooth LE Scanner not available.")
            return
        }

        val hasScan = ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
        val hasConnect = ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED

        if (hasScan && hasConnect) {
            Log.i("BT_LOG", "Permissions verified. Starting scan...")
            try {
                scanner.startScan(scanCallback)
            } catch (e: SecurityException) {
                Log.e("BT_LOG", "SecurityException starting scan: ${e.message}")
            }
        } else {
            Log.e("BT_LOG", "ABORT: Permissions missing. Scan: $hasScan, Connect: $hasConnect")
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i("BT_LOG", "Connected to GATT. Discovering services...")
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    try {
                        gatt.discoverServices()
                    } catch (e: SecurityException) {
                        Log.e("BT_LOG", "SecurityException discovering services: ${e.message}")
                    }
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.w("BT_LOG", "Disconnected from ESP32.")
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt.getService(SERVICE_UUID)
                val characteristic = service?.getCharacteristic(TX_CHAR_UUID)

                if (characteristic != null && ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    try {
                        gatt.setCharacteristicNotification(characteristic, true)

                        val descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                        if (descriptor != null) {
                            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            gatt.writeDescriptor(descriptor)
                        }

                        Log.d("BT_LOG", "Notifications enabled for $TX_CHAR_UUID")
                    } catch (e: SecurityException) {
                        Log.e("BT_LOG", "SecurityException enabling notifications: ${e.message}")
                    }
                }
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            val rawData = String(characteristic.value)
            Log.v("BT_LOG", "RAW DATA: $rawData")

            val parts = rawData.split(":")
            if (parts.size == 2) {
                val deviceName = if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    try { gatt.device.name } catch (e: SecurityException) { "Unknown" }
                } else {
                    "Unknown"
                }
                onDataReceived(deviceName ?: "Unknown", parts[0], parts[1].toIntOrNull() ?: 0)
            }
        }
    }
}