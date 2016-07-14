package com.yxjie.chatrobot.activity;

import android.support.v4.app.Fragment;

import com.yxjie.chatrobot.fragment.ChatFragment;

/**
 * Created by yangxiangjie on 16/3/25.
 * 聊天界面
 */
public class ChatActivity extends SingleFragmentActivity {


    @Override
    public Fragment createFragment() {
        return ChatFragment.getInstance();
    }



}
