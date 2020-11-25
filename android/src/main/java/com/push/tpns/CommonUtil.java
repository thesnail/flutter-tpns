package com.push.tpns;

import android.os.Handler;
import android.os.Looper;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class CommonUtil {
    private final static Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    public static <T> T getParam(MethodCall methodCall, MethodChannel.Result result, String param) {
        T par = methodCall.argument(param);
        if (par == null) {
            result.error("Missing parameter", "Cannot find parameter `" + param + "` or `" + param + "` is null!", 5);
            throw new RuntimeException("Cannot find parameter `" + param + "` or `" + param + "` is null!");
        }
        return par;
    }
    public static void runMainThread(Runnable runnable) {
        MAIN_HANDLER.post(runnable);
    }
    public static void runMainThreadReturn(final MethodChannel.Result result, final Object param) {
        MAIN_HANDLER.post(new Runnable() {
            @Override
            public void run() {
                result.success(param);
            }
        });
    }
    public static void runMainThreadReturnError(final MethodChannel.Result result, final String errorCode, final String errorMessage, final Object errorDetails) {
        MAIN_HANDLER.post(new Runnable() {
            @Override
            public void run() {
                result.error(errorCode, errorMessage, errorDetails);
            }
        });
    }
}
