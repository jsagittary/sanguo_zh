package com.gryphpoem.game.zw.resource.util.eventdata;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.handler.DealType;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticBuildingDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticHeroDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticNpcDataMgr;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Account;
import com.gryphpoem.game.zw.resource.domain.p.Lord;
import com.gryphpoem.game.zw.resource.domain.p.Resource;
import com.gryphpoem.game.zw.resource.domain.s.StaticHero;
import com.gryphpoem.game.zw.resource.domain.s.StaticNpc;
import com.gryphpoem.game.zw.resource.domain.s.StaticWallHeroLv;
import com.gryphpoem.game.zw.resource.pojo.fight.Fighter;
import com.gryphpoem.game.zw.resource.pojo.fight.Force;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.util.CalculateUtil;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.StringUtil;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.server.SendEventDataServer;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

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

    public static Queue<Map<String, Object>> eventList = new LinkedBlockingQueue<>(51);

    /**
     *
     */
    private static ServerSetting serverSetting = DataResource.ac.getBean(ServerSetting.class);

    /**
     * 到达最大容量，开始上报
     *
     * @param parameter
     */
    public static void request(int type, Map<String, Object> parameter) {
        DataResource.logicServer.addCommandByType(() -> {
            eventList.add(parameter);
            if (eventList.size() >= 50) {
                sendData();
            }
        }, DealType.BACKGROUND);
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
        try {
            common.put("?channel_name", StringUtils.isEmpty(account.getPublisher()) ? "" : account.getPublisher());
        } catch (IllegalArgumentException e) {}
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
    public static void gold(AwardFrom from, Account account, Lord lord, int gold, String info) {
        if (account == null || lord == null) {
            return;
        }
        // 检测数数上报的功能
        if (functionUnlock(account)) {
            return;
        }
        Map<String, Object> common = getCommonParams(account, lord); //通用属性，后面塞特殊属性
        int opType = gold > 0 ? 1 : 2;
        common.put("@public_data", "");
        common.put("main_group_id", serverSetting.getServerID());
        common.put("*money_total", lord.getGold());
        common.put("money_num", Math.abs(gold));
        common.put("money_change_type", opType);
        common.put("money_reason", CheckNull.isNull(from) ? "" : from.getCode());
        common.put("money_info", info);

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
     *
     * @param from
     * @param account
     * @param lord
     * @param type    货币类型：16宝具金锭,17宝具微尘, 18宝具精华
     * @param change  变更数量
     * @param total   变更后的货币存量
     */
    public static void otherCurrency(AwardFrom from, Account account, Lord lord, int type, long change, long total, String info, String info2) {
        if (account == null || lord == null) {
            return;
        }
        // 检测数数上报的功能
        if (functionUnlock(account)) {
            return;
        }
        Map<String, Object> common = getCommonParams(account, lord); //通用属性，后面塞特殊属性
        common.put("main_group_id", serverSetting.getServerID());
        common.remove("vip_level");
        common.put("*vip_level", lord.getVip());
        common.put("knighthood", lord.getRanks());

        common.put("other_currency_type", type); //货币类型
        common.put("other_currency_nums", change); //变更数量
        int opType = change > 0 ? 1 : 2;
        common.put("other_currency_change_type", opType);  //变更类型：1增加，2减少
        common.put("other_currency_change_reason", from.getCode()); //变更原因
        common.put("other_currency_total", total);  //变更后的货币存量
        common.put("other_currency_info", info);
        common.put("other_cuurrency_info2", info2);

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
     * @param count
     * @param action
     * @propType 3将领, 4道具, 5装备, 10宝石, 9头像, 11聊天气泡, 14勋章, 19皮肤, 27图腾, 29宝具
     */
    public static void prop(AwardFrom from, Account account, Lord lord, int propId, int propType, int count, int action, int keyId, String info) {
        if (account == null || lord == null) {
            return;
        }
        // 检测数数上报的功能
        if (functionUnlock(account)) {
            return;
        }
        Map<String, Object> common = getCommonParams(account, lord);
        common.put("@public_data", "");
        common.put("main_group_id", serverSetting.getServerID());
        common.put("money", lord.getGold());
        common.put("props_id", String.valueOf(propId));
        common.put("props_type", String.valueOf(propType));
        int opType = action > 0 ? 1 : 2;
        common.put("props_change_type", opType); //变动类型，1增2减
        common.put("props_num", count); //变动数量
        common.put("props_keyid", keyId);
        common.put("props_info", info);
        common.put("props_reason", CheckNull.isNull(from) ? "" : from.getCode());

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
        common.put("@public_data", "");
        common.put("main_group_id", serverSetting.getServerID());
        common.put("money", lord.getGold());
        common.put("resource_type", id);
        common.put("resource_change_num", add); //变动数量
        int opType = add > 0 ? 1 : 2;
        common.put("resources_change_type", opType); //变动类型，1增2减
        common.put("*after_gold", resource.getOil()); //黄金
        common.put("*after_wood", resource.getElec()); //木材
        common.put("*after_food", resource.getFood()); //粮食
        common.put("*after_ore", resource.getOre()); //矿石
        common.put("resources_reason", from.getCode());

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
        common.put("@public_data", "");
        common.put("main_group_id", serverSetting.getServerID());
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
        common.put("@public_data", "");
        common.put("main_group_id", serverSetting.getServerID());
        common.put("money", lord.getGold());
        common.put("order_sn", serialId);
        common.put("+amount", amount);
        common.put("goods_id", payId);
        common.put("is_first_pay", lord.getTopup() - amount <= 0);
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
     *
     * @param account
     * @param lord
     * @param fighter   战斗模块
     * @param atk       atk:攻击，def:防守
     * @param battleId
     * @param type      战斗类型 WorldConstant
     * @param win       1 成功，2失败
     * @param sponsorId 发起者id
     */
    public static void battle(Account account, Lord lord, Fighter fighter, String atk, String battleId, String type, String win, long sponsorId) {
        if (account == null || lord == null) {
            return;
        }
        // 检测数数上报的功能
        if (functionUnlock(account)) {
            return;
        }
        Map<String, Object> common = getCommonParams(account, lord);
        common.put("@public_data", "");
        common.put("main_group_id", serverSetting.getServerID());
        common.put("money", lord.getGold());
        common.put("battle_area", String.valueOf(lord.getArea()));
        common.put("curr_faction", lord.getCamp());
        common.put("battle_id", battleId); //战役唯一标识
        common.put("battle_type", type); //战役类型
        boolean isOrg = false;   //是否发起
        if (sponsorId == lord.getLordId()) {
            isOrg = true;
        }
        common.put("is_org", isOrg);
        boolean isAttacker = false; //是否进攻方
        if (atk.equals("atk")) {
            isAttacker = true;
        } else {
            if (win.equals("1")) {
                win = "2";
            } else {
                win = "1";
            }
        }
        common.put("is_attacker", isAttacker);
        common.put("battle_result", win);  //结果

        int heroCount = fighter.getForces().size();
        String[] forceArray = new String[heroCount];
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        if (heroCount > 0) {
            StringBuilder forceStr = new StringBuilder();
            for (int i = 0; i < heroCount; i++) {
                Force force = fighter.getForces().get(i);
                if (CheckNull.isNull(force))
                    continue;
                switch (force.roleType) {
                    case Constant.Role.PLAYER:
                        Player player = playerDataManager.getPlayer(force.ownerId);
                        StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(force.ownerId);
                        if (CheckNull.isNull(staticHero))
                            break;
                        if (CheckNull.isNull(player) || CheckNull.isNull(player.heros.get(force.id)))
                            break;
                        Hero hero = player.heros.get(force.id);
                        CalculateUtil.processHeroAttr(player, hero);
                        forceStr.append("{").append(force.ownerId).append(",").append(i + 1).append(",").
                                append(force.id).append(",").append(hero.getFightVal()).append(",").append(staticHero.getType()).append(",").
                                append(force.hp).append(",").append(force.killed).append(",").append(force.totalLost).append(",").append(0).append("}");
                        break;
                    case Constant.Role.BANDIT:
                    case Constant.Role.CITY:
                    case Constant.Role.GESTAPO:
                        StaticNpc npc = StaticNpcDataMgr.getNpcMap().get(force.id);
                        if (CheckNull.isNull(npc)) {
                            break;
                        }
                        forceStr.append("{").append(npc.getNpcId()).append(",").append(i + 1).append(",").
                                append(force.id).append(",").append(0).append(",").append(npc.getArmType()).append(",").
                                append(force.hp).append(",").append(force.killed).append(",").append(force.totalLost).append(",").append(0).append("}");
                        break;
                    case Constant.Role.WALL:
                        StaticWallHeroLv wallNpc = StaticBuildingDataMgr.getStaticWallHeroLv(force.id);
                        if (CheckNull.isNull(wallNpc))
                            break;
                        forceStr.append("{").append(wallNpc.getId()).append(",").append(i + 1).append(",").
                                append(force.id).append(",").append(0).append(",").append(wallNpc.getType()).append(",").
                                append(force.hp).append(",").append(force.killed).append(",").append(force.totalLost).append(",").append(0).append("}");
                        break;
                    default:
                        npc = StaticNpcDataMgr.getNpcMap().get(force.id);
                        if (CheckNull.isNull(npc)) {
                            break;
                        }
                        forceStr.append("{").append(npc.getNpcId()).append(",").append(i + 1).append(",").
                                append(force.id).append(",").append(0).append(",").append(npc.getArmType()).append(",").
                                append(force.hp).append(",").append(force.killed).append(",").append(force.totalLost).append(",").append(0).append("}");
                        break;
                }
                if (forceStr.length() > 0) {
                    forceArray[i] = forceStr.toString();
                    forceStr = new StringBuilder();
                }
            }
        }
        if (isAttacker)
            common.put("attacker_troops", forceArray);
        else
            common.put("defender_troops", forceArray);

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
     * 矿点资源变更上报
     *
     * @param player
     * @param troopType
     * @param mineType
     * @param lv
     * @param time
     * @param grab
     */
    public static void troop(Player player, int troopType, int mineType, int lv, int time, List<CommonPb.Award> grab) {
        if (Objects.isNull(player)) {
            return;
        }
        Account account = player.account;
        // 检测数数上报的功能
        if (functionUnlock(account)) {
            return;
        }
        Lord lord = player.lord;
        Map<String, Object> common = getCommonParams(account, lord);
        common.put("@public_data", "");
        common.put("main_group_id", serverSetting.getServerID());
        common.put("money", lord.getGold());
        common.put("curr_faction", lord.getCamp());
        common.put("troop_area", lord.getArea());
        common.put("troop_type", troopType);
        common.put("target_type", mineType);
        common.put("target_level", lv);
        common.put("troop_time", time);
        JSONArray result = grab.stream().map(award -> {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", award.getType());
            jsonObject.put("id", award.getId());
            jsonObject.put("count", award.getCount());
            return jsonObject;
        }).collect(Collectors.toCollection(JSONArray::new));
        common.put("troop_result", result);

        Map<String, Object> property = new HashMap<>();
        property.put("name", "troop");
        property.put("time", TimeHelper.getCurrentSecond());
        property.put("data", common);
        Map<String, Object> properties = new HashMap<>();
        properties.put("type", "track");
        properties.put("data", property);
        request(0, properties);
    }

    /**
     * 建筑升级上报
     *
     * @param player
     * @param buildingId
     * @param buildingKeyId
     * @param lv
     */
    public static void buildingSuccess(Player player, int buildingId, int buildingKeyId, int lv) {
        if (Objects.isNull(player)) {
            return;
        }
        Account account = player.account;
        // 检测数数上报的功能
        if (functionUnlock(account)) {
            return;
        }
        Lord lord = player.lord;
        Map<String, Object> common = getCommonParams(account, lord);
        common.put("@public_data", "");
        common.put("main_group_id", serverSetting.getServerID());
        common.put("money", lord.getGold());
        common.put("faction", lord.getCamp());
        common.put("building_id", buildingId);
        common.put("building_keyid", buildingKeyId);
        common.put("building_level", lv);

        Map<String, Object> property = new HashMap<>();
        property.put("name", "building_success");
        property.put("time", TimeHelper.getCurrentSecond());
        property.put("data", common);
        Map<String, Object> properties = new HashMap<>();
        properties.put("type", "track");
        properties.put("data", property);
        request(0, properties);
    }

    /**
     * 科技升级上报
     *
     * @param player
     * @param techType
     * @param techId
     * @param lv
     */
    public static void technologySuccess(Player player, int techType, int techId, int lv) {
        if (Objects.isNull(player)) {
            return;
        }
        Account account = player.account;
        // 检测数数上报的功能
        if (functionUnlock(account)) {
            return;
        }
        Lord lord = player.lord;
        Map<String, Object> common = getCommonParams(account, lord);
        common.put("@public_data", "");
        common.put("main_group_id", serverSetting.getServerID());
        common.put("money", lord.getGold());
        common.put("faction", lord.getCamp());
        common.put("technology_type", techType);
        common.put("technology_id", techId);
        common.put("technology_level", lv);

        Map<String, Object> property = new HashMap<>();
        property.put("name", "technology_success");
        property.put("time", TimeHelper.getCurrentSecond());
        property.put("data", common);
        Map<String, Object> properties = new HashMap<>();
        properties.put("type", "track");
        properties.put("data", property);
        request(0, properties);
    }

    /**
     * 兵力变化上报
     *
     * @param account
     * @param lord
     * @param from
     * @param armyType
     * @param add
     * @param current
     */
    public static void heroArmy(Account account, Lord lord, AwardFrom from, int armyType, int add, int current) {
        if (Objects.isNull(account) || CheckNull.isNull(lord)) {
            return;
        }
        // 检测数数上报的功能
        if (functionUnlock(account)) {
            return;
        }
        Map<String, Object> common = getCommonParams(account, lord);
        common.put("@public_data", "");
        common.put("main_group_id", serverSetting.getServerID());
        common.put("money", lord.getGold());
        common.put("faction", lord.getCamp());
        common.put("army_type", armyType);
        int changeType = add > 0 ? 1 : 2;
        common.put("army_change_type", changeType);
        common.put("army_nums", add);
        common.put("army_after_nums", current);
        common.put("army_change_reason", CheckNull.isNull(from) ? "" : from.getCode());

        Map<String, Object> property = new HashMap<>();
        property.put("name", "army");
        property.put("time", TimeHelper.getCurrentSecond());
        property.put("data", common);
        Map<String, Object> properties = new HashMap<>();
        properties.put("type", "track");
        properties.put("data", property);
        request(0, properties);
    }

    /**
     * 活动积分
     *
     * @param account
     * @param lord
     * @param from
     * @param actType
     * @param actId
     * @param add
     * @param current
     */
    public static void activityCredits(Account account, Lord lord, AwardFrom from, int actType, int actId, int add, int current) {
        if (Objects.isNull(account) || CheckNull.isNull(lord)) {
            return;
        }
        // 检测数数上报的功能
        if (functionUnlock(account)) {
            return;
        }
        Map<String, Object> common = getCommonParams(account, lord);
        common.put("@public_data", "");
        common.put("main_group_id", serverSetting.getServerID());
        common.put("activity_credits_total", current);
        int changeType = add > 0 ? 1 : 2;
        common.put("activity_credits_change_type", changeType);
        common.put("activity_credits_num", add);
        common.put("activity_credits_reason", CheckNull.isNull(from) ? "" : from.getCode());
        common.put("activity_type", actType);
        common.put("activity_id", actId);

        Map<String, Object> property = new HashMap<>();
        property.put("name", "activity_credits");
        property.put("time", TimeHelper.getCurrentSecond());
        property.put("data", common);
        Map<String, Object> properties = new HashMap<>();
        properties.put("type", "track");
        properties.put("data", property);
        request(0, properties);
    }

    /**
     * 积分变更相关
     *
     * @param account
     * @param lord
     * @param creditsTotal 变更后的货币存量
     * @param amount       变更的数量
     * @param opType       变更类型,1圣域，2战火，3叛乱，4沙盘，5叛军来袭，6，日常任务积分
     * @param from         变更描述
     */
    public static void credits(Account account, Lord lord, int creditsTotal, int amount, int opType, AwardFrom from) {
        if (account == null || lord == null) {
            return;
        }
        // 检测数数上报的功能
        if (functionUnlock(account)) {
            return;
        }
        Map<String, Object> common = getCommonParams(account, lord);
        common.put("@public_data", "");
        common.put("main_group_id", serverSetting.getServerID());
        common.put("credits_total", creditsTotal); //变更后的货币存量
        common.put("credits_num", amount);
        int changeType = amount > 0 ? 1 : 2;
        common.put("credits_change_type", changeType); //变动类型，1增2减
        common.put("credits_type", opType);
        common.put("credits_reason", from.getCode());

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
        boolean b = !Constant.THINKING_DATA_PLAT.contains(account.getPlatNo()) || !"release".equalsIgnoreCase(DataResource.environment);
        if (!b) {
            LogUtil.common(String.format("send event data false, platNo:%d, DataResource.environment:%s", account.getPlatNo(), DataResource.environment));
        }
        return b;
    }
}
