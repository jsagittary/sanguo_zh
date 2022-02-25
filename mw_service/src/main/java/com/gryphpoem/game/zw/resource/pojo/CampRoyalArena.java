package com.gryphpoem.game.zw.resource.pojo;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.ActParamConstant;

/**
 * 阵营对拼的阵营数据
 * User:        zhoujie
 * Date:        2020/4/2 14:26
 * Description:
 */
public class CampRoyalArena {

    // 阵营
    private int camp;

    // 贡献度
    private int contribution;

    // 大国风范购买次数
    private int countryStyleCnt;

    // 刺探购买次数
    private int detectCnt;

    /**
     * 转点清除数据
     */
    public void clearTaskAndData() {
        this.countryStyleCnt = 0;
        this.detectCnt = 0;
    }

    /**
     * 反序列化
     * @param campRoyalData
     */
    public CampRoyalArena(CommonPb.CampRoyalData campRoyalData) {
        this.camp = campRoyalData.getCamp();
        this.contribution = campRoyalData.getContribution();
        this.countryStyleCnt = campRoyalData.getCountryStyleCnt();
        this.detectCnt = campRoyalData.getDetectCnt();
    }

    /**
     * 自增大国风范购买的次数
     * @return 购买大国风范的次数
     */
    public int incrementCountryStyleCnt() {
        return ++this.countryStyleCnt;
    }


    /**
     * 自增刺探购买的次数
     * @return 购买刺探的次数
     */
    public int incrementDetectStyleCnt() {
        return ++this.detectCnt;
    }


    /**
     * 增加贡献值
     * @param add 增加的值
     * @return 增加后的贡献值
     */
    public int addPst(int add) {
        this.contribution += add;
        return this.countryStyleCnt;
    }

    /**
     * 初始化阵营
     * @param camp
     */
    public CampRoyalArena(int camp) {
        this.camp = camp;
        this.contribution = 0;
        this.countryStyleCnt = 0;
        this.detectCnt = 0;
    }

    /**
     * 序列化
     * @return
     */
    public CommonPb.CampRoyalData ser() {
        CommonPb.CampRoyalData.Builder builder = CommonPb.CampRoyalData.newBuilder();
        builder.setCamp(this.camp);
        builder.setContribution(this.contribution);
        builder.setCountryStyleCnt(this.countryStyleCnt);
        builder.setDetectCnt(this.detectCnt);
        return builder.build();
    }

    public int getContribution() {
        return contribution;
    }

    public void setContribution(int contribution) {
        this.contribution = contribution;
    }

    public int getCountryStyleRadio() {
        int radio = ActParamConstant.ROYAL_ARENA_COUNTRY_STYLE.get(0).get(0);
        if (radio != 0) {
            return this.countryStyleCnt * radio;
        }
        return 0;
    }

    public int getDetectCnt() {
        return detectCnt;
    }

    public int getCountryStyleCnt() {
        return countryStyleCnt;
    }

    public int getCamp() {
        return camp;
    }
}
