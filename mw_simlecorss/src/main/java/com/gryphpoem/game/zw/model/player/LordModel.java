package com.gryphpoem.game.zw.model.player;

import com.gryphpoem.game.zw.pb.CommonPb.CrossLordPb;

/**
 * @ClassName LordModel.java
 * @Description
 * @author QiuKun
 * @date 2019年5月11日
 */
public class LordModel extends BasePlayerModel {

    // 游戏服传过来的只读信息
    private CrossLordPb lordPb;

    @Override
    public PlayerModelType getModelType() {
        return PlayerModelType.LORD_MODEL;
    }

    public CrossLordPb getLordPb() {
        return lordPb;
    }

    public void setLordPb(CrossLordPb lordPb) {
        this.lordPb = lordPb;
    }

    public int getSelfServerId() {
        return lordPb.getServerId();
    }

    public String getNick() {
        return lordPb.getNick();
    }

    public int getPortrait() {
        return lordPb.getPortrait();
    }
}
