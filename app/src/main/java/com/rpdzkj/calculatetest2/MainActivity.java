package com.rpdzkj.calculatetest2;

import static com.rpdzkj.calculatetest2.BootReceiver.action_boot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Message;
import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;


import com.rpdzkj.calculatetest2.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import android.util.Log;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.TextView;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledFuture;
import android.os.AsyncTask;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import android.os.SystemClock;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private ThreadPoolExecutor threadPoolExecutor;

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final boolean DBG = true;
    private ScheduledExecutorService scheduledExecutorService;
    private ScheduledFuture<?> scheduledFuture;
    private Process process;
    private Button btnStart;
    private Button btnStartSingle;
    private Button btnHalf;
    private Button btnPoll;
    private Button btnMem;
    private Button btnEmmc;
    private Handler handler = new Handler();
    private Runnable updateUIRunnable;
    private Runnable cycleRunnable;
    private int cpuCount = Runtime.getRuntime().availableProcessors();
    private static final int EXECUTE_RUNNABLE = 0x01;
    private AtomicBoolean isRunning = new AtomicBoolean(true);
    File file;
    public Handler Viewhandler;
    private final BroadcastReceiver mReceiverBroadCast = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String msg = intent.getStringExtra("msg");
            Log.e("charge start", "启动完成");

                Intent mBootIntent = new Intent(context, MainActivity.class);
                // 下面这句话必须加上才能开机自动运行app的界面
                mBootIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(mBootIntent);
        }
    };


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        btnStart = (Button) findViewById(R.id.button);
        btnStartSingle = (Button) findViewById(R.id.buttonSingleStart);
        btnHalf = (Button) findViewById(R.id.buttonhalf);
        btnPoll = (Button) findViewById(R.id.buttonpoll);
        btnMem = (Button) findViewById(R.id.buttonmem);
        btnEmmc = (Button) findViewById(R.id.buttonemmc);
        upgradeRootPermission(getPackageCodePath());
        IntentFilter filter = new IntentFilter();
        //filter.addAction("android.intent.action.BROADCAST_FORM_ADB");
        filter.addAction("android.intent.action.BOOT_COMPLETED");
        registerReceiver(mReceiverBroadCast, filter);
        file = new File("/vendor/etc/rp_stress_test/");
        if (file.exists()) {
            // 如果文件存在
            setButtonsEnabled(true, true, true, true, true, true);
        } else {
            // 如果文件不存在
            setButtonsEnabled(true, true, true, true, false, false);
        }

        // default status
        int cpuCount = Runtime.getRuntime().availableProcessors();
        threadPoolExecutor = new ThreadPoolExecutor(cpuCount, cpuCount, 200, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(cpuCount));
        mHandler.sendEmptyMessage(EXECUTE_RUNNABLE);
        btnStart.setEnabled(false);

        Viewhandler=new Handler();
        Viewhandler.postDelayed(runnable, 1500);
    }

    public static boolean upgradeRootPermission(String pkgCodePath) {
        Process process = null;
        DataOutputStream os = null;
        try {
            String cmd="chmod 777 " + pkgCodePath;
            process = Runtime.getRuntime().exec("su"); //切换到root帐号
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }
        return true;
    }


public static String getProperty(String key, String defaultValue) {
    String value = defaultValue;
    try {
        Class<?> c = Class.forName("android.os.SystemProperties");
        Method get = c.getMethod("get", String.class, String.class);
        value = (String) (get.invoke(c, key, "unknown"));
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        return value;
    }
}

public static int setProperty(String key, String value) {
    try {
        Class<?> c = Class.forName("android.os.SystemProperties");
        Method set = c.getMethod("set", String.class, String.class);
        set.invoke(c, key, value);
        return 0;
    } catch (Exception e) {
        e.printStackTrace();
        return -1;
    }
}

    public static String execShellCmd(String command) {
        File file = new File(command);
        String s1 = "";
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            int length = fis.available();
            byte [] buffer = new byte[1000];
            fis.read(buffer);
            s1=new String(buffer);
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s1;
    }

    Runnable runnable=new Runnable(){
        public void run() {

            String TempResult = execShellCmd("/sys/class/thermal/thermal_zone0/temp");
            String FreqResult = execShellCmd("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq");
          //  String Uptime = execShellCmd("cat /proc/uptime | awk '{print $1}'");
          long uptimeMillis = SystemClock.uptimeMillis();
          long Uptime = uptimeMillis / 1000;

          String DDRResult = execShellCmd("/proc/meminfo");
            int cpuCount = Runtime.getRuntime().availableProcessors();

            TextView CPUTempView = (TextView) findViewById(R.id.CPUTemp);
            TextView CPUFreqView = (TextView) findViewById(R.id.CPUFreq);
            TextView CPUNumView = (TextView) findViewById(R.id.CPUNum); 
            TextView DDRFreeView = (TextView) findViewById(R.id.DDRFree);
            TextView UptimeView = (TextView) findViewById(R.id.Uptime);

            CPUFreqView.setText("CPU频率 : " + FreqResult);
            CPUTempView.setText("CPU温度 : " + TempResult );
            UptimeView.setText("系统运行时间 : " + Uptime);
            DDRFreeView.setText("DDR信息 : \n" + DDRResult);
            CPUNumView.setText("CPU核心数 : " + cpuCount);
            Viewhandler.postDelayed(this, 1000);// 每1000毫秒循环一次
        }
    };


    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what){
                case EXECUTE_RUNNABLE:
                    int totalSize = threadPoolExecutor.getCorePoolSize();
                    int active = threadPoolExecutor.getActiveCount();
                    Log.d(TAG,"totalSize:"+totalSize+" active:"+active);
                    for(int i = 0;i< totalSize;i++){
                        threadPoolExecutor.execute(new CalculateRunnable());
                    }
                    break;
                default:break;
            }
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private class CalculateRunnable implements Runnable {
        @Override
        public void run() {
            long start = SystemClock.uptimeMillis();
            runCalculate(1, 100000);
           // Log.d(TAG, Thread.currentThread().getName() + " spend time:" +
           //         ((SystemClock.uptimeMillis() - start) / 1000f));
            if(!threadPoolExecutor.isShutdown())
                threadPoolExecutor.execute(this);
        }
    }
    public void start(int corePoolSize, int maximumPoolSize, int queueCapacity, View v) {
        stop(v);  // 停止当前的操作
        threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, 200, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(queueCapacity));
        mHandler.sendEmptyMessage(EXECUTE_RUNNABLE);
    }
 
    
    public void Start(View v) {
        int cpuCount = Runtime.getRuntime().availableProcessors();
        start(cpuCount, cpuCount, cpuCount, v);
        if (file.exists()) {
            // 如果文件存在
        set1ButtonsEnabled(false, true, true, true, true);
        } else {
            // 如果文件不存在
        set1ButtonsEnabled(false, true, true, true, false);
        }
    
    }

    public void startSingle(View v) {
        start(1, 1, 1, v);
        if (file.exists()) {
            // 如果文件存在
        set1ButtonsEnabled(true, false, true, true, true);
        } else {
            // 如果文件不存在
        set1ButtonsEnabled(true, false, true, true, false);
        }
    
    }
    
    public void startHalf(View v) {
        int cpuCount = Runtime.getRuntime().availableProcessors();
        start(cpuCount/2, cpuCount/2, cpuCount/2, v);
       if (file.exists()) {
            // 如果文件存在
        set1ButtonsEnabled(true, true, false, true, true);
        } else {
            // 如果文件不存在
        set1ButtonsEnabled(true, true, false, true, false);
        }
    }

public void startMem(View v) {
    stop(v);  // 停止当前的操作
    new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                // 创建ProcessBuilder对象并设置要执行的Shell命令
                ProcessBuilder processBuilder = new ProcessBuilder("sh", "vendor/etc/rp_stress_test/cpu_ddr_stress_test.sh");
                // 将标准输入、输出和错误流重定向到当前进程
                processBuilder.redirectInput(ProcessBuilder.Redirect.INHERIT);
                processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                processBuilder.redirectErrorStream(true);

                // 开始执行Shell命令
                Process process = processBuilder.start();

                // 等待Shell命令执行结束
                int exitCode = process.waitFor();

                if (exitCode == 0) {
                    Log.i("Shell Execution", "Shell脚本执行成功！");
                } else {
                    Log.e("Shell Execution", "Shell脚本执行失败！退出码为 " + exitCode);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }).start();

    set1ButtonsEnabled(true, true, true, true, false);
}


public void startRandom(View v) {
    stop(v);  // 停止当前的操作
    isRunning.set(true);
//    AtomicBoolean isRunning = new AtomicBoolean(true);
    new Thread(new Runnable() {
        @Override
        public void run() {
            while (isRunning.get()) {  // 循环执行
                try {
                    // 创建ProcessBuilder对象并设置要执行的Shell命令
                    ProcessBuilder processBuilder = new ProcessBuilder("sh", "vendor/etc/rp_stress_test/cpu_ddr_stress_test.sh");
                    // 将标准输入、输出和错误流重定向到当前进程
                    processBuilder.redirectInput(ProcessBuilder.Redirect.INHERIT);
                    processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                    processBuilder.redirectErrorStream(true);

                    // 开始执行Shell命令
                    Process process = processBuilder.start();

                    // 等待Shell命令执行结束
                    int exitCode = process.waitFor();

                    if (exitCode == 0) {
                        Log.i("Shell Execution", "Shell脚本执行成功！");
                    } else {
                        Log.e("Shell Execution", "Shell脚本执行失败！退出码为 " + exitCode);
                    }

                    // 运行6秒
                    Thread.sleep(6000);

                    // 在UI线程中停止当前的操作
                    v.post(() -> stopRandom(v));

                    // 停止6秒
                    Thread.sleep(60000);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }).start();
//    File file = new File("your_file_path_here");
    if (file.exists()) {
        // 如果文件存在
        set1ButtonsEnabled(true, true, true, false, true);
    } else {
        // 如果文件不存在
        set1ButtonsEnabled(true, true, true, false, false);
    }
}

//    public void startRandom(View v) {
//        stop(v);  // 停止当前的操作
//        scheduledExecutorService = Executors.newScheduledThreadPool(1);
//        Runnable task = new Runnable() {
//            public void run() {
//                threadPoolExecutor = new ThreadPoolExecutor(1, 1, 200, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(1));
//                mHandler.sendEmptyMessage(EXECUTE_RUNNABLE);
//                try {
//                    Thread.sleep(8000);  // 让线程运行5秒
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                // 在这里直接关闭 ThreadPoolExecutor
//                if(threadPoolExecutor != null){
//                    try {
//                        threadPoolExecutor.shutdown();
//                        if (!threadPoolExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
//                            threadPoolExecutor.shutdownNow();
//                        }
//                    } catch (InterruptedException ex) {
//                        threadPoolExecutor.shutdownNow();
//                        Thread.currentThread().interrupt();
//                    }
//                }
//            }
//        };
//        scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(task, 0, 68, TimeUnit.SECONDS);
//        if (file.exists()) {
//            // 如果文件存在
//        setButtonsEnabled(true, true, true, false, true, true);
//        } else {
//            // 如果文件不存在
//        setButtonsEnabled(true, true, true, false, false, false);
//        }
//    }



    public void startEmmc(View v) {
        stop(v);  // 停止当前的操作
       setProperty("emmc_test", "1");
       if (file.exists()) {
            // 如果文件存在
           btnEmmc.setEnabled(false);
       // setButtonsEnabled(true, true, true, true, true, false);
        } else {
            // 如果文件不存在
           btnEmmc.setEnabled(false);
        //setButtonsEnabled(true, true, true, true, false, false);
        }
    }

      public void stop(View v) {
        mHandler.removeMessages(EXECUTE_RUNNABLE);
        if(threadPoolExecutor != null){
            try {
                threadPoolExecutor.shutdown();
                if (!threadPoolExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                    threadPoolExecutor.shutdownNow();
                }
            } catch (InterruptedException ex) {
                threadPoolExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        if(scheduledFuture != null){
            scheduledFuture.cancel(true);
            scheduledExecutorService.shutdown();
        }
        setProperty("emmc_test", "0"); 
        // 新增的代码，用于停止所有的memtester进程
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 创建ProcessBuilder对象并设置要执行的Shell命令
                    ProcessBuilder processBuilder = new ProcessBuilder("killall", "memtester");
    
                    // 开始执行Shell命令
                    Process process = processBuilder.start();
    
                    // 等待Shell命令执行结束
                    int exitCode = process.waitFor();
    
                    if (exitCode == 0) {
                        Log.i("Shell Execution", "成功停止了所有的memtester进程！");
                    } else {
                        Log.e("Shell Execution", "停止memtester进程失败！退出码为 " + exitCode);
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        if (file.exists()) {
            // 如果文件存在

        set1ButtonsEnabled(true, true, true, true, true);
            btnEmmc.setEnabled(true);
        } else {
            // 如果文件不存在
        set1ButtonsEnabled(true, true, true, true, false);
        } 
    }
      
    public void stopRandom(View v) {
        mHandler.removeMessages(EXECUTE_RUNNABLE);
        if(threadPoolExecutor != null){
            try {
                threadPoolExecutor.shutdown();
                if (!threadPoolExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                    threadPoolExecutor.shutdownNow();
                }
            } catch (InterruptedException ex) {
                threadPoolExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        if(scheduledFuture != null){
            scheduledFuture.cancel(true);
            scheduledExecutorService.shutdown();
        }
        setProperty("emmc_test", "0"); 
        // 新增的代码，用于停止所有的memtester进程
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 创建ProcessBuilder对象并设置要执行的Shell命令
                    ProcessBuilder processBuilder = new ProcessBuilder("killall", "memtester");
    
                    // 开始执行Shell命令
                    Process process = processBuilder.start();
    
                    // 等待Shell命令执行结束
                    int exitCode = process.waitFor();
    
                    if (exitCode == 0) {
                        Log.i("Shell Execution", "成功停止了所有的memtester进程！");
                    } else {
                        Log.e("Shell Execution", "停止memtester进程失败！退出码为 " + exitCode);
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }     

    
    public void setButtonsEnabled(boolean start, boolean startSingle, boolean half, boolean poll, boolean mem, boolean emmc) {
        btnStart.setEnabled(start);
        btnStartSingle.setEnabled(startSingle);
        btnHalf.setEnabled(half);
        btnPoll.setEnabled(poll);
        btnMem.setEnabled(mem);
        btnEmmc.setEnabled(emmc);
    }

    public void set1ButtonsEnabled(boolean start, boolean startSingle, boolean half, boolean poll, boolean emmc) {
        btnStart.setEnabled(start);
        btnStartSingle.setEnabled(startSingle);
        btnHalf.setEnabled(half);
        btnPoll.setEnabled(poll);
    //    btnMem.setEnabled(mem);
        btnEmmc.setEnabled(emmc);
    }

    @Override
    public void onBackPressed() {
        // 调用stop()方法
        View v = new View(this);
        stop(v);
        super.onBackPressed();
    }
    

    
    
    private BigDecimal factorial(int n) {
        BigDecimal value = BigDecimal.valueOf(1);
        for (int i = 1; i <= n; i++) {
            value = value.multiply(BigDecimal.valueOf(i));
        }
        return value;
    }

    private BigDecimal pow(BigDecimal val, int power) {
        BigDecimal value = BigDecimal.valueOf(1);
        for (int i = 1; i <= power; i++) {
            value = value.multiply(val);
        }
        return value;
    }

    private void runCalculate(int n, int scale) {
       // if(DBG)Log.d(TAG, "n=" + n + " scale=" + scale);
        BigDecimal upper = new BigDecimal(426880.0000 * Math.sqrt(10005.0000));
        upper = upper.setScale(scale, BigDecimal.ROUND_HALF_EVEN);
        BigDecimal downer = BigDecimal.valueOf(0);
        for (int i = 0; i <= n; i++) {
            BigDecimal d0 = factorial(6 * i).multiply(new BigDecimal(545140134 * i + 13591409));
            BigDecimal d1 = factorial(i);
            BigDecimal d2 = pow(d1, 3);
            BigDecimal d3 = factorial(3 * i);
            BigDecimal d4 = pow(-640320, 3 * i);
            BigDecimal d5 = d2.multiply(d3);
            BigDecimal d6 = d4.multiply(d5);
            BigDecimal d = d0.divide(d6, BigDecimal.ROUND_HALF_EVEN);
            downer = downer.add(d);
        }

        BigDecimal result = upper.divide(downer, BigDecimal.ROUND_HALF_EVEN);
       // if(DBG)Log.d(TAG, "BigDecimal->result:" + result);
    }


    private BigDecimal pow(double val, int power) {
        return pow(new BigDecimal(val), power);
    }

}
