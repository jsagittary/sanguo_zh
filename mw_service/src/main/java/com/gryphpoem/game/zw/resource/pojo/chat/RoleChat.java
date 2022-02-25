package com.gryphpoem.game.zw.resource.pojo.chat;

import com.gryphpoem.game.zw.pb.CommonPb.Chat.Builder;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Lord;
import com.gryphpoem.game.zw.resource.util.MapHelper;

/**
 * @ClassName RoleChat.java
 * @Description 玩家聊天信息
 * @author TanDonghai
 * @date 创建时间：2017年4月7日 上午10:23:34
 *
 */
public class RoleChat extends Chat {
    private Player player;
    private String msg;
    private String[] myParam;// 倒计时 资源
    private int redType;//红包类型
    

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String[] getMyParam() {
        return myParam;
    }

    public void setMyParam(String[] myParam) {
        this.myParam = myParam;
    }

    @Override
    public void serCustom(Builder builder) {
        Lord lord = player.lord;
        builder.setName(lord.getNick());
        builder.setPortrait(lord.getPortrait());
        builder.setPortraitFrame(player.getDressUp().getCurPortraitFrame());
        builder.setTitle(player.getDressUp().getCurTitle());
        if (lord.getVip() > 0) {
            builder.setVip(lord.getVip());
        }
        builder.setLv(lord.getLevel());
        builder.setJob(lord.getJob());
        builder.setArea(lord.getArea());
        builder.setLordId(lord.getLordId());
        builder.setMsg(msg);
        if (player.account.getIsGm() > 0) {
            builder.setIsGm(true);
        }

        if (player.account.getIsGuider() > 0) {
            builder.setIsGuider(true);
        }
        builder.setCamp(lord.getCamp());
        builder.setRanks(lord.getRanks());

        if (myParam != null) {
            for (int i = 0; i < myParam.length; i++) {
                if (myParam[i] != null) {
                    builder.addMyParam(myParam[i]);
                }
            }
        }
        if (player.lord.getRanks() > 0) {
            builder.setRanks(player.lord.getRanks());
        }
        builder.setRedType(getRedType());
        builder.setBubbleId(player.getDressUp().getCurrChatBubble());
    }

    public int getRedType() {
        return redType;
    }

    public void setRedType(int redType) {
        this.redType = redType;
    }
    
}
