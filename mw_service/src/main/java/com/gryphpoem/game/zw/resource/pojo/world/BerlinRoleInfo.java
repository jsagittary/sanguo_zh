package com.gryphpoem.game.zw.resource.pojo.world;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.constant.BerlinWarConstant;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;

import java.util.HashMap;
import java.util.List;

/**
 * @author: ZhouJie
 * @date: Create in 2018-08-28 11:04
 * @description: 柏林相关数据
 * @modified By:
 */
public class BerlinRoleInfo {

    // 下次进攻CD(上阵将领复活时间) val: nextAtkTime
    private int atkCD;

    private int freeCDTime;// 免CD的时间点,在此时间点之前都是免CD的

    /**
     * 柏林玩家相关数据 key值参考{@link BerlinWarConstant.RoleInfo}
     */
    private HashMap<Integer, Integer> statusData = new HashMap<>();

    /**
     * 玩家立即出击次数 key: heroId, val: cnt
     */
    private HashMap<Integer, Integer> countData = new HashMap<>();

    public BerlinRoleInfo() {
    }

    public BerlinRoleInfo(SerializePb.SerBerlinRoleInfo info) {
        this();
        this.atkCD = info.getAtkCD();
        List<CommonPb.TwoInt> statusDataList = info.getStatusDataList();
        if (!CheckNull.isEmpty(statusDataList)) {
            statusDataList.forEach(sdl -> this.statusData.put(sdl.getV1(), sdl.getV2()));
        }
        List<CommonPb.TwoInt> countDataList = info.getCountDataList();
        if (!CheckNull.isEmpty(countDataList)) {
            countDataList.forEach(cdl -> this.countData.put(cdl.getV1(), cdl.getV2()));
        }
    }

    public int getAtkCD() {
        return atkCD;
    }

    public void setAtkCD(int atkCD) {
        this.atkCD = atkCD;
    }

    public int getFreeCDTime() {
        return freeCDTime;
    }

    public void setFreeCDTime(int freeCDTime) {
        this.freeCDTime = freeCDTime;
    }

    public int getStatus(int type) {
        return this.statusData.containsKey(type) ? this.statusData.get(type) : 0;
    }

    public int getCntByType(int type) {
        return this.countData.containsKey(type) ? this.countData.get(type) : 0;
    }

    public void updateCnt(int type, int val) {
        this.countData.put(type, val);
    }

    public void updateStatus(int type, int val) {
        this.statusData.put(type, val);
    }

    public SerializePb.SerBerlinRoleInfo.Builder ser() {
        SerializePb.SerBerlinRoleInfo.Builder builder = SerializePb.SerBerlinRoleInfo.newBuilder();
        builder.setAtkCD(atkCD);
        if (!CheckNull.isEmpty(statusData)) {
            statusData.entrySet().forEach(en -> {
                int type = en.getKey();
                builder.addStatusData(PbHelper.createTwoIntPb(type, getStatus(type)));
            });
        }
        if (!CheckNull.isEmpty(countData)) {
            countData.entrySet().forEach(en -> {
                int type = en.getKey();
                builder.addCountData(PbHelper.createTwoIntPb(type, getCntByType(type)));
            });
        }
        return builder;
    }

    public HashMap<Integer, Integer> getStatusData() {
        return statusData;
    }

}
