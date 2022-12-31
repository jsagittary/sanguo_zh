package com.gryphpoem.game.zw.resource.util;

import com.gryphpoem.game.zw.dataMgr.StaticHeroDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticPropDataMgr;
import com.gryphpoem.game.zw.manager.BuildingDataManager;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.ActivityTask;
import com.gryphpoem.game.zw.resource.domain.s.StaticStone;
import com.gryphpoem.game.zw.resource.pojo.Stone;
import com.gryphpoem.game.zw.resource.pojo.StoneHole;
import com.gryphpoem.game.zw.resource.pojo.SuperEquip;
import com.gryphpoem.game.zw.resource.pojo.activity.ETask;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.treasureware.TreasureWare;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-10-27 17:32
 */
public class ActTaskUtil {

    public static boolean updTaskSchedule(Player player, ActivityTask activityTask, ETask eTask, List<Integer> cfgParams, int... params) {
        boolean b = false;
        Hero hero;
        int progress = activityTask.getProgress();
        switch (eTask) {
            case FIGHT_REBEL:
            case FIGHT_ELITE_REBEL:
                if (cfgParams.size() < 2) return false;
                if (params[1] >= cfgParams.get(0)) {
                    if (cfgParams.size() == 2 || params[1] <= cfgParams.get(1)) {
                        b = true;
                        activityTask.setProgress(activityTask.getProgress() + 1);
                    }
                }
                break;
            case JOIN_CITY_WAR:
            case JOIN_ACTIVITY:
            case FINISHED_TASK:
            case PASS_TREASURE_WARE_COMBAT_ID:
            case UNLOCK_TREASURE_WARE_COPY_HERO_INDEX:
                if (params[0] == cfgParams.get(0)) {
                    b = true;
                    activityTask.setProgress(activityTask.getProgress() + 1);
                }
                break;
            case MAKE_QUALITY_AND_COUNT_TREASURE_WARE:
            case MAKE_QUALITY_AND_COUNT_AND_SPECIAL_TREASURE_WARE:
                if (params[0] == cfgParams.get(0)) {
                    b = true;
                    activityTask.setProgress(activityTask.getProgress() + params[1]);
                }
                break;
            case PASS_BARRIER:
            case PASS_EXPEDITION:
            case CONSUME_DIAMOND:
            case RECHARGE_DIAMOND:
            case DEATH_NUMBER:
            case KILLED_NUMBER:
            case ARMY_MAK_LOST:
            case GOLDEN_AUTUMN_CATCH_FISH:
                if (progress < cfgParams.get(0)) {
                    b = true;
                    activityTask.setProgress(progress + params[0]);
                }
                break;
            case CONSUME_ITEM:
            case COLLECT_RES:
                if (params[0] == cfgParams.get(1)) {
                    b = true;
                    activityTask.setProgress(activityTask.getProgress() + params[1]);
                }
                break;
            case BUILD_UP:
                int buildLv = BuildingDataManager.getBuildingLv(cfgParams.get(0), player);
                if (buildLv > activityTask.getProgress()) {
                    b = true;
                    activityTask.setProgress(buildLv);
                }
                break;
            case TECHNOLOGY_UP:
                int technologyLv = player.tech.getTechLvById(cfgParams.get(0));
                if (technologyLv > activityTask.getProgress()) {
                    b = true;
                    activityTask.setProgress(technologyLv);
                }
                break;
            case MAKE_EQUIP:
            case GET_TASKAWARD:
                if (params[1] == cfgParams.get(0)) {
                    b = true;
                    activityTask.setProgress(activityTask.getProgress() + 1);
                }
                break;
            case REFORM_EQUIP:
            case HERO_TRAINING:
            case DAILY_LOGIN:
            case CITY_FIRSTKILLED:
            case APPOINTMENT:
            case TRAINING_HIGH:
            case TRAINING_LOW:
            case FINISHED_DAILYTASK:
            case BUILD_CAMP:
            case HITFLY_PLAYER:
            case GOLDEN_AUTUMN_FISHING:
                b = true;
                activityTask.setProgress(activityTask.getProgress() + 1);
                break;
            case GET_ITEM:
                if (params[0] == cfgParams.get(0)) {
                    b = true;
                    activityTask.setProgress(activityTask.getProgress() + params[2]);
                }
                break;
            case ARTIFACT_UP:
                SuperEquip superEquip = player.supEquips.get(cfgParams.get(0));
                if (superEquip != null && superEquip.getLv() > activityTask.getProgress()) {
                    b = true;
                    activityTask.setProgress(superEquip.getLv());
                }
                break;
            case BEAUTY_GIFT:
                if ((cfgParams.get(0) == 0 && (cfgParams.get(1) == 0 || params[1] == cfgParams.get(1))) ||
                        (params[0] == cfgParams.get(0) && (cfgParams.get(1) == 0 || params[1] == cfgParams.get(1)))) {
                    b = true;
                    activityTask.setProgress(activityTask.getProgress() + params[2]);
                }
                break;
            case MAKE_ARMY:
                if (cfgParams.get(0) == 0 || params[0] == cfgParams.get(0)) {
                    b = true;
                    activityTask.setProgress(activityTask.getProgress() + params[1]);
                }
                break;
            case BEAUTY_INTIMACY:
                AtomicInteger femaleAgentExp = new AtomicInteger(0);
                Optional.ofNullable(player.getCia()).ifPresent(cia ->
                        Optional.ofNullable(cia.getFemaleAngets().get(cfgParams.get(0))).ifPresent(femaleAgent ->
                                femaleAgentExp.set(femaleAgent.getExp())));
                if (femaleAgentExp.get() > activityTask.getProgress()) {
                    b = true;
                    activityTask.setProgress(femaleAgentExp.get());
                }
                break;
            case ORNAMENT_COUNT:
                int stoneCount = 0;
                for (Stone stone : player.getStoneInfo().getStones().values()) {
                    StaticStone sStone = StaticPropDataMgr.getStoneMapById(stone.getStoneId());
                    if (sStone.getLv() >= cfgParams.get(0)) {
                        stoneCount += stone.getCnt();
                    }
                }
                for (StoneHole stoneHole : player.getStoneInfo().getStoneHoles().values()) {
                    StaticStone sStone = StaticPropDataMgr.getStoneMapById(stoneHole.getStoneId());
                    if (sStone.getLv() >= cfgParams.get(0)) {
                        stoneCount += 1;
                    }
                }
                if (activityTask.getProgress() < stoneCount) {
                    b = true;
                    activityTask.setProgress(stoneCount);
                }
                break;
            case TITLE_LV:
                if (player.lord.getRanks() > activityTask.getProgress()) {
                    b = true;
                    activityTask.setProgress(player.lord.getRanks());
                }
                break;
            case OWN_BEAUTY:
                long count = player.getCia().getFemaleAngets().values().stream().filter(o -> o.getStatus() == 2 && o.getStar() >= cfgParams.get(1)).count();
                if (count > activityTask.getProgress()) {
                    b = true;
                    activityTask.setProgress((int) count);
                }
                break;
            case OWN_HERO:
                int heroCount = (int) player.heros.values().stream().map(tmp -> StaticHeroDataMgr.getHeroMap().get(tmp.getHeroId())).filter(staticHero -> staticHero.getQuality() == cfgParams.get(1)).count();
                if (heroCount > activityTask.getProgress()) {
                    b = true;
                    activityTask.setProgress(heroCount);
                }
                break;
            case PLAYER_LV:
                if (player.lord.getLevel() > activityTask.getProgress()) {
                    b = true;
                    activityTask.setProgress(player.lord.getLevel());
                }
                break;
            case PLAYER_POWER:
                if (player.lord.getFight() > activityTask.getProgress()) {
                    b = true;
                    activityTask.setProgress((int) player.lord.getFight());
                }
                break;
            case TRADE_TIMES:
                if (params[0] == cfgParams.get(1)) {
                    activityTask.setProgress(activityTask.getProgress() + 1);
                }
                break;
            case GET_HERO:
                if (Objects.nonNull(player.heros.get(cfgParams.get(0)))) {
                    b = true;
                    activityTask.setProgress(activityTask.getProgress() + 1);
                }
                break;
            case HERO_UPSTAR:
                hero = player.heros.get(cfgParams.get(0));
                if (Objects.nonNull(hero)) {
                    b = true;
                    activityTask.setProgress(hero.getCgyStage());
                }
                break;
            case HERO_UPSKILL:
                hero = player.heros.get(cfgParams.get(0));
                if (Objects.nonNull(hero)) {
                    b = true;
//                    activityTask.setProgress(hero.getSkillLevels().getOrDefault(cfgParams.get(1), 0));
                }
                break;
            case HERO_LEVELUP:
                hero = player.heros.get(cfgParams.get(0));
                if (Objects.nonNull(hero)) {
                    b = true;
                    activityTask.setProgress(hero.getLevel());
                }
                break;
            case GOLDEN_AUTUMN_GET_RESOURCE:
                int resType = cfgParams.get(0);
                if (resType == 0 || resType == params[0]) {
                    activityTask.setProgress(activityTask.getProgress() + params[1]);
                }
                break;
            case STRENGTH_QUALITY_AND_COUNT_TREASURE_WARE:
                if (params[1] == cfgParams.get(0) && params[2] == cfgParams.get(1)) {
                    b = true;
                    activityTask.setProgress(activityTask.getProgress() + params[0]);
                }
                break;
            case TRAIN_QUALITY_AND_COUNT_TREASURE_WARE:
                if (params[1] == cfgParams.get(0)) {
                    b = true;
                    activityTask.setProgress(activityTask.getProgress() + params[0]);
                }
                break;
            case TRAIN_QUALITY_AND_2SAME_ANY_ATTR_TREASURE_WARE:
                TreasureWare treasureWare = player.treasureWares.get(params[0]);
                if (Objects.nonNull(treasureWare)) {
                    b = treasureWare.getSameAttrCnt(cfgParams.get(0), 2, cfgParams.get(1));
                    if (b)
                        activityTask.setProgress(activityTask.getProgress() + 1);
                    break;
                }
            default:
        }
        return b;
    }


    public static boolean checkTaskFinished(Player player, ActivityTask activityTask, ETask eTask, List<Integer> cfgParams) {
        Hero hero;
        switch (eTask) {
            case FIGHT_REBEL:
            case FIGHT_ELITE_REBEL:
                return activityTask.getProgress() >= cfgParams.get(cfgParams.size() - 1);
            case COLLECT_RES:
            case HERO_UPSKILL:
            case BEAUTY_GIFT:
            case STRENGTH_QUALITY_AND_COUNT_TREASURE_WARE:
            case TRAIN_QUALITY_AND_2SAME_ANY_ATTR_TREASURE_WARE:
                return activityTask.getProgress() >= cfgParams.get(2);
            case JOIN_CITY_WAR:
            case MAKE_EQUIP:
            case JOIN_ACTIVITY:
            case FINISHED_TASK:
            case GET_TASKAWARD:
            case HERO_LEVELUP:
            case MAKE_QUALITY_AND_COUNT_TREASURE_WARE:
            case MAKE_QUALITY_AND_COUNT_AND_SPECIAL_TREASURE_WARE:
            case TRAIN_QUALITY_AND_COUNT_TREASURE_WARE:
                return activityTask.getProgress() >= cfgParams.get(1);
            case PASS_BARRIER:
            case PASS_EXPEDITION:
            case REFORM_EQUIP:
            case CONSUME_DIAMOND:
            case TRAINING_HIGH:
            case TRAINING_LOW:
            case HERO_TRAINING:
            case KILLED_NUMBER:
            case DEATH_NUMBER:
            case ARMY_MAK_LOST:
            case GOLDEN_AUTUMN_FISHING:
            case GOLDEN_AUTUMN_CATCH_FISH:
            case HITFLY_PLAYER:
                return activityTask.getProgress() >= cfgParams.get(0);
            case CONSUME_ITEM:
            case BUILD_UP:
            case TECHNOLOGY_UP:
            case GET_ITEM:
            case ARTIFACT_UP:
            case MAKE_ARMY:
            case BEAUTY_INTIMACY:
            case DAILY_LOGIN:
            case CITY_FIRSTKILLED:
            case ORNAMENT_COUNT:
            case APPOINTMENT:
            case RECHARGE_DIAMOND:
            case TITLE_LV:
            case FINISHED_DAILYTASK:
            case OWN_BEAUTY:
            case OWN_HERO:
            case BUILD_CAMP:
            case PLAYER_LV:
            case PLAYER_POWER:
            case TRADE_TIMES:
                break;
            case GET_HERO:
            case PASS_THE_COMBAT:
            case UNLOCK_TREASURE_WARE_COPY_HERO_INDEX:
            case PASS_TREASURE_WARE_COMBAT_ID:
                return activityTask.getProgress() >= 1;
            case HERO_UPSTAR:
                hero = player.heros.get(cfgParams.get(0));
                if (Objects.nonNull(hero)) {
                    return hero.getCgyStage() > cfgParams.get(1) || (hero.getCgyStage() == cfgParams.get(1) && hero.getCgyLv() >= cfgParams.get(2));
                }
                break;
            default:
        }
        return false;
    }
}
