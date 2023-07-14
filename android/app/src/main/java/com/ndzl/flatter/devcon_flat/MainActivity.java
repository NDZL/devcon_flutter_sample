package com.ndzl.flatter.devcon_flat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.Date;
import java.util.Objects;

import androidx.annotation.NonNull;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.EventChannel.EventSink;
import io.flutter.plugin.common.EventChannel.StreamHandler;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.MethodCall;

public class MainActivity extends FlutterActivity {
    private final static String TAG_LOG ="Zebra";
    private final static String TAG_EVENT_CHANNEL_NAME="com.ndzl.dw/ZebraDatawedgeEventChannel";
    private final static String TAG_METHOD_CHANNEL_NAME="com.ndzl.dw/ZebraDatawedgeMethodChannel";
    private BroadcastReceiver dwBroadcastReceiver;

    private EventChannel.EventSink dwEventChannelSink;

/*    @Override
    public void configureFlutterEngine(FlutterEngine flutterEngine) {
        Log.i(TAG,"configureFlutterEngine");
        GeneratedPluginRegistrant.registerWith(flutterEngine);
    }*/

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        Log.i(TAG_LOG,"configureFlutterEngine");
        //new EventChannel(flutterEngine.getDartExecutor(), TAG_EVENT_CHANNEL_NAME).setStreamHandler(
        //new MethodChannel(flutterEngine.getDartExecutor(), TAG_METHOD_CHANNEL_NAME).setMethodCallHandler(
    }

    @Override
    protected void onCreate( Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.i(TAG_LOG,"onCreate");

        new MethodChannel(Objects.requireNonNull(getFlutterEngine()).getDartExecutor(),
                "com.ndzl.dw/ZebraDatawedgeMethodChannel").setMethodCallHandler(

                new MethodCallHandler() {
                    @Override
                    public void onMethodCall(MethodCall call, Result result) {

                        if (call.method.equals("softScanTriggerStart")) {
                            dwStartScan();
                            result.success(0);
                        } else {
                            result.notImplemented();
                        }
                    }
                }
        );

        new EventChannel(Objects.requireNonNull(getFlutterEngine()).getDartExecutor(),
                "com.ndzl.dw/ZebraDatawedgeEventChannel").setStreamHandler(
                new EventChannel.StreamHandler() {
                    @Override
                    public void onListen(Object arguments, EventSink events) {
                        Log.i(TAG_LOG,"EventChannel/onListen");
                        dwEventChannelSink = events;

                        //like onCreate for initialization, but with events available
                        dwBroadcastReceiver = createDWBroadcastReceiver(events);
                        IntentFilter filter = new IntentFilter();
                        filter.addAction("com.ndzl.DW");
                        filter.addCategory("android.intent.category.DEFAULT");
                        registerReceiver(dwBroadcastReceiver, filter);
                    }

                    @Override
                    public void onCancel(Object arguments) {
                        dwEventChannelSink = null;
                    }
                }
        );

    }

    private BroadcastReceiver  createDWBroadcastReceiver(EventSink events) {
        Log.i(TAG_LOG,"createDWBroadcastReceiver");
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //
                String barcode_value = intent.getStringExtra("com.symbol.datawedge.data_string");
                String barcode_type = intent.getStringExtra("com.symbol.datawedge.label_type");
                long epoch_scan_time =  intent.getLongExtra("com.symbol.datawedge.data_dispatch_time", -1 );
                String readable_scan_time = (new Date(epoch_scan_time)).toGMTString();
                Log.i(TAG_LOG,"onReceive DW="+ barcode_type+" "+barcode_value );
                if(events!=null)
                    events.success( "Scanned data: <"+barcode_value+">\nSymbology: "+barcode_type+"\nEvent time: "+readable_scan_time );
            }
        };
    }

    public void dwStartScan(){
        String softScanTrigger = "com.symbol.datawedge.api.ACTION";
        String extraData = "com.symbol.datawedge.api.SOFT_SCAN_TRIGGER";

        Intent i = new Intent();
        i.setAction(softScanTrigger);
        i.putExtra(extraData, "START_SCANNING");
        this.sendBroadcast(i);
    }

}
