package com.gryphpoem.game.zw.model;

import java.util.HashMap;
import java.util.Map;

import com.gryphpoem.game.zw.model.player.BasePlayerModel;
import com.gryphpoem.game.zw.model.player.CrossWarModel;
import com.gryphpoem.game.zw.model.player.HeroModel;
import com.gryphpoem.game.zw.model.player.LordModel;
import com.gryphpoem.game.zw.model.player.PlayerModelType;

/**
 * @ClassName CrossPlayer.java
 * @Description 跨服玩家信息
 * @author QiuKun
 * @date 2019年5月5日
 */
public class CrossPlayer {
    /** 离线 */
    public final static short STATE_OFFLINE = 0;
    /** 在线 */
    public final static short STATE_ONLINE = 1;
    /** 聚焦跨服界面状态 */
    public final static short STATE_FOCUS = 2;

    private final long lordId;
    private final int mainServerId; // 来自的区服(是主服)
    private volatile short state;// 玩家的状态

    // 各个模块
    private final Map<PlayerModelType, BasePlayerModel> modelMap = new HashMap<>();

    public CrossPlayer(long lordId, int mainServerId) {
        this.lordId = lordId;
        this.mainServerId = mainServerId;
        initModel();
    }

    private void initModel() {
        for (PlayerModelType mt : PlayerModelType.values()) {
            BasePlayerModel basePlayerModel = mt.newBasePlayerModel();
            basePlayerModel.setLordId(this.lordId);
            modelMap.put(mt, basePlayerModel);
        }
    }

    public Map<PlayerModelType, BasePlayerModel> getModelMap() {
        return modelMap;
    }

    public BasePlayerModel getModel(PlayerModelType playerModelType) {
        return modelMap.get(playerModelType);
    }

    public LordModel getLordModel() {
        return (LordModel) getModel(PlayerModelType.LORD_MODEL);
    }

    public HeroModel getHeroModel() {
        return (HeroModel) getModel(PlayerModelType.HERO_MODEL);
    }

    public CrossWarModel getCrossWarModel() {
        return (CrossWarModel) getModel(PlayerModelType.CROSS_WAR_MODEL);
    }

    public long getLordId() {
        return lordId;
    }

    public int getMainServerId() {
        return mainServerId;
    }

    /**
     * 是否在线
     * 
     * @return
     */
    public boolean isOnline() {
        return state >= STATE_ONLINE;
    }

    public boolean isFouce() {
        return state == STATE_FOCUS;
    }

    /**
     * 上线
     */
    public void online() {
        this.state = STATE_ONLINE;
    }

    public void focus() {
        this.state = STATE_FOCUS;
    }

    /**
     * 取消关注,但没下线
     */
    public void unFocus() {
        if (isOnline()) {
            this.state = STATE_ONLINE;
        }
    }

    /**
     * 离线设置
     */
    public void offline() {
        state = STATE_OFFLINE;
    }

    public int getCamp() {
        return getLordModel().getLordPb().getCamp();
    }

}
