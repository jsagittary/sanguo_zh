package com.gryphpoem.game.zw.resource.domain.p;

import java.util.Map;

public class Lord implements Cloneable {
    private long lordId; // 玩家id
    private String nick;// 主公名字
    private String signature = ""; // 个性签名
    private int portrait;// 头像
    private int sex;
    private int camp;// 所属阵营
    private int level;// 当前等级
    private long exp;// 当前经验值
    private int vip;// vip等级
    private int vipExp;// 送的vip经验
    private int topup;// 总充值金额
    private int area;// 玩家所属分区
    private int pos;// 坐标
    private int gold;// 金币
    private int goldCost;// 金币总消耗
    private int goldGive;// 总共赠予的金币
    private int power;// 体力
    private int powerTime;
    private int ranks;// 军阶
    private long exploit;// 军功
    private int job;// 玩家在军团中的职位
    private long fight;// 战斗力
    private long maxFight;// 历史最大战力
    private int newState;// 新手引导步骤
    private int newerGift;// 0未领取新手礼包 1已领取
    private int onTime;// 最近一次上线时间
    private int olTime;// 当日在线时长
    private int offTime;// 最近一次离线时间
    private int ctTime;// 在线奖励倒计时开始时间
    private int olAward;// 领取了第几个在线奖励
    private int olMonth;// 每月登录天数，值=月份*10000+登录时间*100+天数
    private int silence;// 禁言
    private int taskTime;
    private int buildCount;
    private int heroToken;// 将令
    // 普通副本进度
    public int combatId;
    private int mouthCardDay;// 月卡剩余天数
    private int mouthCLastTime; // 最近一次发放月卡时间
    private int credit;// 师徒积分
    private int refreshTime;// 最近一次刷新时间
    private int honor;// 荣誉点数
    private int goldBar;// 金条数
    private int agentAllLv;// 特工总等级用于亲密度排行,此值不会序列化到数据库
    private int pitchType1CombatId;// 荣耀演练场 type=1的combatId,此值不会序列化到数据库
    private long treasureWareGolden;//宝具金锭 用于打造宝具
    private long treasureWareDust;//宝具微尘 用于升级宝具
    private long treasureWareEssence;//宝具精华 用于宝具突破
    private int ancientBook;//古籍，用于武将天赋
    private Map<Integer, Integer> buildingInfo;// 玩家初始建筑信息，例如: [建筑id, 地基id]
    private int farmerMax; // 农民上限
    private int farmerCnt; // 农民数量
    private int scoutCnt; // 初始侦察兵数量

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void clearJob() {
        job = 0;
    }

    public long getLordId() {
        return lordId;
    }

    public void setLordId(long lordId) {
        this.lordId = lordId;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public int getPortrait() {
        return portrait;
    }

    public void setPortrait(int portrait) {
        this.portrait = portrait;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public int getCamp() {
        return camp;
    }

    public void setCamp(int camp) {
        this.camp = camp;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getExp() {
        return exp;
    }

    public void setExp(long exp) {
        this.exp = exp;
    }

    public int getVip() {
        return vip;
    }

    public void setVip(int vip) {
        this.vip = vip;
    }

    public int getTopup() {
        return topup;
    }

    public void setTopup(int topup) {
        this.topup = topup;
    }

    public int getArea() {
        return area;
    }

    public void setArea(int area) {
        this.area = area;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public int getGoldCost() {
        return goldCost;
    }

    public void setGoldCost(int goldCost) {
        this.goldCost = goldCost;
    }

    public int getGoldGive() {
        return goldGive;
    }

    public void setGoldGive(int goldGive) {
        this.goldGive = goldGive;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getPowerTime() {
        return powerTime;
    }

    public void setPowerTime(int powerTime) {
        this.powerTime = powerTime;
    }

    public int getRanks() {
        return ranks;
    }

    public void setRanks(int ranks) {
        this.ranks = ranks;
    }

    public long getExploit() {
        return exploit;
    }

    public void setExploit(long exploit) {
        this.exploit = exploit;
    }

    public int getJob() {
        return job;
    }

    public void setJob(int job) {
        this.job = job;
    }

    public long getFight() {
        return fight;
    }

    public void setFight(long fight) {
        if (fight > maxFight) {
            this.maxFight = fight;
        }
        this.fight = fight;
    }

    public int getNewState() {
        return newState;
    }

    public void setNewState(int newState) {
        this.newState = newState;
    }

    public int getNewerGift() {
        return newerGift;
    }

    public void setNewerGift(int newerGift) {
        this.newerGift = newerGift;
    }

    public int getOnTime() {
        return onTime;
    }

    public void setOnTime(int onTime) {
        this.onTime = onTime;
    }

    public int getOlTime() {
        return olTime;
    }

    public void setOlTime(int olTime) {
        this.olTime = olTime;
    }

    public int getOffTime() {
        return offTime;
    }

    public void setOffTime(int offTime) {
        this.offTime = offTime;
    }

    public int getCtTime() {
        return ctTime;
    }

    public void setCtTime(int ctTime) {
        this.ctTime = ctTime;
    }

    public int getOlAward() {
        return olAward;
    }

    public void setOlAward(int olAward) {
        this.olAward = olAward;
    }

    public int getOlMonth() {
        return olMonth;
    }

    public void setOlMonth(int olMonth) {
        this.olMonth = olMonth;
    }

    public int getSilence() {
        return silence;
    }

    public void setSilence(int silence) {
        this.silence = silence;
    }

    public int getBuildCount() {
        return buildCount;
    }

    public void setBuildCount(int buildCount) {
        this.buildCount = buildCount;
    }

    public int getTaskTime() {
        return taskTime;
    }

    public void setTaskTime(int taskTime) {
        this.taskTime = taskTime;
    }

    public int getCombatId() {
        return combatId;
    }

    public void setCombatId(int combatId) {
        this.combatId = combatId;
    }

    public int getHeroToken() {
        return heroToken;
    }

    public void setHeroToken(int heroToken) {
        this.heroToken = heroToken;
    }

    public int getMouthCardDay() {
        return mouthCardDay;
    }

    public void setMouthCardDay(int mouthCardDay) {
        this.mouthCardDay = mouthCardDay;
    }

    public int getMouthCLastTime() {
        return mouthCLastTime;
    }

    public void setMouthCLastTime(int mouthCLastTime) {
        this.mouthCLastTime = mouthCLastTime;
    }

    public int getCredit() {
        return credit;
    }

    public void setCredit(int credit) {
        this.credit = credit;
    }

    public int getRefreshTime() {
        return refreshTime;
    }

    public void setRefreshTime(int refreshTime) {
        this.refreshTime = refreshTime;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public int getVipExp() {
        return vipExp;
    }

    public void setVipExp(int vipExp) {
        this.vipExp = vipExp;
    }

    public int getAgentAllLv() {
        return agentAllLv;
    }

    public void setAgentAllLv(int agentAllLv) {
        this.agentAllLv = agentAllLv;
    }

    public int getHonor() {
        return honor;
    }

    public void setHonor(int honor) {
        this.honor = honor;
    }

    public int getGoldBar() {
        return goldBar;
    }

    public void setGoldBar(int goldBar) {
        this.goldBar = goldBar;
    }

    public int getPitchType1CombatId() {
        return pitchType1CombatId;
    }

    public void setPitchType1CombatId(int pitchType1CombatId) {
        this.pitchType1CombatId = pitchType1CombatId;
    }

    public long getTreasureWareGolden() {
        return treasureWareGolden;
    }

    public void setTreasureWareGolden(long treasureWareGolden) {
        this.treasureWareGolden = treasureWareGolden;
    }

    public long getTreasureWareDust() {
        return treasureWareDust;
    }

    public void setTreasureWareDust(long treasureWareDust) {
        this.treasureWareDust = treasureWareDust;
    }

    public long getTreasureWareEssence() {
        return treasureWareEssence;
    }

    public void setTreasureWareEssence(long treasureWareEssence) {
        this.treasureWareEssence = treasureWareEssence;
    }

    public int getAncientBook() {
        return ancientBook;
    }

    public void setAncientBook(int ancientBook) {
        this.ancientBook = ancientBook;
    }

    public long getMaxFight() {
        return maxFight;
    }

    public void setMaxFight(long maxFight) {
        this.maxFight = maxFight;
    }

    public Map<Integer, Integer> getBuildingInfo() {
        return buildingInfo;
    }

    public void setBuildingInfo(Map<Integer, Integer> buildingInfo) {
        this.buildingInfo = buildingInfo;
    }

    public int getFarmerMax() {
        return farmerMax;
    }

    public void setFarmerMax(int farmerMax) {
        this.farmerMax = farmerMax;
    }

    public int getFarmerCnt() {
        return farmerCnt;
    }

    public void setFarmerCnt(int farmerCnt) {
        this.farmerCnt = farmerCnt;
    }

    public int getScoutCnt() {
        return scoutCnt;
    }

    public void setScoutCnt(int scoutCnt) {
        this.scoutCnt = scoutCnt;
    }

    @Override
    public String toString() {
        return "Lord [lordId=" + lordId + ", nick=" + nick + ", signature=" + signature + ", portrait=" + portrait
                + ", sex=" + sex + ", camp=" + camp + ", level=" + level + ", exp=" + exp + ", vip=" + vip + ", topup="
                + topup + ", area=" + area + ", pos=" + pos + ", gold=" + gold + ", goldCost=" + goldCost
                + ", goldGive=" + goldGive + ", power=" + power + ", powerTime=" + powerTime + ", ranks=" + ranks
                + ", exploit=" + exploit + ", job=" + job + ", fight=" + fight + ", newState=" + newState
                + ", newerGift=" + newerGift + ", onTime=" + onTime + ", olTime=" + olTime + ", offTime=" + offTime
                + ", ctTime=" + ctTime + ", olAward=" + olAward + ", olMonth=" + olMonth + ", silence=" + silence
                + ", taskTime=" + taskTime + ", buildCount=" + buildCount + ", heroToken=" + heroToken + ", combatId="
                + combatId + ", mouthCardDay=" + mouthCardDay + ", mouthCLastTime=" + mouthCLastTime + ", credit="
                + credit + ", refreshTime=" + refreshTime + ", honor=" + honor + ", goldBar=" + goldBar + "]";
    }

}
