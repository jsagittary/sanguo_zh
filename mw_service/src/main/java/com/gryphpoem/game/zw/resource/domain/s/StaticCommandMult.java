package com.gryphpoem.game.zw.resource.domain.s;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.manager.VipDataManager;
import com.gryphpoem.game.zw.resource.constant.BuildingType;
import com.gryphpoem.game.zw.resource.constant.SeasonConst;
import com.gryphpoem.game.zw.resource.constant.VipConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;

import java.util.List;
import java.util.Objects;

/**
 * 各个建筑部招募
 * 
 * @author tyler
 *
 */
public class StaticCommandMult {
    private int id;// 官员id
    private int type;// 建筑类型ID
    private int lv;//
    private int needBuildingType;// 招募需要建筑类型
    private int needBuildingLv;// 招募需要建筑等级
    private int lordLv;// 需要司令部等级
    private List<List<Integer>> cost;// 消耗
    private int addTime;// 持续时间(分钟)
    private List<List<Integer>> resMult;// 资源加成[[类型，Id，万分比]]
    private int effectType;// 效果类型1资源加成，2时间缩减;
    private boolean firstFree;// 是否首次招募免费(军工厂)
    private int freeTime;// 免费持续时长（秒）
    private int quality;
    private int speedTime;// 加速时间（秒）
    private int group; // 分组 0补给组，1元宝组

    public int getLordLv() {
        return lordLv;
    }

    public void setLordLv(int lordLv) {
        this.lordLv = lordLv;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }

    public int getNeedBuildingType() {
        return needBuildingType;
    }

    public void setNeedBuildingType(int needBuildingType) {
        this.needBuildingType = needBuildingType;
    }

    public int getNeedBuildingLv() {
        return needBuildingLv;
    }

    public void setNeedBuildingLv(int needBuildingLv) {
        this.needBuildingLv = needBuildingLv;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<List<Integer>> getCost() {
        return cost;
    }

    public void setCost(List<List<Integer>> cost) {
        this.cost = cost;
    }

    public int getAddTime() {
        return addTime;
    }

    public void setAddTime(int addTime) {
        this.addTime = addTime;
    }

    public List<List<Integer>> getResMult() {
        return resMult;
    }

    public void setResMult(List<List<Integer>> resMult) {
        this.resMult = resMult;
    }

    public int getEffectType() {
        return effectType;
    }

    public void setEffectType(int effectType) {
        this.effectType = effectType;
    }

    public boolean isFirstFree() {
        return firstFree;
    }

    public void setFirstFree(boolean firstFree) {
        this.firstFree = firstFree;
    }

    public int getFreeTime() {
        return freeTime;
    }

    public void setFreeTime(int freeTime) {
        this.freeTime = freeTime;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public int getSpeedTime(Player player) {
        switch (this.type) {
            case BuildingType.TECH:
                int techSpeedTime = (speedTime + (DataResource.getBean(SeasonTalentService.class).
                        getSeasonTalentEffectValue(player, SeasonConst.TALENT_EFFECT_616)));
                return techSpeedTime;
            case BuildingType.ORDNANCE_FACTORY:
                int factorySpeedTime = (speedTime + (DataResource.getBean(SeasonTalentService.class).
                        getSeasonTalentEffectValue(player, SeasonConst.TALENT_EFFECT_617)));
                // VIP特权减少打造时间
                VipDataManager vipDataManager = DataResource.ac.getBean(VipDataManager.class);
                if (Objects.nonNull(vipDataManager)) {
                    int speedForgeTime = vipDataManager.getNum(player.lord.getVip(), VipConstant.EQUIP_FORGE);
                    if (speedForgeTime > 0) {
                        factorySpeedTime += speedForgeTime;
                    }
                }
                return factorySpeedTime;
            default:
                return speedTime;
        }
    }

    public void setSpeedTime(int speedTime) {
        this.speedTime = speedTime;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

}
