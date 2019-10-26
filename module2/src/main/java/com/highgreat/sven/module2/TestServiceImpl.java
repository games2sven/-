package com.highgreat.sven.module2;

import android.util.Log;

import com.highgreat.sven.base.TestService;
import com.highgreat.sven.router_annotation.Route;

@Route(path = "/module2/service")
public class TestServiceImpl implements TestService {
    @Override
    public void test() {
        Log.i("Service", "我是Module2模块测试服务通信");
    }
}
