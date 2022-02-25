package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * VIP
 * 
 * @author tyler
 *
 */
public class StaticVip {
    private int vipLv;
    private int exp;
    private List<List<Integer>> reward;
    private int price;
    private int showPrice;// 购买的显示价格,也是购买时扣的价格
    private int freeBuildTime;
    private int wipe;
    private int buyAct;// 购买体力次数
    private int retreat;// 免费行军召回次数
    private int factoryRecruit;
    private int autoBuild; // 自动建造次数
    private int buyStone; // 购买宝石关卡的次数（买一次增加10次副本次数）
    private int killFriend;//同阵营攻打采集点
    private List<List<Integer>> share;//英雄，兵书每日分享次数,排序为[英雄，兵书]
    private int totemUp;
    /**
     * 佃农额外效果
     */
    private int productionGain;
    /**
     * 铁匠额外效果
     */
    private int speedForgeTime;

    /**
     * 宝具副本额外挂机时长(秒)
     */
    private int cumulativeTime;

    /**
     * 宝具副本额外快速挂机次数
     */
    private int hangUpTime;

    public int getVipLv() {
        return vipLv;
    }

    public void setVipLv(int vipLv) {
        this.vipLv = vipLv;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public List<List<Integer>> getReward() {
        return reward;
    }

    public void setReward(List<List<Integer>> reward) {
        this.reward = reward;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getFreeBuildTime() {
        return freeBuildTime;
    }

    public void setFreeBuildTime(int freeBuildTime) {
        this.freeBuildTime = freeBuildTime;
    }

    public int getWipe() {
        return wipe;
    }

    public void setWipe(int wipe) {
        this.wipe = wipe;
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

    public int getFactoryRecruit() {
        return factoryRecruit;
    }

    public void setFactoryRecruit(int factoryRecruit) {
        this.factoryRecruit = factoryRecruit;
    }

    public int getShowPrice() {
        return showPrice;
    }

    public void setShowPrice(int showPrice) {
        this.showPrice = showPrice;
    }

    public int getAutoBuild() {
        return autoBuild;
    }

    public void setAutoBuild(int autoBuild) {
        this.autoBuild = autoBuild;
    }

    public int getBuyStone() {
        return buyStone;
    }

    public void setBuyStone(int buyStone) {
        this.buyStone = buyStone;
    }

    public List<List<Integer>> getShare() {
        return share;
    }

    public void setShare(List<List<Integer>> share) {
        this.share = share;
    }

    public int getKillFriend() {
        return killFriend;
    }

    public void setKillFriend(int killFriend) {
        this.killFriend = killFriend;
    }

    public int getProductionGain() {
        return productionGain;
    }

    public void setProductionGain(int productionGain) {
        this.productionGain = productionGain;
    }

    public int getSpeedForgeTime() {
        return speedForgeTime;
    }

    public void setSpeedForgeTime(int speedForgeTime) {
        this.speedForgeTime = speedForgeTime;
    }

    public int getCumulativeTime() {
        return cumulativeTime;
    }

    public void setCumulativeTime(int cumulativeTime) {
        this.cumulativeTime = cumulativeTime;
    }

    public int getHangUpTime() {
        return hangUpTime;
    }

    public void setHangUpTime(int hangUpTime) {
        this.hangUpTime = hangUpTime;
    }

    @Override
    public String toString() {
        return "StaticVip{" +
                "vipLv=" + vipLv +
                ", exp=" + exp +
                ", reward=" + reward +
                ", price=" + price +
                ", showPrice=" + showPrice +
                ", freeBuildTime=" + freeBuildTime +
                ", wipe=" + wipe +
                ", buyAct=" + buyAct +
                ", retreat=" + retreat +
                ", factoryRecruit=" + factoryRecruit +
                ", autoBuild=" + autoBuild +
                ", buyStone=" + buyStone +
                ", killFriend=" + killFriend +
                ", share=" + share +
                ", productionGain=" + productionGain +
                ", speedForgeTime=" + speedForgeTime +
                '}';
    }

    public int getTotemUp() {
        return totemUp;
    }

    public void setTotemUp(int totemUp) {
        this.totemUp = totemUp;
    }
}
