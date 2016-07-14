package com.yxjie.chatrobot.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yxjie.chatrobot.R;
import com.yxjie.chatrobot.bean.ChatRecord;
import com.yxjie.chatrobot.utils.TypCodeUtil;

import java.util.ArrayList;

/**
 * Created by yangxiangjie on 16/3/25.
 */
public class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements AdapterClickListener {

    private static final int CHAT_FROM = 0;

    private static final int CHAT_TO = CHAT_FROM + 1;

    private ArrayList<ChatRecord> mList;

    private Context mContext;

    private AdapterClickListener mListener;

    public MyAdapter(Context mContext, ArrayList<ChatRecord> mlist) {
        this.mContext = mContext;
        this.mList = mlist;
    }

    @Override
    public int getItemViewType(int position) {
        if (mList.get(position).type == ChatRecord.TYPE.TYPE_ACCEPT) {
            return CHAT_FROM;
        } else {
            return CHAT_TO;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (CHAT_FROM == viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_chat_from, parent, false);
            return new ChatFromHolder(view, this);
        } else {
            View viewTo = LayoutInflater.from(mContext).inflate(R.layout.item_chat_send, parent, false);
            return new ChatTOHolder(viewTo);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        ChatRecord mChat = (ChatRecord) getItem(position);
        if (CHAT_FROM == getItemViewType(position)) {
            ChatFromHolder chatFromHolder = (ChatFromHolder) holder;
            chatFromHolder.chat_from_Txt.setText(mChat.text);
            boolean isUrl = mChat.code == TypCodeUtil.TYPE_LINK && !TextUtils.equals("", mChat.url);
            chatFromHolder.chat_from_Link.setVisibility(isUrl ? View.VISIBLE : View.GONE);

        } else {
            ChatTOHolder chatTOHolder = (ChatTOHolder) holder;
            chatTOHolder.chat_to_Txt.setText(mChat.text);
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    private Object getItem(int pos) {
        return mList.get(pos);
    }

    @Override
    public void onClick(View view, int pos) {
        ChatRecord record = (ChatRecord) getItem(pos);
        if (record.url == null) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(record.url));
        mContext.startActivity(intent);
    }

    public static class ChatFromHolder extends RecyclerView.ViewHolder {
        public TextView chat_from_Txt;
        public TextView chat_from_Link;

        public ChatFromHolder(View itemView, final AdapterClickListener listener) {
            super(itemView);
            chat_from_Txt = (TextView) itemView.findViewById(R.id.chat_from_txt);
            chat_from_Link = (TextView) itemView.findViewById(R.id.chat_from_link);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(v, getAdapterPosition());
                }
            });
        }
    }

    public static class ChatTOHolder extends RecyclerView.ViewHolder {
        public TextView chat_to_Txt;

        public ChatTOHolder(View itemView) {
            super(itemView);
            chat_to_Txt = (TextView) itemView.findViewById(R.id.chat_to_txt);
        }
    }
}
