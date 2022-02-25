package com.gryphpoem.game.zw.model.global;

import java.util.function.Supplier;

import com.gryphpoem.game.zw.mgr.CrossRankMgr;
import com.gryphpoem.game.zw.mgr.FortressMgr;
import com.gryphpoem.game.zw.server.CrossServer;

/**
 * @ClassName GlobalModelType.java
 * @Description 公共数据模块类型
 * @author QiuKun
 * @date 2019年6月19日
 */
public enum GlobalModelType {
    FORT_MODEL(1, "堡垒数据", () -> {
        return CrossServer.ac.getBean(FortressMgr.class);
    }), RANK_MODEL(2, "排行榜数据", () -> {
        return CrossServer.ac.getBean(CrossRankMgr.class);
    });

    // 模块类型
    private int type;
    private Supplier<BaseGlobalSaveModel> func;
    private String desc;

    private GlobalModelType(int type, String desc, Supplier<BaseGlobalSaveModel> func) {
        this.type = type;
        this.func = func;
        this.desc = desc;
    }

    public int getType() {
        return type;
    }

    public Supplier<BaseGlobalSaveModel> getFunc() {
        return func;
    }

    public String getDesc() {
        return desc;
    }

    public static GlobalModelType getGlobalModelType(int type) {
        for (GlobalModelType modelType : GlobalModelType.values()) {
            if (modelType.getType() == type) {
                return modelType;
            }
        }
        return null;
    }
}
