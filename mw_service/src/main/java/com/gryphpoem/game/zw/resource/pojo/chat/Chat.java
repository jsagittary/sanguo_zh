package com.gryphpoem.game.zw.resource.pojo.chat;

import com.gryphpoem.game.zw.pb.CommonPb;

/**
 * @ClassName Chat.java
 * @Description 聊天父类
 * @author TanDonghai
 * @date 创建时间：2017年4月6日 下午5:32:35
 *
 */
public abstract class Chat {
    protected int time;
    protected int channel; // 频道，1 世界，2 GM，3 国家，4 私聊
    protected int style;// 显示的样式 0为默认, 1 使用喇叭
    protected int isCampChat;// 是否为阵营聊天 0否 1是
    protected int camp;// 发起者阵营
    protected boolean system; //是否系统发送的私聊 

    /**
     * 返回消息ptotobuf对象
     * 
     * @return
     */
    public CommonPb.Chat ser() {
        return serBuilder().build();
    }

    public CommonPb.Chat.Builder serBuilder() {
        CommonPb.Chat.Builder builder = CommonPb.Chat.newBuilder();
        builder.setTime(time);
        builder.setChannel(channel);
        builder.setStyle(style);
        builder.setIsCampChat(isCampChat);
        builder.setCamp(camp);
        builder.setSystem(system);
        serCustom(builder);
        return builder;
    }

    /**
     * 返回消息ptotobuf对象，子类必须实现该方法
     * 
     * @param builder
     */
    public abstract void serCustom(CommonPb.Chat.Builder builder);

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public int getStyle() {
        return style;
    }

    public void setStyle(int style) {
        this.style = style;
    }

    public int getIsCampChat() {
        return isCampChat;
    }

    public void setIsCampChat(int isCampChat) {
        this.isCampChat = isCampChat;
    }

    public int getCamp() {
        return camp;
    }

    public void setCamp(int camp) {
        this.camp = camp;
    }

    public boolean isSystem() {
        return system;
    }

    public void setSystem(boolean system) {
        this.system = system;
    }
}
