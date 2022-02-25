package com.gryphpoem.game.zw.resource.pojo.chat;

import java.util.List;

import com.gryphpoem.game.zw.pb.CommonPb.Chat.Builder;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Lord;

/**
 * @ClassName ShareChat.java
 * @Description 玩家分享的战报或其他分享信息
 * @author TanDonghai
 * @date 创建时间：2017年4月7日 下午4:28:00
 *
 */
public class ShareChat extends Chat {
    private Player player;
    private int moldId;// 分享报告时的moldId
    private List<String> param;// 邮件标题参数
    private int report; // 报告
    private int chatId; // 聊天模版id

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public int getMoldId() {
        return moldId;
    }

    public void setMoldId(int moldId) {
        this.moldId = moldId;
    }

    public List<String> getParam() {
        return param;
    }

    public void setParam(List<String> param) {
        this.param = param;
    }

    public int getReport() {
        return report;
    }

    public void setReport(int report) {
        this.report = report;
    }

    public int getChatId() {
        return chatId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
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

        if (moldId != 0) {
            builder.setMoldId(moldId);
        }

        if (param != null) {
            builder.addAllParam(param);
        }

        if (report != 0) {
            builder.setReport(report);
        }

        if (chatId != 0) {
            builder.setChatId(chatId);
        }

        if (player.account.getIsGuider() > 0) {
            builder.setIsGuider(true);
        }
        if (player.lord.getRanks() > 0) {
            builder.setRanks(player.lord.getRanks());
        }
    }

}
