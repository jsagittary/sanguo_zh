package com.gryphpoem.game.zw.resource.pojo.hero;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticHeroDataMgr;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.TwoInt;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.constant.HeroConstant;
import com.gryphpoem.game.zw.resource.constant.MedalConst;
import com.gryphpoem.game.zw.resource.domain.s.StaticHero;
import com.gryphpoem.game.zw.resource.domain.s.StaticHeroUpgrade;
import com.gryphpoem.game.zw.resource.pojo.treasureware.TreasureWare;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

import java.util.*;

/**
 * @author TanDonghai
 * @ClassName Hero.java
 * @Description 将领
 * @date 创建时间：2017年3月27日 上午10:39:15
 */
public class Hero {
    private int heroId; // 将领id
    private int heroType; // 将领类型
    private int level; // 等级
    private int exp; // 当前等级经验
    private int[] attr; // 将领属性，属性：1 攻击，2 防御，3 兵力
    private int[] wash; // 将领洗髓属性，0 总资质，1 防御，2 防御，3 兵力
    private int[] equip; // 装备栏信息，记录装备id
    private volatile int status; // 将领所在队列，0 空闲，1 上阵，2 采集，3 防守 ,4城墙上阵
    private volatile int state; // 将领状态，0 空闲，1 出征，2 采集
    /**
     * 角色类型
     */
    private volatile int roleType;
    private int count; // 如果是上阵将领，当前兵力
    private int pos; // 如果是上阵将领，在上阵队列中的位置，1-4
    private int wallPos; // 如果是城墙将领，在上阵队列中的位置，1-4
    private int wallArmyTime;// 城墙上次补兵时间
    private int acqPos; // 如果是采集将领，在上阵队列中的位置，1-4
    private int quality;// 品质
    private int breakExp;// 突破经验
    private int cgyStage;//神职等级(大)
    private int cgyLv;//神职阶数(小)
    private int washTotalFloorCount; // 当前洗髓总属性保底的次数
    private int defPos; // 如果是防守将领, 在防守队列中的位置, 1-4
    private Map<Integer, Integer> extAttrs;// 除去攻防兵的额外的装备属性
    private int decorated; // 将领授勋次数 没有授勋是0次
    private int medalKeyId;//勋章keyId  在勋章反序列化的时候赋值
    private List<Integer> medalKeys = new ArrayList<>(MedalConst.HERO_MEDAL_UP_CNT);
    private Map<Integer, Integer> showFight;// 显示战力, key见ConStant的ShowFightId
    private Map<Integer, AwakenData> awaken;
    private int sandTableState;//是否出战沙盘 1是 0否
    private int fightVal;//英雄战力值
    private boolean isOnBaitTeam;//是否在采集鱼饵队列
    private int[] totem;
    private Integer treasureWare;//宝具
    private Map<Integer, TalentData> talent; // 武将天赋信息：key, 天赋页索引; value, 天赋页详情

    /**
     * 英雄品阶keyId
     */
    private int gradeKeyId;
    /**
     * 是否在主界面显示英雄奖励
     */
    private boolean showClient;

    public Hero() {
        attr = new int[4];// 三种属性，对应1-3位，0位为补位，不用
        equip = new int[HeroConstant.HERO_EQUIP_NUM + 1];// 0位为补位，1-6为装备部位
        status = HeroConstant.HERO_STATUS_IDLE;
        state = HeroConstant.HERO_STATE_IDLE;
        level = 1;// 默认1级
        count = 0;
        exp = 0;
        pos = 0;
        wallPos = 0;
        washTotalFloorCount = 0;
        acqPos = 0;
        defPos = 0;
        extAttrs = new HashMap<>();
        decorated = 0;
        showFight = new HashMap<>();
        initMedalKeys();
        totem = new int[9];
        awaken = new TreeMap<>(Integer::compareTo);
        showClient = true;
        talent = new TreeMap<>(Integer::compareTo);
    }

    /**
     * 初始化英雄品阶
     */
    public void initHeroGrade() {
        StaticHeroUpgrade staticData = StaticHeroDataMgr.getInitHeroUpgrade(getHeroId());
        if (CheckNull.isNull(staticData))
            return;
        this.gradeKeyId = staticData.getKeyId();
    }

    /**
     * 避免越界的情况
     */
    private void initMedalKeys() {
        for (int i = 0; i < MedalConst.HERO_MEDAL_UP_CNT; i++) {
            modifyMedalKey(i, 0);
        }
    }

    public int getGradeKeyId() {
        return gradeKeyId;
    }

    public void setGradeKeyId(int gradeKeyId) {
        this.gradeKeyId = gradeKeyId;
    }

    public Hero(CommonPb.Hero hero) {
        this();
        setHeroId(hero.getHeroId());
        setLevel(hero.getLevel());
        setExp(hero.getExp());
        setStatus(hero.getStatus());
        setState(hero.getState());
        setBreakExp(hero.getBreakExp());
        setWallArmyTime(hero.getWallArmyTime());
        if (hero.hasQuality()) {
            setQuality(hero.getQuality());
        }
        if (hero.hasCount()) {
            setCount(hero.getCount());
        }
        if (hero.hasPos()) {
            setPos(hero.getPos());
        }
        if (hero.hasWallPos()) {
            setWallPos(hero.getWallPos());
        }
        if (hero.hasAcqPos()) {
            setAcqPos(hero.getAcqPos());
        }
        if (hero.hasWashTotalFloorCount()) {
            setWashTotalFloorCount(hero.getWashTotalFloorCount());
        }
        if (hero.hasDefPos()) {
            setDefPos(hero.getDefPos());
        }
        if (hero.hasComandoPos()) {
            setCommandoPos(hero.getComandoPos());
        }
        for (int i = 0; i < hero.getAttrList().size(); i++) {
            TwoInt two = hero.getAttrList().get(i);
            if (i < 3) {
                attr[two.getV1()] = two.getV2();
            } else {
                extAttrs.put(two.getV1(), two.getV2());
            }
        }

        for (TwoInt two : hero.getEquipList()) {
            equip[two.getV1()] = two.getV2();
        }
        setDecorated(hero.getDecorated());
//        for (TwoInt twoInt : hero.getWarPlaneList()) {
//            warPlanes.add(twoInt.getV1());
//        }
        for (TwoInt two : hero.getShowFightList()) {
            showFight.put(two.getV1(), two.getV2());
        }
        if (CheckNull.nonEmpty(hero.getAwakendataList())) {
            hero.getAwakendataList().forEach(awakenDataPb -> {
                if (CheckNull.isNull(awakenDataPb))
                    return;
                this.awaken.put(awakenDataPb.getIndex(), new AwakenData(awakenDataPb));
            });
        }
        if (CheckNull.nonEmpty(hero.getTalentDataList())) {
            hero.getTalentDataList().forEach(talentDataPb -> {
                if (CheckNull.isNull(talentDataPb)) {
                    return;
                }
                this.talent.put(talentDataPb.getIndex(), new TalentData(talentDataPb));
            });
        } else {
            // 由于新天赋功能上线前，线上玩家武将已进行觉醒过，需要给其初始化天赋页，否则客户端页面显示报错
            if (hero.hasQuality() && hero.getQuality() != 3) {
                if (decorated > 0) {
                    int maxPart;
                    switch (hero.getQuality()) {
                        case HeroConstant.QUALITY_PURPLE_HERO:
                            maxPart = HeroConstant.TALENT_PART_MAX_OF_PURPLE_HERO;
                            break;
                        case HeroConstant.QUALITY_ORANGE_HERO:
                            maxPart = HeroConstant.TALENT_PART_MAX_OF_ORANGE_HERO;
                            break;
                        default:
                            throw new MwException(GameError.NO_CONFIG.getCode(), "武将天赋球个数配置错误, heroId:", heroId);
                    }
                    for (int i = 1; i <= decorated; i++) {
                        this.talent.put(i, new TalentData(0, i, maxPart));
                    }
                }
            }
        }
        initMedalKeys();
        this.sandTableState = hero.getSandTableState();
        this.fightVal = hero.getFightVal();
        this.cgyStage = hero.getCgyStage();
        this.cgyLv = hero.getCgyLv();
        this.isOnBaitTeam = hero.getIsOnBaitTeam() == 1 ? true : false;
        hero.getTotemList().forEach(o -> totem[o.getV1()] = o.getV2());
        if (hero.hasTreasureWare()) {
            setTreasureWare(hero.getTreasureWare());
        }
        setGradeKeyId(hero.getGrade());
        this.setShowClient(hero.getShowClient());
    }

    public int getHeroType() {
        return heroType;
    }

    public int getType() {
        StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(heroId);
        return staticHero != null ? staticHero.getType() : -1;
    }

    public void setHeroType(int heroType) {
        this.heroType = heroType;
    }

    public int getHeroId() {
        return heroId;
    }

    public void setHeroId(int heroId) {
        this.heroId = heroId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int[] getAttr() {
        return attr;
    }

    public void setAttr(int[] attr) {
        this.attr = attr;
    }

    public int[] getWash() {
        return wash;
    }

    public void setWash(int[] wash) {
        this.wash = wash;
    }

    public int[] getEquip() {
        return equip;
    }

    public void setEquip(int[] equip) {
        this.equip = equip;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        if (this.count > 0 && this.roleType == HeroConstant.HERO_ROLE_TYPE_DEPUTY) {
            LogUtil.error("副将不可补兵, heroId: ", heroId);
            return;
        }
        // 兵力是否有变动
        boolean isChange = count != this.count;
        this.count = count;
        // 如果是城防将领,兵力变动是就会更新时间
        if (isOnWall() && isChange) {
            setWallArmyTime(TimeHelper.getCurrentSecond());
        }
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public int getWallPos() {
        return wallPos;
    }

    public void setWallPos(int wallPos) {
        this.wallPos = wallPos;
    }

    public int getWallArmyTime() {
        return wallArmyTime;
    }

    public void setWallArmyTime(int wallArmyTime) {
        this.wallArmyTime = wallArmyTime;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public int getBreakExp() {
        return breakExp;
    }

    public void setBreakExp(int breakExp) {
        this.breakExp = breakExp;
    }

    public int getAcqPos() {
        return acqPos;
    }

    public void setAcqPos(int acqPos) {
        this.acqPos = acqPos;
    }

    public Map<Integer, Integer> getExtAttrs() {
        return extAttrs;
    }

    public void setExtAttrs(Map<Integer, Integer> extAttrs) {
        this.extAttrs = extAttrs;
    }

    public int getDefPos() {
        return defPos;
    }

    public void setDefPos(int defPos) {
        this.defPos = defPos;
    }

    public List<Integer> getWarPlanes() {
        return Collections.EMPTY_LIST;
    }

    public void setWarPlanes(List<Integer> warPlanes) {
    }

    public Integer getTreasureWare() {
        return treasureWare;
    }

    public void setTreasureWare(Integer treasureWare) {
        this.treasureWare = treasureWare;
    }

    public boolean isShowClient() {
        return showClient;
    }

    public void setShowClient(boolean showClient) {
        this.showClient = showClient;
    }

    /**
     * 战机改造
     *
     * @param oldPlane
     * @param newPlane
     */
    public void planeRemould(int oldPlane, int newPlane) {
//        if (oldPlane <= 0 || oldPlane > Integer.MAX_VALUE || newPlane <= 0 || newPlane >= Integer.MAX_VALUE) {
//            return;
//        }
//        Iterator<Integer> it = warPlanes.iterator();
//        boolean suc = false;
//        while (it.hasNext()) {
//            if (it.next() == oldPlane) {
//                it.remove();
//                suc = true;
//            }
//        }
//        if (suc) {
//            warPlanes.add(newPlane);
//        }
    }

    /**
     * 将领上阵、下阵，更新pos信息，并更新将领状态
     *
     * @param pos 如果pos值在1-4内，为上阵，否则为下阵
     */
    public void onBattle(int pos) {
        setPos(pos);
        if (pos >= HeroConstant.HERO_BATTLE_1 && pos <= HeroConstant.HERO_BATTLE_4) {
            setStatus(HeroConstant.HERO_STATUS_BATTLE);
        } else {
            setStatus(HeroConstant.HERO_STATUS_IDLE);
        }
    }

    /**
     * 城防将领,将领上阵、下阵，更新pos信息，并更新将领状态
     *
     * @param pos 如果pos值在1-4内，为上阵，否则为下阵
     */
    public void onWall(int pos) {
        setWallPos(pos);
        setWallArmyTime(TimeHelper.getCurrentSecond());
        if (pos >= HeroConstant.HERO_BATTLE_1 && pos <= HeroConstant.HERO_BATTLE_4) {
            setStatus(HeroConstant.HERO_STATUS_WALL_BATTLE);
        } else {
            setStatus(HeroConstant.HERO_STATUS_IDLE);
        }
    }

    /**
     * 采集将领,将领上阵、下阵，更新pos信息，并更新将领状态
     *
     * @param pos
     */
    public void onAcq(int pos) {
        setAcqPos(pos);
        if (pos >= HeroConstant.HERO_BATTLE_1 && pos <= HeroConstant.HERO_BATTLE_4) {
            setStatus(HeroConstant.HERO_STATUS_COLLECT);
        } else {
            setStatus(HeroConstant.HERO_STATUS_IDLE);
        }
    }

    /**
     * 特攻将领, 将领上阵、下阵，更新pos信息，并更新将领状态
     *
     * @param pos
     */
    public void onCommando(int pos) {
    }

    /**
     * 防守将领上阵、下阵，更新pos信息，并更新将领状态
     *
     * @param pos 如果pos值在1-4内，为上阵，否则为下阵
     */
    public void onDef(int pos) {
        setDefPos(pos);
        if (pos >= HeroConstant.HERO_BATTLE_1 && pos <= HeroConstant.HERO_BATTLE_4) {
            setStatus(HeroConstant.HERO_STATUS_BATTLE);
        } else {
            setStatus(HeroConstant.HERO_STATUS_IDLE);
        }
    }

    public int getRoleType() {
        return roleType;
    }

    public void setRoleType(int roleType) {
        this.roleType = roleType;
    }

    /**
     * 是否是武将上阵将领
     *
     * @return
     */
    public boolean isOnBattle() {
        return status == HeroConstant.HERO_STATUS_BATTLE;
    }

    public boolean isOnSandTable() {//status; // 将领所在队列，0 空闲，1 上阵，2 采集，3 防守 ,4城墙上阵
        return sandTableState > 0;
    }

    /**
     * 是否是城墙上阵将领
     *
     * @return
     */
    public boolean isOnWall() {
        return status == HeroConstant.HERO_STATUS_WALL_BATTLE;
    }

    /**
     * 是否是采集上阵将领
     *
     * @return
     */
    public boolean isOnAcq() {
        return status == HeroConstant.HERO_STATUS_COLLECT;
    }

    /**
     * 是否是防守将领
     *
     * @return
     */
    public boolean isOnDef() {
        return status == HeroConstant.HERO_STATUS_DEFEND;
    }

    /**
     * 是否是未上阵的将领
     *
     * @return
     */
    public boolean isFaineant() {
        return status == HeroConstant.HERO_STATUS_IDLE;
    }

    /**
     * 是否是特攻队的将领
     *
     * @return
     */
    public boolean isCommando() {
        return status == HeroConstant.HERO_STATUS_COMMANDO;
    }

    /**
     * 将领当前状态是否处于空闲
     *
     * @return
     */
    public boolean isIdle() {
        return state == HeroConstant.HERO_STATE_IDLE;
    }

    /**
     * 将领升级
     *
     * @return
     */
    public int levelUp() {
        this.level++;
        this.exp = 0;
        return level;
    }

    /**
     * 将领身上是否穿戴了该装备
     *
     * @param equipKeyId 装备私有id
     * @return
     */
    public boolean hasEquip(int equipKeyId) {
        if (equipKeyId <= 0 || equipKeyId >= Integer.MAX_VALUE) {
            return false;
        }

        for (int keyId : equip) {
            if (keyId == equipKeyId) {
                return true;
            }
        }
        return false;
    }

    /**
     * 将领是否穿戴了该宝具
     *
     * @param treasureWareKeyId
     * @return
     */
    public boolean hasEquipTreasureWare(int treasureWareKeyId) {
        if (treasureWareKeyId <= 0 || treasureWareKeyId >= Integer.MAX_VALUE) {
            return false;
        }
        if (CheckNull.isNull(this.treasureWare))
            return false;
        if (this.treasureWare.intValue() == treasureWareKeyId)
            return true;

        return false;
    }

    /**
     * 战机下阵
     *
     * @param planId 战机Id
     * @return 是否上阵成功
     */
    public boolean downPlane(int planId) {
//        boolean downSuc = false;
//        if (planId <= 0 || planId >= Integer.MAX_VALUE) {
//            return downSuc;
//        }
//
//        Iterator<Integer> it = warPlanes.iterator();
//        while (it.hasNext()) {
//            if (it.next() == planId) {
//                it.remove();
//                downSuc = true;
//            }
//        }
//        return downSuc;
        return true;
    }

    /**
     * 战机上阵
     *
     * @param planId 上阵战机Id
     * @param max    当前队列的解锁栏位
     * @return 是否下阵成功
     */
    public boolean upPlane(int planId, int max) {
//        boolean onSuc = false;
//        if (planId <= 0 || planId >= Integer.MAX_VALUE) {
//            return onSuc;
//        }
//
//        if (warPlanes.size() < max) {
//            warPlanes.add(planId);
//            onSuc = true;
//        }
//        return onSuc;
        return true;
    }

    /**
     * 卸下装备
     *
     * @param equipKeyId
     * @return 返回卸下装备的栏位
     */
    public int downEquip(int equipKeyId) {
        if (equipKeyId <= 0 || equipKeyId >= Integer.MAX_VALUE) {
            return 0;
        }

        int index = 0;
        for (int i = 1; i < equip.length; i++) {
            if (equip[i] == equipKeyId) {
                index = i;
                break;
            }
        }

        if (index > 0) {
            equip[index] = 0;
        }
        return index;
    }

    /**
     * 卸下宝具
     */
    public void downTreasureWare() {
        this.treasureWare = null;
    }

    /**
     * 穿戴宝具
     *
     * @param treasureWare
     */
    public void onTreasureWare(TreasureWare treasureWare) {
        this.treasureWare = treasureWare.getKeyId();
    }

    /**
     * 穿戴装备
     *
     * @param pos        装备栏位置
     * @param equipKeyId 装备私有id
     */
    public void onEquip(int pos, int equipKeyId) {
        if (equipKeyId <= 0 || equipKeyId >= Integer.MAX_VALUE) {
            return;
        }

        if (pos <= 0 || pos > HeroConstant.HERO_EQUIP_NUM) {
            return;
        }

        equip[pos] = equipKeyId;
    }

    /**
     * 扣除兵力
     *
     * @param sub
     */
    public int subArm(int sub) {
        if (sub <= 0) {
            return 0;
        }

        if (sub >= count) {
            sub = count;
        }
        setCount(count - sub);

        return sub;
    }

    /**
     * @param addArm
     * @return int
     * @Title: addArm
     * @Description: 增加兵力
     */
    public int addArm(int addArm) {
        if (addArm <= 0) {
            return 0;
        }
        setCount(count + addArm);
        return addArm;
    }

    public int getWashTotalFloorCount() {
        return washTotalFloorCount;
    }

    public void setWashTotalFloorCount(int washTotalFloorCount) {
        this.washTotalFloorCount = washTotalFloorCount;
    }

    public int getDecorated() {
        return decorated;
    }

    public void setDecorated(int decorated) {
        this.decorated = decorated;
    }

    public int getMedalKeyId() {
        return medalKeyId;
    }

    public void setMedalKeyId(int medalKeyId) {
        this.medalKeyId = medalKeyId;
    }

    public Map<Integer, Integer> getShowFight() {
        return showFight;
    }

    public void setShowFight(Map<Integer, Integer> showFight) {
        this.showFight = showFight;
    }

    public List<Integer> getMedalKeys() {
        return medalKeys;
    }

    public void setMedalKeys(List<Integer> medalKeys) {
        this.medalKeys = medalKeys;
    }

    public int getCgyLv() {
        return cgyLv;
    }

    public void setCgyLv(int cgyLv) {
        this.cgyLv = cgyLv;
    }

    public int getCgyStage() {
        return cgyStage;
    }

    public void setCgyStage(int cgyStage) {
        this.cgyStage = cgyStage;
    }

    public void modifyMedalKey(int index, int key) {
        if (index <= MedalConst.HERO_MEDAL_UP_CNT) {
            if (this.medalKeys.size() <= index) {
                this.medalKeys.add(index, key);
            } else {
                this.medalKeys.set(index, key);
            }
        }
    }

    public int getCommandoPos() {
        return 0;
    }

    public void setCommandoPos(int commandoPos) {
    }

    public Map<Integer, AwakenData> getAwaken() {
        return awaken;
    }

    public void setAwaken(Map<Integer, AwakenData> awaken) {
        this.awaken = awaken;
    }

    public AwakenData getAwaken(int index) {
        if (CheckNull.isEmpty(awaken))
            awaken = new TreeMap<>(Integer::compareTo);
        return awaken.get(index);
    }

    public Map<Integer, TalentData> getTalent() {
        return talent;
    }

    public void setTalent(Map<Integer, TalentData> talent) {
        this.talent = talent;
    }

    public TalentData getTalent(int index) {
        if (CheckNull.isEmpty(talent)) {
            talent = new TreeMap<>(Integer::compareTo);
        }

        return talent.get(index);
    }

    public int getSandTableState() {
        return sandTableState;
    }

    public void setSandTableState(int sandTableState) {
        this.sandTableState = sandTableState;
    }

    public int getFightVal() {
        return fightVal;
    }

    public void setFightVal(int fightVal) {
        this.fightVal = fightVal;
    }

    @Override
    public String toString() {
        return "Hero{" +
                "heroId=" + heroId +
                ", heroType=" + heroType +
                ", level=" + level +
                ", exp=" + exp +
                ", attr=" + Arrays.toString(attr) +
                ", wash=" + Arrays.toString(wash) +
                ", equip=" + Arrays.toString(equip) +
                ", status=" + status +
                ", state=" + state +
                ", count=" + count +
                ", pos=" + pos +
                ", wallPos=" + wallPos +
                ", wallArmyTime=" + wallArmyTime +
                ", acqPos=" + acqPos +
                ", quality=" + quality +
                ", breakExp=" + breakExp +
                ", washTotalFloorCount=" + washTotalFloorCount +
                ", defPos=" + defPos +
                ", extAttrs=" + extAttrs +
                ", decorated=" + decorated +
                ", medalKeyId=" + medalKeyId +
                ", medalKeys=" + medalKeys +
                ", showFight=" + showFight +
                ", awaken=" + awaken +
                ", talent=" + talent +
                ", treasureWare=" + (CheckNull.isNull(treasureWare) ? -1 : treasureWare) +
                '}';
    }

    public boolean isOnBaitTeam() {
        return isOnBaitTeam;
    }

    public void setOnBaitTeam(boolean onBaitTeam) {
        isOnBaitTeam = onBaitTeam;
    }

    public int[] getTotem() {
        return totem;
    }

    public int getTotemKey(int idx) {
        return totem[idx];
    }

    public void setTotemKey(int idx, int totemKey) {
        totem[idx] = totemKey;
    }
}
