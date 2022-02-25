package com.gryphpoem.game.zw.gameplay.local.world.map;

import com.gryphpoem.game.zw.gameplay.local.util.dto.AttackParamDto;
import com.gryphpoem.game.zw.gameplay.local.world.WorldEntityType;
import com.gryphpoem.game.zw.pb.CommonPb;

/**
 * @program: civilization_zh
 * @description:
 * @author: zhou jie
 * @create: 2021-01-28 15:13
 */
public class WFSafeAreaMapEntity extends BaseWorldEntity {

    /**
     * 自定义安全区id
     */
    private int safeId;
    /**
     * 安全区块
     */
    private int cellId;
    /**
     * 所属阵营
     */
    private int camp;

    public int getCamp() {
        return camp;
    }

    public void setCamp(int camp) {
        this.camp = camp;
    }

    public int getSafeId() {
        return safeId;
    }

    public void setSafeId(int safeId) {
        this.safeId = safeId;
    }

    public int getCellId() {
        return cellId;
    }

    public void setCellId(int cellId) {
        this.cellId = cellId;
    }

    public WFSafeAreaMapEntity(int pos) {
        super(pos, WorldEntityType.WAR_FIRE_SAFE_AREA);
    }

    @Override
    public void attackPos(AttackParamDto param) {

    }

    public CommonPb.AreaSafe toAreaSafePb() {
        CommonPb.AreaSafe.Builder builder = CommonPb.AreaSafe.newBuilder();
        builder.setSafeId(safeId);
        builder.setCamp(camp);
        builder.setCellId(cellId);
        builder.setPos(pos);
        return builder.build();
    }
}