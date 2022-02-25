package com.gryphpoem.game.zw.resource.pojo.world;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.pb.CommonPb.BattlePO;
import com.gryphpoem.game.zw.pb.CommonPb.BattleRole;
import com.gryphpoem.game.zw.resource.constant.WorldConstant;
import com.gryphpoem.game.zw.resource.domain.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @ClassName Battle.java
 * @Description 城战、阵营战类战斗信息记录
 * @author TanDonghai
 * @date 创建时间：2017年4月12日 下午3:07:11
 *
 */
public class Battle {
    public static int battleKey = 10000;

    private int battleId;
    private int pos;
    private int type;// 战斗类型，城战、阵营战
    private boolean isAtkNpc;// 是否是攻击NPC城池
    private Player sponsor;// 发起人角色
    private Player defencer;// 城战被攻击方
    private int atkArm;// 进攻方兵力
    private int defArm;// 防守方兵力
    private int defCamp;// 防守方阵营
    private int battleTime;// 战斗开始时间
    private int beginTime;// 战斗发起时间

    // 进攻方参与玩家id(发起或加入就存入此容器, 被攻击的玩家随机的时候会通知次容器内的所有部队撤回)
    private HashSet<Long> atkRoles = new HashSet<>();
    private HashSet<Long> defRoles = new HashSet<>();

    private long sponsorId;
    private long defencerId;// 个人城池防守玩家ID
    private List<BattleRole> atkList = new ArrayList<>(); // 参与攻击的先后顺序,到达之后才会添加的army信息,主要用来战斗
    private List<BattleRole> defList = new ArrayList<>(); // 参与防守的先后顺序
    private int atkCity;// 都城发起的cityID
    private int atkCamp;// 攻方阵营
    private int atkPos; // 攻防坐标(都城自动攻城)
    private int battleType;// 1 闪电战，2 奔袭战，3 远征战 ,如果type类型是匪军叛乱此值表示s_rebel_round的id值

    private int atkHelpChatCnt;// 营地战进攻方帮助喊话次数
    private int defHelpChatCnt;// 营地战防守方帮助喊话次数

    private Map<Long, Integer> helpChatCnt = new HashMap<>(); // 这场战斗的喊话次数

    // 邀请过的玩家
    private HashSet<Long> invites = new HashSet<>();

    public Battle() {
        battleId = ++battleKey;
    }

    public Battle(BattlePO battle) {
        setBattleId(battle.getBattleId());
        setPos(battle.getPos());
        setType(battle.getType());
        setBattleTime(battle.getBattleTime());
        setAtkCity(battle.getAtkCity());
        setAtkCamp(battle.getAtkCamp());
        atkRoles.addAll(battle.getAtkRolesList());
        defRoles.addAll(battle.getDefRolesList());
        sponsorId = battle.getSponsorId();
        defencerId = battle.getDefencerId();
        atkList.addAll(battle.getAtkRoleList());
        defList.addAll(battle.getDefRoleList());
        atkPos = battle.getAtkPos();
        battleType = battle.getBattleType();
        setAtkArm(battle.getAtkArm());
        setDefArm(battle.getDefArm());
        invites.addAll(battle.getInvitesList());
        setBeginTime(battle.getBeginTime());
        setDefCamp(battle.getDefCamp());
    }

    public int getBattleType() {
        return battleType;
    }

    public void setBattleType(int battleType) {
        this.battleType = battleType;
    }

    public int getBattleId() {
        return battleId;
    }

    public void setBattleId(int battleId) {
        this.battleId = battleId;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isAtkNpc() {
        return isAtkNpc;
    }

    public void setAtkNpc(boolean isAtkNpc) {
        this.isAtkNpc = isAtkNpc;
    }

    public Player getSponsor() {
        return sponsor;
    }

    public void setSponsor(Player sponsor) {
        this.sponsor = sponsor;
    }

    public Player getDefencer() {
        return defencer;
    }

    public void setDefencer(Player defencer) {
        this.defencer = defencer;
    }

    public int getAtkArm() {
        return atkArm;
    }

    public void setAtkArm(int atkArm) {
        this.atkArm = atkArm;
    }

    public int getDefArm() {
        return defArm;
    }

    public void setDefArm(int defArm) {
        this.defArm = defArm;
    }

    public int getBattleTime() {
        return battleTime;
    }

    public void setBattleTime(int battleTime) {
        this.battleTime = battleTime;
    }

    public long getSponsorId() {
        return sponsorId;
    }

    public void setSponsorId(long sponsorId) {
        this.sponsorId = sponsorId;
    }

    public long getDefencerId() {
        return defencerId;
    }

    public void setDefencerId(long defencerId) {
        this.defencerId = defencerId;
    }

    public List<BattleRole> getAtkList() {
        return atkList;
    }

    public void setAtkList(List<BattleRole> atkList) {
        this.atkList = atkList;
    }

    public List<BattleRole> getDefList() {
        return defList;
    }

    public void setDefList(List<BattleRole> defList) {
        this.defList = defList;
    }

    public void setDefCamp(int defCamp) {
        this.defCamp = defCamp;
    }

    public int getDefCamp() {
        return defCamp;
    }

    public int getAtkCamp() {
        return sponsor != null ? sponsor.lord.getCamp() : atkCamp;
    }

    public boolean isCityBattle() {
        return type == WorldConstant.BATTLE_TYPE_CITY;
    }
    public boolean isDecisiveBattle() {
        return type == WorldConstant.BATTLE_TYPE_DECISIVE_BATTLE;
    }

    public boolean isMineGuardBattle() {
        return type == WorldConstant.BATTLE_TYPE_MINE_GUARD;
    }

    public boolean isCampBattle() {
        return type == WorldConstant.BATTLE_TYPE_CAMP;
    }

    public boolean isGestapoBattle() {
        return type == WorldConstant.BATTLE_TYPE_GESTAPO;
    }

    public boolean isLightningWar() {
        return type == WorldConstant.BATTLE_TYPE_LIGHTNING_WAR;
    }

    public boolean isAtkSuperMine() {
        return type == WorldConstant.BATTLE_TYPE_SUPER_MINE;
    }

    public boolean isRebellionBattle() {
        return type == WorldConstant.BATTLE_TYPE_REBELLION;
    }

    public boolean isCounterAtkBattle() {
        return type == WorldConstant.BATTLE_TYPE_COUNTER_ATK;
    }

    public void addAtkArm(int add) {
        this.atkArm += add;
    }

    public void addDefArm(int add) {
        this.defArm += add;
    }

    /**
     * 扣除防守方兵力
     * @param sub
     */
    public void subDefArm(int sub) {
        this.defArm -= sub;
    }

    public void updateArm(int camp, int add) {
        if (camp == getAtkCamp()) {
            this.atkArm += add;
            if (this.atkArm < 0) {
                this.atkArm = 0;
            }
        } else if (camp == getDefCamp()) {
            this.defArm += add;
            if (this.defArm < 0) {
                this.defArm = 0;
            }
        } else {
            LogUtil.error("修改兵力，阵营不正确, camp:", camp, ", atkCamp:", getAtkCamp(), ", defCamp:", getDefCamp());
        }
    }

    public void updateAtkBoss(int camp, int add) {
        if (camp != getDefCamp()) {
            this.atkArm += add;
            if (this.atkArm < 0) {
                this.atkArm = 0;
            }
        }
    }

    public int getAtkPos() {
        return sponsor != null ? sponsor.lord.getPos() : atkPos;
    }

    public String getAtkName() {
        return sponsor != null ? sponsor.lord.getNick() : "";
    }

    public String getDefName() {
        if (defencer != null) {
            return defencer.lord.getNick();
        }

        return null;
    }

    public int getAtkCity() {
        return atkCity;
    }

    public void setAtkCity(int atkCity) {
        this.atkCity = atkCity;
    }

    public void setAtkCamp(int atkCamp) {
        this.atkCamp = atkCamp;
    }

    public void setAtkPos(int atkPos) {
        this.atkPos = atkPos;
    }

    public HashSet<Long> getAtkRoles() {
        return atkRoles;
    }

    public void setAtkRoles(HashSet<Long> atkRoles) {
        this.atkRoles = atkRoles;
    }

    public HashSet<Long> getDefRoles() {
        return defRoles;
    }

    public void setDefRoles(HashSet<Long> defRoles) {
        this.defRoles = defRoles;
    }

    public int getAtkHelpChatCnt() {
        return atkHelpChatCnt;
    }

    public void setAtkHelpChatCnt(int atkHelpChatCnt) {
        this.atkHelpChatCnt = atkHelpChatCnt;
    }

    public int getDefHelpChatCnt() {
        return defHelpChatCnt;
    }

    public void setDefHelpChatCnt(int defHelpChatCnt) {
        this.defHelpChatCnt = defHelpChatCnt;
    }

    /** 获取进攻方城池等级 */
    public int getAtkCityLv() {
        if (sponsor != null) {
            return sponsor.building.getCommand();
        }
        return 0;
    }

    /** 获取防守方城池等级 */
    public int getDefCityLv() {
        if (defencer != null) {
            return defencer.building.getCommand();
        }
        return 0;
    }

    /** 获取发起者的lordId */
    public long getAtkLordId() {
        return sponsor != null ? sponsor.roleId : 0l;
    }

    /** 获取防守者的lordId */
    public long getDefLordId() {
        return defencer != null ? defencer.roleId : 0l;
    }

    public Map<Long, Integer> getHelpChatCnt() {
        return helpChatCnt;
    }

    /**
     * 获取发起者城池皮肤
     * @return 城池皮肤
     */
    public int getAtkCastleSkin() {
        if (sponsor != null) {
            return sponsor.getCurCastleSkin();
        }
        return 0;
    }

    /**
     * 获取防守者城池皮肤
     * @return 城池皮肤
     */
    public int getDefCastleSkin() {
        if (defencer != null) {
            return defencer.getCurCastleSkin();
        }
        return 0;
    }

    /**
     * 获取发起者城池皮肤星级
     * @return 皮肤星级
     */
    public int getAtkCastleSkinStar() {
        if (sponsor != null) {
            int atkCastleSkin = getAtkCastleSkin();
            if (atkCastleSkin == 0) {
                return 0;
            }
            return sponsor.getCastleSkinStarById(atkCastleSkin);
        }
        return 0;
    }

    /**
     * 获取防守者城池皮肤星级
     * @return 皮肤星级
     */
    public int getDefCastleSkinStar() {
        if (defencer != null) {
            int defCastleSkin = getDefCastleSkin();
            if (defCastleSkin == 0) {
                return 0;
            }
            return defencer.getCastleSkinStarById(defCastleSkin);
        }
        return 0;
    }

    /**
     * 是否是集结战斗
     * @return 是否需要返回给客户端集结战斗
     */
    public boolean isRallyBattleType() {
        return type == WorldConstant.BATTLE_TYPE_CITY || type == WorldConstant.BATTLE_TYPE_CAMP ||
                type == WorldConstant.BATTLE_TYPE_GESTAPO || type == WorldConstant.BATTLE_TYPE_SUPER_MINE ||
                type == WorldConstant.BATTLE_TYPE_MINE_GUARD;
    }

    /**
     * 获取进攻者头像
     * @return 头像
     */
    public int getAtkPortrait() {
        if (sponsor != null) {
            return sponsor.lord.getPortrait();
        }
        return 0;
    }


    /**
     * 获取防守者头像
     * @return 头像
     */
    public int getDefPortrait() {
        if (defencer != null) {
            return defencer.lord.getPortrait();
        }
        return 0;
    }

    public HashSet<Long> getInvites() {
        return invites;
    }

    public int getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(int beginTime) {
        this.beginTime = beginTime;
    }

    @Override
    public String toString() {
        return "Battle{" +
                "battleId=" + battleId +
                ", pos=" + pos +
                ", type=" + type +
                ", isAtkNpc=" + isAtkNpc +
                ", sponsor=" + sponsor +
                ", defencer=" + defencer +
                ", atkArm=" + atkArm +
                ", defArm=" + defArm +
                ", defCamp=" + defCamp +
                ", battleTime=" + battleTime +
                ", beginTime=" + beginTime +
                ", atkRoles=" + atkRoles +
                ", defRoles=" + defRoles +
                ", sponsorId=" + sponsorId +
                ", defencerId=" + defencerId +
                ", atkList=" + atkList +
                ", defList=" + defList +
                ", atkCity=" + atkCity +
                ", atkCamp=" + atkCamp +
                ", atkPos=" + atkPos +
                ", battleType=" + battleType +
                ", atkHelpChatCnt=" + atkHelpChatCnt +
                ", defHelpChatCnt=" + defHelpChatCnt +
                ", helpChatCnt=" + helpChatCnt +
                ", invites=" + invites +
                '}';
    }

    public int getAtkPortraitFrame() {
        if (sponsor != null) {
            return sponsor.getDressUp().getCurPortraitFrame();
        }
        return 0;
    }

    public int getDefPortraitFrame() {
        if (defencer != null) {
            return defencer.getDressUp().getCurPortraitFrame();
        }
        return 0;
    }
}
