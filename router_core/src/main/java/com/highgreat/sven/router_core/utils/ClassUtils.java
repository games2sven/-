package com.highgreat.sven.router_core.utils;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.highgreat.sven.router_core.thread.DefaultPoolExecutor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;

import dalvik.system.DexFile;

public class ClassUtils {

    /**
     * 路由表
     *
     * @param context
     * @param packageName
     * @return
     * @throws PackageManager.NameNotFoundException
     * @throws IOException
     * @throws InterruptedException
     */
    public static Set<String> getFileNameByPackageName(Application context, final String
            packageName) throws PackageManager.NameNotFoundException, InterruptedException {
        final Set<String> classNames = new HashSet<>();
        List<String> paths = getSourcePaths(context);
        //使用同步计数器判断均处理完成
        final CountDownLatch parserCtl = new CountDownLatch(paths.size());
        ThreadPoolExecutor threadPoolExecutor = DefaultPoolExecutor.newDefaultPoolExecutor(paths
                .size());
        for (final String path : paths) {
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    DexFile dexfile = null;
                    try {
                        //加载 apk中的dex 并遍历 获得所有包名为 {packageName} 的类
                        dexfile = new DexFile(path);//获得编译后的dex文件
                        Enumeration<String> dexEntries = dexfile.entries();// 获得编译后的dex文件中的所有class
                        while(dexEntries.hasMoreElements()){
                            String className = dexEntries.nextElement();
                            if(className.startsWith(packageName)){
                                classNames.add(className);
                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }finally {
                        if(null != dexfile){
                            try {
                                dexfile.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        //释放一个
                        parserCtl.countDown();
                    }
                }
            });
        }
        //等待执行完成
        parserCtl.await();
        return classNames;
    }

    /**
     * 获得程序所有的apk(instant run会产生很多split apk)
     */
    private static List<String> getSourcePaths(Application context) throws PackageManager.NameNotFoundException {
        ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
        List<String> sourcePaths = new ArrayList<>();
        sourcePaths.add(applicationInfo.sourceDir);//APK主项目完整路劲
        //instant run
        if (null != applicationInfo.splitSourceDirs) {// 获得所有的APK的路径
            sourcePaths.addAll(Arrays.asList(applicationInfo.splitSourceDirs));
        }
        return sourcePaths;
    }


}
