package com.yxjie.chatrobot.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.yxjie.chatrobot.R;
import com.yxjie.chatrobot.adapter.MyAdapter;
import com.yxjie.chatrobot.bean.ChatRecord;
import com.yxjie.chatrobot.bean.ChatResult;
import com.yxjie.chatrobot.utils.HttpHelper;
import com.yxjie.chatrobot.utils.ThreadHelper;
import com.yxjie.chatrobot.utils.Url;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by yangxiangjie on 16/3/25.
 */
public class ChatFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = ChatFragment.class.getSimpleName();
    private RecyclerView chatRecyclerView;
    private EditText chatEdit;
    private ArrayList<ChatRecord> mlist;
    private MyAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
    }

    private void initData() {
        mlist = new ArrayList<>();
//        首条消息
        ChatRecord record = new ChatRecord();
        record.type = ChatRecord.TYPE.TYPE_ACCEPT;
        record.data = new Date();
        record.text = "你好，我是小娜";

        mlist.add(record);

        adapter = new MyAdapter(getActivity(), mlist);
    }


    public static ChatFragment getInstance() {
        return new ChatFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.chat_fragment_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);

    }

    /**
     * 初始化 控件
     *
     * @param view
     */
    private void initView(View view) {
        chatEdit = (EditText) view.findViewById(R.id.edit_chat);
        chatRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_chat);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        chatRecyclerView.setLayoutManager(linearLayoutManager);
        chatRecyclerView.setAdapter(adapter);
        view.findViewById(R.id.btn_send).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if (TextUtils.isEmpty(chatEdit.getText().toString().trim())) {
            Toast.makeText(getActivity(), "请输入内容", Toast.LENGTH_SHORT).show();
            return;
        }

        ChatRecord r = new ChatRecord();
        r.data = new Date();
        r.type = ChatRecord.TYPE.TYPE_SEND;
        r.text = chatEdit.getText().toString().trim();
        mlist.add(r);
        refreshAdapter();

        ThreadHelper.getInstance().runOnWorkerThread(new Runnable() {
            @Override
            public void run() {
                String sendMsg = stringEncode(chatEdit.getText().toString().trim());
                String url = Url.SERVER_URL + Url.API_KEY + String.format(Url.INFO, sendMsg);
                Log.d(TAG, "url =" + url);

                HttpHelper.getInstance().doGet().url(url).listener(new HttpHelper.OnNetEventListener() {
                    @Override
                    public void onSuccess(String content) {
                        Log.d("yxjie", content);
                        Gson gson = new Gson();
                        ChatResult result = gson.fromJson(content, ChatResult.class);
                        ChatRecord chatRecord = new ChatRecord();
                        chatRecord.text = result.text;
                        chatRecord.type = ChatRecord.TYPE.TYPE_ACCEPT;
                        chatRecord.data = new Date();
                        chatRecord.code = result.code;
                        chatRecord.url = result.url;
                        mlist.add(chatRecord);
                        refreshAdapter();
                    }

                    @Override
                    public void onFailure(String msg) {
                        ChatRecord chatRecord = new ChatRecord();
                        chatRecord.text = "发送失败！";
                        chatRecord.type = ChatRecord.TYPE.TYPE_ACCEPT;
                        chatRecord.data = new Date();
                        mlist.add(chatRecord);
                        refreshAdapter();
                    }
                }).request();
            }
        });
        chatEdit.setText("");
    }


    /**
     * 刷新界面
     */
    private void refreshAdapter() {
        ThreadHelper.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                //滚动到最新位置
                chatRecyclerView.getLayoutManager().scrollToPosition(mlist.size()-1);
//                ((LinearLayoutManager) chatRecyclerView.getLayoutManager()).scrollToPositionWithOffset(mlist.size(), 0);
//                adapter.notifyDataSetChanged();
            }
        });


    }

    /**
     * utf 转码
     *
     * @param str str
     * @return string
     */
    private String stringEncode(String str) {
        String string = "";
        try {
            string = URLEncoder.encode(str, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return string;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mlist) {
            mlist.clear();
            mlist = null;
        }
    }
}
