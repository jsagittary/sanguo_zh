package com.gryphpoem.game.zw.resource.domain.s;

/**
 * @author QiuKun
 * @ClassName StaticPay.java
 * @Description: 充值支付类型配置
 * @date 2017年6月24日
 */
public class StaticPay {

    private int payId;
    /**
     * 充值金币数量
     */
    private int topup;
    /**
     * 充值金币额外送的比例 百分比整数
     */
    private int extraGold;
    /**
     * 购买类型 0.金币 1.月卡,2.礼包
     */
    private int banFlag;
    /**
     * 客户端显示顺序
     */
    private int asset;
    /**
     * appstore和GP的productId，国服可以不填
     */
    private String productId;
    /**
     * 图标地址
     */
    private String icon;
    /**
     * 对应金币数的价格
     */
    private int price;
    /**
     * 对应vip经验
     */
    private int vipexp;
    /**
     * 充值获取的钻石
     */
    private int eventpts;
    /**
     * 美金价格
     */
    private float usd;

    public int getPayId() {
        return payId;
    }

    public void setPayId(int payId) {
        this.payId = payId;
    }

    public int getTopup() {
        return topup;
    }

    public void setTopup(int topup) {
        this.topup = topup;
    }

    public int getExtraGold() {
        return extraGold;
    }

    public void setExtraGold(int extraGold) {
        this.extraGold = extraGold;
    }

    public int getBanFlag() {
        return banFlag;
    }

    public void setBanFlag(int banFlag) {
        this.banFlag = banFlag;
    }

    public int getAsset() {
        return asset;
    }

    public void setAsset(int asset) {
        this.asset = asset;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getVipexp() {
        return vipexp;
    }

    public void setVipexp(int vipexp) {
        this.vipexp = vipexp;
    }

    public int getEventpts() {
        return eventpts;
    }

    public void setEventpts(int eventpts) {
        this.eventpts = eventpts;
    }

    public float getUsd() {
        return usd;
    }

    public void setUsd(float usd) {
        this.usd = usd;
    }
}
