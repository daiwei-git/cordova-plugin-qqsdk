#import "QQSDKPlugin.h"

NSString *appId = @"";

@implementation QQSDKPlugin {
    TencentOAuth *tencentOAuth;
}

/**
 *  插件初始化，主要用户appkey注册
 */
- (void)pluginInitialize {
    appId = [[self.commandDelegate settings] objectForKey:@"qq_app_id"];
    if (nil == tencentOAuth) {
        tencentOAuth = [[TencentOAuth alloc] initWithAppId:appId andDelegate:self];
    }
}

/**
 *  处理URL
 *
 *  @param notification cordova 传递进来的消息
 */
- (void)handleOpenURL:(NSNotification *)notification {
    NSURL *url = [notification object];
    NSString *schemaPrefix = [@"tencent" stringByAppendingString:appId];
    if ([url isKindOfClass:[NSURL class]] && [[url absoluteString] hasPrefix:[schemaPrefix stringByAppendingString:@"://response_from_qq"]]) {
        [QQApiInterface handleOpenURL:url delegate:self];
    } else {
        [TencentOAuth HandleOpenURL:url];
    }
}

/**
 *  检查QQ官方客户端是否安装
 *
 *  @param command CDVInvokedUrlCommand
 */
- (void)checkClientInstalled:(CDVInvokedUrlCommand *)command {
    NSDictionary *args = [command.arguments objectAtIndex:0];
    int type = [[args valueForKey:@"client"] intValue];
    if(type == 0) {
        [tencentOAuth setAuthShareType:AuthShareType_QQ];
        [self checkQQInstalled:command];
    } else if (type == 1) {
        [self checkTIMInstalled:command];
    } else {
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}

/**
 *  检查QQ官方客户端是否安装
 *
 *  @param command CDVInvokedUrlCommand
 */
- (void)checkQQInstalled:(CDVInvokedUrlCommand *)command {
    if ([TencentOAuth iphoneQQInstalled]) {
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    } else {
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}

/**
 *  检查TIM客户端是否安装
 *
 *  @param command CDVInvokedUrlCommand
 */
- (void)checkTIMInstalled:(CDVInvokedUrlCommand *)command {
    if ([TencentOAuth iphoneTIMInstalled]) {
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    } else {
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}

/**
 *  设置用户是否已授权获取设备信息
 *
 *  @param command CDVInvokedUrlCommand
 */
- (void)setIsPermissionGranted:(CDVInvokedUrlCommand *)command {
    self.callback = command.callbackId;
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

/**
 *  QQ 登出
 *
 *  @param command CDVInvokedUrlCommand
 */
- (void)logout:(CDVInvokedUrlCommand *)command {
    self.callback = command.callbackId;
    [tencentOAuth logout:self];
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

/**
 *  QQ 登录
 *
 *  @param command CDVInvokedUrlCommand
 */
- (void)login:(CDVInvokedUrlCommand *)command {
    if (nil == tencentOAuth) {
        tencentOAuth = [[TencentOAuth alloc] initWithAppId:appId andDelegate:self];
    }
    NSDictionary *args = [command.arguments objectAtIndex:0];
    self.callback = command.callbackId;
    NSArray *permissions = [NSArray arrayWithObjects:
                                        kOPEN_PERMISSION_GET_USER_INFO,
                                        kOPEN_PERMISSION_GET_SIMPLE_USER_INFO,
                                        kOPEN_PERMISSION_ADD_ALBUM,
                                        kOPEN_PERMISSION_ADD_TOPIC,
                                        kOPEN_PERMISSION_CHECK_PAGE_FANS,
                                        kOPEN_PERMISSION_GET_INFO,
                                        kOPEN_PERMISSION_GET_OTHER_INFO,
                                        kOPEN_PERMISSION_LIST_ALBUM,
                                        kOPEN_PERMISSION_UPLOAD_PIC,
                                        kOPEN_PERMISSION_GET_VIP_INFO,
                                        kOPEN_PERMISSION_GET_VIP_RICH_INFO,
                                        nil];
    int type = [[args valueForKey:@"client"] intValue];
    if (type == 0) {
        [tencentOAuth setAuthShareType:AuthShareType_QQ];
    } else if (type == 1) {
        [tencentOAuth setAuthShareType:AuthShareType_TIM];
    }
    [tencentOAuth authorize:permissions];
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

/**
 分享到QQ
 
 @param command cordova参数
 */
- (void)shareToQQ:(CDVInvokedUrlCommand *)command {
    @try {
        self.callback = command.callbackId;
        NSDictionary *args = [command.arguments objectAtIndex:0];

        NSString *type = [args objectForKey:@"type"];
        NSString *title = [args objectForKey:@"title"];
        NSString *summary = [args objectForKey:@"summary"];
        // NSString *arkjson = [args objectForKey:@"arkjson"];
        NSString *targeturl = [args objectForKey:@"targeturl"];
        NSString *imageurl = [args objectForKey:@"imageurl"];
        NSString *audiourl = [args objectForKey:@"audiourl"];
        // NSString *videourl = [args objectForKey:@"videourl"];
        NSString *miniprogramappid = [args objectForKey:@"miniprogramappid"];
        NSString *miniprogrampath = [args objectForKey:@"miniprogrampath"];
        NSString *miniprogramtype = [args objectForKey:@"miniprogramtype"];

        SendMessageToQQReq *req = NULL;
        if ([type isEqualToString: @"text"]) { // 纯文本
            QQApiTextObject *txtObj = [QQApiTextObject objectWithText: title];
            req = [SendMessageToQQReq reqWithContent:txtObj];
        } else if ([type isEqualToString: @"image"]) { // 纯图片
            NSData *imgData = [self processImage: imageurl];
            QQApiImageObject *imgObj = [QQApiImageObject objectWithData:imgData previewImageData: imgData title: title description: summary];
            req = [SendMessageToQQReq reqWithContent:imgObj];
        } else if ([type isEqualToString: @"audio"]) { // 音乐
            NSData *imgData = [self processImage: imageurl];
            QQApiAudioObject *audioObj =[QQApiAudioObject objectWithURL :[NSURL URLWithString:audiourl] title: title description: summary previewImageData: imgData];
            [audioObj setCflag:kQQAPICtrlFlagQQShare];
            [audioObj setFlashURL:[NSURL URLWithString:audiourl]];
            req = [SendMessageToQQReq reqWithContent:audioObj];
        // } else if ([type isEqualToString: @"video"]) { // 视频
            //     NSData *imgData = [self processImage: imageurl];
            //     QQApiVideoObject *videoObj =[QQApiVideoObject objectWithURL :[NSURL URLWithString:audiourl] title: title description: summary previewImageData: imgData];
            //     [videoObj setFlashUrl:videourl];
            //     req = [SendMessageToQQReq reqWithContent:videoObj]
        } else if ([type isEqualToString: @"miniprogram"]) { // 小程序
            NSData *imageData = [self processImage: imageurl];
            QQApiNewsObject *newsObj = [QQApiNewsObject objectWithURL :[NSURL URLWithString:targeturl] title: title description: summary previewImageData: imageData];
            QQApiMiniProgramObject *miniObj = [QQApiMiniProgramObject new];
            miniObj.qqApiObject = newsObj;
            miniObj.miniAppID = miniprogramappid;
            miniObj.miniPath = miniprogrampath;
            miniObj.webpageUrl = @"";
            miniObj.miniprogramType = [miniprogramtype integerValue];
            req = [SendMessageToQQReq reqWithMiniContent:miniObj];
        } else { // 默认图文 = 新闻
            NSData *imageData = [self processImage: imageurl];
            QQApiNewsObject *newsObj = [QQApiNewsObject objectWithURL :[NSURL URLWithString:targeturl] title: title description: summary previewImageData: imageData];
            req = [SendMessageToQQReq reqWithContent:newsObj];
        }
        QQApiSendResultCode ret = [QQApiInterface sendReq:req];
        [self handleSendResult:ret];
    } @catch (NSException *exception) {
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString: [exception name]];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}

/**
 分享到QQ空间
 
 @param command cordova参数
 */
- (void)shareToQzone:(CDVInvokedUrlCommand *)command {
    @try {
        self.callback = command.callbackId;
        NSDictionary *args = [command.arguments objectAtIndex:0];

        NSString *type = [args objectForKey:@"type"];
        NSString *title = [args objectForKey:@"title"];
        NSString *summary = [args objectForKey:@"summary"];
        // NSString *arkjson = [args objectForKey:@"arkjson"];
        NSString *targeturl = [args objectForKey:@"targeturl"];
        NSString *imageurl = [args objectForKey:@"imageurl"];
        // NSString *audiourl = [args objectForKey:@"audiourl"];
        // NSString *videourl = [args objectForKey:@"videourl"];
        NSString *miniprogramappid = [args objectForKey:@"miniprogramappid"];
        NSString *miniprogrampath = [args objectForKey:@"miniprogrampath"];
        NSString *miniprogramtype = [args objectForKey:@"miniprogramtype"];

        SendMessageToQQReq *req = NULL;
        if ([type isEqualToString: @"miniprogram"]) { // 小程序
            NSData *imageData = [self processImage: imageurl];
            QQApiNewsObject *newsObj = [QQApiNewsObject objectWithURL :[NSURL URLWithString:targeturl] title: title description: summary previewImageData: imageData];
            newsObj.cflag |= kQQAPICtrlFlagQQShareEnableMiniProgram;
            newsObj.cflag |= kQQAPICtrlFlagQZoneShareOnStart;
            QQApiMiniProgramObject *miniObj = [QQApiMiniProgramObject new];
            miniObj.qqApiObject = newsObj;
            miniObj.miniAppID = miniprogramappid;
            miniObj.miniPath = miniprogrampath;
            miniObj.webpageUrl = @"";
            miniObj.miniprogramType = [miniprogramtype integerValue];
            req = [SendMessageToQQReq reqWithMiniContent:miniObj];
        // } else if ([typ isEqualToString: @"video"]) { // 视频
         //     NSData *imgData = [self processImage: imageurl];
            //     QQApiVideoObject *vidoeObj =[QQApiVideoObject objectWithURL :[NSURL URLWithString:audiourl] title: title description: summary previewImageData: imgData];
            //     [vidoeObj setFlashUrl:videourl];
            //     vidoeObj.cflag |= kQQAPICtrlFlagQZoneShareOnStart;
            //     req = [SendMessageToQQReq reqWithContent:vidoeObj]
        // } else if ([typ isEqualToString: @"audio"]) { // 音乐
        //     NSData *imgData = [self processImage: imageurl];
            //     QQApiAudioObject *audioObj =[QQApiAudioObject objectWithURL :[NSURL URLWithString:audiourl] title: title description: summary previewImageData: imgData];
            //     [audioObj setFlashUrl:audiourl];
            //     audioObj.cflag |= kQQAPICtrlFlagQZoneShareOnStart;
            //     req = [SendMessageToQQReq reqWithContent:audioObj]
        } else { // 默认图文 = 新闻
            NSData *imageData = [self processImage: imageurl];
            QQApiNewsObject *newsObj = [QQApiNewsObject objectWithURL :[NSURL URLWithString:targeturl] title: title description: summary previewImageData: imageData];
            newsObj.cflag |= kQQAPICtrlFlagQZoneShareOnStart;
            req = [SendMessageToQQReq reqWithContent:newsObj];
        }
        QQApiSendResultCode ret = [QQApiInterface SendReqToQZone:req];
        [self handleSendResult:ret];
    } @catch (NSException *exception) {
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString: [exception name]];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}

/**
 结果处理
 @param sendResult 分享结果
 */
- (void)handleSendResult:(QQApiSendResultCode)sendResult {
    switch (sendResult) {
        case EQQAPISENDSUCESS:
            break;
        case EQQAPIAPPSHAREASYNC:
            break;
        case EQQAPIAPPNOTREGISTED: {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"App 未注册"];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callback];
            break;
        }
        case EQQAPIMESSAGECONTENTINVALID:
        case EQQAPIMESSAGECONTENTNULL:
        case EQQAPIMESSAGETYPEINVALID: {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"发送参数错误"];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callback];
            break;
        }
        case EQQAPITIMNOTINSTALLED: {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"没有安装 TIM"];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callback];
            break;
        }
        case EQQAPIQQNOTINSTALLED: {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"没有安装手机 QQ"];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callback];
            break;
        }
        case EQQAPITIMNOTSUPPORTAPI:
        case EQQAPIQQNOTSUPPORTAPI: {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"API 接口不支持"];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callback];
            break;
        }
        case EQQAPISENDFAILD: {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"发送失败"];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callback];
            break;
        }
        case EQQAPIVERSIONNEEDUPDATE: {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"当前 QQ 版本太低"];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callback];
            break;
        }
        case ETIMAPIVERSIONNEEDUPDATE: {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"当前 TIM 版本太低"];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callback];
            break;
        }
        case EQQAPIQZONENOTSUPPORTTEXT: {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"QQZone 不支持 QQApiTextObject 分享"];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callback];
            break;
        }
        case EQQAPIQZONENOTSUPPORTIMAGE: {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"QQZone 不支持 QQApiImageObject 分享"];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callback];
            break;
        }
        case EQQAPISHAREDESTUNKNOWN: {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"未指定分享到 QQ 或 TIM"];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callback];
            break;
        }
        default: {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"发生其他错误"];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callback];
            break;
        }
    }
}

#pragma mark - QQApiInterfaceDelegate
- (void)onReq:(QQBaseReq *)req {
}

- (void)onResp:(QQBaseResp *)resp {
    switch ([resp.result integerValue]) {
        case 0: {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callback];
            break;
        }
        case -4: {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"QQ share cancelled by user"];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callback];
            break;
        }
        default: {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callback];
            break;
        }
    }
}

- (void)isOnlineResponse:(NSDictionary *)response {
}

#pragma mark - TencentSessionDelegate
- (void)tencentDidLogin {
    if (tencentOAuth.accessToken && 0 != [tencentOAuth.accessToken length]) {
        NSMutableDictionary *Dic = [NSMutableDictionary dictionaryWithCapacity:2];
        [Dic setObject:tencentOAuth.openId forKey:@"userid"];
        [Dic setObject:tencentOAuth.accessToken forKey:@"access_token"];
        [Dic setObject:[NSString stringWithFormat:@"%f", [tencentOAuth.expirationDate timeIntervalSince1970] * 1000] forKey:@"expires_time"];
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:Dic];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callback];
    } else {
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"QQ login error"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callback];
    }
}

- (void)tencentDidLogout {
    tencentOAuth = nil;
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callback];
}

- (void)tencentDidNotLogin:(BOOL)cancelled {
    if (cancelled) {
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"QQ login cancelled"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callback];
    } else {
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"QQ login error"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callback];
    }
}

- (void)tencentDidNotNetWork {
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"QQ login network error"];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callback];
}

/**
 图片处理
 
 @param image 图片数据
 @return 图片NSdata数据
 */
- (NSData *)processImage:(NSString *)image {
    if ([self isBase64Data:image]) {
        return [[NSData alloc] initWithBase64EncodedString:image options:0];
    } else if ([image hasPrefix:@"http://"] || [image hasPrefix:@"https://"]) {
        NSURL *url = [NSURL URLWithString:image];
        return [NSData dataWithContentsOfURL:url];
    } else {
        return [NSData dataWithContentsOfFile:image];
    }
}

/**
 检查图片是不是Base64
 
 @param data 图片数据
 @return 结果true or false
 */
- (BOOL)isBase64Data:(NSString *)data {
    data = [[data componentsSeparatedByCharactersInSet:
             [NSCharacterSet whitespaceAndNewlineCharacterSet]]
            componentsJoinedByString:@""];
    if ([data length] % 4 == 0) {
        static NSCharacterSet *invertedBase64CharacterSet = nil;
        if (invertedBase64CharacterSet == nil) {
            invertedBase64CharacterSet = [[NSCharacterSet characterSetWithCharactersInString:@"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/="] invertedSet];
        }
        return [data rangeOfCharacterFromSet:invertedBase64CharacterSet options:NSLiteralSearch].location == NSNotFound;
    }
    return NO;
}

/**
 检查参数是否存在
 
 @param param 要检查的参数
 @param args 参数字典
 @return 参数
 */
- (NSString *)check:(NSString *)param in:(NSDictionary *)args {
    NSString *data = [args objectForKey:param];
    return data?data:@"";
}

@end
