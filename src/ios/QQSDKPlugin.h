#import <Cordova/CDVPlugin.h>
#import <Cordova/CDVPluginResult.h>
#import <TencentOpenAPI/TencentOAuth.h>
#import <TencentOpenAPI/QQApiInterface.h>

@interface QQSDKPlugin : CDVPlugin <TencentSessionDelegate, QQApiInterfaceDelegate>

@property (nonatomic, copy) NSString *callback;

- (void)checkClientInstalled:(CDVInvokedUrlCommand *)command;

- (void)setIsPermissionGranted:(CDVInvokedUrlCommand *)command;

- (void)login:(CDVInvokedUrlCommand *)command;

- (void)logout:(CDVInvokedUrlCommand *)command;

- (void)shareToQQ:(CDVInvokedUrlCommand *)command;

- (void)shareToQzone:(CDVInvokedUrlCommand *)command;

@end