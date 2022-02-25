package com.gryphpoem.game.zw.resource.domain.p;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.gameplay.local.constant.cross.CrossFunction;
import com.gryphpoem.game.zw.gameplay.local.manger.aop.CrossGameMapDataMgr;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class CrossPlayerLocalData {

    private Map<Integer, CrossFunctionData> crossPlayerFunctionDataMap;

    public CommonPb.SaveCrossDataPb createPb(boolean isSaveDb) {
        CommonPb.SaveCrossDataPb.Builder builder = CommonPb.SaveCrossDataPb.newBuilder();
        Optional.ofNullable(crossPlayerFunctionDataMap).ifPresent(functionData -> {
            functionData.keySet().forEach(functionId -> {
                CrossFunction crossFunction = CrossFunction.convertTo(functionId);
                switch (crossFunction) {
                    case CROSS_WAR_FIRE:
                        CrossWarFireLocalData crossFunctionData = (CrossWarFireLocalData) functionData.get(functionId);
                        builder.setSaveCrossWarFireData(crossFunctionData.createDataPb());
                        break;
                    default:
                        break;
                }

            });
        });

        return builder.build();
    }

    public CrossPlayerLocalData(CommonPb.SaveCrossDataPb pb) {
        this.crossPlayerFunctionDataMap = new ConcurrentHashMap<>();
        if (pb.hasSaveCrossWarFireData()) {
            CrossWarFireLocalData crossWarFireLocalData = CrossWarFireLocalData.newCrossWarFireLocalData(pb.getSaveCrossWarFireData());
            this.crossPlayerFunctionDataMap.put(crossWarFireLocalData.getCrossFunction().getFunctionId(), crossWarFireLocalData);
        }
    }

    public CrossPlayerLocalData() {
        this.crossPlayerFunctionDataMap = new ConcurrentHashMap<>();
    }

    public int getLeaveTime(int functionId, int keyId) {
        CrossFunctionData crossFunctionData = getCrossFunctionData(CrossFunction.convertTo(functionId), keyId, false);
        return crossFunctionData.getLeaveTime();
    }

    /**
     * 进入跨服
     *
     * @param crossFunction
     * @param params
     */
    public void enterCross(CrossFunction crossFunction, int keyId, Object... params) {
        CrossFunctionData crossFunctionData = this.getCrossFunctionData(crossFunction, keyId, true);

        switch (crossFunction) {
            case CROSS_WAR_FIRE:
                CrossWarFireLocalData crossWarFireLocalData = (CrossWarFireLocalData) crossFunctionData;
                crossWarFireLocalData.setInCross(true);
                crossWarFireLocalData.setCrossFunction(crossFunction);
                crossWarFireLocalData.setPos((Integer) params[0]);
                break;
        }
    }

    public CrossFunctionData getCrossFunctionData(CrossFunction crossFunction, int keyId, boolean save) {
        CrossFunctionData crossFunctionData = this.crossPlayerFunctionDataMap.get(crossFunction.getFunctionId());
        switch (crossFunction) {
            case CROSS_WAR_FIRE:
                if (CheckNull.isNull(crossFunctionData)) {
                    crossFunctionData = new CrossWarFireLocalData(crossFunction, keyId);
                    if (save)
                        this.crossPlayerFunctionDataMap.put(crossFunction.getFunctionId(), crossFunctionData);
                } else {
                    crossFunctionData.reset(keyId);
                }
                break;
        }

        return crossFunctionData;
    }

    /**
     * 退出跨服
     *
     * @param crossFunction
     */
    public void leaveCross(CrossFunction crossFunction, int keyId, boolean save) {
        CrossFunctionData crossFunctionData = this.getCrossFunctionData(crossFunction, keyId, true);
        crossFunctionData.setInCross(false);
        if (save) {
            crossFunctionData.setLeaveTime(TimeHelper.getCurrentSecond());
        }
    }

    /**
     * 判断玩家是否参与某功能
     *
     * @param functionId
     * @return
     */
    public boolean inFunction(int functionId) {
        if (!this.crossPlayerFunctionDataMap.containsKey(functionId))
            return false;

        return crossPlayerFunctionDataMap.get(functionId).isInCross();
    }

    /**
     * 更新跨服相关数据
     *
     * @param event
     */
    public void updateData(Player player, Events.CrossPlayerChangeEvent event) {
        CrossFunctionData crossFunctionData = this.crossPlayerFunctionDataMap.
                get(event.function.getFunctionId());

        if (CheckNull.isNull(crossFunctionData)) {
            return;
        }
        if (!crossFunctionData.isInCross()) {
            return;
        }

        crossFunctionData.setUploadCrossData(event.uploadType);
        crossFunctionData.setCrossFunction(event.function);

        DataResource.getBean(CrossGameMapDataMgr.class).uploadCrossPlayer(player);
    }
}
