package com.gryphpoem.game.zw.resource.pojo.world;

import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.domain.s.StaticAltarArea;
import com.gryphpoem.game.zw.resource.domain.s.StaticArea;
import com.gryphpoem.game.zw.resource.util.MapHelper;
import com.gryphpoem.game.zw.service.activity.RamadanVisitAltarService;

import java.util.List;
import java.util.Objects;

/**
 * @description:
 * @author: zhou jie
 * @time: 2021/7/21 15:23
 */
public class Altar {

    /**
     * 坐标
     */
    private int pos;
    /**
     * 互动次数
     */
    private int interactionCount;

    public Altar() {
    }

    public Altar(int pos) {
        this.pos = pos;
    }

    /**
     * 反序列化
     * @param altar Altar pb
     */
    public Altar(CommonPb.Altar altar) {
        this.pos = altar.getPos();
        this.interactionCount = altar.getInteractionCount();
    }

    /**
     * 序列化
     * @return Altar Pb
     */
    public CommonPb.Altar ser() {
        CommonPb.Altar.Builder builder = CommonPb.Altar.newBuilder();
        builder.setPos(pos);
        builder.setInteractionCount(interactionCount);
        return builder.build();
    }

    /**
     * 获取圣坛的配置
     * @return 配置
     */
    public StaticAltarArea getConf() {
        StaticArea staticArea = StaticWorldDataMgr.getAreaMap().get(MapHelper.getAreaIdByPos(this.pos));
        if (Objects.nonNull(staticArea)) {
            StaticAltarArea config = StaticWorldDataMgr.getAltarAreaMap().get(staticArea.getOpenOrder());
            if (Objects.nonNull(config)) {
                return config;
            }
        }
        return null;
    }

    /**
     * 根据拜访类型获取拜访奖励
     * @param visitType 拜访类型
     * @return 拜访奖励
     */
    public List<List<Integer>> getVisitAward(int visitType) {
        StaticAltarArea conf = getConf();
        if (Objects.nonNull(conf)) {
            if (visitType == RamadanVisitAltarService.VISIT_ALTAR_TYPE_OIL) {
                return conf.getGoldAward();
            } else if (visitType == RamadanVisitAltarService.VISIT_ALTAR_TYPE_GOLD) {
                return conf.getDiamondAward();
            }
        }
        return null;
    }

    /**
     * 是否达到互动次数上限
     * 圣坛没有拜访次数
     * @return true 已达到 false 未达到
     */
    @Deprecated
    public boolean isInteractionMax() {
        StaticAltarArea conf = getConf();
        if (Objects.nonNull(conf)) {
            return interactionCount >= conf.getUpperLimit();
        }
        return false;
    }

    /**
     * 增加已互动的次数
     */
    @Deprecated
    public void addInteractionCount() {
        this.interactionCount++;
    }

    /**
     * 在当前区域的上半部分
     * @return 1 上半部分 2 下半部分
     */
    public int isAreaTop() {
        return MapHelper.getRangeBlockByPos(MapHelper.getRangeBlockByPos(this.pos)) > 50 ? MapHelper.UP_HALF_IN_AREA: MapHelper.DOWN_HALF_IN_AREA;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }


    public int getInteractionCount() {
        return interactionCount;
    }

    public void setInteractionCount(int interactionCount) {
        this.interactionCount = interactionCount;
    }
}
