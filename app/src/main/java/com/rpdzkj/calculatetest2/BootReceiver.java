package com.rpdzkj.calculatetest2;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class BootReceiver extends BroadcastReceiver {
    static final String action_boot ="android.intent.action.BOOT_COMPLETED";
    public void onReceive (Context context, Intent intent) {
        Log.e("charge start", "启动完成");
        if (intent.getAction().equals(action_boot)){

            Intent mBootIntent = new Intent(context, MainActivity.class);
            // 下面这句话必须加上才能开机自动运行app的界面
            mBootIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mBootIntent);
        }
    }
}
