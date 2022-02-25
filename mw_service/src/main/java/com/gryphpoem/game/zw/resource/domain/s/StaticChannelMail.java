package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @author shao kq
 * @create 2020/9/23 16:26
 * @desc 渠道邮件模板
 **/
public class StaticChannelMail {
    private int mailId;
    private int channelId;
    private int childId;
    private String desc;
    private String title;
    private String content;
    private String detail;
    private String icon;
    private List<List<Integer>> rewards;

    public int getMailId() {
        return mailId;
    }

    public void setMailId(int mailId) {
        this.mailId = mailId;
    }

    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    public int getChildId() {
        return childId;
    }

    public void setChildId(int childId) {
        this.childId = childId;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
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

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public List<List<Integer>> getRewards() {
        return rewards;
    }

    public void setRewards(List<List<Integer>> rewards) {
        this.rewards = rewards;
    }
}