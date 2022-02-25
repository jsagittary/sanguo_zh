package com.gryphpoem.game.zw.resource.util.eventdata;

import com.alibaba.fastjson.JSON;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Account;
import com.gryphpoem.game.zw.resource.domain.p.Lord;
import com.gryphpoem.game.zw.resource.domain.p.Resource;
import com.gryphpoem.game.zw.resource.pojo.fight.Fighter;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.server.SendEventDataServer;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * ClassName: EventDataUp
 * 掌游安锋数据上报
 * Date:      2020/8/14 15:17
 * author     shi.pei
 */
public class EventDataUp {

    //    static final String SA_SERVER_URL = "https://dotlog.dian5.com/api/event/report";
    //    static final int PROJECT_ID = 89;
    //    static final String PROJECT_KEY = "e8ce20b811fbb7b2694472d223684df2";
    public static Logger GAME_LOGGER = Logger.getLogger("GAME");

    public static List<Map<String, Object>> eventList = new CopyOnWriteArrayList<>();

    /**
     * 到达最大容量，开始上报
     *
     * @param parameter
     */
    public static void request(int type, Map<String, Object> parameter) {
        if (eventList.size() < 50) {
            eventList.add(parameter);
        } else {
            sendData();
        }
    }


    /**
     * 数据上报
     * 定时执行、停服执行上报
     */
    public static void allRequest() {
        sendData();
    }

    /**
     * 事件发送
     */
    public static void sendData() {
        if (eventList.size() >= 1) {
            StringBuilder body = new StringBuilder();
            for (Map<String, Object> map : eventList) {
                if (body.toString().equals("")) {
                    body = new StringBuilder(JSON.toJSONString(map));
                } else {
                    body.append("\n").append(JSON.toJSONString(map));
                }
            }
            SendEventDataServer.getIns().sendData(body.toString());
            eventList.clear();
        }
    }

    /**
     * 公共属性
     *
     * @param account
     * @param lord
     * @return
     */
    private static Map<String, Object> getCommonParams(Account account, Lord lord) {
        int serverId = account.getServerId();
        long lordId = lord.getLordId();
        int vip = lord.getVip();
        int level = lord.getLevel();
//        int topup = lord.getTopup();
        String nick = lord.getNick();
        long fight = lord.getFight();
//        int gold = lord.getGold();
        Map<String, Object> common = new HashMap<String, Object>();
        //        common.put("$token","$token"); //$token拿不到,跟接入方协商不传了
        common.put("group_id", serverId);
        //        common.put("group_name",serverId); //区服名称,提供
        common.put("role_id", lordId);
        common.put("role_name", nick);
        common.put("role_level", level);
        common.put("vip_level", vip);
        common.put("power", fight);
        return common;
    }

    /**
     * 金币、钻石，增减
     *
     * @param from
     * @param account
     * @param lord
     * @param gold
     */
    public static void gold(AwardFrom from, Account account, Lord lord, int gold) {
        if (account == null || lord == null) {
            return;
        }
        // 检测数数上报的功能
        if (functionUnlock(account)) {
            return;
        }
        Map<String, Object> common = getCommonParams(account, lord); //通用属性，后面塞特殊属性
        common.put("money_num", gold);
        int opType = gold > 0 ? 1 : 2;
        common.put("op_type", opType);
        common.put("type", from.getCode());
        common.put("money_total", lord.getGold());
        Map<String, Object> propert = new HashMap<>(); //固定格式，只改name
        propert.put("name", "money");
        propert.put("time", TimeHelper.getCurrentSecond());
        propert.put("data", common);
        Map<String, Object> properties = new HashMap<>(); //固定格式
        properties.put("type", "track");
        properties.put("data", propert);
        request(1, properties);
    }

    /**
     * 货币，增减
     * @param from
     * @param account
     * @param lord
     * @param type 货币类型：16宝具金锭,17宝具微尘, 18宝具精华
     * @param change 变更数量
     * @param total  变更后的货币存量
     */
    public static void currency(AwardFrom from, Account account, Lord lord,int type, long change,long total) {
        if (account == null || lord == null) {
            return;
        }
        // 检测数数上报的功能
        if (functionUnlock(account)) {
            return;
        }
        Map<String, Object> common = getCommonParams(account, lord); //通用属性，后面塞特殊属性
        common.put("other_currency_type",type); //货币类型
        common.put("other_currency_nums", change); //变更数量
        int opType = change > 0 ? 1 : 2;
        common.put("other_currency_change_type", opType);  //变更类型：1增加，2减少
        common.put("other_currency_change_reason", from.getCode()); //变更原因
        common.put("other_currency_total", total);  //变更后的货币存量
        Map<String, Object> propert = new HashMap<>(); //固定格式，只改name
        propert.put("name", "other_currency"); //
        propert.put("time", TimeHelper.getCurrentSecond());
        propert.put("data", common);
        Map<String, Object> properties = new HashMap<>(); //固定格式
        properties.put("type", "track");
        properties.put("data", propert);
        request(1, properties);
    }

    /**
     * 道具，产出消耗
     *
     * @param from
     * @param account
     * @param lord
     * @param propId
     * @propType 3将领,4道具,5装备,10宝石,9头像,11聊天气泡,14勋章,19皮肤,27图腾,29宝具
     * @param count
     * @param action
     */
    public static void prop(AwardFrom from, Account account, Lord lord, int propId, int propType, int count, int action,int keyId) {
        if (account == null || lord == null) {
            return;
        }
        // 检测数数上报的功能
        if (functionUnlock(account)) {
            return;
        }
        Map<String, Object> common = getCommonParams(account, lord);
        common.put("props_id", propId);
        common.put("props_type", propType);
        int opType = action > 0 ? 1 : 2;
        common.put("op_type", opType); //变动类型，1增2减
        common.put("props_num", count); //变动数量
        common.put("type", from.getCode());
        common.put("props_keyid",keyId);
        Map<String, Object> propert = new HashMap<>();
        propert.put("name", "props");
        propert.put("time", TimeHelper.getCurrentSecond());
        propert.put("data", common);
        Map<String, Object> properties = new HashMap<>();
        properties.put("type", "track");
        properties.put("data", propert);
        request(2, properties);
    }

    /**
     * 资源变动
     *
     * @param from
     * @param account
     * @param lord
     * @param resource
     * @param id
     * @param add
     */
    static public void resource(AwardFrom from, Account account, Lord lord, Resource resource, int id, long add) {
        if (account == null || lord == null) {
            return;
        }
        // 检测数数上报的功能
        if (functionUnlock(account)) {
            return;
        }
        Map<String, Object> common = getCommonParams(account, lord);
        common.put("type", id);
        int opType = add > 0 ? 1 : 2;
        common.put("op_type", opType); //变动类型，1增2减
        common.put("num", add); //变动数量
        common.put("chang_type", from.getCode());
        common.put("get_gold", resource.getOil()); //黄金
        common.put("get_wood", resource.getElec()); //木材
        common.put("get_food", resource.getFood()); //粮食
        common.put("get_ore", resource.getOre()); //矿石

        Map<String, Object> propert = new HashMap<>();
        propert.put("name", "resources");
        propert.put("time", TimeHelper.getCurrentSecond());
        propert.put("data", common);
        Map<String, Object> properties = new HashMap<>();
        properties.put("type", "track");
        properties.put("data", propert);
        request(3, properties);

    }

    /**
     * 创建订单
     */
    public static void orderCreate(Account account, Lord lord, String serialId, int amount, int payId) {
        if (account == null || lord == null) {
            return;
        }
        // 检测数数上报的功能
        if (functionUnlock(account)) {
            return;
        }
        Map<String, Object> common = getCommonParams(account, lord);
        common.put("exp", lord.getExp());
        common.put("money", lord.getGold());
        common.put("order_sn", serialId);
        common.put("amount", amount);
        common.put("goods_id", payId);
        Map<String, Object> propert = new HashMap<>();
        propert.put("name", "order_create");
        propert.put("time", TimeHelper.getCurrentSecond());
        propert.put("data", common);
        Map<String, Object> properties = new HashMap<>();
        properties.put("type", "track");
        properties.put("data", propert);
        request(5, properties);
    }

    /**
     * 充值、发货成功
     */
    public static void paySuccess(Account account, Lord lord, String serialId, int amount, int payId) {
        if (account == null || lord == null) {
            return;
        }
        // 检测数数上报的功能
        if (functionUnlock(account)) {
            return;
        }
        Map<String, Object> common = getCommonParams(account, lord);
        common.put("exp", lord.getExp());
        common.put("money", lord.getGold());
        common.put("order_sn", serialId);
        common.put("amount", amount);
        common.put("goods_id", payId);
        boolean isGm = false;
        if (amount == 0) { //GM充值
            isGm = true;
        }
        common.put("is_gm", isGm);
        Map<String, Object> propert = new HashMap<>();
        propert.put("name", "order_success");
        propert.put("time", TimeHelper.getCurrentSecond());
        propert.put("data", common);
        Map<String, Object> properties = new HashMap<>();
        properties.put("type", "track");
        properties.put("data", propert);
        request(6, properties);

    }

    public static Logger THINKINGDATA_LOGGER = Logger.getLogger("THINKINGDATA");

    /**
     * 战斗相关
     * @param account
     * @param lord
     * @param fighter 战斗模块
     * @param atk atk:攻击，def:防守
     * @param battleId 
     * @param type 战斗类型 WorldConstant
     * @param win 1 成功，2失败
     * @param sponsorId 发起者id
     */
    public static void battle(Account account, Lord lord, Fighter fighter,String atk,String battleId,String type,String win,long sponsorId){
        if (account == null || lord == null) {
            return;
        }
        // 检测数数上报的功能
        if (functionUnlock(account)) {
            return;
        }
        Map<String, Object> common = getCommonParams(account, lord);
        common.put("battle_id",battleId); //战役唯一标识
        common.put("battle_type",type); //战役类型
        boolean isOrg = false;   //是否发起
        if(sponsorId==lord.getLordId()){
            isOrg = true;
        }
        common.put("is_org",isOrg); 
        boolean isAttacker = false; //是否进攻方
        if(atk.equals("atk")){
            isAttacker = true;
        }else{
            if (win.equals("1")){
                win = "2";
            }else {
                win = "1";
            }
        }
        common.put("is_attacker",isAttacker);  
        common.put("result",win);  //结果
        int heroCount = fighter.getForces().size();
        int j = 1;
        if (heroCount > 0){
            for (int i = 0; i < heroCount; i++) {
                if(fighter.getForces().get(i).ownerId==lord.getLordId() && j<=8){ //判断是否是自己的兵,并且不超过8排兵
                    int heroid = fighter.getForces().get(i).id;
                    int lost = fighter.getForces().get(i).totalLost;
                    common.put("hero" + j, heroid);  //将领
                    common.put("damage" + j, lost); //损兵
                    j++;
                }
            }
        }
        for (int h = j;h<=8;h++){
            common.put("hero" + h, 0);  //将领
            common.put("damage" + h, 0); //损兵
        }
//        THINKINGDATA_LOGGER.info(common);
        
        Map<String, Object> propert = new HashMap<>();
        propert.put("name", "battle");
        propert.put("time", TimeHelper.getCurrentSecond());
        propert.put("data", common);
        Map<String, Object> properties = new HashMap<>();
        properties.put("type", "track");
        properties.put("data", propert);
        request(7, properties);
    }

    /**
     * 积分变更相关
     * @param account
     * @param lord
     * @param creditsTotal 变更后的货币存量
     * @param amount 变更的数量
     * @param opType 变更类型,1圣域，2战火，3叛乱，4沙盘，5叛军来袭，6，日常任务积分
     * @param from 变更描述
     */
    public static void credits(Account account, Lord lord, int creditsTotal, int amount,int opType,AwardFrom from) {
        if (account == null || lord == null) {
            return;
        }
        // 检测数数上报的功能
        if (functionUnlock(account)) {
            return;
        }
        Map<String, Object> common = getCommonParams(account, lord);
        common.put("credits_total",creditsTotal); //变更后的货币存量
        common.put("credits_num",amount);
        int changeType = amount > 0 ? 1 : 2;
        common.put("credits_change_type", changeType); //变动类型，1增2减
        common.put("op_type",opType);
        common.put("reason",from.getCode());

        Map<String, Object> propert = new HashMap<>();
        propert.put("name", "credits");
        propert.put("time", TimeHelper.getCurrentSecond());
        propert.put("data", common);
        Map<String, Object> properties = new HashMap<>();
        properties.put("type", "track");
        properties.put("data", propert);
        request(8, properties);
    }
    


    /**
     * 检测数数上报的功能
     *
     * @param account 账号信息
     * @return true: lock , false: unlock
     */
    private static boolean functionUnlock(Account account) {
        return !Constant.THINKING_DATA_PLAT.contains(account.getPlatNo()) || !"release".equalsIgnoreCase(DataResource.environment);
    }
}
