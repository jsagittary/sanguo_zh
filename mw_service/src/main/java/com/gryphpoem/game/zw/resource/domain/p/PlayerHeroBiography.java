package com.gryphpoem.game.zw.resource.domain.p;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.pojo.GamePb;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-08-12 17:20
 */
public class PlayerHeroBiography implements Serializable, GamePb<SerializePb.SerHeroBiographyData> {
    private Map<Integer, Integer> levelMap;

    public PlayerHeroBiography() {
        this.levelMap = new HashMap<>();
    }

    public Map<Integer, Integer> getLevelMap() {
        return levelMap;
    }

    public void setLevelMap(Map<Integer, Integer> levelMap) {
        this.levelMap = levelMap;
    }

    public CommonPb.HeroBiographyData buildClientData() {
        CommonPb.HeroBiographyData.Builder builder = CommonPb.HeroBiographyData.newBuilder();
        if (CheckNull.nonEmpty(levelMap)) {
            levelMap.entrySet().forEach(data -> builder.addData(PbHelper.createTwoIntPb(data.getKey(), data.getValue())));
        }
        return builder.build();
    }

    @Override
    public SerializePb.SerHeroBiographyData createPb(boolean isSaveDb) {
        SerializePb.SerHeroBiographyData.Builder builder = SerializePb.SerHeroBiographyData.newBuilder();
        if (CheckNull.nonEmpty(levelMap)) {
            levelMap.entrySet().forEach(data -> builder.addData(PbHelper.createTwoIntPb(data.getKey(), data.getValue())));
        }
        return builder.build();
    }
}
