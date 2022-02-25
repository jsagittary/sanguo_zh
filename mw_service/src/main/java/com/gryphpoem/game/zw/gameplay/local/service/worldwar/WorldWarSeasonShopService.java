package com.gryphpoem.game.zw.gameplay.local.service.worldwar;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.dataMgr.StaticCrossWorldDataMgr;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.GamePb5.WorldWarSeasonShopGoodsRs;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.AwardType;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.constant.PropConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticWorldWarShop;
import com.gryphpoem.game.zw.resource.pojo.Equip;
import com.gryphpoem.game.zw.resource.pojo.Prop;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.RandomUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by pengshuo on 2019/3/25 11:51
 * <br>Description: 世界争霸-世界阵营-赛季商店
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
@Service
public class WorldWarSeasonShopService {

    @Autowired
    private PlayerDataManager playerDataManager;

    @Autowired
    private RewardDataManager rewardDataManager;

    /**
     * 获取玩家赛季商店记录
     * @param lordId
     * @return
     */
    public Map<Integer,Integer> getPersonalSeasonShopData(long lordId) throws MwException{
        Player player = playerDataManager.checkPlayerIsExist(lordId);
        return player.getPlayerWorldWarData().getSeasonShop();
    }

    /**
     * 购买赛季商店物品
     * @param lordId 玩家
     * @param keyId 商品keyId
     */
    public WorldWarSeasonShopGoodsRs buySeasonShopEquip(long lordId, int keyId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(lordId);
        Map<Integer, Integer> seasonShop = Optional.ofNullable(getPersonalSeasonShopData(lordId)).orElse(new HashMap<>(2));
        // 已兑换数量
        int hasChange = Optional.ofNullable(seasonShop.get(keyId)).orElse(0);
        StaticWorldWarShop goods = StaticCrossWorldDataMgr.getStaticWorldWarShop(keyId);
        if (goods == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "找不到配置, roleId: ", lordId," keyId: ",keyId);
        }
        int max = goods.getBuyCnt();
        List<List<Integer>> awards = goods.getAward();
        // 是否可兑换
        if(max != 0 && hasChange >= max){
            throw new MwException(GameError.PROMOTION_GIFT_MAX.getCode(), "领取奖励已达上限, roleId: ", lordId);
        }
        if(awards == null || awards.isEmpty()){
            throw new MwException(GameError.NO_CONFIG.getCode(), "awardList 找不到配置, roleId: ", lordId," keyId: ",keyId);
        }
        // 根据权重获取商品
        List<Integer> award = getWeightAward(awards);
        if(award == null || award.isEmpty()){
            throw new MwException(GameError.NO_CONFIG.getCode(), "award 找不到, roleId: ", lordId," keyId: ",keyId);
        }
        // 扣除对应的道具,并且向客户端同步 [type,id,cnt]
        List<Integer> subList = Arrays.asList(AwardType.PROP,PropConstant.EQUIP_RING_EXCHANGE_PROOF,goods.getCostPoint());
        rewardDataManager.checkAndSubPlayerRes(player,Arrays.asList(subList),AwardFrom.WORLD_WAR_EQUIP_EXCHANGE,keyId);
        // 保存以获取状态
        seasonShop.put(keyId,++hasChange);
        player.getPlayerWorldWarData().setSeasonShop(seasonShop);
        // 玩家获得装备 "[type,id,cnt]" 发放奖励
        int type = award.get(0);
        int id = award.get(1);
        int cnt = award.get(2);
        // 返回结果
        WorldWarSeasonShopGoodsRs.Builder builder = WorldWarSeasonShopGoodsRs.newBuilder();
        if (type == AwardType.EQUIP) {
            List<Integer> keyIds = rewardDataManager.addEquip(player, id, cnt, AwardFrom.WORLD_WAR_EQUIP_EXCHANGE, keyId);
            // 装备属性
            if(keyIds != null && !keyIds.isEmpty()){
                Equip playerEquip = getPlayerEquip(player, keyIds.get(0));
                if(playerEquip != null){
                    builder.setEquip(PbHelper.createEquipPb(playerEquip));
                }
            }
        } else {
            rewardDataManager.addAward(player, type, id, cnt, AwardFrom.WORLD_WAR_EQUIP_EXCHANGE, keyId);
        }
        // 奖励
        builder.addAward(PbHelper.createAwardPb(type, id, cnt));
        builder.setKeyId(keyId);
        return builder.build();
    }

    /**
     * 获取玩家装备
     * @param player
     * @param equipId
     * @return
     */
    private Equip getPlayerEquip(Player player,int equipId){
        return player.equips.get(equipId);
    }

    /**
     * 获取玩家装备兑换券数量
     * @param player
     * @return
     */
    public int playerEquipExchangeProof(Player player){
        Prop prop = player.props.get(PropConstant.EQUIP_RING_EXCHANGE_PROOF);
        if (null != prop) {
            return prop.getCount();
        }
        return 0;
    }

    /***
     * 根据权重获取物品 （[type,id,cnt,weight]）
     * @param awards
     * @return
     */
    private List<Integer> getWeightAward(List<List<Integer>> awards){
        List<List<Integer>> collect = awards.stream().filter(list -> list != null && list.size() > 3)
                .map(list -> Arrays.asList(list.get(1), list.get(3))).collect(Collectors.toList());
        Integer keyId = RandomUtil.getRandomByWeight(collect);
        return awards.stream().
                filter(list -> list != null && list.size() > 3 && list.get(1).equals(keyId))
                .findFirst().orElse(null);
    }
}
