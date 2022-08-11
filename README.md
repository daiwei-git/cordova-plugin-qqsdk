# @daiweinpm/cordova-plugin-qqsdk

QQ安卓SDK 3.5.12 IOS SDK 3.5.11

# 新版说明
集成QQSDK

# 安装

```shell
npm i @daiweinpm/cordova-plugin-qqsdk
```

```shell
cordova plugin add @daiweinpm/cordova-plugin-qqsdk --variable QQ_APP_ID=YOUR_QQ_APPID
```

```shell
cordova build ios
cordova build android
```

### 使用方式
```Javascript

/**
 * 请在代码中引入这一句 或者从window中使用 window.QQSDK
 */
declare const QQSDK;

/**
 * 插件安装失败时此对象可能无法使用 所以需要加入判断
 */
if (QQSDK) {
    /**
    * 检查QQ是否安装
    */
    QQSDK.checkClientInstalled(): Promise<bool>;

    /**
    * 在调用其他SDK接口前调用该接口通知 SDK 用户是否已授权应用获取设备信息的权限，或在应用的取消授权界面中提供用户撤销获取设备信息的权限
    * 
    * 布尔 isPermission 
    */
    QQSDK.setIsPermissionGranted(isPermission): Promise<any>;

    /**
    * QQ授权登录 client 客户端 1 = QQ（默认），2 = tim
    */
    QQSDK.login(client): Promise<any>;

    /**
    * QQ退出登录
    */
    QQSDK.logout(): Promise<any>;

    /**
    * 分享到QQ
    * 
    * params.client 客户端 1 = QQ（默认），2 = tim
    * params.type  text: 分享纯文本，image: 分享纯图片，audio：分享音乐，miniprogram：分享小程序，default：分享图文消息/新闻消息
    * params.title 标题（type为text，audio，video，miniprogram，default时生效）
    * params.summary 摘要（type为audio，video，miniprogram，default时生效）
    * params.arkjson 参照官网说明
    * params.targeturl 目标地址（type为audio，video，miniprogram，default时生效）
    * params.imageurl 图片地址（type为image，audio，video，miniprogram，default时生效）
    * params.audiourl 音乐地址（type为audio时生效）
    * params.miniprogramappid 小程序ID（type为miniprogram时生效）
    * params.miniprogrampath 小程序路径（type为miniprogram时生效）
    * params.miniprogramtype 小程序类型：默认正式版（3），可选测试版（1）、预览版（4）（type为miniprogram时生效）
    */
    QQSDK.shareToQQ(params: {}): Promise<any>;


    /**
    * 分享到QQ空间
    * 
    * params.client 客户端 1 = QQ（默认），2 = tim
    * params.type  miniprogram：分享小程序，default：分享图文消息/新闻消息
    * params.title 标题
    * params.summary 摘要
    * params.targeturl 目标地址
    * params.imageurl 图片地址
    * params.miniprogramappid type为miniprogram时生效，小程序ID
    * params.miniprogrampath type为miniprogram时生效，小程序路径
    * params.miniprogramtype type为miniprogram时生效，小程序类型：默认正式版（3），可选测试版（1）、预览版（4）
    */
    QQSDK.shareToQzone(params: {}): Promise<any>;
}
```