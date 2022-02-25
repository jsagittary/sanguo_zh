package com.gryphpoem.game.zw.resource.pojo.world;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.WorldConstant;
import com.gryphpoem.game.zw.resource.util.CheckNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author TanDonghai
 * @ClassName Area.java
 * @Description 记录世界分区信息
 * @date 创建时间：2017年3月22日 下午7:32:21
 */
public class Area {

    private int area;
    private int status;// 分区状态，0 未开启，1 已开启，2 已开通下一级分区
    private volatile int playerNum;// 记录分区中玩家数量
    private volatile int realPlayerNum;// 真实玩家在区域中额数量
    // key: cityType + "_" + "cityId", map key: "sponsor" OR "atkList"
    private Map<String, Map<String, List<Long>>> cityFirstKill = new HashMap<>();

    public Area() {
    }

    public Area(CommonPb.Area area) {
        setArea(area.getArea());
        setStatus(area.getStatus());
        setCityFirstKillDb(area.getFirstKillList());
    }

    public int getArea() {
        return area;
    }

    public void setArea(int area) {
        this.area = area;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getPlayerNum() {
        return playerNum;
    }

    public void setPlayerNum(int playerNum) {
        this.playerNum = playerNum;
    }

    public int getRealPlayerNum() {
        return realPlayerNum;
    }

    public void setRealPlayerNum(int realPlayerNum) {
        this.realPlayerNum = realPlayerNum;
    }

    public synchronized void addPlayerNum(boolean isRobot) {
        this.playerNum++;
        if (!isRobot) {
            realPlayerNum++;
        }
    }

    public synchronized void subPlayerNum(boolean isRobot) {
        this.playerNum--;
        if (!isRobot) {
            realPlayerNum--;
        }
    }

    public boolean isOpen() {
        return status == WorldConstant.AREA_STATUS_OPEN;
    }

    public boolean canPass() {
        return status == WorldConstant.AREA_STATUS_PASS;
    }

    @Override
    public String toString() {
        return "Area [area=" + area + ", status=" + status + ", playerNum=" + playerNum + "]";
    }

    /**
     * 是否有该城池类型的首杀记录
     *
     * @param type
     * @return
     */
    public boolean isInKillList(int type) {
        for (String s : cityFirstKill.keySet()) {
            String[] infos = s.split("_");
            Integer cityType = Integer.valueOf(infos[0]);
            if (cityType.equals(type)) {
                return true;
            }
        }
        return false;
    }

    public Map<String, Map<String, List<Long>>> getCityFirstKill() {
        return cityFirstKill;
    }

    public void setCityFirstKill(Map<String, Map<String, List<Long>>> cityFirstKill) {
        this.cityFirstKill = cityFirstKill;
    }

    public void setCityFirstKillDb(List<CommonPb.CityFirstKill> cityFirstKillDb) {
        for (CommonPb.CityFirstKill firstKill : cityFirstKillDb) {
            Map<String, List<Long>> map = this.cityFirstKill.get(firstKill.getCityInfo());
            if (CheckNull.isEmpty(map)) {
                map = new HashMap<>();
                cityFirstKill.put(firstKill.getCityInfo(), map);
            }
            CommonPb.FirstKillInfo sponsor = firstKill.getSponsor();
            CommonPb.FirstKillInfo atklist = firstKill.getAtklist();
            List<Long> sponsorList = map.get(sponsor.getRole());
            if (CheckNull.isEmpty(sponsorList)) {
                sponsorList = new ArrayList<>();
                map.put(sponsor.getRole(), sponsorList);
            }
            sponsorList.addAll(sponsor.getRolesIdList());
            List<Long> atklists = map.get(atklist.getRole());
            if (CheckNull.isEmpty(atklists)) {
                atklists = new ArrayList<>();
                map.put(atklist.getRole(), atklists);
            }
            atklists.addAll(atklist.getRolesIdList());
        }
    }

    public Map<String,List<Long>> getFirstKillInfo(String cityInfo) {
        if (cityFirstKill.containsKey(cityInfo)) {
            return cityFirstKill.get(cityInfo);
        }
        return null;
    }

    public void clearFirstKillInfo() {
        this.cityFirstKill.clear();
    }
}
