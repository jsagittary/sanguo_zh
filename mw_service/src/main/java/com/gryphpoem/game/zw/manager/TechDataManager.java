package com.gryphpoem.game.zw.manager;

import java.util.List;

import org.springframework.stereotype.Component;

import com.gryphpoem.game.zw.dataMgr.StaticBuildingDataMgr;
import com.gryphpoem.game.zw.resource.constant.ArmyConstant;
import com.gryphpoem.game.zw.resource.constant.AwardType.Army;
import com.gryphpoem.game.zw.resource.constant.BuildingType;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.TechConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Gains;
import com.gryphpoem.game.zw.resource.domain.p.ResourceMult;
import com.gryphpoem.game.zw.resource.domain.p.Tech;
import com.gryphpoem.game.zw.resource.domain.p.TechLv;
import com.gryphpoem.game.zw.resource.domain.s.StaticCommandMult;
import com.gryphpoem.game.zw.resource.domain.s.StaticTechLv;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

/**
 * 科技
 * 
 * @author tyler
 *
 */
@Component
public class TechDataManager {

    /**
     * 是否开启功能
     * 
     * @param player
     * @param techType
     * @param needLv
     * @return
     */
    public boolean isOpen(Player player, int techType) {
        return getTechEffect4SingleVal(player, techType) > 0 ? true : false;
    }

    /**
     * 获取某个科技等级
     * 
     * @param player
     * @param techType
     * @return
     */
    public int getTechLv(Player player, int techType) {
        Tech tech = player.tech;
        if (tech == null) {
            return 0;
        }
        TechLv techLv = tech.getTechLv().get(techType);
        return techLv != null ? techLv.getLv() : 0;
    }

    /**
     * 获取科技效果值
     * 
     * @param player
     * @param techType
     * @return
     */
    public List<Integer> getTechEffect(Player player, int techType) {
        Tech tech = player.tech;
        if (tech == null) {
            return null;
        }
        TechLv techLv = tech.getTechLv().get(techType);
        if (techLv == null) {
            return null;
        }
        StaticTechLv staticTechLv = StaticBuildingDataMgr.getTechLvMap(techLv.getId(), techLv.getLv());
        if (staticTechLv == null) {
            return null;
        }
        return staticTechLv.getEffect();
    }

    /**
     * 根据科技类型获取加成万分比(算好后的值)
     * 
     * @param player
     * @param techType
     * @return
     */
    public double getTechEffect4Single(Player player, int techType) {
        Tech tech = player.tech;
        if (tech == null) {
            return 0;
        }
        TechLv techLv = tech.getTechLv().get(techType);
        if (techLv == null) {
            return 0;
        }
        StaticTechLv staticTechLv = StaticBuildingDataMgr.getTechLvMap(techLv.getId(), techLv.getLv());
        if (staticTechLv == null || staticTechLv.getEffect() == null || staticTechLv.getEffect().isEmpty()) {
            return 0;
        }
        return staticTechLv.getEffect().get(0) / Constant.TEN_THROUSAND;
    }

    /**
     * 根据建筑类型获取募兵加成万分比(算好后的值)
     * 
     * @param player
     * @param armyType 兵种类型
     * @return
     */
    public double getTechEffect4BuildingType(Player player, int armyType) {
        int techType = 0;
        if (armyType == Army.FACTORY_1_ARM) {
            techType = TechConstant.TYPE_4;
        } else if (armyType == Army.FACTORY_2_ARM) {
            techType = TechConstant.TYPE_5;
        } else if (armyType == Army.FACTORY_3_ARM) {
            techType = TechConstant.TYPE_12;
        }
        return getTechEffect4Single(player, techType);
    }

    /**
     * 募兵消耗系数（兵营宣誓）
     * 
     * @param player
     * @param buildingType
     * @return
     */
    public int getFood4BuildingType(Player player, int buildingType) {
        int techType = 0;
        if (buildingType == BuildingType.FACTORY_1) {
            techType = TechConstant.TYPE_8;
        } else if (buildingType == BuildingType.FACTORY_2) {
            techType = TechConstant.TYPE_9;
        } else if (buildingType == BuildingType.FACTORY_3) {
            techType = TechConstant.TYPE_15;
        }
        return getTechEffectVal(player, techType, 1);
    }

    /**
     * 募兵消耗系数（根据兵类型）
     * 
     * @param player
     * @param buildingType
     * @return
     */
    public int getFood4ArmyType(Player player, int armyType) {
        int techType = 0;
        if (armyType == ArmyConstant.ARM1) {
            techType = TechConstant.TYPE_8;
        } else if (armyType == ArmyConstant.ARM2) {
            techType = TechConstant.TYPE_9;
        } else if (armyType == ArmyConstant.ARM3) {
            techType = TechConstant.TYPE_15;
        }
        return getTechEffectVal(player, techType, 1);
    }

    /**
     * 科技加成（兵营宣誓）
     * 
     * @param player
     * @param buildingType
     * @return
     */
    public int getEffect4BuildingType(Player player, int buildingType) {
        int techType = 0;
        if (buildingType == BuildingType.FACTORY_1) {
            techType = TechConstant.TYPE_8;
        } else if (buildingType == BuildingType.FACTORY_2) {
            techType = TechConstant.TYPE_9;
        } else if (buildingType == BuildingType.FACTORY_3) {
            techType = TechConstant.TYPE_15;
        }
        return getTechEffectVal(player, techType, 0);
    }
    
    /**
     * 募兵消耗系数（兵营强化）
     * 
     * @param player
     * @param buildingType
     * @return
     */
    public int getStrengthenFood4BuildingType(Player player, int buildingType) {
        int techType = 0;
        if (buildingType == BuildingType.FACTORY_1) {
            techType = TechConstant.TYPE_28;
        } else if (buildingType == BuildingType.FACTORY_2) {
            techType = TechConstant.TYPE_29;
        } else if (buildingType == BuildingType.FACTORY_3) {
            techType = TechConstant.TYPE_30;
        }
        return getTechEffectVal(player, techType, 3);
    }
    /**
     * 
    * @Title: getIntensifyLv4HeroType
    * @Description: 获取玩家兵种强化等级  根据兵种类型
    * @param player
    * @param heroType
    * @return int
     */
    public int getIntensifyLv4HeroType(Player player, int heroType) {
    	int lv = 1;//默认1级
    	int techType = 0;
    	if (heroType == 1) {
    		techType = TechConstant.TYPE_28;
    	} else if (heroType == 2) {
    		techType = TechConstant.TYPE_29;
    	} else if (heroType == 3) {
    		techType = TechConstant.TYPE_30;
    	}
    	List<Integer> list = getTechEffect(player, techType);
    	if (list != null && list.size() >= 1) {
    		lv = list.get(0);
    	}
    	return lv;
    }
    /**
     * 
    * @Title: getIntensifyRestrain4HeroType
    * @Description: 获取玩家兵种强化等级对应的  克制比
    * @param player
    * @param heroType
    * @return
    * @return int
     */
    public int getIntensifyRestrain4HeroType(Player player, int heroType) {
    	int restrain = 0;//默认1级的是无克制
    	int techType = 0;
    	if (heroType == 1) {
    		techType = TechConstant.TYPE_28;
    	} else if (heroType == 2) {
    		techType = TechConstant.TYPE_29;
    	} else if (heroType == 3) {
    		techType = TechConstant.TYPE_30;
    	}
    	List<Integer> list = getTechEffect(player, techType);
    	if (list != null && list.size() >= 5) {
    		if(list.get(0) > 1) {
    			restrain = list.get(4);
    		}
    	}
    	return restrain;
    }
    

    /**
     * 获取数组第几个值
     * 
     * @param player
     * @param techType
     * @param index
     * @return
     */
    public int getTechEffectVal(Player player, int techType, int index) {
        List<Integer> list = getTechEffect(player, techType);
        if (list == null || list.isEmpty() || list.size() - 1 < index) {
            return 0;
        }
        return list.get(index);
    }

    /**
     * 根据科技类型获取加成值
     * 
     * @param player
     * @param techType
     * @return
     */
    public int getTechEffect4SingleVal(Player player, int techType) {
        return getTechEffectVal(player, techType, 0);
    }

    public void getResourceOut4Tech(Player player, ResourceMult resMult) {
        resMult.setOilTech(getTechEffect4SingleVal(player, TechConstant.TYPE_1));
        resMult.setElecTech(getTechEffect4SingleVal(player, TechConstant.TYPE_2));
        resMult.setFoodTech(getTechEffect4SingleVal(player, TechConstant.TYPE_3));
        resMult.setOreTech(getTechEffect4SingleVal(player, TechConstant.TYPE_17));
    }

    /**
     * 是否雇佣了高级研究员
     * 
     * @param player
     * @return true雇佣了高级研究员
     */
    public boolean isAdvanceTechGain(Player player) {
        if (player == null) return false;
        Gains gains = player.gains.get(BuildingType.TECH);
        if (gains == null) return false; // 没有雇佣
        if (TimeHelper.getCurrentSecond() > gains.getEndTime()) return false; // 过期
        // 是否是高级
        StaticCommandMult commandMult = StaticBuildingDataMgr.getCommandMult(gains.getId());
        if (commandMult == null) return false;
        return commandMult.getType() == BuildingType.TECH && commandMult.getGroup() == 1;
    }
}
