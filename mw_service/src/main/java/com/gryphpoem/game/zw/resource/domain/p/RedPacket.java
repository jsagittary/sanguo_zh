package com.gryphpoem.game.zw.resource.domain.p;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

/**
 * @ClassName RedPacket.java
 * @Description 红包
 * @author QiuKun
 * @date 2018年6月8日
 */
public class RedPacket {
    public static int ID_KEY = 1;

    private int id; // 存储红包id
    private int sendTime;// 发送时间
    private int exceedTime;// 过期时间
    private int chatId;// 红包对应chatId
    private int redPackId;// 红包显示模板id
    private int redType;// 红包类型 默认0是系统红包，27是红包活动类型
    private List<String> param; // 红包显示参数
    private List<Integer> rewarPond;// 奖池的id
    private Map<Long, RedPacketRole> role;// 领取红包玩家信息


    /**
     * 
     * @param rewarPond
     * @param param
     * @param duarTime 持续时间
     * @param chatId
     * @param redPackId
     */
    public RedPacket(List<Integer> rewarPond, List<String> param, int duarTime, int chatId, int redPackId,int redType) {
        this.id = ++ID_KEY;
        this.rewarPond = rewarPond;
        this.param = param;
        this.role = new HashMap<>(rewarPond.size());
        this.sendTime = TimeHelper.getCurrentSecond();
        this.exceedTime = this.sendTime + duarTime;
        this.chatId = chatId;
        this.redPackId = redPackId;
        this.redType = redType;
    }

    public RedPacket(CommonPb.RedPacket ser) {
        this.id = ser.getId();
        this.sendTime = ser.getSendTime();
        this.exceedTime = ser.getExceedTime();
        this.chatId = ser.getChatId();
        this.redPackId = ser.getRedPackId();

        this.param = new ArrayList<>(ser.getParamCount());
        for (String p : ser.getParamList()) {
            this.param.add(p);
        }
        this.rewarPond = new ArrayList<>(ser.getRewarPondCount());
        for (Integer i : ser.getRewarPondList()) {
            this.rewarPond.add(i);
        }
        this.role = new HashMap<>(ser.getRewarPondCount());
        for (CommonPb.RedPacketRole r : ser.getRoleList()) {
            this.role.put(r.getRoleId(), new RedPacketRole(r));
        }
        this.redType = ser.getRedType();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSendTime() {
        return sendTime;
    }

    public void setSendTime(int sendTime) {
        this.sendTime = sendTime;
    }

    public int getExceedTime() {
        return exceedTime;
    }

    public void setExceedTime(int exceedTime) {
        this.exceedTime = exceedTime;
    }

    public int getChatId() {
        return chatId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    public int getRedPackId() {
        return redPackId;
    }

    public void setRedPackId(int redPackId) {
        this.redPackId = redPackId;
    }

    public List<String> getParam() {
        return param;
    }

    public void setParam(List<String> param) {
        this.param = param;
    }

    public List<Integer> getRewarPond() {
        return rewarPond;
    }

    public void setRewarPond(List<Integer> rewarPond) {
        this.rewarPond = rewarPond;
    }

    public Map<Long, RedPacketRole> getRole() {
        return role;
    }

    public void setRole(Map<Long, RedPacketRole> role) {
        this.role = role;
    }

    public int getRedType() {
        return redType;
    }

    public void setRedType(int redType) {
        this.redType = redType;
    }

    @Override
    public String toString() {
        return "RedPacket [id=" + id + ", sendTime=" + sendTime + ", exceedTime=" + exceedTime + ", chatId=" + chatId
                + ", redPackId=" + redPackId + ", param=" + param + ", rewarPond=" + rewarPond + ", role=" + role + "]";
    }

}
