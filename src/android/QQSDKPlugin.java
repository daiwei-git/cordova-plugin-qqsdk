package com.daiwei.qqsdk.cdv;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.webkit.URLUtil;
import com.tencent.connect.common.Constants;
import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzonePublish;
import com.tencent.connect.share.QzoneShare;
import com.tencent.tauth.DefaultUiListener;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.UiError;
import com.tencent.tauth.Tencent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

import static org.apache.cordova.CordovaActivity.TAG;

public class QQSDKPlugin extends CordovaPlugin {

  private static Tencent mTencent;
  private CallbackContext currentCallbackContext;

  @Override protected void pluginInitialize() {
    super.pluginInitialize();
    String appid = webView.getPreferences().getString("qq_app_id", "");
    mTencent = Tencent.createInstance(appid, this.cordova.getActivity().getApplicationContext());
  }

  @Override
  public boolean execute(String action, final CordovaArgs args, final CallbackContext callbackContext) throws JSONException {
    if (action.equalsIgnoreCase("checkClientInstalled")) {
      return checkClientInstalled(callbackContext);
    }
    if (action.equals("setIsPermissionGranted")) {
      return setIsPermissionGranted(args, callbackContext);
    }
    if (action.equals("login")) {
      return login(callbackContext);
    }
    if (action.equals("logout")) {
      return logout(callbackContext);
    }
    if (action.equals("shareToQQ")) {
      return shareToQQ(args, callbackContext);
    }
    if (action.equals("shareToQzone")) {
      return shareToQzone(args, callbackContext);
    }
    return super.execute(action, args, callbackContext);
  }

  /**
   * ????????????QQ?????????????????????
   */
  private boolean checkClientInstalled(CallbackContext callbackContext) {
    Boolean installed = mTencent.isSupportSSOLogin(QQSDKPlugin.this.cordova.getActivity());
    if (installed) {
      callbackContext.success();
    } else {
      callbackContext.error("not installed");
    }
    return true;
  }

  /**
   * ?????????????????????????????????????????????
   */
  private boolean setIsPermissionGranted(CordovaArgs args, CallbackContext callbackContext) {
    final JSONObject data;
    try {
      data = args.getJSONObject(0);
      boolean isPermissionGranted = data.has("isPermissionGranted") ? data.getBoolean("isPermissionGranted") : true;
      mTencent.setIsPermissionGranted(isPermissionGranted);
    } catch (JSONException e) {

    }
    callbackContext.success();
    return true;
  }

  /**
   * QQ ????????????
   */
  private boolean login(CallbackContext callbackContext) {
    currentCallbackContext = callbackContext;
    Runnable runnable = new Runnable() {
      @Override public void run() {
        mTencent.login(QQSDKPlugin.this.cordova.getActivity(), "all", loginListener);
      }
    };
    this.cordova.getActivity().runOnUiThread(runnable);
    this.cordova.setActivityResultCallback(this);
    return true;
  }

  /**
   * QQ ??????
   */
  private boolean logout(CallbackContext callbackContext) {
    mTencent.logout(this.cordova.getActivity());
    callbackContext.success();
    return true;
  }

  /**
   * ?????????QQ
   */
  private boolean shareToQQ(CordovaArgs args, CallbackContext callbackContext) {
    try {
      final JSONObject data;
      currentCallbackContext = callbackContext;
      data = args.getJSONObject(0);

      String type = data.has("type") ?  data.getString("type") : "default";
      String appname = data.has("appname") ?  data.getString("appname") : getAppName();
      String title = data.has("title") ?  data.getString("title") : "??????";
      String summary = data.has("summary") ?  data.getString("summary") : "";
      String arkjson = data.has("arkjson") ?  data.getString("arkjson") : "";
      String targeturl = data.has("targeturl") ?  data.getString("targeturl") : "";
      String imageurl = data.has("imageurl") ?  data.getString("imageurl") : "";
      String audiourl = data.has("audiourl") ?  data.getString("audiourl") : "";
      String videourl = data.has("videourl") ?  data.getString("videourl") : "";
      String miniprogramappid = data.has("miniprogramappid") ?  data.getString("miniprogramappid") : "";
      String miniprogrampath = data.has("miniprogrampath") ?  data.getString("miniprogrampath") : "";
      String miniprogramtype = data.has("miniprogramtype") ?  data.getString("miniprogramtype") : "3";

      final Bundle params = new Bundle();

      if (arkjson.length() > 0) {
        params.putString(QQShare.SHARE_TO_QQ_ARK_INFO, arkjson);
      }

      switch (type) {
        case "image": // ?????????
          params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);
          params.putString(QQShare.SHARE_TO_QQ_APP_NAME, appname);
          params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, processImage(imageurl)); // ??????
          // params.putInt(QQShare.SHARE_TO_QQ_EXT_INT, QQShare.SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN);
          break;
        case "audio": // ??????
          params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_AUDIO);
          params.putString(QQShare.SHARE_TO_QQ_APP_NAME, appname);
          params.putString(QQShare.SHARE_TO_QQ_TITLE, title); // ??????
          params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, targeturl); // ??????
          params.putString(QQShare.SHARE_TO_QQ_AUDIO_URL, audiourl); // ??????
          // ????????????
          if (summary.length() > 0) {
            params.putString(QQShare.SHARE_TO_QQ_SUMMARY, summary);
          }
          // ????????????
          if (imageurl.length() > 0) {
            if(URLUtil.isHttpUrl(imageurl) || URLUtil.isHttpsUrl(imageurl)) {
              params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, imageurl);
            } else {
              params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, processImage(imageurl));
            }
          }
          break;
        // case "video": // ??????
        //   params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_VIDEO);
        //   params.putString(QQShare.SHARE_TO_QQ_APP_NAME, appname);
        //   params.putString(QQShare.SHARE_TO_QQ_TITLE, title); // ??????
        //   params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, targeturl); // ??????
        //   params.putString(QQShare.SHARE_TO_QQ_VIDEO_URL, videourl); // ??????
        //   // ????????????
        //   if (summary.length() > 0) {
        //     params.putString(QQShare.SHARE_TO_QQ_SUMMARY, summary);
        //   }
        //   // ????????????
        //   if (imageurl.length() > 0) {
        //     if(URLUtil.isHttpUrl(imageurl) || URLUtil.isHttpsUrl(imageurl)) {
        //       params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, imageurl);
        //     } else {
        //       params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, processImage(imageurl));
        //     }
        //   }
        //   break;
        case "miniprogram": // ?????????
          params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_MINI_PROGRAM);
          params.putString(QQShare.SHARE_TO_QQ_TITLE, title); // ??????
          params.putString(QQShare.SHARE_TO_QQ_SUMMARY, summary); // ??????
          params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, targeturl); // ??????
          // ??????
          if(URLUtil.isHttpUrl(imageurl) || URLUtil.isHttpsUrl(imageurl)) {
            params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, imageurl);
          } else {
            params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, processImage(imageurl));
          }
          params.putString(QQShare.SHARE_TO_QQ_MINI_PROGRAM_APPID, miniprogramappid); // ??????
          params.putString(QQShare.SHARE_TO_QQ_MINI_PROGRAM_PATH, miniprogrampath); // ??????
          params.putString(QQShare.SHARE_TO_QQ_MINI_PROGRAM_TYPE, miniprogramtype);
          break;
        case "text": // ?????????
        case "news": // ??????
        default: // ????????????
          params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
          params.putString(QQShare.SHARE_TO_QQ_APP_NAME, appname); // ??????
          params.putString(QQShare.SHARE_TO_QQ_TITLE, title); // ??????
          params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, targeturl); // ??????
          // ????????????
          if (summary.length() > 0) {
            params.putString(QQShare.SHARE_TO_QQ_SUMMARY, summary);
          }
          // ????????????
          if (imageurl.length() > 0) {
            if(URLUtil.isHttpUrl(imageurl) || URLUtil.isHttpsUrl(imageurl)) {
              params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, imageurl);
            } else {
              params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, processImage(imageurl));
            }
          }
          break;
      }
      Runnable qqRunnable = new Runnable() {
        @Override public void run() {
          mTencent.shareToQQ(QQSDKPlugin.this.cordova.getActivity(), params, qqShareListener);
        }
      };
      this.cordova.getActivity().runOnUiThread(qqRunnable);
      this.cordova.setActivityResultCallback(this);
    } catch (JSONException e) {
      callbackContext.error(e.getMessage());
    }
    return true;
  }

  /**
   * ?????????QQ??????
   */
  private boolean shareToQzone(CordovaArgs args, CallbackContext callbackContext) {
    try {
      final JSONObject data;
      currentCallbackContext = callbackContext;
      data = args.getJSONObject(0);

      String type = data.has("type") ?  data.getString("type") : "default";
      String title = data.has("title") ?  data.getString("title") : "??????";
      String summary = data.has("summary") ?  data.getString("summary") : "??????";
      String targeturl = data.has("targeturl") ?  data.getString("targeturl") : "";
      String imageurl = data.has("imageurl") ?  data.getString("imageurl") : "";
      String audiourl = data.has("audiourl") ?  data.getString("audiourl") : "";
      String videourl = data.has("videourl") ?  data.getString("videourl") : "";
      String extrascene = data.has("extrascene") ?  data.getString("extrascene") : "";
      String callback = data.has("callback") ?  data.getString("callback") : "";
      String miniprogramappid = data.has("miniprogramappid") ?  data.getString("miniprogramappid") : "";
      String miniprogrampath = data.has("miniprogrampath") ?  data.getString("miniprogrampath") : "";
      String miniprogramtype = data.has("miniprogramtype") ?  data.getString("miniprogramtype") : "3";

      ArrayList<String> imageUrls = new ArrayList<String>();
      final Bundle params = new Bundle();

      switch (type) {
        // case "audio": // ????????????
        // case "video": // ????????????
        // case "publish": // ????????????
        //   params.putInt(QzonePublish.PUBLISH_TO_QZONE_KEY_TYPE, QzonePublish.PUBLISH_TO_QZONE_TYPE_PUBLISHMOOD);
        //   // ????????????
        //   if (summary.length() > 0) {
        //     params.putString(QzonePublish.PUBLISH_TO_QZONE_SUMMARY, summary);
        //   }
        //   // ????????????
        //   if (imageurl.length() > 0) {
        //     imageUrls.add(processImage(imageurl));
        //     params.putStringArrayList(QzonePublish.PUBLISH_TO_QZONE_IMAGE_URL, imageUrls);
        //   }
        //   // ????????????
        //   if (audiourl.length() > 0) {
        //     params.putString(QzonePublish.PUBLISH_TO_QZONE_AUDIO_PATH, audiourl);
        //   }
        //   // // ????????????
        //   // if (videourl.length() > 0) {
        //   //   params.putString(QzonePublish.PUBLISH_TO_QZONE_VIDEO_PATH, videourl);
        //   // }
        //   // ????????????
        //   if (extrascene.length() > 0 || callback.length() > 0) {
        //     Bundle extParams = new Bundle();
        //     if (extrascene.length() > 0) {
        //       extParams.putString(QzonePublish.HULIAN_EXTRA_SCENE, extrascene);
        //     }
        //     if (callback.length() > 0) {
        //       extParams.putString(QzonePublish.HULIAN_CALL_BACK, callback);
        //     }
        //     params.putBundle(QzonePublish.PUBLISH_TO_QZONE_EXTMAP, extParams);
        //   }
        //   break;
        case "miniprogram": // ?????????
          params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_MINI_PROGRAM);
          imageUrls.add(processImage(imageurl));
          params.putStringArrayList(QQShare.SHARE_TO_QQ_IMAGE_URL, imageUrls);
          // ??????
          if (title.length() > 0) {
            params.putString(QQShare.SHARE_TO_QQ_TITLE, title);
          }
          // ??????
          if (summary.length() > 0) {
            params.putString(QQShare.SHARE_TO_QQ_SUMMARY, summary);
          }
          params.putString(QQShare.SHARE_TO_QQ_MINI_PROGRAM_APPID, miniprogramappid);
          params.putString(QQShare.SHARE_TO_QQ_MINI_PROGRAM_PATH, miniprogrampath);
          params.putString(QQShare.SHARE_TO_QQ_MINI_PROGRAM_TYPE, miniprogramtype);
          break;
        case "news": // ??????
        default: // ????????????
          params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
          params.putString(QzoneShare.SHARE_TO_QQ_TITLE, title);//??????
          params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, targeturl);//??????
          // ????????????
          if (summary.length() > 0) {
            params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, summary);//??????
          }
          // ????????????
          if (imageurl.length() > 0) {
            imageUrls.add(processImage(imageurl));
            params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imageUrls);
          }
          break;
      }
      Runnable zoneRunnable = new Runnable() {
        @Override public void run() {
          mTencent.shareToQzone(QQSDKPlugin.this.cordova.getActivity(), params, qZoneShareListener);
        }
      };
      this.cordova.getActivity().runOnUiThread(zoneRunnable);
      this.cordova.setActivityResultCallback(this);
    } catch (JSONException e) {
      callbackContext.error(e.getMessage());
    }
    return true;
  }

  /**
   * ??????token ??? openid
   */
  public static void initOpenidAndToken(JSONObject jsonObject) {
    try {
      String token = jsonObject.getString(Constants.PARAM_ACCESS_TOKEN);
      String expires = jsonObject.getString(Constants.PARAM_EXPIRES_IN);
      String openId = jsonObject.getString(Constants.PARAM_OPEN_ID);
      if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(expires) && !TextUtils.isEmpty(openId)) {
        mTencent.setAccessToken(token, expires);
        mTencent.setOpenId(openId);
      }
    } catch (Exception e) {
    }
  }

  /**
   * ?????????????????????
   */
  private String getAppName() {
    PackageManager packageManager = this.cordova.getActivity().getPackageManager();
    ApplicationInfo applicationInfo = null;
    try {
      applicationInfo =
          packageManager.getApplicationInfo(this.cordova.getActivity().getPackageName(), 0);
    } catch (final PackageManager.NameNotFoundException e) {
    }
    final String AppName =
        (String) ((applicationInfo != null) ? packageManager.getApplicationLabel(applicationInfo)
            : "AppName");
    return AppName;
  }

  /**
   * ????????????
   * @param image
   * @return
   */
  private String processImage(String image) {
    if(URLUtil.isHttpUrl(image) || URLUtil.isHttpsUrl(image)) {
      return saveBitmapToFile(getBitmapFromURL(image));
    } else if (isBase64(image)) {
      return saveBitmapToFile(decodeBase64ToBitmap(image));
    } else if (image.startsWith("/") ){
      File file = new File(image);
      return file.getAbsolutePath();
    } else {
      return null;
    }
  }

  /**
   * ??????????????????????????????Base64
   * @param image
   * @return
   */
  private boolean isBase64(String image) {
    try {
      byte[] decodedString = Base64.decode(image, Base64.DEFAULT);
      Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
      if (bitmap == null) {
        return false;
      }
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public static Bitmap getBitmapFromURL(String src) {
    try {
      URL url = new URL(src);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setDoInput(true);
      connection.connect();
      InputStream input = connection.getInputStream();
      Bitmap bitmap = BitmapFactory.decodeStream(input);
      return bitmap;
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * ???Base64?????????Bitmap
   */

  private Bitmap decodeBase64ToBitmap(String Base64String) {
    byte[] decode = Base64.decode(Base64String, Base64.DEFAULT);
    Bitmap bitmap = BitmapFactory.decodeByteArray(decode, 0, decode.length);
    return bitmap;
  }

  /**
   * ???bitmap ???????????????
   */
  private String saveBitmapToFile(Bitmap bitmap) {
    File pictureFile = getOutputMediaFile();
    if (pictureFile == null) {
      return null;
    }
    try {
      FileOutputStream fos = new FileOutputStream(pictureFile);
      bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
      fos.close();
    } catch (FileNotFoundException e) {
      Log.d(TAG, "File not found: " + e.getMessage());
    } catch (IOException e) {
      Log.d(TAG, "Error accessing file: " + e.getMessage());
    }
    return pictureFile.getAbsolutePath();
  }

  /**
   * ??????????????????????????????
   */
  private File getOutputMediaFile() {
    File mediaStorageDir = this.cordova.getActivity().getExternalCacheDir();
    if (!mediaStorageDir.exists()) {
      if (!mediaStorageDir.mkdirs()) {
        return null;
      }
    }
    String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
    File mediaFile;
    String mImageName = "Cordova_" + timeStamp + ".jpg";
    mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
    return mediaFile;
  }

  /**
   * ????????????
   */
  IUiListener loginListener = new DefaultUiListener() {
    @Override public void onComplete(Object response) {
      if (null == response) {
        QQSDKPlugin.this.webView.sendPluginResult(
            new PluginResult(PluginResult.Status.ERROR, "response error"),
            currentCallbackContext.getCallbackId());
        return;
      }
      JSONObject jsonResponse = (JSONObject) response;
      if (null != jsonResponse && jsonResponse.length() == 0) {
        QQSDKPlugin.this.webView.sendPluginResult(
            new PluginResult(PluginResult.Status.ERROR, "response error"),
            currentCallbackContext.getCallbackId());
        return;
      }
      initOpenidAndToken(jsonResponse);
      JSONObject jo =
          makeJson(mTencent.getAccessToken(), mTencent.getOpenId(), mTencent.getExpiresIn());
      QQSDKPlugin.this.webView.sendPluginResult(new PluginResult(PluginResult.Status.OK, jo),
          currentCallbackContext.getCallbackId());
    }

    @Override public void onError(UiError e) {
      QQSDKPlugin.this.webView.sendPluginResult(
          new PluginResult(PluginResult.Status.ERROR, e.errorMessage),
          currentCallbackContext.getCallbackId());
    }

    @Override public void onCancel() {
      QQSDKPlugin.this.webView.sendPluginResult(
          new PluginResult(PluginResult.Status.ERROR, "cancelled"),
          currentCallbackContext.getCallbackId());
    }
  };

  /**
   * QQ????????????
   */
  IUiListener qqShareListener = new DefaultUiListener() {
    @Override public void onCancel() {
      QQSDKPlugin.this.webView.sendPluginResult(
          new PluginResult(PluginResult.Status.ERROR, "cancelled"),
          currentCallbackContext.getCallbackId());
    }

    @Override public void onComplete(Object response) {
      QQSDKPlugin.this.webView.sendPluginResult(new PluginResult(PluginResult.Status.OK),
          currentCallbackContext.getCallbackId());
    }

    @Override public void onError(UiError e) {
      QQSDKPlugin.this.webView.sendPluginResult(
          new PluginResult(PluginResult.Status.ERROR, e.errorMessage),
          currentCallbackContext.getCallbackId());
    }
  };

  /**
   * QQZONE ????????????
   */
  IUiListener qZoneShareListener = new DefaultUiListener() {

    @Override public void onCancel() {
      QQSDKPlugin.this.webView.sendPluginResult(
          new PluginResult(PluginResult.Status.ERROR, "cancelled"),
          currentCallbackContext.getCallbackId());
    }

    @Override public void onError(UiError e) {
      QQSDKPlugin.this.webView.sendPluginResult(
          new PluginResult(PluginResult.Status.ERROR, e.errorMessage),
          currentCallbackContext.getCallbackId());
    }

    @Override public void onComplete(Object response) {
      QQSDKPlugin.this.webView.sendPluginResult(new PluginResult(PluginResult.Status.OK),
          currentCallbackContext.getCallbackId());
    }
  };

  /**
   * ??????JSON
   */
  private JSONObject makeJson(String access_token, String userid, long expires_time) {
    String json = "{\"access_token\": \"" + access_token + "\", " +
        " \"userid\": \"" + userid + "\", " +
        " \"expires_time\": \"" + String.valueOf(expires_time) + "\"" +
        "}";
    JSONObject jo = null;
    try {
      jo = new JSONObject(json);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return jo;
  }

  @Override public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    if (resultCode == Constants.ACTIVITY_OK) {
      if (requestCode == Constants.REQUEST_LOGIN) {
        Tencent.onActivityResultData(requestCode, resultCode, intent, loginListener);
      }
      if (requestCode == Constants.REQUEST_QQ_SHARE) {
        Tencent.onActivityResultData(requestCode, resultCode, intent, qqShareListener);
      }
    }
    super.onActivityResult(requestCode, resultCode, intent);
  }

  @Override public void onDestroy() {
    super.onDestroy();
    if (mTencent != null) {
    //   mTencent.releaseResource();
    }
  }
}
