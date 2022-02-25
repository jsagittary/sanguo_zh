package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.resource.constant.MedalConst;
import com.gryphpoem.game.zw.resource.dao.impl.s.StaticDataDao;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.util.random.MedalGoodsRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
* @ClassName: StaticMedalDataMgr
* @Description: 勋章相关
* @author chenqi
* @date 2018年9月11日
*
 */
public class StaticMedalDataMgr {

    private static StaticDataDao staticDataDao = DataResource.ac.getBean(StaticDataDao.class);

    // 勋章配置-- key:medalId 勋章id 
    private static Map<Integer, StaticMedal> medalMap;
    // 勋章-普通技能配置-- key:generalSkillId 普通技能id
    private static Map<Integer, StaticMedalGeneralSkill> medalGeneralSkillMap;
    // 勋章-特殊技能配置-- key:specialSkillId 特技技能id
    private static Map<Integer, StaticMedalSpecialSkill> medalSpecialSkillMap;
    // 勋章-光环技能配置-- key:auraSkillId 光环技能id
    private static Map<Integer, StaticMedalAuraSkill> medalAuraSkillMap;
    // 勋章-初始化技能权重配置-- key:medalId 勋章id  [[类型,技能id,权重值],...]  权重类型  1光环 2特技 3普通
    private static Map<Integer, List<List<Integer>>> medalSkillWeightMap;
    // 勋章-属性配置-- key:quality 品质            [[属性id,基础值,每升一级提升的值],...]
    private static Map<Integer, List<List<Integer>>> medalAttrMap;
    // 勋章-商品配置-- key：medalGoodsId 勋章商品id 
    private static Map<Integer,StaticMedalGoods> medalGoodsMap;
    // 免费的商品id
    private static Integer gratisMedalGoodsId;
    // 荣誉商品配置-- key:honorGoodsId 荣誉商品id
    private static Map<Integer,StaticHonorGoods> honorGoodsMap;
    // 勋章-捐献配置-- key:quality 品质
    private static Map<Integer,StaticMedalDonate> medalDonateMap;
 
    public static void init() {
        // 初始化 勋章配置
        Map<Integer, StaticMedal> medalMap = staticDataDao.selectMedalMap();
        StaticMedalDataMgr.medalMap = medalMap;
        
        // 初始化 勋章-普通技能 配置
        Map<Integer, StaticMedalGeneralSkill> medalGeneralSkillMap = staticDataDao.selectMedalGeneralSkillMap();
        StaticMedalDataMgr.medalGeneralSkillMap = medalGeneralSkillMap;
        
        // 初始化 勋章-特技技能 配置
        Map<Integer, StaticMedalSpecialSkill> medalSpecialSkillMap = staticDataDao.selectMedalSpecialSkillMap();
        StaticMedalDataMgr.medalSpecialSkillMap = medalSpecialSkillMap;
        
        // 初始化 勋章-光环技能 配置
        Map<Integer, StaticMedalAuraSkill> medalAuraSkillMap = staticDataDao.selectMedalAuraSkillMap();
        StaticMedalDataMgr.medalAuraSkillMap = medalAuraSkillMap;
        
        // 初始化 勋章-商品配置
        Map<Integer, StaticMedalGoods> medalGoodsMap = staticDataDao.selectMedalGoodsMap();
        StaticMedalDataMgr.medalGoodsMap = medalGoodsMap;
        
        //初始化免费商品id
        for(StaticMedalGoods goods : medalGoodsMap.values()) {
        	if(goods.getType() == MedalConst.GRATIS_GOODS_TYPE) {
        		StaticMedalDataMgr.gratisMedalGoodsId = goods.getMedalGoodsId();
        		break;
        	}
        }
		// 初始化  商品权重抽奖类
        MedalGoodsRandom.init(medalGoodsMap);

        //初始化荣誉商品配置
        Map<Integer, StaticHonorGoods> honorGoodsMap = staticDataDao.selectHonorGoodsMap();
        StaticMedalDataMgr.honorGoodsMap = honorGoodsMap;
        
        //初始化勋章捐献配置
        Map<Integer, StaticMedalDonate> medalDonateMap = staticDataDao.selectMedalDonateMap();
        StaticMedalDataMgr.medalDonateMap = medalDonateMap;
        
        //初始化 勋章-技能获取权重 配置
        List<StaticMedalSkillWeight> weightList = staticDataDao.selectMedalSkillWeightList();
        Map<Integer, List<List<Integer>>> medalSkillWeightMap = new HashMap<Integer, List<List<Integer>>>();
        for(StaticMedalSkillWeight weight : weightList) {
        	if(medalSkillWeightMap.get(weight.getMedalId()) == null) {
        		medalSkillWeightMap.put(weight.getMedalId(),new ArrayList<List<Integer>>());
        	}
        	List<Integer> list = new ArrayList<Integer>();
        	list.add(weight.getType());//类型
        	list.add(weight.getSkillId());//技能id
        	list.add(weight.getWeightValue());//权重值
        	medalSkillWeightMap.get(weight.getMedalId()).add(list);
        }
        StaticMedalDataMgr.medalSkillWeightMap = medalSkillWeightMap;
        
        //初始化 勋章-属性配置
        List<StaticMedalAttr> medalAttrList = staticDataDao.selectMedalAttrList();
        Map<Integer, List<List<Integer>>> medalAttrMap = new HashMap<Integer, List<List<Integer>>>();
        for(StaticMedalAttr attr : medalAttrList) {
        	if(medalAttrMap.get(attr.getQuality()) == null) {
        		medalAttrMap.put(attr.getQuality(),new ArrayList<List<Integer>>());
        	}
        	List<Integer> list = new ArrayList<Integer>();
        	list.add(attr.getAttrId());//属性id
        	list.add(attr.getBase());//基础值
        	list.add(attr.getGrow());//每强化一级的提升值
        	medalAttrMap.get(attr.getQuality()).add(list);
        }
        StaticMedalDataMgr.medalAttrMap = medalAttrMap;
    }
    
    /**
     * 
    * @Title: getMedalById
    * @Description: 根据 勋章id 获取勋章配置信息
    * @param medalId
    * @return StaticMedal
     */
    public static StaticMedal getMedalById(int medalId) {
    	return medalMap.get(medalId);
    }
    /**
     * 
    * @Title: getMedalDonateWeightByQuality
    * @Description: 勋章捐献-升级  根据品质获取平均权重
    * @param quality
    * @return
    * @return List<List<Integer>>
     */
    public static List<List<Integer>> getMedalDonateWeightByQuality(int quality){
    	List<List<Integer>> lists = new ArrayList<List<Integer>>();
    	for(StaticMedal medal : medalMap.values()) {
    		if(medal.getQuality() == quality) {
    			List<Integer> list = new ArrayList<Integer>();
    			list.add(medal.getMedalId());
    			list.add(1000);
    			lists.add(list);
    		}
    	}
    	return lists;
    }
    
    /**
     * 
    * @Title: getInitAttrById
    * @Description: 根据勋章 id  获取该勋章的 初始化属性权重配置
    * @param medalId
    * @return List<List<Integer>>
     */
    public static List<List<Integer>> getInitAttrById(int medalId){
    	return medalMap.get(medalId) == null ? null : medalMap.get(medalId).getInitAttr();
    }
    /**
     * 
    * @Title: getInitSkillNumById
    * @Description: 根据勋章 id  获取该勋章的 初始化技能数量权重配置
    * @param medalId
    * @return List<List<Integer>>
     */
    public static List<List<Integer>> getInitSkillNumById(int medalId){
    	return medalMap.get(medalId) == null ? null : medalMap.get(medalId).getInitSkillNum();
    }
    /**
     * 
    * @Title: getInitGeneralSkillById
    * @Description: 根据勋章 id  获取该勋章的 初始化技能 权重配置
    * @param medalId
    * @return List<List<Integer>>
     */
    public static List<List<Integer>> getInitGeneralSkillById(int medalId){
    	return medalMap.get(medalId) == null ? null : medalMap.get(medalId).getInitGeneralSkill();
    }
    
    /**
     * 
    * @Title: getGeneralSkillById
    * @Description: 根据普通技能id  获取技能配置
    * @param generalSkillId
    * @return StaticMedalGeneralSkill
     */
    public static StaticMedalGeneralSkill getGeneralSkillById(int generalSkillId) {
    	return medalGeneralSkillMap.get(generalSkillId);
    }
    /**
     * 
    * @Title: getSpecialSkillById
    * @Description: 根据特殊技能id 获取技能配置
    * @param specialSkillId
    * @return StaticMedalSpecialSkill
     */
    public static StaticMedalSpecialSkill getSpecialSkillById(int specialSkillId) {
    	return medalSpecialSkillMap.get(specialSkillId);
    }
    /**
     * 
    * @Title: getAuraSkillById
    * @Description: 根据 光环技能id 获取技能配置
    * @param auraSkillId
    * @return StaticMedalAuraSkill
     */
    public static StaticMedalAuraSkill getAuraSkillById(int auraSkillId) {
    	return medalAuraSkillMap.get(auraSkillId);
    }
    /**
     * 
    * @Title: getMedalGoodsById
    * @Description: 根据勋章商品id  获取商品
    * @param medalGoodsId
    * @return StaticMedalGoods
     */
    public static StaticMedalGoods getMedalGoodsById(int medalGoodsId) {
    	return medalGoodsMap.get(medalGoodsId);
    }
    /**
     * 
    * @Title: getHonorGoodsById
    * @Description: 根据荣誉商品id  获取商品
    * @param honorGoodsId
    * @return
    * @return StaticHonorGoods
     */
    public static StaticHonorGoods getHonorGoodsById(int honorGoodsId) {
    	return honorGoodsMap.get(honorGoodsId);
    }
    /**
     * 
    * @Title: getMedalDonateByQuality
    * @Description:根据品质 获取捐献配置
    * @param quality
    * @return
    * @return StaticMedalDonate
     */
    public static StaticMedalDonate getMedalDonateByQuality(int quality) {
    	return medalDonateMap.get(quality);
    }
    
    /**
     * 
    * @Title: getMedalSkillWeightByType
    * @Description: 根据勋章 id  获取指定技能类型的  初始化权重配置
    * @param medalId 
    * @param type 1光环 2特技 3普通
    * @return
    * @return List<List<Integer>> [[技能id,权重值],...]
     */
    public static List<List<Integer>> getMedalSkillWeightByType(int medalId,int type){
    	List<List<Integer>> lists = new ArrayList<List<Integer>>();
    	for(List<Integer> l : medalSkillWeightMap.get(medalId)) {
    		if(l.get(0) == type) {
    			List<Integer> list = new ArrayList<Integer>();
    			list.add(l.get(1));
    			list.add(l.get(2));
    			lists.add(list);
    		}
    	}
    	return lists;
    }
    /**
     * 
    * @Title: getMedalAttrConfig
    * @Description: 根据勋章 品质 和 属性id  获取 基础值 和 提升值
    * @param quality 品质
    * @param attrId 属性id
    * @return
    * @return List<Integer>   [基础值,提升值]
     */
    public static List<Integer> getMedalAttrConfig(int quality,int attrId){
    	List<Integer> list = new ArrayList<Integer>();
    	for(List<Integer> l : medalAttrMap.get(quality)) {
    		if(l.get(0) == attrId) {
    			list.add(l.get(1));
    			list.add(l.get(2));
    			break;
    		}
    	}
    	return list;
    }

	public static StaticDataDao getStaticDataDao() {
		return staticDataDao;
	}

	public static void setStaticDataDao(StaticDataDao staticDataDao) {
		StaticMedalDataMgr.staticDataDao = staticDataDao;
	}

	public static Map<Integer, StaticMedal> getMedalMap() {
		return medalMap;
	}

	public static void setMedalMap(Map<Integer, StaticMedal> medalMap) {
		StaticMedalDataMgr.medalMap = medalMap;
	}

	public static Map<Integer, StaticMedalGeneralSkill> getMedalGeneralSkillMap() {
		return medalGeneralSkillMap;
	}

	public static void setMedalGeneralSkillMap(Map<Integer, StaticMedalGeneralSkill> medalGeneralSkillMap) {
		StaticMedalDataMgr.medalGeneralSkillMap = medalGeneralSkillMap;
	}

	public static Map<Integer, StaticMedalSpecialSkill> getMedalSpecialSkillMap() {
		return medalSpecialSkillMap;
	}

	public static void setMedalSpecialSkillMap(Map<Integer, StaticMedalSpecialSkill> medalSpecialSkillMap) {
		StaticMedalDataMgr.medalSpecialSkillMap = medalSpecialSkillMap;
	}

	public static Map<Integer, StaticMedalAuraSkill> getMedalAuraSkillMap() {
		return medalAuraSkillMap;
	}

	public static void setMedalAuraSkillMap(Map<Integer, StaticMedalAuraSkill> medalAuraSkillMap) {
		StaticMedalDataMgr.medalAuraSkillMap = medalAuraSkillMap;
	}

	public static Map<Integer, List<List<Integer>>> getMedalSkillWeightMap() {
		return medalSkillWeightMap;
	}

	public static void setMedalSkillWeightMap(Map<Integer, List<List<Integer>>> medalSkillWeightMap) {
		StaticMedalDataMgr.medalSkillWeightMap = medalSkillWeightMap;
	}

	public static Map<Integer, List<List<Integer>>> getMedalAttrMap() {
		return medalAttrMap;
	}

	public static void setMedalAttrMap(Map<Integer, List<List<Integer>>> medalAttrMap) {
		StaticMedalDataMgr.medalAttrMap = medalAttrMap;
	}

	public static Map<Integer, StaticMedalGoods> getMedalGoodsMap() {
		return medalGoodsMap;
	}

	public static void setMedalGoodsMap(Map<Integer, StaticMedalGoods> medalGoodsMap) {
		StaticMedalDataMgr.medalGoodsMap = medalGoodsMap;
	}

	public static Integer getGratisMedalGoodsId() {
		return gratisMedalGoodsId;
	}

	public static void setGratisMedalGoodsId(Integer gratisMedalGoodsId) {
		StaticMedalDataMgr.gratisMedalGoodsId = gratisMedalGoodsId;
	}

	public static Map<Integer, StaticHonorGoods> getHonorGoodsMap() {
		return honorGoodsMap;
	}

	public static void setHonorGoodsMap(Map<Integer, StaticHonorGoods> honorGoodsMap) {
		StaticMedalDataMgr.honorGoodsMap = honorGoodsMap;
	}

	public static Map<Integer, StaticMedalDonate> getMedalDonateMap() {
		return medalDonateMap;
	}

	public static void setMedalDonateMap(Map<Integer, StaticMedalDonate> medalDonateMap) {
		StaticMedalDataMgr.medalDonateMap = medalDonateMap;
	}
}
