package com.gryphpoem.game.zw.resource.util;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.Java8Utils;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticHeroDataMgr;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb1;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Account;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.p.Lord;
import com.gryphpoem.game.zw.resource.domain.p.Resource;
import com.gryphpoem.game.zw.resource.domain.s.StaticHero;
import com.gryphpoem.game.zw.resource.pojo.Mail;
import com.gryphpoem.game.zw.resource.pojo.attr.TreasureWareAttrItem;
import com.gryphpoem.game.zw.resource.pojo.dressup.BaseDressUpEntity;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.treasureware.TreasureWare;
import com.gryphpoem.game.zw.resource.util.eventdata.EventDataUp;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.util.ObjectUtils;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class LogLordHelper {

    public static Logger GAME_LOGGER = Logger.getLogger("GAME");
    public static Logger POINT_LOGGER = Logger.getLogger("POINT");

    private static StringBuffer getCommonParams(String type, AwardFrom from, Account account, Lord lord) {
        int serverId = account.getServerId();
        long lordId = lord.getLordId();
        int vip = lord.getVip();
        int level = lord.getLevel();
        int topUp = lord.getTopup();
        String nick = lord.getNick();
        int fromCode = CheckNull.isNull(from) ? 0 : from.getCode();
        StringBuffer sb = new StringBuffer();
        sb.append(type).append("|").append(fromCode).append("|").append(serverId).append("|").append(lordId)
                .append("|").append(nick).append("|").append(vip).append("|").append(level).append("|")
                .append(lord.getGold()).append("|").append(topUp).append("|").append(DataResource.ac.getBean(ServerSetting.class).getServerID());
        return sb;
    }

    /**
     * 其他日子打印(不需要携带玩家信息的)
     *
     * @param type
     * @param params
     */
    public static void otherLog(String type, int serverId, Object... params) {
        StringBuffer message = new StringBuffer();
        message.append(type).append("|").append(serverId);
        if (params != null && params.length > 0) {
            Stream.of(params).forEach(p -> message.append("|").append(p));
        }
        GAME_LOGGER.info(message);
    }

    public static void otherLog(String type, AwardFrom from, int serverId, Object... params) {
        StringBuffer message = new StringBuffer();
        message.append(type).append("|").append(from.getCode())
                .append("|").append(serverId);
        if (params != null && params.length > 0) {
            Stream.of(params).forEach(p -> message.append("|").append(p));
        }
        GAME_LOGGER.info(message);
    }

    /**
     * 通用的game日志
     *
     * @param type   日志分割的类型
     * @param from   来源
     * @param player 玩家对象
     * @param params 参数
     */
    public static void commonLog(String type, AwardFrom from, Player player, Object... params) {
        if (player == null) {
            return;
        }
        commonLog(type, from, player.account, player.lord, params);
    }

    /**
     * 通用的game日志
     *
     * @param type
     * @param from
     * @param account
     * @param lord
     * @param params
     */
    public static void commonLog(String type, AwardFrom from, Account account, Lord lord, Object... params) {
        if (account == null || lord == null) {
            return;
        }
        final StringBuffer message = getCommonParams(type, from, account, lord);
        if (params != null && params.length > 0) {
            Stream.of(params).filter(Objects::nonNull).forEach(p -> message.append("|").append(p));
        }
        GAME_LOGGER.info(message);
    }

    /**
     * 记录打折礼包购买
     *
     * @param from
     * @param account
     * @param lord
     * @param gold
     * @param giftId
     * @param type    ActivityType
     */
    public static void gift(AwardFrom from, Account account, Lord lord, int gold, int giftId, int type) {
        if (account == null || lord == null) {
            return;
        }
        StringBuffer message = getCommonParams("gift", from, account, lord).append("|").append(gold).append("|")
                .append(giftId).append("|").append(type);
        GAME_LOGGER.info(message);
    }


    /**
     * 教官装备日志
     *
     * @param from
     * @param account
     * @param lord
     * @param equipId
     * @param count
     */
    public static void mentorEquip(AwardFrom from, Account account, Lord lord, int equipId, int count) {
        if (account == null || lord == null) {
            return;
        }
        StringBuffer message = getCommonParams("mentorEquip", from, account, lord).append("|").append(equipId).append("|")
                .append(count);
        GAME_LOGGER.info(message);
    }


    /**
     * 发送阵营邮件
     *
     * @param type
     * @param from
     * @param account
     * @param lord
     * @param content
     * @param currentServerId
     */
    public static void commonChat(String type, AwardFrom from, Account account, Lord lord, String content, int currentServerId) {
        if (account == null || lord == null) {
            return;
        }
        StringBuffer message = getCommonParams(type, from, account, lord).append("|").append(content).append("|")
                .append(currentServerId);
        GAME_LOGGER.info(message);
    }


    /**
     * 建筑
     *
     * @param from
     * @param account
     * @param lord
     * @param buildId
     * @param lv
     */
    static public void build(AwardFrom from, Account account, Lord lord, int buildId, int lv) {
        if (account == null || lord == null) {
            return;
        }
        StringBuffer message = getCommonParams("build", from, account, lord).append("|").append(buildId).append("|")
                .append(lv);
        GAME_LOGGER.info(message);
    }

    /**
     * 孔位埋点
     *
     * @param from
     * @param account
     * @param lord
     * @param hole
     * @param stoneId
     */
    public static void stoneHole(AwardFrom from, Account account, Lord lord, int hole, int stoneId, int type) {
        if (account == null || lord == null) {
            return;
        }
        StringBuffer message = getCommonParams("stoneHole", from, account, lord).append("|").append(hole).append("|")
                .append(stoneId).append("|").append(type);
        GAME_LOGGER.info(message);
    }

    /**
     * 宝石副本埋点
     *
     * @param from
     * @param account
     * @param lord
     * @param combatId
     */
    public static void stoneCombat(AwardFrom from, Account account, Lord lord, int combatId, int cnt) {
        if (account == null || lord == null) {
            return;
        }
        StringBuffer message = getCommonParams("stoneCombat", from, account, lord).append("|").append(combatId)
                .append("|").append(cnt);
        GAME_LOGGER.info(message);
    }

    /**
     * 上报数数，战斗结束后兵力变化
     *
     * @param from
     * @param account
     * @param lord
     * @param heroId
     * @param count
     * @param add
     * @param action
     * @param armyType
     * @param quality
     */
    public static void filterHeroArm(AwardFrom from, Account account, Lord lord, int heroId, int count, int add, int action, int armyType, int quality) {
        if (account == null || lord == null) {
            return;
        }

        // EventDataUp.heroArmy(account, lord, from, armyType, add, count);
        if (quality < HeroConstant.QUALITY_ORANGE_HERO)
            return;
        StringBuffer message = getCommonParams("heroArm", from, account, lord).append("|").append(heroId).append("|")
                .append(count).append("|").append(add).append("|").append(action);
        GAME_LOGGER.info(message);
    }

    /**
     * 将领兵量更新操作记录
     *
     * @param from
     * @param account
     * @param lord
     * @param heroId  将领id
     * @param count   当前剩余兵量
     * @param add     改变数量
     * @param action  加或减，1 获得，0 失去
     */
    public static void heroArm(AwardFrom from, Account account, Lord lord, int heroId, int count, int add, int armyType, int action) {
        if (account == null || lord == null) {
            return;
        }
        StringBuffer message = getCommonParams("heroArm", from, account, lord).append("|").append(heroId).append("|")
                .append(count).append("|").append(add).append("|").append(action);
        GAME_LOGGER.info(message);
        // EventDataUp.heroArmy(account, lord, from, armyType, add, count);
    }

    private static void contactParamsOneByOne(StringBuffer message, Object... params) {
        if (params != null && params.length > 0) {
            message.append("|");
            message.append(StringUtils.join(params, "|"));
        }
    }

    /**
     * 玩家整体兵力变化记录
     *
     * @param from
     * @param player
     * @param armyType
     * @param action
     * @param add
     * @param params
     */
    public static void playerArm(AwardFrom from, Player player, int armyType, int action, int add, Object... params) {
        if (player == null) {
            return;
        }

        LogUtil.getLogThread().addCommand(() -> {
            int current = DataResource.ac.getBean(PlayerDataManager.class).getArmCount(player.resource, armyType);
            for (Hero hero : player.heros.values()) {
                if (CheckNull.isNull(hero)) continue;
                StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
                if (CheckNull.isNull(staticHero)) continue;
                if (staticHero.getType() != armyType) continue;
                current += hero.getCount();
            }
            StringBuffer message = getCommonParams("heroArm", from, player.account, player.lord).append("|")
                    .append(current).append("|").append(add).append("|").append(action);
            GAME_LOGGER.info(message);
            EventDataUp.playerArmy(player, from, armyType, add, current);
        });
    }

    /*---------------------------------------RewardDataManager里的埋点日志start ---------------------------------------*/

    /**
     * 拼接可变参数,只用于RewardDataManager里的埋点日志
     *
     * @param message
     * @param params
     */
    private static void contactParamsArr(StringBuffer message, Object... params) {
        message.append("|");
        if (params != null && params.length > 0) {
            message.append(Arrays.toString(params));
        } else {
            message.append(0);
        }
    }

    /**
     * 记录金币变更
     *
     * @param from
     * @param account
     * @param lord
     * @param gold
     * @param topup
     */
    public static void gold(AwardFrom from, Account account, Lord lord, int gold, int topup, Object... params) {
        if (account == null || lord == null) {
            return;
        }
        StringBuffer message = getCommonParams("gold", from, account, lord).append("|").append(gold).append("|")
                .append(lord.getTopup()).append("|").append(topup);
        contactParamsArr(message, params);
        GAME_LOGGER.info(message);
        EventDataUp.gold(from, account, lord, gold, Arrays.toString(params));

    }

    /**
     * 资源
     *
     * @param from
     * @param account
     * @param lord
     * @param resource
     * @param id
     * @param add
     */
    static public void resource(AwardFrom from, Account account, Lord lord, Resource resource, int id, long add,
                                Object... params) {
        if (account == null || lord == null) {
            return;
        }
        StringBuffer message = getCommonParams("resource", from, account, lord).append("|").append(resource.getOre())
                .append("|").append(resource.getOil()).append("|").append(resource.getFood()).append("|")
                .append(resource.getElec()).append("|").append(id).append("|").append(add);
        contactParamsArr(message, params);
        GAME_LOGGER.info(message);
        EventDataUp.resource(from, account, lord, resource, id, add);
    }

    /**
     * 兵种资源
     *
     * @param from
     * @param account
     * @param lord
     * @param resource
     * @param id
     * @param add
     */
    static public void army(AwardFrom from, Account account, Lord lord, Resource resource, int id, long add,
                            Object... params) {
        if (account == null || lord == null) {
            return;
        }
        StringBuffer message = getCommonParams("army", from, account, lord).append("|").append(resource.getOre())
                .append("|").append(resource.getOil()).append("|").append(resource.getFood()).append("|")
                .append(resource.getElec()).append("|").append(id).append("|").append(add);
        contactParamsArr(message, params);
        GAME_LOGGER.info(message);
    }

    /**
     * 战机变更
     *
     * @param from
     * @param account
     * @param lord
     * @param action  加或减，1 获得，0 失去
     */
    public static void plane(AwardFrom from, Account account, Lord lord, int palneId, int action, Object... params) {
        if (account == null || lord == null) {
            return;
        }
        StringBuffer message = getCommonParams("plane", from, account, lord).append("|").append(palneId).append("|")
                .append(action);
        contactParamsArr(message, params);
        GAME_LOGGER.info(message);
    }

    /**
     * 战机变更
     *
     * @param from
     * @param account
     * @param lord
     * @param chipId  碎片id
     * @param action  加或减，1 获得，0 失去
     */
    public static void planeChip(AwardFrom from, Account account, Lord lord, int chipId, int action, Object... params) {
        if (account == null || lord == null) {
            return;
        }
        StringBuffer message = getCommonParams("planeChip", from, account, lord).append("|").append(chipId).append("|")
                .append(action);
        contactParamsArr(message, params);
        GAME_LOGGER.info(message);
    }

    /**
     * 将领变更
     *
     * @param from
     * @param account
     * @param lord
     * @param heroId  将领id
     * @param action  加或减，1 获得，0 失去
     */
    public static void hero(AwardFrom from, Account account, Lord lord, int heroId, int action, Object... params) {
        if (account == null || lord == null) {
            return;
        }
        StringBuffer message = getCommonParams("hero", from, account, lord).append("|").append(heroId).append("|")
                .append(action);
        contactParamsArr(message, params);
        GAME_LOGGER.info(message);
        EventDataUp.prop(from, account, lord, heroId, AwardType.HERO, 1, action, heroId, Arrays.toString(params), 1);
    }

    /**
     * 武将碎片变更
     *
     * @param from
     * @param account
     * @param lord
     * @param heroId  将领id
     * @param action  加或减，1 获得，0 失去
     */
    public static void heroFragment(AwardFrom from, Account account, Lord lord, int heroId, int action, int count, Object... params) {
        if (account == null || lord == null) {
            return;
        }
        StringBuffer message = getCommonParams("heroFragment", from, account, lord).append("|")
                .append(heroId).append("|")
                .append(action);
        contactParamsArr(message, params);
        GAME_LOGGER.info(message);
        EventDataUp.prop(from, account, lord, heroId, AwardType.HERO_FRAGMENT, count, action, heroId, Arrays.toString(params), params);
    }

    /**
     * 装备变更
     *
     * @param from
     * @param account
     * @param lord
     * @param equipId    装备id
     * @param equipKeyId 装备私有id
     * @param action     加或减，1 获得，0 失去
     */
    public static void equip(AwardFrom from, Account account, Lord lord, int equipId, int equipKeyId, int action,
                             Object... params) {
        if (account == null || lord == null) {
            return;
        }
        StringBuffer message = getCommonParams("equip", from, account, lord).append("|").append(equipId).append("|")
                .append(equipKeyId).append("|").append(action);
        contactParamsArr(message, params);
        GAME_LOGGER.info(message);
        EventDataUp.prop(from, account, lord, equipId, AwardType.EQUIP, 1, action, equipKeyId, Arrays.toString(params), action == Constant.ACTION_SUB ? 0 : 1);
    }

    /**
     * 装备分解
     *
     * @param from
     * @param account
     * @param lord
     * @param equipId
     * @param equipKeyId
     * @param action
     * @param params
     */
    public static void equipDecompose(AwardFrom from, Account account, Lord lord, int equipId, int equipKeyId, int action,
                                      Object... params) {
        if (account == null || lord == null) {
            return;
        }
        StringBuffer message = getCommonParams("equipDecompose", from, account, lord).append("|").append(equipId).append("|")
                .append(equipKeyId).append("|").append(action);
        contactParamsArr(message, params);
        GAME_LOGGER.info(message);
        EventDataUp.prop(from, account, lord, equipId, AwardType.EQUIP, 1, action, equipKeyId, Arrays.toString(params));
    }

    /**
     * 装备洗练
     *
     * @param from
     * @param account
     * @param lord
     * @param equipId
     * @param equipKeyId
     * @param action
     * @param params
     */
    public static void equipBaptize(AwardFrom from, Account account, Lord lord, int equipId, int equipKeyId, int action,
                                    Object... params) {
        if (account == null || lord == null) {
            return;
        }
        StringBuffer message = getCommonParams("equipBaptize", from, account, lord).append("|").append(equipId).append("|")
                .append(equipKeyId).append("|").append(action);
        contactParamsArr(message, params);
        GAME_LOGGER.info(message);
        EventDataUp.prop(from, account, lord, equipId, AwardType.EQUIP, 1, action, equipKeyId, Arrays.toString(params));
    }

    /**
     * 装备洗脸次数变更
     *
     * @param from
     * @param player
     * @param count
     * @param action
     * @param params
     */
    public static void equipBaptizeNew(AwardFrom from, Player player, int count, int action, String method, Object... params) {
        Java8Utils.invokeNoExceptionICommand(() -> {
            if (player.account == null || player.lord == null) {
                return;
            }
            // 若方法名为空, 则不打印埋点
            if (StringUtils.isNotBlank(method))
                commonLog(method, from, player, count, player.common.getBaptizeCnt());
            EventDataUp.prop(from, player.account, player.lord, AwardType.Special.BAPTIZE,
                    AwardType.SPECIAL, count, action, 0, Arrays.toString(params), player.common.getBaptizeCnt());
        });
    }

    public static void equipBaptizeLevelUp(AwardFrom from, Player player, int count, int action, String method, Object... params) {
        Java8Utils.invokeNoExceptionICommand(() -> {
            if (player.account == null || player.lord == null) {
                return;
            }
            EventDataUp.prop(from, player.account, player.lord, AwardType.Special.BAPTIZE,
                    AwardType.SPECIAL, count, action, 0, Arrays.toString(params), player.common.getBaptizeCnt());
        });
    }


    /**
     * 宝具变更
     *
     * @param from
     * @param account
     * @param lord
     * @param treasureWareId
     * @param keyId
     * @param action
     * @param params
     */
    public static void treasureWare(AwardFrom from, Account account, Lord lord, int treasureWareId, int keyId, int action, int profileId,
                                    Object... params) {
        if (account == null || lord == null) {
            return;
        }
        StringBuffer message = getCommonParams("treasureWare", from, account, lord).append("|").append(treasureWareId).append("|")
                .append(keyId).append("|").append(action).append("|").append(profileId);
        contactParamsArr(message, params);
        GAME_LOGGER.info(message);
        EventDataUp.prop(from, account, lord, profileId, AwardType.TREASURE_WARE, 1, action, keyId, Arrays.toString(params), action == Constant.ACTION_ADD ? 1 : 0);
    }

    /**
     * @param from
     * @param account
     * @param lord
     * @param medalId    勋章id
     * @param medalKeyId 勋章私有id
     * @param action     加或减，1 获得，0 失去
     * @param params
     * @return void
     * @Title: medal
     * @Description: 勋章变更
     */
    public static void medal(AwardFrom from, Account account, Lord lord, int medalId, int medalKeyId, int action,
                             Object... params) {
        if (account == null || lord == null) {
            return;
        }
        StringBuffer message = getCommonParams("medal", from, account, lord).append("|").append(medalId).append("|")
                .append(medalKeyId).append("|").append(action);
        contactParamsArr(message, params);
        GAME_LOGGER.info(message);
        EventDataUp.prop(from, account, lord, medalId, AwardType.MEDAL, 1, action, medalKeyId, Arrays.toString(params), action == Constant.ACTION_ADD ? 1 : 0);
    }

    /**
     * 进阶的宝石变更
     *
     * @param from
     * @param account
     * @param lord
     * @param stoneImproveId 宝石进阶的id
     * @param keyId          私有id
     * @param action         加或减，1 获得，0 失去
     */
    public static void stoneImprove(AwardFrom from, Account account, Lord lord, int stoneImproveId, int keyId,
                                    int action, Object... params) {
        if (account == null || lord == null) {
            return;
        }
        StringBuffer message = getCommonParams("stoneImprove", from, account, lord).append("|").append(stoneImproveId)
                .append("|").append(keyId).append("|").append(action);
        contactParamsArr(message, params);
        GAME_LOGGER.info(message);
        EventDataUp.prop(from, account, lord, stoneImproveId, AwardType.STONE, 1, action, keyId, Arrays.toString(params), Constant.ACTION_ADD == action ? 1 : 0);
    }

    /**
     * 道具变更
     *
     * @param from
     * @param account
     * @param lord
     * @param propId
     * @param curCount 现在拥有的个数
     * @param count    获取的个数
     * @param action   行为 1是增加 0是减少
     */
    public static void prop(AwardFrom from, Account account, Lord lord, int propId, int curCount, int count, int action,
                            Object... params) {
        if (account == null || lord == null) {
            return;
        }
        StringBuffer message = getCommonParams("prop", from, account, lord).append("|").append(propId).append("|")
                .append(curCount).append("|").append(count).append("|").append(action);
        contactParamsArr(message, params);
        GAME_LOGGER.info(message);
        EventDataUp.prop(from, account, lord, propId, AwardType.PROP, count, action, propId, Arrays.toString(params), curCount);
    }

    /**
     * 图腾日志
     *
     * @param awardFrom 来源
     * @param player
     * @param id        图腾配表id
     * @param keyId     图腾keyid
     * @param quality   品质
     * @param qhlv      强化等级
     * @param gmlv      共鸣等级
     * @param action    1增加 0 更新 -1减少
     * @param params
     */
    public static void totem(AwardFrom awardFrom, Player player, int id, int keyId, int quality, int qhlv, int gmlv, int action, Object... params) {
        StringBuffer message = getCommonParams("totem", awardFrom, player.account, player.lord)
                .append("|").append(id).append("|").append(keyId).append("|").append(quality).append("|").append(qhlv).append("|").append(gmlv)
                .append("|").append(action);
        if (params != null && params.length > 0) {
            message.append("|").append(Arrays.toString(params));
        }
        GAME_LOGGER.info(message);
        EventDataUp.prop(awardFrom, player.account, player.lord, id, AwardType.TOTEM, 1, action, keyId, Arrays.toString(params), action == Constant.ACTION_ADD ? 1 : 0);
    }


    /**
     * 头像获取
     *
     * @param from
     * @param account
     * @param lord
     * @param portraitId
     */
    public static void portrait(AwardFrom from, Account account, Lord lord, int portraitId, Object... params) {
        if (account == null || lord == null) {
            return;
        }
        StringBuffer message = getCommonParams("portrait", from, account, lord).append("|").append(portraitId)
                .append("|");
        contactParamsArr(message, params);
        GAME_LOGGER.info(message);
    }

    /**
     * 宝石
     *
     * @param from
     * @param account
     * @param lord
     * @param stoneId
     * @param curCount
     * @param count
     * @param action
     */
    public static void stone(AwardFrom from, Account account, Lord lord, int stoneId, int curCount, int count,
                             int action, Object... params) {
        if (account == null || lord == null) {
            return;
        }
        StringBuffer message = getCommonParams("stone", from, account, lord).append("|").append(stoneId).append("|")
                .append(curCount).append("|").append(count).append("|").append(action);
        contactParamsArr(message, params);
        GAME_LOGGER.info(message);
        EventDataUp.prop(from, account, lord, stoneId, AwardType.STONE, count, action, stoneId, Arrays.toString(params), curCount);
    }

    /**
     * 宝石升星
     */
    public static void stoneLvUp(AwardFrom from, Account account, Lord lord, int keyId, int stoneId, int curexp, int addexp) {
        if (account == null || lord == null) {
            return;
        }
        StringBuffer message = getCommonParams("stoneLvUp", from, account, lord).append("|").append(keyId)
                .append("|").append(stoneId).append("|").append(curexp).append("|").append(addexp);
        GAME_LOGGER.info(message);
    }

    /**
     * 装备宝石
     *
     * @param from     来源
     * @param account  账号信息
     * @param lord     玩家信息
     * @param jewel    宝石等级
     * @param curCount 当前拥有的数量
     * @param count    变动的数量
     * @param action   变动行为, 增加{@link com.gryphpoem.game.zw.resource.constant.Constant#ACTION_ADD}还是减少 {@link com.gryphpoem.game.zw.resource.constant.Constant#ACTION_SUB}
     * @param params   扩展参数
     */
    public static void jewel(AwardFrom from, Account account, Lord lord, int jewel, int curCount, int count,
                             int action, Object... params) {
        if (account == null || lord == null) {
            return;
        }
        StringBuffer message = getCommonParams("jewel", from, account, lord).append("|").append(jewel).append("|")
                .append(curCount).append("|").append(count).append("|").append(action);
        contactParamsArr(message, params);
        GAME_LOGGER.info(message);
    }

    /**
     * 角色经验变更
     *
     * @param from
     * @param account
     * @param lord
     * @param count
     */
    public static void exp(AwardFrom from, Account account, Lord lord, long count, Object... params) {
        if (account == null || lord == null) {
            return;
        }
        StringBuffer message = getCommonParams("exp", from, account, lord).append("|").append(lord.getExp()).append("|")
                .append(count);
        contactParamsArr(message, params);
        GAME_LOGGER.info(message);
    }

    /**
     * vip经验,免费的获取才打印
     *
     * @param from
     * @param account
     * @param lord
     * @param count
     */
    public static void vipExp(AwardFrom from, Account account, Lord lord, long count, Object... params) {
        if (account == null || lord == null) {
            return;
        }
        StringBuffer message = getCommonParams("vipExp", from, account, lord).append("|").append(lord.getVipExp())
                .append("|").append(count);
        contactParamsArr(message, params);
        GAME_LOGGER.info(message);
    }

    public static void sandTableScore(AwardFrom from, Player player, int count, Object... params) {
        if (player == null) {
            return;
        }
        StringBuffer message = getCommonParams("sandTableScore", from, player.account, player.lord).append("|").append(player.getSandTableScore()).append("|").append(count);
        contactParamsArr(message, params);
        GAME_LOGGER.info(message);
        EventDataUp.credits(player.account, player.lord, player.getSandTableScore(), count, CreditsConstant.SANDTABLE, from);
    }

    /**
     * 活动积分
     *
     * @param type
     * @param from
     * @param player
     * @param total
     * @param add
     * @param activity
     * @param params
     */
    public static void activityScore(String type, AwardFrom from, Player player, int total, int add, Activity activity, Object... params) {
        if (player == null) {
            return;
        }

        int actType = CheckNull.isNull(activity) ? 0 : activity.getActivityType();
        int actId = CheckNull.isNull(activity) ? 0 : activity.getActivityId();
        StringBuffer message = getCommonParams(type, from, player.account, player.lord).append("|").append(add).append("|").append(total).append("|").append(actType);
        if (Objects.nonNull(params) && params.length > 0) {
            for (Object param : params) {
                message.append("|").append(param);
            }
        }
        GAME_LOGGER.info(message);
        if (Objects.nonNull(activity))
            EventDataUp.activityCredits(player.account, player.lord, from, actType, actId, add, total);
    }

    /**
     * 军功
     *
     * @param from
     * @param account
     * @param lord
     * @param count
     * @param add
     */
    public static void exploit(AwardFrom from, Account account, Lord lord, long count, int add, Object... params) {
        if (account == null || lord == null) {
            return;
        }
        StringBuffer message = getCommonParams("exploit", from, account, lord).append("|").append(lord.getExploit())
                .append("|").append(count).append("|").append(add);
        contactParamsArr(message, params);
        GAME_LOGGER.info(message);
    }

    /**
     * 体力
     *
     * @param from
     * @param account
     * @param lord
     * @param count
     * @param add
     */
    public static void power(AwardFrom from, Account account, Lord lord, int count, int add, Object... params) {
        if (account == null || lord == null) {
            return;
        }
        StringBuffer message = getCommonParams("power", from, account, lord).append("|").append(lord.getPower())
                .append("|").append(count).append("|").append(add);
        contactParamsArr(message, params);
        GAME_LOGGER.info(message);
    }

    /**
     * 将令
     *
     * @param from
     * @param account
     * @param lord
     * @param count
     * @param add
     */
    public static void heroToken(AwardFrom from, Account account, Lord lord, int count, int add, Object... params) {
        if (account == null || lord == null) {
            return;
        }
        StringBuffer message = getCommonParams("heroToken", from, account, lord).append("|").append(lord.getHeroToken())
                .append("|").append(count).append("|").append(add);
        contactParamsArr(message, params);
        GAME_LOGGER.info(message);
    }

    /**
     * 积分
     *
     * @param from
     * @param account
     * @param lord
     * @param count
     * @param change
     */
    public static void credit(AwardFrom from, Account account, Lord lord, int count, int change, Object... params) {
        if (account == null || lord == null) {
            return;
        }
        StringBuffer message = getCommonParams("credit", from, account, lord).append("|").append(count).append("|")
                .append(change);
        contactParamsArr(message, params);
        GAME_LOGGER.info(message);
    }

    /**
     * @param from
     * @param account
     * @param lord
     * @param count
     * @param change
     * @param params
     * @return void
     * @Title: honor
     * @Description: 荣誉
     */
    public static void honor(AwardFrom from, Account account, Lord lord, int count, int change, Object... params) {
        if (account == null || lord == null) {
            return;
        }
        StringBuffer message = getCommonParams("honor", from, account, lord).append("|").append(count).append("|")
                .append(change);
        contactParamsArr(message, params);
        GAME_LOGGER.info(message);
    }

    /**
     * 活动积分
     *
     * @param player
     * @param awardFrom
     * @param score
     * @param actType
     * @param actId
     */
    public static void actScore(Player player, AwardFrom awardFrom, int score, int currScore, int actType, int actId, Object... params) {
        StringBuffer message = getCommonParams("actScore", awardFrom, player.account, player.lord).append("|").append(actType).append("|").append(actId).append("|").append(score).append("|").append(currScore);
        contactParamsArr(message, params);
        GAME_LOGGER.info(message);
    }

    /**
     * @param from
     * @param account
     * @param lord
     * @param count
     * @param change
     * @param params
     * @return void
     * @Title: honor
     * @Description: 金条
     */
    public static void goldBar(AwardFrom from, Account account, Lord lord, int count, int change, Object... params) {
        if (account == null || lord == null) {
            return;
        }
        StringBuffer message = getCommonParams("goldBar", from, account, lord).append("|").append(count).append("|")
                .append(change);
        contactParamsArr(message, params);
        GAME_LOGGER.info(message);
    }

    /**
     * @param from
     * @param account
     * @param lord
     * @param count
     * @param change
     * @param params
     * @return void
     * @Title: honor
     * @Description: 金条
     */
    public static void goldIngot(AwardFrom from, Account account, Lord lord, int count, int change, Object... params) {
        if (account == null || lord == null) {
            return;
        }
        StringBuffer message = getCommonParams("goldIngot", from, account, lord).append("|").append(count).append("|")
                .append(change);
        contactParamsArr(message, params);
        GAME_LOGGER.info(message);
    }

    public static void warFireCoin(AwardFrom from, Account account, Lord lord, int count, int change, Object... params) {
        if (account == null || lord == null) {
            return;
        }
        StringBuffer message = getCommonParams("warFireCoin", from, account, lord)
                .append("|").append(count).append("|").append(change);
        contactParamsArr(message, params);
        GAME_LOGGER.info(message);
    }

    public static void seasonTalentStone(AwardFrom from, Account account, Lord lord, int count, int change, Object... params) {
        if (account == null || lord == null) {
            return;
        }
        StringBuffer message = getCommonParams("seasonRunesPoint", from, account, lord)
                .append("|").append(count).append("|").append(change);
        contactParamsArr(message, params);
        GAME_LOGGER.info(message);
    }


    /**
     * 记录玩家战火活动积分变化情况
     *
     * @param from
     * @param have
     * @param add
     * @param params 矿点产出积分: 矿点当前剩余资源
     *               据点产出资源时:据点ID
     *               杀敌产出资源时: 累计杀敌数, 当前杀敌数
     */
    public static void warFireScore(AwardFrom from, Player player, int have, int add, Object... params) {
        Account account = player.account;
        Lord lord = player.lord;
        if (account == null || lord == null) {
            return;
        }
        StringBuffer message = getCommonParams("warFireScore", from, account, lord)
                .append("|").append(have).append("|").append(add);
        contactParamsArr(message, params);
        GAME_LOGGER.info(message);
    }

    /**
     * 教官钞票
     *
     * @param from
     * @param account
     * @param lord
     * @param have
     * @param add
     * @param params
     */
    public static void mentorBill(AwardFrom from, Account account, Lord lord, int have, int add, Object... params) {
        if (account == null || lord == null) {
            return;
        }
        StringBuffer message = getCommonParams("goldBar", from, account, lord).append("|").append(have).append("|")
                .append(add);
        contactParamsArr(message, params);
        GAME_LOGGER.info(message);
    }

    /*---------------------------------------RewardDataManager里的埋点日志end---------------------------------------*/

    /**
     * 登录时长
     *
     * @param player
     */
    public static void loginLong(Player player) {
        Lord lord = player.lord;
        long accountKey = 0;
        int serverId = 0;
        String deviceNo = "";
        int platNo = 0;
        int childNo = 0;
        String platId = "";
        String createDate = "";
        if (player.account != null) {
            Account account = player.account;
            accountKey = account.getAccountKey();
            serverId = account.getServerId();
            deviceNo = account.getDeviceNo();
            platNo = account.getPlatNo();
            childNo = account.getChildNo();
            platId = account.getPlatId();
            if (account.getCreateDate() != null) {
                createDate = DateHelper.formatDateTime(account.getCreateDate(), DateHelper.format1);
            }
        }
        int onTime = lord.getOnTime();
        int offTime = lord.getOffTime();
        int lTime = offTime - onTime;
        int allLTime = lord.getOlTime();
        GAME_LOGGER.error("loginLong|" + lord.getLordId() + "|" + lord.getNick() + "|" + lord.getVip() + "|"
                + lord.getTopup() + "|" + player.common.getBagCnt() + "|" + lord.getGold() + "|" + lord.getGoldCost()
                + "|" + lord.getGoldGive() + "|" + accountKey + "|" + serverId + "|" + lord.getLevel() + "|" + platNo
                + "|" + platId + "|" + deviceNo + "|" + createDate + "|" + childNo + "|" + onTime + "|" + offTime + "|"
                + lTime + "|" + allLTime);
    }

    static public void logLogin(Player player) {
        Lord lord = player.lord;
        long accountKey = 0;
        int serverId = 0;
        String deviceNo = "";
        int platNo = 0;
        int childNo = 0;
        String platId = "";
        String createDate = "";
        String clientIp = "";
        if (player.account != null) {
            Account account = player.account;
            accountKey = account.getAccountKey();
            serverId = account.getServerId();
            deviceNo = account.getDeviceNo();
            platNo = account.getPlatNo();
            childNo = account.getChildNo();
            platId = account.getPlatId();
            if (account.getCreateDate() != null) {
                createDate = DateHelper.formatDateTime(account.getCreateDate(), DateHelper.format1);
            }
        }
        if (!CheckNull.isNull(player.ctx)) {
            InetSocketAddress insocket = (InetSocketAddress) player.ctx.channel().remoteAddress();
            if (insocket != null) {
                clientIp = insocket.getHostString();
            }
        }
        GAME_LOGGER.error("login|" + lord.getLordId() + "|" + lord.getNick() + "|" + lord.getVip() + "|"
                + lord.getTopup() + "|" + player.common.getBagCnt() + "|" + lord.getGold() + "|" + lord.getGoldCost()
                + "|" + lord.getGoldGive() + "|" + accountKey + "|" + serverId + "|" + lord.getLevel() + "|" + platNo
                + "|" + platId + "|" + deviceNo + "|" + createDate + "|" + childNo + "|" + clientIp);

        // recordRoleLogin(accountKey, platNo, lord.getLordId(), lord.getNick(), lord.getLevel(), player.account);
    }

    /**
     * 记录玩家基础信息
     */
    static public void logLord(Player player) {
        Lord lord = player.lord;
        long accountKey = 0;
        int serverId = 0; //原始区服
        int masterServerId = DataResource.ac.getBean(ServerSetting.class).getServerID(); //主服
        if (player.account != null) {
            Account account = player.account;
            accountKey = account.getAccountKey();
            serverId = account.getServerId();
        }
        GAME_LOGGER.error("plord|" + serverId + "|" + masterServerId + "|" + lord.getLordId() + "|" + accountKey
                + "|" + lord.getNick() + "|" + lord.getPortrait() + "|" + lord.getSex()
                + "|" + lord.getCamp() + "|" + lord.getLevel() + "|" + lord.getExp() + "|" + lord.getVip() + "|" + lord.getVipExp() + "|" + lord.getTopup() + "|" + lord.getArea()
                + "|" + lord.getPos() + "|" + lord.getGold() + "|" + lord.getGoldCost() + "|" + lord.getGoldGive() + "|" + lord.getPower() + "|" + lord.getRanks()
                + "|" + lord.getExploit() + "|" + lord.getJob() + "|" + lord.getFight() + "|" + lord.getNewState() + "|" + lord.getNewerGift()
                + "|" + lord.getOnTime() + "|" + lord.getOlTime() + "|" + lord.getOffTime() + "|" + lord.getOlMonth() + "|" + lord.getSilence() + "|" + lord.getCombatId()
                + "|" + lord.getHeroToken() + "|" + lord.getMouthCardDay() + "|" + lord.getMouthCLastTime() + "|" + lord.getCredit() + "|" + lord.getRefreshTime()
                + "|" + lord.getSignature() + "|" + lord.getHonor() + "|" + lord.getGoldBar());
    }


    /**
     * 支付日志
     *
     * @param lord     角色信息
     * @param account  账号信息
     * @param orderId  第三方的订单id
     * @param serialId 我们定义的订单id
     * @param amount   RMB价格
     * @param payId    s_pay表定义的id
     * @param usd      美元价格
     */
    static public void logPay(Lord lord, Account account, String orderId, String serialId, int amount, int payId, float usd) {
        commonLog("pay|", AwardFrom.PAY, account, lord, orderId, serialId, amount, payId, account.getPlatNo(),
                account.getChildNo(), usd);

        // GAME_LOGGER.error("pay|" + serverId + "|" + lord.getLordId() + "|" + account.getPlatNo() + "|"
        // + account.getPlatId() + "|" + orderId + "|" + serialId + "|" + amount + "|" + payTime + "|"
        // + account.getAccountKey() + "|" + account.getChildNo() + "|" + lord.getVip() + "|" + lord.getLevel());
        EventDataUp.paySuccess(account, lord, serialId, amount, payId);
    }

    /**
     * 行为埋点
     *
     * @param player
     * @param actionType
     * @param count
     * @param param
     */
    public static void actionPoint(Player player, int actionType, int count, String... param) {
        if (player == null || actionType <= 0) {
            return;
        }

        LogUtil.getLogThread().addCommand(() -> {
            StringBuffer sb = new StringBuffer();
            sb.append("point|").append(actionType).append("|").append(player.account.getServerId()).append("|")
                    .append(player.lord.getCamp()).append("|").append(player.lord.getArea()).append("|")
                    .append(player.lord.getLordId()).append("|").append(player.lord.getNick()).append("|")
                    .append(player.lord.getLevel()).append("|").append(player.lord.getVip()).append("|")
                    .append(player.lord.getGold()).append("|").append(player.lord.getTopup()).append("|")
                    .append(player.account.getPlatNo()).append("|").append(player.account.getPlatId());
            if (param != null && param.length > 0) {
                for (String par : param) {
                    if (par != "") {
                        sb.append('_').append(par);
                    }
                }
            }
            GAME_LOGGER.info(sb);
        });
    }

    static public void logRegister(Account account) {
        if (account == null) {
            return;
        }
        String createDate = "";
        if (account.getCreateDate() != null) {
            createDate = DateHelper.formatDateTime(account.getCreateDate(), DateHelper.format1);
        }
        GAME_LOGGER.error("register|" + account.getLordId() + "|" + account.getServerId() + "|" + account.getPlatNo()
                + "|" + account.getPlatId() + "|" + account.getDeviceNo() + "|" + account.getCreated() + "|"
                + createDate + "|" + account.getAccountKey() + "|" + account.getChildNo());
    }

    /**
     * 删除邮件时记录自动收取附件
     *
     * @param player
     * @param mail
     */
    public static void autoDelMail(AwardFrom from, Player player, Mail mail) {
        Account account = player.account;
        Lord lord = player.lord;
        int serverId = account.getServerId();
        long lordId = lord.getLordId();
        int vip = lord.getVip();
        int level = lord.getLevel();
        String nick = lord.getNick();

        String a = "autodelmail|" + from.getCode() + "|" + serverId + "|" + lordId + "|" + nick + "|" + vip + "|"
                + level + "|" + lord.getGold() + "|";

        StringBuilder awards = new StringBuilder();

        for (CommonPb.Award e : mail.getRewardList()) {
            awards.append("[" + e.getType() + "," + e.getId() + "," + e.getCount() + "],");
        }
        if (awards.length() > 0) {
            awards.setLength(awards.length() - 1);
        }

        GAME_LOGGER.error(a + "[" + awards + "]");
    }

    public static void wallNPCArm(AwardFrom from, Account account, Lord lord, int heroId, int count, int add,
                                  int action) {
        if (account == null || lord == null) {
            return;
        }
        StringBuffer message = getCommonParams("WallNPCArm", from, account, lord).append("|").append(heroId).append("|")
                .append(count).append("|").append(add).append("|").append(action);
        GAME_LOGGER.info(message);
    }

    /**
     * 保护罩取消埋点
     *
     * @param player
     */
    public static void logRemoveProtect(Player player, AwardFrom from, int pos) {
        StringBuffer message = getCommonParams("protect", from, player.account, player.lord)
                .append("|").append(pos);
        GAME_LOGGER.info(message);
    }

    /**
     * 武将升级
     */
    public static void heroLvUp(Player player, int heroId, int addExp, int oldLv, int newLv) {
        StringBuffer message = getCommonParams("heroLvUp", AwardFrom.COMMON, player.account, player.lord)
                .append("|").append(heroId).append("|").append(addExp).append("|").append(oldLv).append("|").append(newLv);
        GAME_LOGGER.info(message);
        EventDataUp.heroLevelUp(player, heroId, newLv);
    }

    /**
     * 装扮变动
     */
    public static void dressUp(Player player, AwardFrom from, BaseDressUpEntity entity, String action, Object... params) {
        StringBuffer message = getCommonParams(action, from, player.account, player.lord)
                .append("|").append(entity.getType()).append("|").append(entity.getId()).append("|").append(entity.isPermanentHas()).append("|").append(entity.getDuration());
        contactParamsArr(message, params);
        GAME_LOGGER.info(message);
        EventDataUp.prop(from, player.account, player.lord, entity.getId(), entity.getType(), 1, Constant.ACTION_ADD, entity.getId(), Arrays.toString(params),
                StringUtils.isNotBlank(action) && action.startsWith("clear") ? 0 : 1);
    }

    /**
     * 喜悦金秋活动
     */
    public static void goldenAutumn(String type, Player player, AwardFrom from, List<CommonPb.Award> awardList) {
        final StringBuffer message = getCommonParams(type, from, player.account, player.lord);
        if (awardList != null && awardList.size() > 0) {
            awardList.forEach(e -> message.append("|").append(e.getType()).append("|").append(e.getId()).append("|").append(e.getCount()));
        }
        GAME_LOGGER.info(message);
    }

    public static void strengthTreasureWare(AwardFrom from, Account account, Lord lord, int keyId, int cnfId, int oldLv, int newLv, Object... params) {
        if (account == null || lord == null) {
            return;
        }
        StringBuffer message = getCommonParams("strengthTreasureWare", from, account, lord)
                .append("|").append(oldLv).append("|").append(newLv).append("|").append(keyId).append("|").append(cnfId);
        contactParamsArr(message, params);
        GAME_LOGGER.info(message);
    }

    /**
     * 打印普通gameLog拼接埋点日志
     *
     * @param type
     * @param player
     * @param from
     * @param params
     */
    public static void gameLog(String type, Player player, AwardFrom from, Object... params) {
        if (CheckNull.isNull(player) || CheckNull.isNull(player.account) || CheckNull.isNull(player.lord))
            return;
        StringBuffer message = getCommonParams(type, from, player.account, player.lord);
        contactParamsOneByOne(message, params);
        GAME_LOGGER.info(message);
    }

    /**
     * 客户端点击事件
     *
     * @param type
     * @param roleId
     * @param params
     */
    public static void pointLog(String type, Long roleId, int cmdId, Object... params) {
        if (CheckNull.isNull(roleId) || cmdId == GamePb1.BeginGameRq.EXT_FIELD_NUMBER)
            return;
        LogUtil.getLogThread().addCommand(() -> {
            Player player = DataResource.ac.getBean(PlayerDataManager.class).getPlayer(roleId);
            if (CheckNull.isNull(player) || CheckNull.isNull(player.lord) || CheckNull.isNull(player.account))
                return;
            StringBuffer message = getCommonParams(type, null, player.account, player.lord);
            contactParamsOneByOne(message, params);
            POINT_LOGGER.info(message);
        });
    }

    /**
     * 记录玩家战斗力 {@link CalculateUtil#reCalcFight(Player)}
     *
     * @param type
     * @param player
     * @param params
     */
    public static void recodePower(String type, Player player, Object... params) {
        if (CheckNull.isNull(player) || CheckNull.isNull(player.account) || CheckNull.isNull(player.lord))
            return;
        Java8Utils.invokeNoExceptionICommand(() -> {
            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            if (ObjectUtils.isEmpty(stackTraceElements)) {
                return;
            }
            LogUtil.getLogThread().addCommand(() -> {
                int runFunctionIndex = 0;
                for (int i = 0; i < stackTraceElements.length; i++) {
                    if (CheckNull.isNull(stackTraceElements[i]))
                        continue;
                    if ("reCalcFight".equalsIgnoreCase(stackTraceElements[i].getMethodName())) {
                        runFunctionIndex = i;
                        break;
                    }
                }

                byte i = 2;
                if (runFunctionIndex + i >= stackTraceElements.length) {
                    return;
                }
                StackTraceElement service = stackTraceElements[runFunctionIndex + i];
                while (Objects.nonNull(service) && service.getFileName().contains("CalculateUtil") &&
                        runFunctionIndex + i < stackTraceElements.length) {
                    service = stackTraceElements[runFunctionIndex + i++];
                }
                if (CheckNull.isNull(service))
                    return;
                StringBuffer message = getCommonParams(type, null, player.account, player.lord);
                contactParamsOneByOne(message, params);
                String function = service.getFileName().replace("java", "") + service.getMethodName() + "(): " + service.getLineNumber();
                message.append("|").append(function);
                GAME_LOGGER.info(message);
            });
        });
    }

    /**
     * 宝具副本埋点
     *
     * @param type
     * @param from
     * @param player
     * @param combatId
     * @param state
     * @param fight
     * @param heroId
     * @param params
     */
    public static void treasureCombatPromote(String type, AwardFrom from, Player player, int combatId, int state, long fight, String heroId, Object... params) {
        if (player == null) {
            return;
        }
        commonLog(type, from, player.account, player.lord, combatId, state, fight, heroId);
    }

    public static void saveTreasureWareTrain(AwardFrom from, Player player, TreasureWare tw) {
        if (player.account == null || player.lord == null || tw == null) {
            return;
        }
        StringBuffer message = getCommonParams("saveTrainTreasureWare", from, player.account, player.lord)
                .append("|").append(tw.getEquipId())
                .append("|").append(tw.getKeyId())
                .append("|").append(tw.getQuality())
                .append("|").append(tw.logAttrs())
                .append("|").append(tw.getProfileId());
        GAME_LOGGER.info(message);
    }

    public static void trainTreasureWare(AwardFrom from, Account account, Lord lord, TreasureWare mat, TreasureWare major, TreasureWareAttrItem trainAttr) {
        if (account == null || lord == null || mat == null || major == null) {
            return;
        }
        StringBuffer message = getCommonParams("trainTreasureWare", from, account, lord)
                .append("|").append(mat.getEquipId())
                .append("|").append(mat.getKeyId())
                .append("|").append(mat.getQuality())
                .append("|").append(mat.logAttrs())
                .append("|").append(major.getEquipId())
                .append("|").append(major.getKeyId())
                .append("|").append(major.getQuality())
                .append("|").append(major.logAttrs())
                .append("|").append(trainAttr.getIndex())
                .append("|").append(trainAttr.getTrainTargetIndex())
                .append("|").append(mat.getProfileId())
                .append("|").append(major.getProfileId());
        GAME_LOGGER.info(message);
    }
}
