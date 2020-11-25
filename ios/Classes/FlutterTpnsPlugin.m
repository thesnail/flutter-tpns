#import "FlutterTpnsPlugin.h"
#import <UserNotifications/UserNotifications.h>

@implementation FlutterTpnsPlugin{
    NSString *_deviceToken;
    FlutterMethodChannel *_methodChannel;
}

+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
    FlutterMethodChannel* channel = [FlutterMethodChannel methodChannelWithName:@"com.message.push/fluttertpns" binaryMessenger:[registrar messenger]];
    FlutterTpnsPlugin* instance = [[FlutterTpnsPlugin alloc] initWithChannel:channel];
    [registrar addApplicationDelegate:instance];
    [registrar addMethodCallDelegate:instance channel:channel];
}

- (instancetype)initWithChannel:(FlutterMethodChannel *)channel {
    self = [super init];
    if (self) {
        _methodChannel = channel;
        dispatch_async(dispatch_get_main_queue(), ^() {
            [[UIApplication sharedApplication] registerForRemoteNotifications];
        });
        if (@available(iOS 10.0, *)) {
            [[UNUserNotificationCenter currentNotificationCenter] getNotificationSettingsWithCompletionHandler:^(UNNotificationSettings *settings) {
                NSDictionary *settingsDictionary = @{
                    @"sound" : [NSNumber numberWithBool:settings.soundSetting == UNNotificationSettingEnabled],
                    @"badge" : [NSNumber numberWithBool:settings.badgeSetting == UNNotificationSettingEnabled],
                    @"alert" : [NSNumber numberWithBool:settings.alertSetting == UNNotificationSettingEnabled],
                };
                [self->_methodChannel invokeMethod:@"onIosSettingsRegistered" arguments:settingsDictionary];
            }];
        }
    }
    return self;
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
    if ([@"checkNotifySetting" isEqualToString:call.method]) {
        BOOL isOpen = NO;
        #if __IPHONE_OS_VERSION_MAX_ALLOWED >= __IPHONE_8_0
            UIUserNotificationSettings *setting = [[UIApplication sharedApplication] currentUserNotificationSettings];
            if (setting.types != UIUserNotificationTypeNone) {
                isOpen = YES;
            }
        #else
            UIRemoteNotificationType type = [[UIApplication sharedApplication] enabledRemoteNotificationTypes];
            if (type != UIRemoteNotificationTypeNone) {
                isOpen = YES;
            }
        #endif
        result(@(isOpen));
    }else if ([@"brand" isEqualToString:call.method]) {
        result(@"iOS");
    }else if ([@"deviceToken" isEqualToString:call.method]) {
        result([self getDeviceToken]);
    }else if ([@"requestPermission" isEqualToString:call.method]) {
        [self requestPermissionWithSettings:[call arguments]];
        result(nil);
    }else {
        result(FlutterMethodNotImplemented);
    }
}

- (void)requestPermissionWithSettings: (NSDictionary<NSString*, NSNumber*> *)settings {
    if (@available(iOS 10.0, *)) {
        UNAuthorizationOptions options = UNAuthorizationOptionNone;
        if ([[settings objectForKey:@"sound"] boolValue]) {
            options |= UNAuthorizationOptionSound;
        }
        if ([[settings objectForKey:@"badge"] boolValue]) {
            options |= UNAuthorizationOptionBadge;
        }
        if ([[settings objectForKey:@"alert"] boolValue]) {
            options |= UNAuthorizationOptionAlert;
        }
        [[UNUserNotificationCenter currentNotificationCenter] requestAuthorizationWithOptions:options completionHandler:^(BOOL granted, NSError * _Nullable error) {
            if (error != nil) {
                NSLog(@"Error during requesting notification permission: %@", error);
            }
            if (granted) {
                dispatch_async(dispatch_get_main_queue(), ^() {
                    [[UIApplication sharedApplication] registerForRemoteNotifications];
                });
                [self-> _methodChannel invokeMethod:@"onIosSettingsRegistered" arguments:settings];
            }
        }];
    }else {
        UIUserNotificationType types = 0;
        if ([[settings objectForKey:@"sound"] boolValue]) {
            types |= UIUserNotificationTypeSound;
        }
        if ([[settings objectForKey:@"badge"] boolValue]) {
            types |= UIUserNotificationTypeBadge;
        }
        if ([[settings objectForKey:@"alert"] boolValue]) {
            types |= UIUserNotificationTypeAlert;
        }
        UIUserNotificationSettings *settings = [UIUserNotificationSettings settingsForTypes:types categories:nil];
        [[UIApplication sharedApplication] registerUserNotificationSettings:settings];
    }
}

- (NSString *)getDeviceToken {
    return _deviceToken;
}

#pragma mark - AppDelegate

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions{
    if (@available(iOS 10.0, *)) {
        UNUserNotificationCenter * center = [UNUserNotificationCenter currentNotificationCenter];
        [center setDelegate:self];
        UNAuthorizationOptions type = UNAuthorizationOptionBadge|UNAuthorizationOptionSound|UNAuthorizationOptionAlert;
        [center requestAuthorizationWithOptions:type completionHandler:^(BOOL granted, NSError * _Nullable error) {
            if (granted) {
                NSLog(@"===============>  注册成功");
            }else{
                NSLog(@"===============>  注册失败");
            }
        }];
    }else{
        UIUserNotificationType notificationTypes = UIUserNotificationTypeBadge | UIUserNotificationTypeSound | UIUserNotificationTypeAlert;
        UIUserNotificationSettings *settings = [UIUserNotificationSettings settingsForTypes:notificationTypes categories:nil];
        [application registerUserNotificationSettings:settings];
    }
    [application registerForRemoteNotifications];
    return YES;
}

- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken {
    const char *data = [deviceToken bytes];
     NSMutableString *ret = [NSMutableString string];
    for (NSUInteger i = 0; i < [deviceToken length]; i++) {
        [ret appendFormat:@"%02.2hhx", data[i]];
    }
    _deviceToken = [ret copy];
     ///NSLog(@"======>注册通知成功  deviceToken:%@",_deviceToken);
}

- (void)application:(UIApplication *)application didFailToRegisterForRemoteNotificationsWithError:(NSError *)error{
    NSLog(@"======>注册通知失败");
}

- (void)userNotificationCenter:(UNUserNotificationCenter *)center willPresentNotification:(UNNotification *)notification withCompletionHandler:(void (^)(UNNotificationPresentationOptions))completionHandler API_AVAILABLE(ios(10.0)){
    completionHandler(UNNotificationPresentationOptionBadge| UNNotificationPresentationOptionSound| UNNotificationPresentationOptionAlert);
}

- (void)userNotificationCenter:(UNUserNotificationCenter *)center didReceiveNotificationResponse:(UNNotificationResponse *)response withCompletionHandler:(void (^)(void))completionHandler{
    NSLog(@"=============> 处理接收到的数据.....");
    completionHandler();
}

- (void)application:(UIApplication *)application didRegisterUserNotificationSettings:(UIUserNotificationSettings *)notificationSettings{
    
    NSDictionary *settingsDictionary = @{
                                         @"sound" : [NSNumber numberWithBool:notificationSettings.types & UIUserNotificationTypeSound],
                                         @"badge" : [NSNumber numberWithBool:notificationSettings.types & UIUserNotificationTypeBadge],
                                         @"alert" : [NSNumber numberWithBool:notificationSettings.types & UIUserNotificationTypeAlert],
                                         };
    [_methodChannel invokeMethod:@"onIosSettingsRegistered" arguments:settingsDictionary];
    
    NSLog(@"====================>   didRegisterUserNotificationSettings");
}

- (void)application:(UIApplication *)application didReceiveRemoteNotification:(nonnull NSDictionary *)userInfo{
    if (userInfo) {
         // 有推送的消息，处理推送的消息
         NSLog(@"====================> 后台进入前台  %@ ",userInfo);
    }
    NSLog(@"====================> 后台进入前台  %@ ",userInfo);
}

- (BOOL)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo fetchCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler{
    if (application.applicationState == UIApplicationStateActive) {
        NSLog(@"====================> 应用再前台,给出一个提示");
    }else{
        NSLog(@"====================> 一种在后台程序没有被杀死，另一种是在程序已经杀死。用户点击推送的消息进入app的情况处理。");
        //其他两种情况，一种在后台程序没有被杀死，另一种是在程序已经杀死。用户点击推送的消息进入app的情况处理。
    }
    completionHandler(UIBackgroundFetchResultNewData);
    return YES;
}

- (void)application:(UIApplication *)application didReceiveLocalNotification:(UILocalNotification *)notification{
    NSLog(@"didReceiveLocalNotification====================> 应用再前台,给出一个提示");
}

@end
