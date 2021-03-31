package com.push.tpns;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;
import java.util.HashMap;
import java.util.Map;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

import static android.app.Notification.EXTRA_CHANNEL_ID;
import static android.provider.Settings.EXTRA_APP_PACKAGE;

public class FlutterTpnsPlugin implements FlutterPlugin, MethodCallHandler {

    final static String TAG = "FlutterTpnsPlugin";

    private static final String PLUGIN_NAME = "com.message.push/fluttertpns";
    private MethodChannel channel;
    private Application application;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), PLUGIN_NAME);
        application = (Application) flutterPluginBinding.getApplicationContext();
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, final @NonNull Result result) {
        if("checkNotifySetting".equals(call.method)){
            boolean isOpened = true;
            try {
                NotificationManagerCompat manager = NotificationManagerCompat.from(application);
                isOpened = manager.areNotificationsEnabled();
            }catch (Exception e){
                e.printStackTrace();
            }
            result.success(isOpened);
        }else if("requestPermission".equals(call.method)){
            //请求开启权限
            try {
                Intent intent = new Intent();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                    //这种方案适用于 API 26, 即8.0（含8.0）以上可以用
                    intent.putExtra(EXTRA_APP_PACKAGE, application.getPackageName());
                    intent.putExtra(EXTRA_CHANNEL_ID, application.getApplicationInfo().uid);
                }else if ("MI 6".equals(Build.MODEL)) {
                    // 小米6 -MIUI9.6-8.0.0系统，是个特例，通知设置界面只能控制"允许使用通知圆点"——然而这个玩意并没有卵用，我想对雷布斯说：I'm not ok!!!
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", application.getPackageName(), null);
                    intent.setData(uri);
                    intent.setAction("com.android.settings/.SubSettings");
                }else{
                    //这种方案适用于 API21——25，即 5.0——7.1 之间的版本可以使用
                    intent.putExtra("app_package", application.getPackageName());
                    intent.putExtra("app_uid", application.getApplicationInfo().uid);
                }
                application.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Intent intent = new Intent();
                //下面这种方案是直接跳转到当前应用的设置界面。
                //https://blog.csdn.net/ysy950803/article/details/71910806
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", application.getPackageName(), null);
                intent.setData(uri);
                application.startActivity(intent);
            }
        }else if("brand".equals(call.method)){
            String brand = Build.BRAND;
            result.success(brand);
        }else if("enableOtherPush".equals(call.method)){
            String brand = call.argument("brand");
            String pushAppId = call.argument("pushAppId");
            String pushAppKey = call.argument("pushAppKey");
            if ("Xiaomi".equals(brand)){
                XGPushConfig.setMiPushAppId(application, pushAppId);
                XGPushConfig.setMiPushAppKey(application, pushAppKey);
            }
            XGPushConfig.enableOtherPush(application, true);
        }else if (call.method.equals("registerPush")) {
            if(call.hasArgument("accessId") && call.hasArgument("accessKey")){
                int accessId = call.argument("accessId");
                String accessKey = call.argument("accessKey");
                XGPushConfig.setAccessId(application,accessId);
                XGPushConfig.setAccessKey(application,accessKey);
            }
            boolean debug = com.push.tpns.BuildConfig.DEBUG;
            if (call.hasArgument("debug")) {
                debug = call.argument("debug");
            }
            XGPushConfig.enableDebug(application, debug);
            XGPushManager.registerPush(application, new XGIOperateCallback() {
                @Override
                public void onSuccess(final Object data,final int flag) {
                    CommonUtil.runMainThread(new Runnable() {
                        @Override
                        public void run() {
                            Map<String, Object> dataMap = new HashMap<>();
                            dataMap.put("data", data);
                            dataMap.put("flag", flag);
                            result.success(dataMap);
                        }
                    });
                }

                @Override
                public void onFail(final Object data,final int errCode,final String msg) {
                    CommonUtil.runMainThread(new Runnable() {
                        @Override
                        public void run() {
                            Map<String, Object> dataMap = new HashMap<>();
                            dataMap.put("data", data);
                            dataMap.put("errCode", errCode);
                            dataMap.put("msg", msg);
                            result.success(dataMap);
                        }
                    });
                }
            });
        } else if ("deviceToken".equals(call.method)) {
            String token = XGPushConfig.getToken(application);
            result.success(token);
        } else if ("unregisterPush".equals(call.method)) {
            XGPushManager.unregisterPush(application);
        } else if ("cancelNotifaction".equals(call.method)) {
            int id = call.argument("id");
            XGPushManager.cancelNotifaction(application, id);
        } else if ("cancelAllNotifaction".equals(call.method)) {
            XGPushManager.cancelAllNotifaction(application);
        }else if("bindAccount".equals(call.method)){
            String account = call.argument("account");
            XGPushManager.bindAccount(application, account,XGPushManager.AccountType.COSTOM.getValue(), new XGIOperateCallback() {
                @Override
                public void onSuccess(final Object data,final int flag) {
                    CommonUtil.runMainThread(new Runnable() {
                        @Override
                        public void run() {
                            Map<String, Object> dataMap = new HashMap<>();
                            dataMap.put("data", data);
                            dataMap.put("flag", flag);
                            result.success(dataMap);
                        }
                    });
                }
                @Override
                public void onFail(final Object data,final int errCode,final String msg) {
                    CommonUtil.runMainThread(new Runnable() {
                        @Override
                        public void run() {
                            Map<String, Object> dataMap = new HashMap<>();
                            dataMap.put("data", data);
                            dataMap.put("errCode", errCode);
                            dataMap.put("msg", msg);
                            result.success(dataMap);
                        }
                    });
                }
            });
        }else if("delAccount".equals(call.method)){
            String account = call.argument("account");
            XGPushManager.delAccount(application,account,XGPushManager.AccountType.COSTOM.getValue(),new XGIOperateCallback(){
                @Override
                public void onSuccess(final Object data,final int flag) {
                    CommonUtil.runMainThread(new Runnable() {
                        @Override
                        public void run() {
                            Map<String, Object> dataMap = new HashMap<>();
                            dataMap.put("data", data);
                            dataMap.put("flag", flag);
                            result.success(dataMap);
                        }
                    });
                }
                @Override
                public void onFail(final Object data,final int errCode,final String msg) {
                    CommonUtil.runMainThread(new Runnable() {
                        @Override
                        public void run() {
                            Map<String, Object> dataMap = new HashMap<>();
                            dataMap.put("data", data);
                            dataMap.put("errCode", errCode);
                            dataMap.put("msg", msg);
                            result.success(dataMap);
                        }
                    });
                }
            });
        }else {
            result.notImplemented();
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }
}
