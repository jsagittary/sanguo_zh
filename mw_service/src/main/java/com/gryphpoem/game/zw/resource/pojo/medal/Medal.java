package com.gryphpoem.game.zw.resource.pojo.medal;

import com.alibaba.fastjson.annotation.JSONField;
import com.gryphpoem.game.zw.core.util.Turple;
import com.gryphpoem.game.zw.dataMgr.StaticMedalDataMgr;
import com.gryphpoem.game.zw.resource.domain.s.StaticMedalGeneralSkill;
import com.gryphpoem.game.zw.resource.util.CheckNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chenqi
 * @ClassName: Medal
 * @Description: 玩家勋章
 * @date 2018年9月11日
 */
public class Medal {

    /**
     * 勋章私有id
     */
    private int keyId;

    /**
     * 勋章id
     */
    private Integer medalId;

    /**
     * 穿戴了该勋章的将领id，未穿戴为0
     */
    private int heroId;

    /**
     * 品质
     */
    private Integer quality;

    /**
     * 勋章等级
     */
    private Integer level;

    /**
     * 普通技能
     */
    private List<Integer> generalSkillId;

    /**
     * 该勋章绑定的特技,橙色勋章才有 0为未激活
     */
    private Integer specialSkillId;

    /**
     * 该勋章的光环,橙色勋章才有 0为未激活
     */
    private Integer auraSkillId;

    /**
     * 该勋章初始化的光环,橙色勋章才有，激活就是激活该光环
     */
    private Integer initAuraSkillId;

    /**
     * 勋章属性 A为属性id B为属性值
     */
//    @JSONField(deserialize = false)
    private Turple<Integer, Integer> medalAttr;

    /**
     * 强化的次数
     */
    private Integer intensifyCnt;

    /**
     * 花费的金条数
     */
    private Integer goldBarCnt;

    /**
     * 是否加锁 0否 1是
     */
    private Integer isLock;

    private int lastTime; // CD时间

    public int getLastTime() {
        return lastTime;
    }

    public void setLastTime(int lastTime) {
        this.lastTime = lastTime;
    }

    public Medal() {
        generalSkillId = new ArrayList<Integer>();
    }

    public Medal(com.gryphpoem.game.zw.pb.CommonPb.Medal medal) {
        this();
        setKeyId(medal.getKeyId());
        setMedalId(medal.getMedalId());
        setQuality(medal.getQuality());
        if (medal.hasHeroId()) {
            setHeroId(medal.getHeroId());
        }
        if (medal.hasLevel()) {
            setLevel(medal.getLevel());
        }
        for (Integer skillId : medal.getGeneralSkillIdList()) {
            generalSkillId.add(skillId);
        }
        if (medal.hasSpecialSkillId()) {
            setSpecialSkillId(medal.getSpecialSkillId());
        }
        if (medal.hasAuraSkillId()) {
            setAuraSkillId(medal.getAuraSkillId());
        }
        if (medal.hasInitAuraSkillId()) {
            setInitAuraSkillId(medal.getInitAuraSkillId());
        }
        if (medal.hasMedalAttr()) {
            medalAttr = new Turple<Integer, Integer>(medal.getMedalAttr().getV1(), medal.getMedalAttr().getV2());
        }
        if (medal.hasIntensifyCnt()) {
            setIntensifyCnt(medal.getIntensifyCnt());
        }
        if (medal.hasGoldBarCnt()) {
            setGoldBarCnt(medal.getGoldBarCnt());
        }
        if (medal.hasIsLock()) {
            setIsLock(medal.getIsLock());
        }
    }

    /**
     * 光环技能是否解锁
     *
     * @return
     */
    public boolean hasAuraSkill() {
        return getAuraSkillId() != null && getAuraSkillId() > 0;
    }

    /**
     * 特技是否解锁
     *
     * @return
     */
    public boolean hasSpecialSkill() {
        return getSpecialSkillId() != null && getSpecialSkillId() > 0;
    }

    /**
     * @return boolean
     * @Title: isLock
     * @Description: 勋章是否加锁
     */
    @JSONField(serialize = false)
    public boolean isLock() {
        return isLock != null && isLock > 0;
    }

    /**
     * 勋章是否被穿戴
     *
     * @return
     */
    public boolean isOnMedal() {
        return heroId > 0;
    }

    /**
     * 设置勋章被穿戴
     *
     * @param heroId
     */
    public void onMedal(int heroId) {
        if (heroId <= 0 || heroId > Integer.MAX_VALUE) {
            return;
        }

        setHeroId(heroId);
    }

    /**
     * 设置勋章被卸下，闲置
     */
    public void downMedal() {
        setHeroId(0);
    }

    /**
     * @return void
     * @Title: addIntensifyCnt
     * @Description: 增加强化次数
     */
    public void addIntensifyCnt() {
        this.intensifyCnt++;
    }

    /**
     * @param golgBarNum
     * @return void
     * @Title: addGoldBarCnt
     * @Description: 增加金条花费
     */
    public void addGoldBarCnt(int golgBarNum) {
        this.goldBarCnt += golgBarNum;
    }

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public Integer getMedalId() {
        return medalId;
    }

    public void setMedalId(Integer medalId) {
        this.medalId = medalId;
    }

    public int getHeroId() {
        return heroId;
    }

    public void setHeroId(int heroId) {
        this.heroId = heroId;
    }

    public Integer getQuality() {
        return quality;
    }

    public void setQuality(Integer quality) {
        this.quality = quality;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public List<Integer> getGeneralSkillId() {
        return generalSkillId;
    }

    public void setGeneralSkillId(List<Integer> generalSkillId) {
        this.generalSkillId = generalSkillId;
    }

    public Integer getSpecialSkillId() {
        return specialSkillId;
    }

    public void setSpecialSkillId(Integer specialSkillId) {
        this.specialSkillId = specialSkillId;
    }

    public Integer getAuraSkillId() {
        return auraSkillId;
    }

    public void setAuraSkillId(Integer auraSkillId) {
        this.auraSkillId = auraSkillId;
    }

    public Integer getInitAuraSkillId() {
        return initAuraSkillId;
    }

    public void setInitAuraSkillId(Integer initAuraSkillId) {
        this.initAuraSkillId = initAuraSkillId;
    }

    public Turple<Integer, Integer> getMedalAttr() {
        return medalAttr;
    }

    public String getMedalAttrLogStr() {
        if (CheckNull.isNull(medalAttr)) return "";
        return medalAttr.getA() + "," + medalAttr.getB();
    }

    public void setMedalAttr(Turple<Integer, Integer> medalAttr) {
        this.medalAttr = medalAttr;
    }

    public Integer getIntensifyCnt() {
        return intensifyCnt;
    }

    public void setIntensifyCnt(Integer intensifyCnt) {
        this.intensifyCnt = intensifyCnt;
    }

    public Integer getGoldBarCnt() {
        return goldBarCnt;
    }

    public void setGoldBarCnt(Integer goldBarCnt) {
        this.goldBarCnt = goldBarCnt;
    }

    public Integer getIsLock() {
        return isLock;
    }

    public void setIsLock(Integer isLock) {
        this.isLock = isLock;
    }

    public Turple<Integer, Integer> getGeneralSkillIdLv(int index) {
        if (CheckNull.isEmpty(this.generalSkillId) || this.generalSkillId.size() < index)
            return new Turple<>(0, 0);
        Turple<Integer, Integer> generalSkillIdLv = new Turple<>(this.generalSkillId.get(index - 1), 0);
        StaticMedalGeneralSkill staticMedalGeneralSkill = StaticMedalDataMgr.getGeneralSkillById(generalSkillIdLv.getA());
        if (CheckNull.isNull(staticMedalGeneralSkill))
            return generalSkillIdLv;
        generalSkillIdLv.setB(staticMedalGeneralSkill.getLevel());
        return generalSkillIdLv;
    }

}
