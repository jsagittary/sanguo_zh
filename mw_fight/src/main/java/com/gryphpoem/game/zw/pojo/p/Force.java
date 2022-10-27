package com.gryphpoem.game.zw.pojo.p;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.push.util.CheckNull;

import java.util.*;

/**
 * @author TanDonghai
 * @ClassName Force.java
 * @Description 记录战斗中单个将领或npc的战斗信息
 * @date 创建时间：2017年3月31日 下午5:14:08
 */
public class Force {
    public int id;//将领id
    public int hp;// 当前血量，兵力
    public int maxHp;// 总血量，兵力
    public int armType;// 兵种类型
    public int lead;// 单排兵力
    public int curLine;// 当前战斗的是第几排兵，从0开始
    public int maxLine;// 总兵力排数
    public int count;// 本排兵剩余数量
    public int killed;// 杀敌数
    public int lost;// 记录单次被攻击损兵数
    public int totalLost;// 将领损兵总数
    public AttrData attrData;// 战斗属性
    public Fighter fighter;
    public int addExp;// 如果是玩家将领，记录本次战斗中获得的经验
    public long ownerId;// 如果是玩家将领，记录所属玩家id
    public String nick;
    public boolean hasFight;// 记录是否参与过战斗
    public int camp; // 阵营
    public int roleType; // 1玩家,2流寇NPC,3城池守将NPC,4城池NPC
    public int skillId;// 技能id 0表示 无技能
    private boolean useSkill; // 是否使用过技能,true表示使用过
    public int intensifyLv; // 兵种强化等级
    public int effect; // 强化克制比
    public int isBcs;//是否有勋章  闪击奇兵 特技   0否  1是
    public int isIronBas;//是否有勋章  铜墙铁壁 特技  0否 1是
    public Map<Integer, PlaneInfo> planeInfos = new HashMap<>();// 战机信息
    /**
     * 发起动作的武将id
     */
    public int actionId;
    /**
     * 被攻击的武将idList
     */
    public List<Integer> beActionId;
    /**
     * 触发buff的武将idList
     */
    public List<Integer> buffTriggerId;
    /**
     * 战斗buff集合 主将的buff列表
     */
    public LinkedList<IFightBuff> buffList = new LinkedList<>();
    /**
     * 副将列表
     */
    public ArrayList<FightAssistantHero> assistantHeroList = new ArrayList<>();

    public Force() {
    }

    public Force(AttrData attrData, int armType, int line) {
        this.attrData = attrData;
        this.armType = armType;
        this.curLine = 0;
        this.maxLine = line;
        this.lead = attrData.lead / line;
        this.count = attrData.lead / line;
        this.maxHp = count * maxLine;
        this.hp = maxHp;
    }

    /**
     * 玩家将领对应的Force初始化
     *
     * @param attrData
     * @param armType
     * @param totalCount 将领总兵力
     * @param lead       单排兵力
     * @param id
     */
    public Force(AttrData attrData, int armType, int totalCount, int lead, int id, long roleId) {
        this.attrData = attrData;
        this.armType = armType;
        this.maxHp = totalCount;
        this.lead = lead;
        this.curLine = 0;
        this.hp = maxHp;
        this.id = id;
        this.ownerId = roleId;

        /**
         * 将领兵力排数计算规则：<br>
         * 当兵力不止一排时，首先满足后排兵力满，首排兵力可以不满
         */
        this.maxLine = (int) Math.ceil(totalCount * 1.0 / lead);// 计算兵力排数，向上取整
        this.count = totalCount % lead;// 计算最前排的兵力
        if (this.count == 0) {
            this.count = lead;
        }
        LogUtil.fight("Force=" + toString());
    }

    /**
     * 新的计算公式 有单排兵改成总兵(NPC对应的Force初始化)
     *
     * @param attrData
     * @param armType
     * @param totalCount 剩余总兵力
     * @param lead
     * @param id
     * @param sCount
     * @param sLine
     */
    public Force(AttrData attrData, int armType, int totalCount, int lead, int id, int sCount, int sLine) {
        int sLead = (int) Math.ceil(sCount * 1.0 / sLine);// 计算单排兵力, 当兵力不能被整除时，向上取整
        this.attrData = attrData;
        this.armType = armType;
        this.maxHp = totalCount;
        this.lead = sLead;
        this.curLine = 0;
        this.hp = maxHp;
        this.id = id;

        /**
         * 将领兵力排数计算规则：<br>
         * 当兵力不止一排时，首先满足后排兵力满，首排兵力可以不满
         */
        this.maxLine = (int) Math.ceil(totalCount * 1.0 / sLead);// 计算兵力排数，向上取整
        this.count = totalCount % sLead;// 计算最前排的兵力
        if (this.count == 0) {
            this.count = sLead;
        }
        LogUtil.fight(
                "Force : 当前兵力=" + totalCount + ",一排多少兵=" + sLead + ",总共多少排=" + maxLine + ",sCount=" + sCount + ",sLine="
                        + sLine);
    }

    /**
     * NPC对应的Force初始化
     *
     * @param attrData
     * @param armType
     * @param line     兵力排数
     * @param id
     */
    public Force(AttrData attrData, int armType, int line, int id) {
        this(attrData, armType, line);
        this.id = id;
    }

    /**
     * 兵力大于0
     *
     * @return 是否存活
     */
    public boolean alive() {
        return hp > 0;
    }

    /**
     * 计算伤害能造成的最大真是损兵量，当伤害高于当前排兵力时，只会扣完本排兵力，不会影响后排兵力
     *
     * @param hurt
     * @return
     * @see FightLogic#(Force, int) 如果需要考虑FightBuff
     */
    public int hurt(int hurt, Force force, int battleType, float crit) {
        //天赋优化 战斗buff
        //攻击方的伤害加成与防守方伤害减免
//        hurt = FightLogic.seasonTalentBuff(force, this, hurt, battleType);
//        // 计算保底伤害
//        if (battleType != Integer.MIN_VALUE)
//            hurt = FightCalc.calRoundGuaranteedDamage(force, this, hurt, battleType, crit);
//
//        if (count <= hurt) {
//            lost = count;
//        } else {
//            lost = hurt;
//        }

        return lost;
    }

    public LinkedList<IFightBuff> buffList(int heroId) {
        if (heroId == this.id)
            return buffList;
        if (!CheckNull.isEmpty(assistantHeroList)) {
            FightAssistantHero assistantHero = assistantHeroList.stream().filter(ass -> ass.getHeroId() == heroId).findFirst().orElse(null);
            if (Objects.nonNull(assistantHero))
                return assistantHero.getBuffList();
        }

        return null;
    }

    /**
     * 执行扣兵逻辑
     *
     * @return 是否死亡兵排数
     */
    public boolean subHp(Force force) {
        boolean deadLine = false;

        LogUtil.fight(String.format("进攻方角色id: %d, 防守方角色id: %d, 防守方当前兵排剩余血量: %d, <<<<<<战斗最终伤害>>>>>>: %d",
                force == null ? 0 : force.ownerId, this.ownerId, count, lost));

        if (count <= lost) {
            curLine++;
            if (curLine >= maxLine) {
                hp = 0;
                count = 0;
            } else {
                hp -= count;
                count = lead;
                deadLine = true;
            }
        } else {
            count -= lost;
            hp -= lost;
        }
        totalLost += lost;
        lost = 0;
        return deadLine;
    }

    /**
     * 在真实扣兵计算前，计算损兵后剩余总兵力
     *
     * @return
     */
    public int getSurplusCount() {
        return hp - lost;
    }

    /**
     * 是否还有可用的技能
     *
     * @return true表示有用技能
     */
    public boolean hasSkill() {
        return this.skillId > 0 && !this.useSkill;
    }

    /**
     * 使用jineng
     */
    public void useSkill() {
        this.useSkill = true;
    }

    /**
     * 在真实扣兵计算前，获取损兵后的死亡排数
     *
     * @return
     */
    public int getDeadLine() {
        if (count <= lost) {
            return curLine + 1;
        }
        return curLine;
    }

    /**
     * 入场技能只释放首位战机
     *
     * @return
     */
    public boolean planeHasSkill() {
        boolean hasShill = false;
        if (!CheckNull.isEmpty(planeInfos)) {
            PlaneInfo info = planeInfos.get(1);
            if (!CheckNull.isNull(info) && info.hasSkill()) {
                hasShill = true;
            }
        }
        return hasShill;
    }

    /**
     * 获取当前行技能
     *
     * @return
     */
//    public FightSkill getCurrentSkill() {
//        FightSkill skill = null;
//        int planePos = PlaneConstant.LINE_PLANE_SKILL.getOrDefault(curLine + 1, -1);
//        if (planePos > -1 && this.fightSkill.containsKey(planePos)) {
//            List<FightSkill> skills = this.fightSkill.get(planePos);
//            if (!CheckNull.isEmpty(skills)) {
//                skill = skills.stream().filter(s -> s.notRollSkill() || s.notReleaseSkill() || s
//                                .notEndSkillEffect()) // 还未Roll技能, 或者技能还未释放, 或者技能还未释放完
//                        .sorted(Comparator.comparing(FightSkill::getOrder)).findFirst().orElse(null);
//            }
//        }
//        return skill;
//    }
//    public FightSkill getCurrentSkill0() {
//        //1.BUFF判断(禁锢,沉默,眩晕...)
//        return fightSkills.stream()
//                .filter(SkillTriggerUtils::doTriggerCond)
//                .min(Comparator.comparing(FightSkill::getOrder))
//                .orElse(null);
//    }

    /**
     * 获取战机攻击力
     *
     * @param battleType
     * @return
     */
    public double getPlaneAtk(int battleType) {
        return 0d;
    }

    /**
     * 获取战机防御力
     *
     * @param battleType
     * @return
     */
    public double getPlaneDef(int battleType) {
        return 0d;
    }

    public int calcAttack() {
        return attrData.attack;
    }

    public int calcDefend() {
        return attrData.defend;
    }

    public int calcCrit() {
        return attrData.crit;
    }

    public int calcCritDef() {
        return attrData.critDef;
    }

    public int calcAtkExt() {
        return attrData.atkExt;
    }

    public int calcDefExt() {
        return attrData.defExt;
    }

    public int calcAtkTown() {
        return attrData.atkTown;
    }

    public int calcDefTown() {
        return attrData.defTown;
    }

    public int getCamp() {
        return camp;
    }

    public void setCamp(int camp) {
        this.camp = camp;
    }

    public int getIntensifyLv() {
        return intensifyLv;
    }

    public void setIntensifyLv(int intensifyLv) {
        this.intensifyLv = intensifyLv;
    }

    public int getEffect() {
        return effect;
    }

    public void setEffect(int effect) {
        this.effect = effect;
    }

    public void setIsBcs(int isBcs) {
        this.isBcs = isBcs;
    }

    public void setIsIronBas(int isIronBas) {
        this.isIronBas = isIronBas;
    }

    public Map<Integer, PlaneInfo> getPlaneInfos() {
        return planeInfos;
    }

    public AttrData getAttrData() {
        return attrData;
    }

//    public String getTreasureWare() {
//        if (ownerId <= 0)
//            return "";
//        Player player = DataResource.getBean(PlayerDataManager.class).getPlayer(ownerId);
//        if (CheckNull.isNull(player))
//            return "";
//        Hero hero = player.heros.get(id);
//        if (CheckNull.isNull(hero) || CheckNull.isNull(hero.getTreasureWare()))
//            return "";
//        TreasureWare treasureWare = player.treasureWares.get(hero.getTreasureWare());
//        if (CheckNull.isNull(treasureWare))
//            return "";
//        return treasureWare.getEquipId() + "--" + (CheckNull.isNull(treasureWare.getSpecialId()) ? -1 : treasureWare.getSpecialId());
//    }

    @Override
    public String toString() {
        return "Force{" + "id=" + id + ", hp=" + hp + ", maxHp=" + maxHp + ", armType=" + armType + ", lead=" + lead
                + ", curLine=" + curLine + ", maxLine=" + maxLine + ", count=" + count + ", killed=" + killed
                + ", lost=" + lost + ", totalLost=" + totalLost + ", attrData=" + attrData + ", fighter=" + fighter
                + ", addExp=" + addExp + ", ownerId=" + ownerId + ", nick='" + nick + '\'' + ", hasFight=" + hasFight
                + ", camp=" + camp + ", roleType=" + roleType + ", skillId=" + skillId + ", useSkill=" + useSkill
                + ", intensifyLv=" + intensifyLv + ", effect=" + effect + ", isBcs=" + isBcs + ", isIronBas="
                + isIronBas + ", planeInfos=" + planeInfos + ", treasureWare= " + 1 + '}';
    }

    public String toBattleString() {
        return "Force{" +
                "id=" + id +
                ", hp=" + hp +
                ", maxHp=" + maxHp +
                ", armType=" + armType +
                ", lead=" + lead +
                ", curLine=" + curLine +
                ", maxLine=" + maxLine +
                ", count=" + count +
                ", killed=" + killed +
                ", lost=" + lost +
                ", totalLost=" + totalLost +
                ", addExp=" + addExp +
                ", ownerId=" + ownerId +
                ", hasFight=" + hasFight +
                ", camp=" + camp +
                ", roleType=" + roleType +
                ", skillId=" + skillId +
                ", useSkill=" + useSkill +
                ", intensifyLv=" + intensifyLv +
                ", effect=" + effect +
                ", isBcs=" + isBcs +
                ", isIronBas=" + isIronBas +
                ", treasureWare =" + 1 +
                '}';
    }
}
