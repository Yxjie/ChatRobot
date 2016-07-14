package com.yxjie.chatrobot.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Window;

import com.yxjie.chatrobot.R;
import com.yxjie.chatrobot.utils.ActivityCollector;
import com.yxjie.chatrobot.utils.HttpHelper;

/**
 * Created by yangxiangjie on 16/3/25.
 */
public abstract class SingleFragmentActivity extends FragmentActivity {

    public abstract Fragment createFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.single_fragment_activity_layout);


        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentById(R.id.fragmentContainer);
        if (null == fragment) {
            fragment = createFragment();
            manager.beginTransaction().add(R.id.fragmentContainer, fragment).commit();
        }
        //add activity
        ActivityCollector.addActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //remove activity
        HttpHelper.getInstance().stopAll();
        ActivityCollector.removeActivity(this);
    }
}
