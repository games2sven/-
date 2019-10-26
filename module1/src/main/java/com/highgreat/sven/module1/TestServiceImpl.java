package com.highgreat.sven.module1;

import android.util.Log;

import com.highgreat.sven.base.TestService;
import com.highgreat.sven.router_annotation.Route;

@Route(path = "/module1/service")
public class TestServiceImpl implements TestService {
    @Override
    public void test() {
        Log.i("Service", "我是Module1模块测试服务通信");
    }
}
