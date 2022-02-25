package com.gryphpoem.game.zw.model.player;

import com.gryphpoem.game.zw.core.util.LogUtil;

/**
 * @ClassName PlayerModelType.java
 * @Description
 * @author QiuKun
 * @date 2019年5月11日
 */
public enum PlayerModelType {
    LORD_MODEL(1, LordModel.class), // 玩家的基础信息
    HERO_MODEL(2, HeroModel.class), // 将领模块
    CROSS_WAR_MODEL(3, CrossWarModel.class),// 跨服战模块
    ;

    private int type; // 模块类型
    private Class<? extends BasePlayerModel> modelClazz;

    private PlayerModelType(int type, Class<? extends BasePlayerModel> modelClazz) {
        this.type = type;
        this.modelClazz = modelClazz;
    }

    public int getType() {
        return type;
    }

    public PlayerModelType getPlayerModelType(int type) {
        for (PlayerModelType modelType : PlayerModelType.values()) {
            if (modelType.getType() == type) {
                return modelType;
            }
        }
        return null;
    }

    public Class<? extends BasePlayerModel> getModelClazz() {
        return modelClazz;
    }

    public BasePlayerModel newBasePlayerModel() {
        try {
            // 此处可以使用 动态代理 去创建对应的model,标注状态
            return this.modelClazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            LogUtil.error(e);
        }
        return null;
    }

}
