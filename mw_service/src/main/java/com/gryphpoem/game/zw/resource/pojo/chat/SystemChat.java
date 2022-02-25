package com.gryphpoem.game.zw.resource.pojo.chat;

import com.gryphpoem.game.zw.pb.CommonPb.Chat.Builder;

/**
 * @ClassName SystemChat.java
 * @Description 系统聊天、公告
 * @author TanDonghai
 * @date 创建时间：2017年4月7日 上午10:20:02
 *
 */
public class SystemChat extends Chat {
    private int style;// 显示样式，1 使用喇叭，滚屏显示
    private int chatId;// 系统消息的chatId
    private String[] myParam;// 倒计时 资源
    private String[] param;

    public String[] getMyParam() {
        return myParam;
    }

    public void setMyParam(String[] myParam) {
        this.myParam = myParam;
    }

    public int getStyle() {
        return style;
    }

    public void setStyle(int style) {
        this.style = style;
    }

    public int getChatId() {
        return chatId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    public String[] getParam() {
        return param;
    }

    public void setParam(String[] param) {
        this.param = param;
    }

    @Override
    public void serCustom(Builder builder) {
        if (style != 0) {
            builder.setStyle(style);
        }

        if (param != null) {
            for (int i = 0; i < param.length; i++) {
                if (param[i] != null) {
                    builder.addParam(param[i]);
                }
            }
        }
        if (myParam != null) {
            for (int i = 0; i < myParam.length; i++) {
                if (myParam[i] != null) {
                    builder.addMyParam(myParam[i]);
                }
            }
        }
        builder.setChatId(chatId);
    }

}
