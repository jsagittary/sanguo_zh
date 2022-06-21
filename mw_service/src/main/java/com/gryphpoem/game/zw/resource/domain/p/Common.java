package com.gryphpoem.game.zw.resource.domain.p;

import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.HeroConstant;
import com.gryphpoem.game.zw.resource.constant.WorldConstant;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

public class Common implements Cloneable {
    private long lordId;
    private int baptizeCnt;// 装备洗练次数
    private int baptizeTime;// 洗练刷新时间
    private int reBuild;// 重建次数
    private int scoutCdTime;// 侦查CD结束时间
    private int bagCnt;// 装备容量
    private int bagBuy;// 装备容量购买次数
//    private int washCount;// 将领洗髓免费次数
//    private int washTime;// 免费洗髓次数下次更新时间
    private int autoArmy;// 自动补兵
//    private int heroCdTime;// 良将寻访功能，下次免费寻访CD结束时间
//    private int normalHero;// 良将已寻访次数
//    private int superProcess;// 神将寻访解锁进度，百分比（0-100）
//    private int superHero;// 神将已寻访次数,不会继续清0操作
//    private int superTime;// 如果已激活神将，本次结束时间
//    private int superOpenNum;// 神将已激活次数，用于做首次激活特殊处理
//    private int superFreeNum;// 神将寻访免费次数
    private int lineAdd;// 兵排额外加
    private int resCnt;// 能领取资源的次数(每小时加一次)
    private int resTime;// 上次领取时间
    private int buyAct;
    private int retreat;
    private int killNum; // 杀敌次数
    private int renameCnt; // 改名次数
    private int autoBuildCnt;// 自动建造剩余次数
    private int autoBuildOnOff;// 自动建造开关,0表示关闭,1表示开启
    private int treasureWareCnt;//宝具背包容量
    private int buyTreasureWareBagCnt;//购买宝具背包次数

    /**
     * 免费洗髓次数是否已满
     * 
     * @return
     */
//    public boolean washCountFull() {
//        return getWashCount() >= WorldConstant.HERO_WASH_FREE_MAX;
//    }

//    /**
//     * 开启洗髓免费次数刷新定时
//     *
//     * @param now
//     */
//    public void beginWashTime(int now) {
//        washTime = now + WorldConstant.HERO_WASH_TIME;
//    }

//    /**
//     * 洗髓次数刷新定时结束处理
//     *
//     * @param now
//     */
//    public void washTimeEnd(int now) {
//        if (washCount < WorldConstant.HERO_WASH_FREE_MAX) {
//            washCount++;
//            if (washCount < WorldConstant.HERO_WASH_FREE_MAX) {
//                beginWashTime(now);// 增加后仍未到上限，开启下一次刷新定时
//                return;
//            }
//        }
//        washTime = 0;
//    }

//    public void addHeroSearchSuperProcess(int process) {
//        if (superProcess >= Constant.INT_HUNDRED) {
//            return;
//        }
//
//        this.superProcess += process;
//        if (superProcess >= Constant.INT_HUNDRED) {
//            superProcess = Constant.INT_HUNDRED;
//            superOpenNum = 1;// 记录神将激活次数
//
//            // 记录失效时间
//            superTime = TimeHelper.getCurrentSecond() + HeroConstant.SUPER_OPEN_TIME;
//        }
//    }

    public long getLordId() {
        return lordId;
    }

    public void setLordId(long lordId) {
        this.lordId = lordId;
    }

    public int getBaptizeCnt() {
        return baptizeCnt;
    }

    public void setBaptizeCnt(int baptizeCnt) {
        this.baptizeCnt = baptizeCnt;
    }

    public int getBaptizeTime() {
        return baptizeTime;
    }

    public void setBaptizeTime(int baptizeTime) {
        this.baptizeTime = baptizeTime;
    }

    public int getReBuild() {
        return reBuild;
    }

    public void setReBuild(int reBuild) {
        this.reBuild = reBuild;
    }

    public int getScoutCdTime() {
        return scoutCdTime;
    }

    public void setScoutCdTime(int scoutCdTime) {
        this.scoutCdTime = scoutCdTime;
    }

    public int getBagCnt() {
        return bagCnt;
    }

    public void setBagCnt(int bagCnt) {
        this.bagCnt = bagCnt;
    }

    public int getBagBuy() {
        return bagBuy;
    }

    public void setBagBuy(int bagBuy) {
        this.bagBuy = bagBuy;
    }
//
//    public int getWashCount() {
//        return washCount;
//    }
//
//    public void setWashCount(int washCount) {
//        this.washCount = washCount;
//    }
//
//    public int getWashTime() {
//        return washTime;
//    }
//
//    public void setWashTime(int washTime) {
//        this.washTime = washTime;
//    }

    public int getAutoArmy() {
        return autoArmy;
    }

    public void setAutoArmy(int autoArmy) {
        this.autoArmy = autoArmy;
    }
//
//    public int getHeroCdTime() {
//        return heroCdTime;
//    }
//
//    public void setHeroCdTime(int heroCdTime) {
//        this.heroCdTime = heroCdTime;
//    }
//
//    public int getNormalHero() {
//        return normalHero;
//    }
//
//    public void setNormalHero(int normalHero) {
//        this.normalHero = normalHero;
//    }
//
//    public int getSuperProcess() {
//        return superProcess;
//    }
//
//    public void setSuperProcess(int superProcess) {
//        this.superProcess = superProcess;
//    }
//
//    public int getSuperHero() {
//        return superHero;
//    }
//
//    public void setSuperHero(int superHero) {
//        this.superHero = superHero;
//    }
//
//    public int getSuperTime() {
//        return superTime;
//    }
//
//    public void setSuperTime(int superTime) {
//        this.superTime = superTime;
//    }
//
//    public int getSuperOpenNum() {
//        return superOpenNum;
//    }
//
//    public void setSuperOpenNum(int superOpenNum) {
//        this.superOpenNum = superOpenNum;
//    }

    public int getLineAdd() {
        return lineAdd;
    }

    public void setLineAdd(int lineAdd) {
        this.lineAdd = lineAdd;
    }

//    public int getSuperFreeNum() {
//        return superFreeNum;
//    }
//
//    public void setSuperFreeNum(int superFreeNum) {
//        this.superFreeNum = superFreeNum;
//    }

    public int getResCnt() {
        return resCnt;
    }

    public void setResCnt(int resCnt) {
        this.resCnt = resCnt;
    }

    public int getResTime() {
        return resTime;
    }

    public void setResTime(int resTime) {
        this.resTime = resTime;
    }

    public int getBuyAct() {
        return buyAct;
    }

    public void setBuyAct(int buyAct) {
        this.buyAct = buyAct;
    }

    public int getRetreat() {
        return retreat;
    }

    public void setRetreat(int retreat) {
        this.retreat = retreat;
    }

    public int getKillNum() {
        return killNum;
    }

    public void setKillNum(int killNum) {
        this.killNum = killNum;
    }

    /** 杀敌数+1 */
    public void incrKillNum() {
        killNum++;
    }

    public int getRenameCnt() {
        return renameCnt;
    }

    public void setRenameCnt(int renameCnt) {
        this.renameCnt = renameCnt;
    }

    public int getAutoBuildCnt() {
        return autoBuildCnt;
    }

    public void setAutoBuildCnt(int autoBuildCnt) {
        this.autoBuildCnt = autoBuildCnt;
    }

    /** 自动建造次数减少 */
    public void decAutoBuildCnt() {
        if (autoBuildCnt > 0) {
            autoBuildCnt--;
        }
    }

    public int getAutoBuildOnOff() {
        return autoBuildOnOff;
    }

    public void setAutoBuildOnOff(int autoBuildOnOff) {
        this.autoBuildOnOff = autoBuildOnOff;
    }

    public int getTreasureWareCnt() {
        if (this.treasureWareCnt == 0) {
            this.treasureWareCnt = Constant.TREASURE_WARE_BAG_INIT;
        }
        return treasureWareCnt;
    }

    public void setTreasureWareCnt(int treasureWareCnt) {
        this.treasureWareCnt = treasureWareCnt;
    }

    public int getBuyTreasureWareBagCnt() {
        return buyTreasureWareBagCnt;
    }

    public void setBuyTreasureWareBagCnt(int buyTreasureWareBagCnt) {
        this.buyTreasureWareBagCnt = buyTreasureWareBagCnt;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 打印将领寻访功能相关的数据
     * 
     * @return
     */
//    public String heroSearchToString() {
//        return "Common [lordId=" + lordId + ", heroCdTime=" + heroCdTime + ", normalHero=" + normalHero
//                + ", superProcess=" + superProcess + ", superHero=" + superHero + ", superTime=" + superTime
//                + ", superOpenNum=" + superOpenNum + ", superFreeNum=" + superFreeNum + "]";
//    }
}
