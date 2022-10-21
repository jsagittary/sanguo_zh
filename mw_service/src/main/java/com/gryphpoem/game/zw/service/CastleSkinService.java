package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.dataMgr.StaticLordDataMgr;
import com.gryphpoem.game.zw.gameplay.local.manger.CrossWorldMapDataManager;
import com.gryphpoem.game.zw.manager.DressUpDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.GamePb4.*;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.AwardType;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticCastleSkin;
import com.gryphpoem.game.zw.resource.domain.s.StaticCastleSkinStar;
import com.gryphpoem.game.zw.resource.pojo.dressup.BaseDressUpEntity;
import com.gryphpoem.game.zw.resource.pojo.dressup.CastleSkinEntity;
import com.gryphpoem.game.zw.resource.util.CalculateUtil;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.LogLordHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author QiuKun
 * @ClassName CastleSkinService.java
 * @Description 城堡皮肤
 * @date 2019年4月14日
 */
@Component
public class CastleSkinService {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private CrossWorldMapDataManager crossWorldMapDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private DressUpDataManager dressUpDataManager;

    /**
     * 获取城堡拥有皮肤
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GetCastleSkinRs getCastleSkin(long roleId, GetCastleSkinRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        GetCastleSkinRs.Builder builder = GetCastleSkinRs.newBuilder();
//        builder.addAllSkinIds(player.getOwnCastleSkin());
//        builder.setCurSkin(player.getCurCastleSkin()).addAllTimerSkin(PbHelper.createTwoIntListByMap(player.getOwnCastleSkinTime()));
//        builder.addAllSkinStar(PbHelper.createTwoIntListByMap(player.getOwnCastleSkinStar()));
        return builder.build();
    }

    /**
     * 更换城堡皮肤
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public ChangeCastleSkinRs changeCastleSkin(long roleId, ChangeCastleSkinRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Map<Integer, BaseDressUpEntity> castleSkinMap = dressUpDataManager.getDressUpByType(player, AwardType.CASTLE_SKIN);
        int skinId = req.getSkinId();
        if (skinId != StaticCastleSkin.DEFAULT_SKIN_ID) {
            StaticCastleSkin staticCastleSkin = StaticLordDataMgr.getCastleSkinMapById(skinId);
            if (staticCastleSkin == null) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "roleId:", roleId, "城堡皮肤未配置 skinId:", skinId);
            }
            if (!castleSkinMap.containsKey(skinId)) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), "roleId:", roleId, "未拥有该城堡皮肤:", skinId);
            }
        }
        int curCastleSkin = player.getCurCastleSkin();
        if (curCastleSkin != skinId) {
            player.setCurCastleSkin(skinId);
            dressUpDataManager.pushPlayerCastleSkinChange(player);
//            changeCastleSkinEffectHeroAttr(player,curCastleSkin,skinId,false);
            CalculateUtil.reCalcBattleHeroAttr(player);
        }
        ChangeCastleSkinRs.Builder builder = ChangeCastleSkinRs.newBuilder();
        return builder.build();
    }

    /**
     * 获得皮肤的采集加成
     *
     * @param player
     * @param mineType
     * @return
     */
    public static double getSkinCollectEffect(Player player, int mineType) {
        int curCastleSkinId = player.getCurCastleSkin();
        StaticCastleSkin castleSkinCfg = StaticLordDataMgr.getCastleSkinMapById(curCastleSkinId);
        if (castleSkinCfg == null || castleSkinCfg.getEffectType() != StaticCastleSkin.EFFECT_TYPE_COLLECT
                || !castleSkinCfg.getEffectParam().contains(mineType)) {
            return 0.0;
        }
        return castleSkinCfg.getEffectVal() / Constant.TEN_THROUSAND;
    }

    /**
     * 切换皮肤触发增加或者减少上阵hero属性
     */
    private void changeCastleSkinEffectHeroAttr(Player player, int beforeSkinId, int currentSkinId, boolean upStar) {
        StaticCastleSkin beforeSkin = StaticLordDataMgr.getCastleSkinMapById(beforeSkinId);
        StaticCastleSkin currentSkin = StaticLordDataMgr.getCastleSkinMapById(currentSkinId);
        /** 之前不是 3 effectType类型 现在是 3 effectType类型  ==> 增加将领属性 */
        boolean add = beforeSkin.getEffectType() != StaticCastleSkin.EFFECT_TYPE_HERO_ATTR
                && currentSkin.getEffectType() == StaticCastleSkin.EFFECT_TYPE_HERO_ATTR;
        /** 之前是 3 effectType类型 现在不是 3 effectType类型 ==> 减少将领属性 */
        boolean minus = beforeSkin.getEffectType() == StaticCastleSkin.EFFECT_TYPE_HERO_ATTR
                && currentSkin.getEffectType() != StaticCastleSkin.EFFECT_TYPE_HERO_ATTR;
        if (add || minus) {
            LogUtil.common("切换皮肤 id: 3 触发调整上阵hero属性 roleId: ", player.roleId, " 增加: ", add, " 减少:", minus);
            // 重新计算 将领属性
//            player.getAllOnBattleHeros().forEach(h -> CalculateUtil.processAttr(player,h));

        } else if (upStar) {
            LogUtil.common("皮肤升星, 触发调整上阵hero属性 roleId: ", player.roleId);
            player.getAllOnBattleHeros().forEach(h -> CalculateUtil.processAttr(player, h));
        }
    }

    static final private int DEFAULT_SKIN_ID = 1;

    @Deprecated
    public void loginAfter(Player player) {
        if (!player.getOwnCastleSkin().contains(DEFAULT_SKIN_ID)) {
            player.getOwnCastleSkin().add(DEFAULT_SKIN_ID);
        }

        //设置初始星数
        player.getOwnCastleSkin().forEach(skinId -> {//修正数据，预防配表修改数据错乱
            if (!player.getOwnCastleSkinStar().containsKey(skinId) || player.getCastleSkinStarById(skinId) < StaticLordDataMgr.getCastleSkinMapById(skinId).getStar()) {
                player.getOwnCastleSkinStar().put(skinId, StaticLordDataMgr.getCastleSkinMapById(skinId).getStar());
            }
        });
    }

    /**
     * 升星
     *
     * @param roleId
     * @param req
     * @return
     */
    public CastleSkinStarUpRs starUp(long roleId, CastleSkinStarUpRq req) throws MwException {
        int skinId = req.getSkinId();
        int count = req.getCount();
        if (count <= 0) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "roleId:", roleId, "客户端参数错误", skinId, count);
        }
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        StaticCastleSkin staticCastleSkin = StaticLordDataMgr.getCastleSkinMapById(skinId);
        if (staticCastleSkin == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "roleId:", roleId, "城堡皮肤未配置 skinId:", skinId);
        }

        Map<Integer, BaseDressUpEntity> castleSkinMap = dressUpDataManager.getDressUpByType(player, AwardType.CASTLE_SKIN);
        if (!castleSkinMap.containsKey(skinId)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "roleId:", roleId, "未拥有该城堡皮肤:", skinId);
        }
        CastleSkinEntity castleSkinEntity = (CastleSkinEntity) castleSkinMap.get(skinId);
        if (Objects.isNull(castleSkinEntity)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "roleId:", roleId, "未拥有该城堡皮肤:", skinId);
        }
        // 非永久装扮, 不能升星
        if (!castleSkinEntity.isPermanentHas()) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "roleId:", roleId, "城堡皮肤未永久获取", skinId);
        }
        int currStar = castleSkinEntity.getStar();
        StaticCastleSkinStar nextStarConfig = StaticLordDataMgr.getCastleSkinStarById(skinId * 100 + (currStar + 1));
        if (nextStarConfig == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "roleId:", roleId, "城堡皮肤已达到最高星", skinId, currStar);
        }
        CastleSkinStarUpRs.Builder resp = CastleSkinStarUpRs.newBuilder();
        resp.setSkinId(skinId);

        List<List<Integer>> nextConsume = new ArrayList<>();
        nextStarConfig.getConsume().forEach(o -> {
            List<Integer> tmp = new ArrayList<>();
            tmp.addAll(o);
            nextConsume.add(tmp);
        });
        nextConsume.get(0).set(2, count);
        rewardDataManager.checkAndSubPlayerRes(player, nextConsume, AwardFrom.CASTLE_SKIN_STAR_UP, skinId, currStar);

        boolean up = false;

        int totalProb = nextStarConfig.getBaseprob() * count;
        if (RandomHelper.isHitRangeIn10000(totalProb)) {
            up = true;
            castleSkinEntity.setStar(nextStarConfig.getStar());

            resp.setStar(nextStarConfig.getStar());
            resp.setResult(1);

            //更新属性加成
//            changeCastleSkinEffectHeroAttr(player,skinId,skinId,true);

            CalculateUtil.reCalcBattleHeroAttr(player);

            dressUpDataManager.pushPlayerCastleSkinChange(player);
        } else {
            resp.setStar(currStar);
        }
        // 皮肤升星埋点
        LogLordHelper.commonLog("castleSkinStarUp", AwardFrom.CASTLE_SKIN_STAR_UP, player, skinId, count, up, currStar);
        return resp.build();
    }

    /**
     * 是否拥有该皮肤
     *
     * @param player 玩家对象
     * @param skinId 皮肤id
     * @return true 拥有 false 未拥有
     */
    public boolean checkSkinHaving(Player player, int skinId) {
        Map<Integer, BaseDressUpEntity> castleSkinMap = dressUpDataManager.getDressUpByType(player, AwardType.CASTLE_SKIN);
        if (CheckNull.isEmpty(castleSkinMap)) {
            return false;
        }
        // 装扮entity
        BaseDressUpEntity dressUpEntity = castleSkinMap.get(skinId);
        return Objects.nonNull(dressUpEntity) && dressUpEntity.isPermanentHas();
    }

}
