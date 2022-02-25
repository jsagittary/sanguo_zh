package com.gryphpoem.game.zw.resource.domain.s;

/**
 * @Description 推送消息模版配置表
 * @author TanDonghai
 * @date 创建时间：2017年9月7日 上午10:54:26
 *
 */
public class StaticPushMessage {
    private int pushId;
    private int showType;// 消息展示类型，1 纯文本，2 链接，3 透传消息
    private String title;// 显示标题
    private String content;// 显示内容

    public int getPushId() {
        return pushId;
    }

    public void setPushId(int pushId) {
        this.pushId = pushId;
    }

    public int getShowType() {
        return showType;
    }

    public void setShowType(int showType) {
        this.showType = showType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "StaticPushMessage [pushId=" + pushId + ", showType=" + showType + ", title=" + title + ", content="
                + content + "]";
    }

}
