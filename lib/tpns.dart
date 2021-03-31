
import 'dart:async';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class FlutterTpns {
  static const MethodChannel _channel = const MethodChannel('com.message.push/fluttertpns');

  ///
  /// 检查通知权限是否开启
  ///
  static Future<bool> get checkNotifySetting async{
    return await _channel.invokeMethod('checkNotifySetting');
  }

  ///
  /// 若是Android获取当前手机厂商
  ///
  static Future<String> get brand async {
    String brand = await _channel.invokeMethod("brand");
    return brand;
  }

  ///
  /// 只有Android会启用其他通道,比如小米的,华为的等
  ///
  static void enableOtherPush({@required String brand,String pushAppId='',String pushAppKey=''}) async{
    if(Platform.isAndroid){
      await _channel.invokeMethod('enableOtherPush',{'brand':brand,'pushAppId':pushAppId,'pushAppKey':pushAppKey});
    }
  }

  ///
  /// 普通注册只注册当前设备，后台能够针对不同的设备 Token 发送推送消息，有2个版本的 API 接口。
  /// accessId AccessID
  /// accessKey 设置 AccessKey
  ///
  /// 返回注册成功或失败的处理接口
  ///
  static Future<Map<String,dynamic>> registerPush({@required int accessId,@required String accessKey,bool debug = false}) async{
    if(Platform.isAndroid){
      Map<String,dynamic> _data = await _channel.invokeMapMethod("registerPush",{'debug':debug,'accessId':accessId,'accessKey':accessKey});
      return _data;
    }else{
      return {};
    }
  }

  static Future<String> get deviceToken async {
    final String version = await _channel.invokeMethod('deviceToken');
    return version;
  }

  ///
  /// 推荐有账号体系的App使用（此接口会覆盖设备之前绑定过的账号，仅当前注册的账号生效）
  /// 用户登录的帐号 
  ///
  static Future<Map<String,dynamic>> bindAccount({@required String account}) async{
    if(Platform.isAndroid){
      Map<String,dynamic> _data = await _channel.invokeMapMethod("bindAccount",{"account":account});
      return _data;
    }
    return {};
  }


  static void requestPermission({@required bool sound,@required bool alert,@required bool badge}) async {
    try {
      await _channel.invokeMethod('requestPermission',{'sound':sound,'alert':alert,'badge':badge});
    } catch (e) {
    }
  }

  ///
  /// 清楚所有通知
  ///
  static void cancelAllNotifaction() async {
    try {
      if(Platform.isAndroid){
        await _channel.invokeMethod('cancelAllNotifaction');
      }
    } catch (e) {
    }
  }
}