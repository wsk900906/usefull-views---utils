package com.swifty.fillcolor.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.swifty.fillcolor.OnLoginSuccessListener;
import com.swifty.fillcolor.OnUnLockImageSuccessListener;
import com.swifty.fillcolor.R;
import com.swifty.fillcolor.model.bean.UserBean;
import com.swifty.fillcolor.view.MyProgressDialog;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners;
import com.umeng.socialize.exception.SocializeException;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.sso.UMSsoHandler;

import java.util.Map;
import java.util.Set;


/**
 * Created by Swifty.Wang on 2015/7/3.
 */
public class SNSUtil {
    private static UMSocialService mController;
    private static OnLoginSuccessListener mOnLoginSuccessListener;

    public static void shareApp(Context context) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = context.getString(R.string.sharecontent);
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name));
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        context.startActivity(Intent.createChooser(sharingIntent, context.getString(R.string.pleaseselect)));

    }

    public static void shareApp(Context context, OnUnLockImageSuccessListener onUnLockImageSuccessListener) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = context.getString(R.string.sharecontent);
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name));
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        context.startActivity(Intent.createChooser(sharingIntent, context.getString(R.string.pleaseselect)));
        onUnLockImageSuccessListener.UnlockImageSuccess();
    }

    /**
     * *************
     * <p/>
     * 发起添加群流程。群号：手指填图 用户交流群(104368068) 的 key 为： 7yxN_oUZcVfWCDOZqS8qvJkl0tgOKj3Q
     * 调用 joinQQGroup(7yxN_oUZcVfWCDOZqS8qvJkl0tgOKj3Q) 即可发起手Q客户端申请加群 手指填图 用户交流群(104368068)
     *
     * @param key 由官网生成的key
     * @return 返回true表示呼起手Q成功，返回fals表示呼起失败
     * ****************
     */
    public static boolean joinQQGroup(Context context, String key) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + key));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            // 未安装手Q或安装的版本不支持
            return false;
        }
    }

    public static void joinQQGroup(Context context) {

        if (!joinQQGroup(context, "7yxN_oUZcVfWCDOZqS8qvJkl0tgOKj3Q")) {
            Toast.makeText(context, context.getString(R.string.joinGroupFailed), Toast.LENGTH_SHORT).show();
        }
    }

    public static UMSocialService qqLogin(final Activity activity, OnLoginSuccessListener onLoginSuccessListener) {
        mOnLoginSuccessListener = onLoginSuccessListener;
        mController = UMServiceFactory.getUMSocialService("com.umeng.login");
        //参数1为当前Activity， 参数2为开发者在QQ互联申请的APP ID，参数3为开发者在QQ互联申请的APP kEY.
        UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler(activity, "1104727259",
                "NddWVIV3BNdRqh97");
        qqSsoHandler.addToSocialSDK();
        MyProgressDialog.show(activity, null, activity.getString(R.string.pullqqloging));
        mController.doOauthVerify(activity, SHARE_MEDIA.QQ, new SocializeListeners.UMAuthListener() {
            @Override
            public void onStart(SHARE_MEDIA platform) {
            }

            @Override
            public void onError(SocializeException e, SHARE_MEDIA platform) {
                Toast.makeText(activity, "授权错误", Toast.LENGTH_SHORT).show();
                MyProgressDialog.DismissDialog();
            }

            @Override
            public void onComplete(Bundle value, SHARE_MEDIA platform) {
                StringBuilder sb = new StringBuilder();
                Set<String> keys = value.keySet();
                for (String key : keys) {
                    sb.append(key + "=" + value.get(key).toString() + "\r\n");
                }
                L.d("TestData2", sb.toString());
                //获取相关授权信息
                mController.getPlatformInfo(activity, SHARE_MEDIA.QQ, new SocializeListeners.UMDataListener() {
                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onComplete(int status, Map<String, Object> info) {
                        MyProgressDialog.DismissDialog();
                        if (status == 200 && info != null) {
                            StringBuilder sb = new StringBuilder();
                            Set<String> keys = info.keySet();
                            for (String key : keys) {
                                sb.append(key + "=" + info.get(key).toString() + "\r\n");
                            }
                            //do register or login
                            UserBean userBean = new UserBean();
                            mOnLoginSuccessListener.onLoginSuccess(userBean);
                            L.d("TestData", sb.toString());
                        } else {
                            L.d("TestData", "发生错误：" + status);
                        }
                    }
                });
            }

            @Override
            public void onCancel(SHARE_MEDIA platform) {
                Toast.makeText(activity, "授权取消", Toast.LENGTH_SHORT).show();
                MyProgressDialog.DismissDialog();
            }
        });
        return mController;
    }

    public static void QQlogout(final Context mContext) {
        mController.deleteOauth(mContext, SHARE_MEDIA.QQ,
                new SocializeListeners.SocializeClientListener() {
                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onComplete(int status, SocializeEntity entity) {
                        if (status == 200) {
                            Toast.makeText(mContext, "删除成功.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mContext, "删除失败",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public static void UmengSSOResult(int requestCode, int resultCode, Intent data) {
        /**使用SSO授权必须添加如下代码 */
        if (mController != null) {
            UMSsoHandler ssoHandler = mController.getConfig().getSsoHandler(requestCode);
            if (ssoHandler != null) {
                ssoHandler.authorizeCallBack(requestCode, resultCode, data);
            }
        }
    }
}
