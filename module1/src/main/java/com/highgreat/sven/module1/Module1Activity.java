package com.highgreat.sven.module1;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.highgreat.sven.base.TestService;
import com.highgreat.sven.router_annotation.Extra;
import com.highgreat.sven.router_annotation.Route;
import com.highgreat.sven.router_core.HGRouter;

@Route(path = "/module1/test")
public class Module1Activity extends Activity {

    @Extra
    String msg;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module1);
        HGRouter.getInstance().inject(this);
        Log.i("module1", "我是模块1:" + msg);


        TestService testService = (TestService) HGRouter.getInstance().build("/main/service1")
                .navigation();
        testService.test();

        TestService testService1 = (TestService) HGRouter.getInstance().build("/main/service2")
                .navigation();
        testService1.test();

        TestService testService2 = (TestService) HGRouter.getInstance().build("/module1/service")
                .navigation();
        testService2.test();

        TestService testService3 = (TestService) HGRouter.getInstance().build("/module2/service")
                .navigation();
        testService3.test();
    }

    public void mainJump(View view) {
        HGRouter.getInstance().build("/main/test").withString("a",
                "从Module1").navigation(this);
    }

    public void module2Jump(View view) {
        HGRouter.getInstance().build("/module2/test").withString("msg",
                "从Module1").navigation(this);
    }

}
