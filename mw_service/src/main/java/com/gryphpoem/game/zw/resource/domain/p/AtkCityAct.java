package com.gryphpoem.game.zw.resource.domain.p;

import java.util.*;

/**
 * @Author: ZhouJie
 * @Date: Create in 2018-05-07 15:43
 * @Description: 攻城活动
 * @Modified By:
 */
public class AtkCityAct {

    /** 达成条件值 */
    private Map<String, Integer> tankTypes = new HashMap<String, Integer>();

    /** 参与击飞4座至少8级的玩家基地 key1:类型 key2:玩家基地等级*/
    private Map<Integer, Map<Integer, Integer>> typeCnt = new HashMap<Integer, Map<Integer, Integer>>();

    /** 类型，达成条件值 */
    private Map<Integer, Integer> status = new HashMap<Integer, Integer>();

    /** 领取状态 key: keyId, value: count */
    private Map<Integer, Integer> statusCnt = new HashMap<>();

    /** 可以领取 */
    private Set<Integer> canRecvKeyId = new HashSet<>();

    public Map<String, Integer> getTankTypes() {
        return tankTypes;
    }

    public void setTankTypes(Map<String, Integer> tankTypes) {
        this.tankTypes = tankTypes;
    }

    public Map<Integer, Map<Integer, Integer>> getTypeCnt() {
        return typeCnt;
    }

    public void setTypeCnt(Map<Integer, Map<Integer, Integer>> typeCnt) {
        this.typeCnt = typeCnt;
    }

    public Set<Integer> getCanRecvKeyId() {
        return canRecvKeyId;
    }

    public void setCanRecvKeyId(Set<Integer> canRecvKeyId) {
        this.canRecvKeyId = canRecvKeyId;
    }

    public Map<Integer, Integer> getTypeMap(int type) {
        Map<Integer, Integer> map = typeCnt.get(type);
        if (map == null) {
            map = new HashMap<>();
            typeCnt.put(type, map);
        }
        return typeCnt.get(type);
    }

    public Map<Integer, Integer> getStatus() {
        return status;
    }

    public void setStatus(Map<Integer, Integer> status) {
        this.status = status;
    }

    public Map<Integer, Integer> getStatusCnt() {
        return statusCnt;
    }

    public void setStatusCnt(Map<Integer, Integer> statusCnt) {
        this.statusCnt = statusCnt;
    }

    public void clear() {
        tankTypes.clear();
        typeCnt.clear();
        status.clear();
        statusCnt.clear();
        canRecvKeyId.clear();
    }
}
