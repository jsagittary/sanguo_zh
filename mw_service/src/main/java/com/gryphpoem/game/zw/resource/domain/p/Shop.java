package com.gryphpoem.game.zw.resource.domain.p;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.gryphpoem.game.zw.resource.util.TimeHelper;

/**
 * 科技
 * 
 * @author tyler
 *
 */
public class Shop {
    private Map<Integer, Integer> idCnt;// 特价商城每日刷新次数(商品ID，购买次数)
    private List<Integer> vipId; // 已购买VIP礼包
    private Map<Integer, Integer> offId; // 折扣商品ID,折扣率
    private int refreshTime;// 每日更新折扣率
    private Map<Integer, Integer> freeCnt;// 免费次数商城每日刷新次数(商品ID，购买次数)

    public Shop() {
        idCnt = new ConcurrentHashMap<>();
        freeCnt = new ConcurrentHashMap<>();
        vipId = new ArrayList<>();
        offId = new HashMap<>();
//        refreshTime = TimeHelper.getCurrentSecond();
    }

    public Map<Integer, Integer> getIdCnt() {
        return idCnt;
    }

    public void setIdCnt(Map<Integer, Integer> idCnt) {
        this.idCnt = idCnt;
    }

    public List<Integer> getVipId() {
        return vipId;
    }

    public void setVipId(List<Integer> vipId) {
        this.vipId = vipId;
    }

    public Map<Integer, Integer> getOffId() {
        return offId;
    }

    public void setOffId(Map<Integer, Integer> offId) {
        this.offId = offId;
    }

    public int getRefreshTime() {
        return refreshTime;
    }

    public void setRefreshTime(int refreshTime) {
        this.refreshTime = refreshTime;
    }

    public Map<Integer, Integer> getFreeCnt() {
        return freeCnt;
    }

    public void setFreeCnt(Map<Integer, Integer> freeCnt) {
        this.freeCnt = freeCnt;
    }

}