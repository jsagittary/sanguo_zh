package com.gryphpoem.game.zw.util;

import java.util.ArrayList;
import java.util.List;

import com.hundredcent.game.ai.util.CheckNull;
import com.gryphpoem.game.zw.dataMgr.StaticFightDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticNpcDataMgr;
import com.gryphpoem.game.zw.mgr.PlayerMgr;
import com.gryphpoem.game.zw.model.CrossPlayer;
import com.gryphpoem.game.zw.model.fort.Fortress;
import com.gryphpoem.game.zw.model.fort.RoleForce;
import com.gryphpoem.game.zw.model.player.CrossHero;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.CrossHeroPb;
import com.gryphpoem.game.zw.pb.CommonPb.CrossPlanePb;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.PlaneConstant;
import com.gryphpoem.game.zw.resource.domain.s.StaticFightSkill;
import com.gryphpoem.game.zw.resource.domain.s.StaticNpc;
import com.gryphpoem.game.zw.resource.pojo.fight.AttrData;
import com.gryphpoem.game.zw.resource.pojo.fight.Fighter;
import com.gryphpoem.game.zw.resource.pojo.fight.Force;
import com.gryphpoem.game.zw.resource.pojo.fight.NpcForce;
import com.gryphpoem.game.zw.resource.pojo.fight.PlaneFightSkill;
import com.gryphpoem.game.zw.resource.pojo.fight.PlaneInfo;
import com.gryphpoem.game.zw.server.CrossServer;

/**
 * @ClassName FightHelper.java
 * @Description
 * @author QiuKun
 * @date 2019年5月14日
 */
public class CrossFightHelper {

    /**
     * 战报的生产
     * 
     * @param fortess
     * @param attacker
     * @param defender
     * @param now
     * @param isWin
     * @return
     */
    public static CommonPb.CrossWarReport toCrossWarReport(Fortress fortess, Fighter attacker, Fighter defender,
            int now, boolean isWin) {
        CommonPb.CrossWarReport.Builder builder = CommonPb.CrossWarReport.newBuilder();
        builder.setAtkMan(toCrossWarRptMan(attacker));
        builder.setDefMan(toCrossWarRptMan(defender));
        builder.setFortId(fortess.getId());
        builder.setIsWin(isWin);
        builder.setTime(now);
        return builder.build();
    }

    public static CommonPb.CrossWarRptMan toCrossWarRptMan(Fighter fighter) {
        CommonPb.CrossWarRptMan.Builder builder = CommonPb.CrossWarRptMan.newBuilder();
        PlayerMgr playerMgr = CrossServer.ac.getBean(PlayerMgr.class);
        if (fighter.roleType == Constant.Role.PLAYER) {
            List<Force> forces = fighter.getForces();
            if (!CheckNull.isEmpty(forces)) {
                Force f = forces.get(0);
                CrossPlayer player = playerMgr.getPlayer(f.ownerId);
                if (player != null) {
                    builder.setRoleId(player.getLordId());
                    builder.setSelfServerId(player.getLordModel().getSelfServerId());
                    builder.setMainServerId(player.getMainServerId());
                    builder.setHeroId(f.id);
                    builder.setCamp(player.getCamp());
                    builder.setLost(fighter.getLost());
                    builder.setNick(player.getLordModel().getNick());
                }
            }
        } else {// NPC
            builder.setIsNpc(true);
            builder.setLost(fighter.getLost());
        }
        return builder.build();
    }

    /**
     * 玩家 Fighter
     * 
     * @param roleForce
     * @return
     */
    public static Fighter createRoleForceFighter(RoleForce roleForce) {
        Fighter fighter = new Fighter();
        fighter.roleType = Constant.Role.PLAYER;
        fighter.addForce(createHeroForce(roleForce.getCrossHero(), roleForce.getCamp()));
        return fighter;
    }

    /**
     * 创建Npc Fighter
     * 
     * @param npcForce
     * @return
     */
    public static Fighter createNpcFighter(List<NpcForce> npcForce) {
        Fighter fighter = new Fighter();
        fighter.roleType = Constant.Role.CITY;
        for (NpcForce f : npcForce) {
            if (f.alive()) {
                fighter.addForce(createNpcForce(f));
            }
        }
        return fighter;
    }

    /**
     * 创建NPC的Force
     *
     * @param npcId
     * @param count
     * @return
     */
    public static Force createNpcForce(NpcForce npcForce) {
        return createNpcForce(npcForce.getNpcId(), npcForce.getHp());
    }

    /**
     * 创建NPC的Force
     * 
     * @param npcId
     * @param count
     * @return
     */
    public static Force createNpcForce(int npcId, int count) {
        StaticNpc npc = StaticNpcDataMgr.getNpcMap().get(npcId);
        AttrData attrData = new AttrData(npc.getAttr());
        return new Force(attrData, npc.getArmType(), count, attrData.lead, npcId, attrData.lead, npc.getLine());
    }

    /**
     * 根据英雄战斗单位
     * 
     * @param hero
     * @return
     */
    public static Force createHeroForce(CrossHero hero, int camp) {
        Force force = new Force(new AttrData(hero.getAttrMap()), hero.getHeroType(), hero.getCount(), hero.getLead(),
                hero.getHeroId(), hero.getLordId());
        force.roleType = Constant.Role.PLAYER;
        force.camp = camp;
        force.ownerId = hero.getLordId();
        // 兵种加成 及 克制比
        force.setIntensifyLv(hero.getIntensifyLv());
        force.setEffect(hero.getRestrain());
        // 战机的添加处理
        addPlaneInfo(hero, force);
        return force;
    }

    private static void addPlaneInfo(CrossHero hero, Force force) {
        CrossHeroPb msg = hero.getMsg();
        if (msg.getPlaneListCount() > 0) {
            for (CrossPlanePb temp : msg.getPlaneListList()) {
                // 入场技能
                if (temp.getEnterSkillId() > 0) {
                    PlaneInfo planeInfo = new PlaneInfo();
                    planeInfo.setPlaneId(temp.getPlaneId());
                    planeInfo.setSkillId(temp.getEnterSkillId());
                    planeInfo.setUseSkill(false);
                    force.planeInfos.put(temp.getBattlePos(), planeInfo);
                }
                // 专业技能
                if (temp.getMentorSkillId() > 0 && temp.getMentorSkillCnt() > 0) {
                    StaticFightSkill fs = StaticFightDataMgr.getFightSkillMapById(temp.getMentorSkillId());
                    PlaneFightSkill planeSkill = new PlaneFightSkill(fs);
                    planeSkill.setPlaneId(temp.getPlaneId());
                    planeSkill.param.put(PlaneConstant.SkillParam.MAX_RELEASE_CNT, temp.getMentorSkillCnt());
                    force.fightSkill.putIfAbsent(temp.getBattlePos(), new ArrayList<>()).add(planeSkill);
                }
            }
        }
    }

}
