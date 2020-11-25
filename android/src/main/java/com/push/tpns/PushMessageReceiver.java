package com.push.tpns;

import android.content.Context;
import android.util.Log;

import com.tencent.android.tpush.XGPushBaseReceiver;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushRegisterResult;
import com.tencent.android.tpush.XGPushShowedResult;
import com.tencent.android.tpush.XGPushTextMessage;

public class PushMessageReceiver extends XGPushBaseReceiver {

    public final static String TAG = "PushMessageReceiver";

    @Override
    public void onRegisterResult(Context context, int i, XGPushRegisterResult xgPushRegisterResult) {
        io.flutter.Log.e(TAG,"==============> onRegisterResult");
    }

    /**
     * 反注册结果
     * @param context 当前上下文
     * @param errorCode  为成功，其它为错误码
     */
    @Override
    public void onUnregisterResult(Context context, int errorCode) {
        String text;
        if (errorCode == XGPushBaseReceiver.SUCCESS) {
            text = "==============> 反注册成功";
        } else {
            text = "==============> 反注册失败" + errorCode;
        }
        Log.e(TAG, text);
    }

    @Override
    public void onSetTagResult(Context context, int i, String s) {
        Log.e(TAG,"==============> onSetTagResult");
    }

    @Override
    public void onDeleteTagResult(Context context, int i, String s) {
        Log.e(TAG,"==============> onDeleteTagResult");
    }

    @Override
    public void onSetAccountResult(Context context, int i, String s) {
        Log.e(TAG,"==============> onSetAccountResult");
    }

    @Override
    public void onDeleteAccountResult(Context context, int i, String s) {
        Log.e(TAG,"==============> onDeleteAccountResult");
    }

    @Override
    public void onSetAttributeResult(Context context, int i, String s) {
        Log.e(TAG,"==============> onSetAttributeResult");
    }

    @Override
    public void onDeleteAttributeResult(Context context, int i, String s) {
        Log.e(TAG,"==============> onDeleteAttributeResult");
    }

    @Override
    public void onTextMessage(Context context, XGPushTextMessage xgPushTextMessage) {
        Log.e(TAG,"==============> onTextMessage");
    }

    @Override
    public void onNotificationClickedResult(Context context, XGPushClickedResult xgPushClickedResult) {
        Log.e(TAG,"==============> onNotificationClickedResult");
    }

    @Override
    public void onNotificationShowedResult(Context context, XGPushShowedResult xgPushShowedResult) {
        Log.e(TAG,"==============> onNotificationShowedResult");
    }
}
