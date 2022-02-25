package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.resource.dao.impl.s.StaticDataDao;
import com.gryphpoem.game.zw.resource.domain.s.StaticDailyReward;
import com.gryphpoem.game.zw.resource.domain.s.StaticFunCard;
import com.gryphpoem.game.zw.resource.domain.s.StaticPay;
import com.gryphpoem.game.zw.resource.domain.s.StaticVip;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * VIP
 * 
 * @author tyler
 *
 */
public class StaticVipDataMgr {

    private static StaticDataDao staticDataDao = DataResource.ac.getBean(StaticDataDao.class);

    private static Map<Integer, StaticVip> vipMap;
    // key:platName 平台
    /*private static Map<String, StaticPayPlat> payPlatMap;
    // key:platCode 平台
    private static Map<Integer, StaticPayPlat> payPlatMapByCode = new HashMap<>();*/

    private static List<StaticPay> payList;

    // key: payId
    private static Map<Integer, StaticPay> payMap;

    // private static List<StaticPay> payIosList;

    private static int maxVipLv;

    private static List<StaticDailyReward> sDailyRewardList;

    // 功能卡 key:id
    private static Map<Integer, StaticFunCard> funCardMap;
    // 功能卡 key:type
    private static Map<Integer, List<StaticFunCard>> funCardByGroupTypeMap;

    public static void init() {
        Map<Integer, StaticVip> vipMap = staticDataDao.selectVipMap();
        StaticVipDataMgr.vipMap = vipMap;
        List<StaticPay> payList = staticDataDao.selectPay();
        StaticVipDataMgr.payList = payList;
        StaticVipDataMgr.payMap = payList.stream().collect(Collectors.toMap(StaticPay::getPayId, sPay -> sPay, (oldV, newV) -> newV));
        // StaticVipDataMgr.payIosList = staticDataDao.selectPayIos();
        // StaticVipDataMgr.payPlatMap = staticDataDao.selectPayPlat();
        maxVipLv = vipMap.keySet().stream().max((lv1, lv2) -> lv1.compareTo(lv2)).get();

        /*for (StaticPayPlat p : payPlatMap.values()) {
            payPlatMapByCode.put(p.getPlatCode(), p);
        }*/
        StaticVipDataMgr.sDailyRewardList = staticDataDao.selectDailyReward();
        StaticVipDataMgr.funCardMap = staticDataDao.selectFunCardMap();
        StaticVipDataMgr.funCardByGroupTypeMap = funCardMap.values().stream()
                .collect(Collectors.groupingBy(sf -> sf.getType()));
    }

    public static int getMaxVipLv() {
        return maxVipLv;
    }

    public static StaticVip getVipMap(int id) {
        return vipMap.get(id);
    }

    public static int calcVip(int gold) {
        int lv = 0;
        for (StaticVip vip : vipMap.values()) {
            if (gold >= vip.getExp() && vip.getVipLv() > lv) {
                lv = vip.getVipLv();
            }
        }
        return lv;
    }

    public static List<StaticPay> getPayList() {
        return payList;
    }

    public static StaticPay getPayById(int payId) {
        return payMap.get(payId);
    }

/*    public static List<StaticPay> getPayIosList() {
        return payIosList;
    }*/

    public static StaticPay getStaticPayByPayId(int payId) {
        return getPayByPayId(payId, getPayList());
    }

/*    public static StaticPay getStaticPayIosByPayId(int payId) {
        return getPayByPayId(payId, getPayIosList());
    }*/

    private static StaticPay getPayByPayId(int payId, List<StaticPay> list) {
        if (null == list) {
            return null;
        }
        StaticPay pay = null;
        for (StaticPay p : list) {
            if (p.getPayId() == payId) {
                pay = p;
                break;
            }
        }
        return pay;
    }

    /*    public static StaticPayPlat getPayPlat(String plat) {
        return payPlatMap.get(plat);
    }
    
    public static StaticPayPlat getPayPlatByCode(int platCode) {
        return payPlatMapByCode.get(platCode);
    }
    */
    public static List<StaticDailyReward> getDailyRewardList() {
        return sDailyRewardList;
    }

    public static Map<Integer, StaticFunCard> getFunCardMap() {
        return funCardMap;
    }

    public static List<StaticFunCard> getFunCardByGroupType(int type) {
        return funCardByGroupTypeMap.get(type);
    }

}
