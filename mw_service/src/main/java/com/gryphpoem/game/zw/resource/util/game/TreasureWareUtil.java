package com.gryphpoem.game.zw.resource.util.game;

import com.alibaba.fastjson.JSON;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.pojo.attr.TreasureWareAttrItem;
import com.gryphpoem.game.zw.resource.pojo.treasureware.TreasureWare;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.RandomUtil;

import java.util.*;

/**
 * Description:
 * Author: zhangdh
 * createTime: 2022-03-02 15:01
 */
public final class TreasureWareUtil {

    /**
     * 宝具洗练, 从材料宝具中随机一条属性, 替换主宝具中的一条随机属性
     * 限制: 主宝具上同一个属性ID的属性条数最多只能有2条
     *
     * @param player   玩家
     * @param major    主宝具
     * @param material 材料宝具
     * @return 洗练属性
     */
    public static TreasureWareAttrItem train(Player player, TreasureWare major, TreasureWare material) {
        TreasureWareAttrItem trainTargetAttr = chooseMajorTreasureWareAttrItem(player, major);
        Set<Integer> multiCountSet = getMultiCountAttr(major);
        //从材料宝具上选中的属性
        TreasureWareAttrItem chooseAttr = chooseMaterialAttrItem(player, material, multiCountSet);
        //洗练好的属性(包含替换的目标属性位置)
        TreasureWareAttrItem trainAttr = chooseAttr.copy();
        trainAttr.setTrainTargetIndex(trainTargetAttr.getIndex());
        return trainAttr;
    }

    /**
     * 根据权重选中需要被洗练的属性
     *
     * @param player       player
     * @param treasureWare 宝具
     * @return 洗练目标属性
     */
    private static TreasureWareAttrItem chooseMajorTreasureWareAttrItem(Player player, TreasureWare treasureWare) {
        //属性类型系数
        Map<Integer, Integer> attrCft = Constant.TREASURE_WARE_MASTER_ATTR_COEFFICIENT;
        //属性品阶系数
        List<Integer> stageCft = Constant.TREASURE_WARE_MASTER_STAGE_COEFFICIENT;
        Map<Integer, Integer> probMap = calcAttrTrainProb(player, treasureWare, attrCft, stageCft, null);
        int index = RandomUtil.getKeyByMap(probMap, -1);
        return treasureWare.getAttrs().get(index);
    }

    /**
     * 从材料宝具中随机一条替换属性
     *
     * @param player               player
     * @param materialTreasureWare 宝具
     * @param isolateAttrIds       隔离属性ID
     * @return 随机到的属性
     */
    private static TreasureWareAttrItem chooseMaterialAttrItem(Player player, TreasureWare materialTreasureWare, Set<Integer> isolateAttrIds) {
        //属性类型系数
        Map<Integer, Integer> attrCft = Constant.TREASURE_WARE_MATERIAL_ATTR_COEFFICIENT;
        //属性品阶系数
        List<Integer> stageCft = Constant.TREASURE_WARE_MATERIAL_STAGE_COEFFICIENT;
        Map<Integer, Integer> probMap = calcAttrTrainProb(player, materialTreasureWare, attrCft, stageCft, isolateAttrIds);
        if (CheckNull.isEmpty(probMap)) {
            throw new MwException(GameError.TREASURE_WARE_TRAIN_FAIL, String.format("roleId :%d, material keyId :%d, train error",
                    player.getLordId(), materialTreasureWare.getKeyId()));
        }
        int index;
        if (probMap.size() == 1) {
            index = probMap.keySet().iterator().next();
        } else {
            index = RandomUtil.getKeyByMap(probMap, -1);
        }
        return materialTreasureWare.getAttrs().get(index);
    }


    /**
     * @param player         玩家
     * @param treasureWare   宝具
     * @param attrCft        属性类型系数
     * @param stageCft       属性阶数系数
     * @param isolateAttrIds 被隔离的属性ID (不能被随机到的属性)
     * @return KEY: 属性位置, VALUE:权重
     */
    private static Map<Integer, Integer> calcAttrTrainProb(Player player, TreasureWare treasureWare, Map<Integer, Integer> attrCft, List<Integer> stageCft, Set<Integer> isolateAttrIds) {
        Map<Integer, Integer> probMap = new HashMap<>();
        treasureWare.getAttrs().forEach((k, v) -> {
            int attId = v.getAttrId();
            if (CheckNull.nonEmpty(isolateAttrIds) && isolateAttrIds.contains(attId)) return;
            int prob1 = attrCft.getOrDefault(attId, 0);
            if (prob1 <= 0) {
                throw new MwException(GameError.CONFIG_NOT_FOUND, String.format("roleId :%d, keyId :%d, cfgId :%d, attrId :%d, not found config :%s",
                        player.getLordId(), treasureWare.getKeyId(), treasureWare.getEquipId(), attId, JSON.toJSONString(attrCft)));
            }
            if (v.getStage() > stageCft.size()) {
                throw new MwException(GameError.NO_CONFIG, String.format("roleId :%d, keyId :%d, cfgId :%d, stage :%d, not found config :%s",
                        player.getLordId(), treasureWare.getKeyId(), treasureWare.getEquipId(), v.getStage(), JSON.toJSONString(stageCft)));
            }
            int prob2 = stageCft.get(v.getStage() - 1);
            LogUtil.debug(String.format("roleId :%d, keyId :%d, attr index :%d, attrId :%d, attr stage :%d, attr id prob :%d, attr stage prob :%d",
                    player.getLordId(), treasureWare.getKeyId(), k, v.getAttrId(), v.getStage(), prob1, prob2));
            probMap.put(k, prob1 * prob2);
        });
        return probMap;
    }

    /**
     * 找出宝具条目数量大于1的属性类型
     *
     * @param treasureWare 宝具
     * @return 拥有多条属性栏目的属性ID集合
     */
    private static Set<Integer> getMultiCountAttr(TreasureWare treasureWare) {
        Map<Integer, Integer> countMap = new HashMap<>();
        Set<Integer> isolateSet = null;
        for (Map.Entry<Integer, TreasureWareAttrItem> entry : treasureWare.getAttrs().entrySet()) {
            TreasureWareAttrItem attr = entry.getValue();
            int count = countMap.merge(attr.getAttrId(), 1, Integer::sum);
            if (count > 1) {
                if (isolateSet == null) isolateSet = new HashSet<>();
                isolateSet.add(attr.getAttrId());
            }
        }
        return isolateSet;
    }
}