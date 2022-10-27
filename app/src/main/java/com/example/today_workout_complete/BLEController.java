package com.example.today_workout_complete;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BLEController {
    private String TAG = BLEController.class.getSimpleName();

    private static BLEController instance;

    private BluetoothLeScanner scanner;
    private BluetoothDevice device;
    private BluetoothGatt bluetoothGatt;
    private BluetoothManager bluetoothManager;

    private BluetoothGattCharacteristic btGattChar = null;

    private ArrayList<BLEControllerListener> listeners = new ArrayList<>();
    private HashMap<String, BluetoothDevice> devices = new HashMap<>();

    private UUID SERIVCE_UUID;
    private UUID SERIVCE_NOTY_UUID;

    private Button bluetoothConnectionButton;
    private Button readyStartButton;

    private BLEController(Context ctx, Button bluetoothConnectionButton, Button readyStartButton) {
        this.bluetoothManager = (BluetoothManager) ctx.getSystemService(Context.BLUETOOTH_SERVICE);
        this.bluetoothConnectionButton = bluetoothConnectionButton;
        this.readyStartButton = readyStartButton;
    }

    public static BLEController getInstance(Context ctx, Button bluetoothConnectionButton, Button readyStartButton) {
        if(null == instance)
            instance = new BLEController((ctx), bluetoothConnectionButton, readyStartButton);

        return instance;
    }

    public boolean isConnected(){
        return btGattChar != null;
    }

    public void addBLEControllerListener(BLEControllerListener l) {
        if(!this.listeners.contains(l))
            this.listeners.add(l);
    }

    public void removeBLEControllerListener(BLEControllerListener l) {
        this.listeners.remove(l);
    }

    public void init() {
        this.devices.clear();
        Log.i("[BLE]","init()");
        this.scanner = this.bluetoothManager.getAdapter().getBluetoothLeScanner();
        Log.i("[BLE]",""+scanner.toString());
        scanner.startScan(bleCallback);
    }

    private ScanCallback bleCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
//            Log.i("[BLE]","onScanResult: "+ device.toString());

            if (device.getName() != null && device.getName().startsWith("TWC")){
                Log.i("[BLE]","onScanResult: "+ device.toString());
                deviceFound(device);
            }
//            if (device.toString().equals("D4:36:39:9C:D4:83")){
//                deviceFound(device);
//            }
//            if(!devices.containsKey(device.getAddress()) && isThisTheDevice(device)) {
//                deviceFound(device);
//            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for(ScanResult sr : results) {
                BluetoothDevice device = sr.getDevice();
                if(!devices.containsKey(device.getAddress()) && isThisTheDevice(device)) {
                    deviceFound(device);
                }
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.i("[BLE]", "scan failed with errorcode: " + errorCode);
        }
    };

    private boolean isThisTheDevice(BluetoothDevice device) {
        return null != device.getName() && device.getName().startsWith("TWC");
    }

    private void deviceFound(BluetoothDevice device) {
        this.devices.put(device.getAddress(), device);
        Log.i("[BLE]","deviceFound: "+ device.toString());
        fireDeviceFound(device);
    }

    public void connectToDevice(String address) {
        this.device = this.devices.get(address);
        this.scanner.stopScan(this.bleCallback);
        Log.i("[BLE]", "connect to device " + device.getAddress());
        this.bluetoothGatt = device.connectGatt(null, false, this.bleConnectCallback);
    }

    private final BluetoothGattCallback bleConnectCallback = new BluetoothGattCallback() {

        final Handler handler = new Handler(Looper.getMainLooper());

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i("[BLE]", "start service discovery " + bluetoothGatt.discoverServices());
                bluetoothConnectionButton.setText("연결 해제"); // 버튼 텍스트 "연결" => "연결 해제"
            }else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                btGattChar = null;
                Log.w("[BLE]", "DISCONNECTED with status " + status);
                fireDisconnected();
            }else {
                Log.i("[BLE]", "unknown state " + newState + " and status " + status);
            }

        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            if(null == btGattChar) {
                Log.i("[BLE]", "onServicesDiscovered");
                for (BluetoothGattService service : gatt.getServices()) {
                    if (service.getUuid().toString().toUpperCase().startsWith("0000FFE0")) {
                        List<BluetoothGattCharacteristic> gattCharacteristics = service.getCharacteristics();
                        for (BluetoothGattCharacteristic bgc : gattCharacteristics) {
                            if (bgc.getUuid().toString().toUpperCase().startsWith("0000FFE1")) {
                                int chprop = bgc.getProperties();
                                if (((chprop & BluetoothGattCharacteristic.PROPERTY_WRITE) | (chprop & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) > 0) {
                                    btGattChar = bgc;

                                    SERIVCE_UUID = service.getUuid();
                                    SERIVCE_NOTY_UUID = bgc.getUuid();

                                    bluetoothGatt.setCharacteristicNotification(btGattChar, true);
                                    bluetoothGatt.readCharacteristic(btGattChar);

                                    Log.i("[BLE]", SERIVCE_UUID.toString() + "::::" + SERIVCE_NOTY_UUID.toString());
                                    Log.i("[BLE]", "PROPERTY_WRITE: " + chprop);
                                    fireConnected();
                                }
                            }
                        }
                    }
                }
            }
        }
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.i("[BLE]",  characteristic.getUuid() + " written)");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    String value = characteristic.getStringValue(0);
                    Log.d(TAG, "run: " + value);
                    if (value.contains("S")){
                        Log.d(TAG,"디바이스로부터 명령 데이터 수신: " + value);
                        WorkoutTrackerActivity.isWorkout = !WorkoutTrackerActivity.isWorkout;
                        WorkoutTrackerActivity.queue.add(0.1f);
                        readyStartButton.setText("종료");
                    } else if(value.contains("E")){
                        Log.d(TAG,"디바이스로부터 명령 데이터 수신: " + value);
                        WorkoutTrackerActivity.isWorkout = !WorkoutTrackerActivity.isWorkout;
                        WorkoutTrackerActivity.queue.add(0.1f);
                        readyStartButton.setText("준비");
                    } else if(value.equals("B")){
                        Log.d(TAG,"블루투스 데이터: " + value);
                    } else if(!value.startsWith("A")) {
                        WorkoutTrackerActivity.queue.add(Float.parseFloat(value));
                    }
                }
            });
        }

    };

    private void fireDisconnected() {
        for(BLEControllerListener l : this.listeners)
            l.BLEControllerDisconnected();

        this.device = null;
    }

    private void fireConnected() {
        for(BLEControllerListener l : this.listeners)
            l.BLEControllerConnected();
    }

    private void fireDeviceFound(BluetoothDevice device) {
        for(BLEControllerListener l : this.listeners) {
            l.BLEDeviceFound(device.getName().trim(), device.getAddress());
            Log.i("[BLE]","fireDeviceFound(): "+ device.getName().trim() + "  || "  + device.getAddress());
        }
    }

    public void sendData(String data) {
        btGattChar.setValue(data);
        Log.i("[BLE]", "sendData: " + data );
        bluetoothGatt.writeCharacteristic(btGattChar);
    }

    public void disconnect() {
//        sendData("B");
        bluetoothGatt.disconnect();
        btGattChar = null;
    }

}