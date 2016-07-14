package com.yxjie.chatrobot.bean;

import java.util.Date;

/**
 * Created by yangxiangjie on 16/3/25.
 */
public class ChatRecord extends SuperChat {

    public enum TYPE {
        TYPE_ACCEPT, TYPE_SEND
    }

    public TYPE type;
    public Date data;


}
