package com.itstermoh.hook;

import android.widget.Toast;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;
import android.app.PendingIntent;
import java.util.HashMap;
import android.util.Log;
import android.hardware.usb.UsbConfiguration;
import android.widget.Magnifier.Builder;
import java.util.ArrayList;

public class Blaster {

    private static final String ACTION_USB_PERMISSION = "com.itstermoh.hook.USB_PERMISSION";
    private static final int USB_READ_BUFFER_SIZE = 64;

    private Context context;
    private UsbManager usbManager;
    private UsbDevice usbDevice;
    private UsbDeviceConnection usbConnection;
    private UsbInterface usbInterface;
    private UsbEndpoint inEndpoint;
    private UsbEndpoint outEndpoint;
    private boolean permissionGranted;
    public byte[] bit;
    public int sent;

    private Handler messageHandler;

    public Blaster(Context context, Handler handler) {
        this.context = context;
        this.messageHandler = handler;
        this.usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        context.registerReceiver(usbPermissionReceiver, filter);
    }

    public void initializeCommunication() {
        UsbDevice device = findDevice();
        if (device != null) {
            usbDevice = device;
            if (!usbManager.hasPermission(usbDevice)) {
                requestPermission();
            } else {
                openConnection();
            }
        } else {
        }
    }
    
    private void openConnection() {
        if (usbDevice != null) {
            int interfaceCount = usbDevice.getInterfaceCount();
            if (interfaceCount > 0) {
                usbInterface = usbDevice.getInterface(0);

                outEndpoint = usbInterface.getEndpoint(0);

                inEndpoint = usbInterface.getEndpoint(1);
                
                usbConnection = usbManager.openDevice(usbDevice);
                if (usbConnection != null) {
                    if (usbConnection.claimInterface(usbInterface, true)) {
                    
                        initStringControlTransfer(usbConnection, 0, "quandoo"); // MANUFACTURER
                        initStringControlTransfer(usbConnection, 1, "Android2AndroidAccessory"); // MODEL
                        initStringControlTransfer(usbConnection, 2, "showcasing android2android USB communication"); // DESCRIPTION
                        initStringControlTransfer(usbConnection, 3, "0.1"); // VERSION
                        initStringControlTransfer(usbConnection, 4, "http://quandoo.de"); // URI
                        initStringControlTransfer(usbConnection, 5, "42"); // SERIAL
                        
                        usbConnection.controlTransfer(0x40, 53, 0, 0, new byte[]{}, 0, 100);
                        
                        sendMessageToHandler("USB connection opened successfully");
                        setBuff(new byte[16384]);
                        return;
                    } else {
                        sendMessageToHandler("Failed to claim USB interface");
                    }
                } else {
                    sendMessageToHandler("Failed to open USB connection");
                }
            } else {

                sendMessageToHandler("No USB interface found");
            }
        } else {
            sendMessageToHandler("USB device is null");
        }
        sendMessageToHandler("Failed to open USB connection");
    }
    
    private void initStringControlTransfer(final UsbDeviceConnection deviceConnection,
                                           final int index,
                                           final String string) {
        deviceConnection.controlTransfer(0x40, 52, 0, index,
                string.getBytes(), string.length(), 100);
    }
    
    private UsbDevice findDevice() {
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        
        for (UsbDevice device : deviceList.values()) {
            if (device.getVendorId() == 0x045c && device.getProductId() == 0x0195) {
                return device;
            }
        }
        return null;
    }

    private void requestPermission() {
        PendingIntent permissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
        usbManager.requestPermission(usbDevice, permissionIntent);
    }

    public void sendData(byte[] data) {
        if (usbConnection != null && outEndpoint != null) {
        int sentBytes = usbConnection.bulkTransfer(outEndpoint,data, data.length, 100);
        
        sent = sentBytes;
        if (sentBytes > 0) {
            byte[] bit1 = new byte[sentBytes];
            byte[] bit2 = getBuff();
            byte bit3 = 0;
            System.arraycopy(bit2, 0, bit1, 0, sentBytes);
            
        }
        if (sentBytes > 0) {
            sent = sentBytes;
        } else {
        }
        /*try {
            Thread.sleep((long)(1000 / 5) - (long)10);
        } catch (Exception e) {
        }*/
    } else {
        }
    }
    
    public void test(ArrayList arrayList) {
        byte[] bytearray = new byte[arrayList.size()];
        for (int i=0;i<arrayList.size();i++) {
            bytearray[i] = arrayList.get(i);
        }
        int transfered = usbConnection.bulkTransfer(outEndpoint, bytearray, arrayList.size(), 100);
        sent = transfered;
    }
    
    public void startReadingData() {
        
    }

    public void closeCommunication() {
        
    }

    private final BroadcastReceiver usbPermissionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            permissionGranted = true;
                            initializeCommunication();
                        }
                    } else {
                        permissionGranted = false;
                    }
                }
            }
        }
    };
    
    public void setBuff(byte[] bit) {
        this.bit = bit;
    }
    
    public byte[] getBuff() {
        return this.bit;
    }
    
    public void sendMessageToHandler(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT);
    }
}
