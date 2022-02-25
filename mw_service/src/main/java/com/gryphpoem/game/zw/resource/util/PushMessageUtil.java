package com.gryphpoem.game.zw.resource.util;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticMailDataMgr;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.push.PushUtil;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Account;
import com.gryphpoem.game.zw.resource.domain.s.StaticPushMessage;

import java.util.Arrays;
import java.util.function.Predicate;

/**
 * @author TanDonghai
 * @Description 应用外消息推送工具类
 * @date 创建时间：2017年9月7日 上午11:08:03
 */
public class PushMessageUtil {

    private PushMessageUtil() {
    }

    private static final String REPLACE_MATCH = "%s";

    /**
     * 使用消息模版推送(玩家不在线推送)
     *
     * @param account 要推送到的玩家帐号
     * @param pushId 消息模版id
     * @param params 消息需要的参数信息，从标题到内容，按顺序填入
     */
    public static void pushMessage(Account account, int pushId, Object... params) {
        if (null == account) {
            return;
        }
        StaticPushMessage spm = StaticMailDataMgr.getPushMap().get(pushId);
        if (null == spm) {
            LogUtil.error("推送消息模版不存在，跳过推送, pushId:", pushId, ", params:", params);
            return;
        }
        Player player = DataResource.ac.getBean(PlayerDataManager.class).getPlayer(account.getLordId());
        if (!checkPlayerLogin(player)) {

        }
        if ("test".equals(DataResource.environment)) {
            LogUtil.debug("test 环境 不进行推送");
            return;
        }
        // 玩家在线时不推送,(***没有包id不推***)
        if (null == player || player.isRobot || CheckNull.isNullTrim(player.getPackId())) {
            return;
        }
        try {
            String packId = player.getPackId();
            String title = spm.getTitle();
            String content = spm.getContent();
            // 计算需要替换的参数个数
            int titleReplace = getStringReplaceCount(spm.getTitle());
            int contentReplace = getStringReplaceCount(spm.getContent());
            int totalParamNum = titleReplace + contentReplace;
            // 如果存在需要替换的参数，将参数数据填入模版字符串中
            if (totalParamNum > 0) {
                if (CheckNull.isEmpty(params) || totalParamNum > params.length) {
                    LogUtil.error("推送消息缺少参数，跳过推送, pushId:", pushId, ", params:", params);
                    return;
                }
                Object[] titleParams = Arrays.copyOfRange(params, 0, titleReplace);
                Object[] contentParams = Arrays.copyOfRange(params, titleReplace, totalParamNum);
                // 格式化字符串
                title = String.format(title, titleParams);
                content = String.format(content, contentParams);
            }
            // 推送消息
            PushUtil.pushMessage(title, content, account.getPlatNo(), account.getDeviceNo(), account.getLordId(), packId);
        } catch (Exception e) {
            LogUtil.error(e, "添加推送消息出错, account:", account, ", pushId:", pushId, ", params:", Arrays.asList(params));
        }
    }

    private static boolean checkPlayerLogin(Player player) {
        return true;
    }

    /**
     * 使用消息模版推送(校验 predicate)
     * @param account 要推送到的玩家帐号
     * @param pushId 消息模版id
     * @param params 消息需要的参数信息，从标题到内容，按顺序填入
     */
    public static void pushMessage(Account account, int pushId, Predicate<Player> predicate, Object... params) {
        if (null == account) {
            return;
        }
        StaticPushMessage spm = StaticMailDataMgr.getPushMap().get(pushId);
        if (null == spm) {
            LogUtil.error("推送消息模版不存在，跳过推送, pushId:", pushId, ", params:", params);
            return;
        }
        Player player = DataResource.ac.getBean(PlayerDataManager.class).getPlayer(account.getLordId());
        if (predicate.test(player)) {
            return;
        }
        try {
            String packId = player.getPackId();
            String title = spm.getTitle();
            String content = spm.getContent();
            // 计算需要替换的参数个数
            int titleReplace = getStringReplaceCount(spm.getTitle());
            int contentReplace = getStringReplaceCount(spm.getContent());
            int totalParamNum = titleReplace + contentReplace;
            // 如果存在需要替换的参数，将参数数据填入模版字符串中
            if (totalParamNum > 0) {
                if (CheckNull.isEmpty(params) || totalParamNum > params.length) {
                    LogUtil.error("推送消息缺少参数， 跳过推送, pushId:", pushId, ", params:", params);
                    return;
                }
                Object[] titleParams = Arrays.copyOfRange(params, 0, titleReplace);
                Object[] contentParams = Arrays.copyOfRange(params, titleReplace, totalParamNum);
                // 格式化字符串
                title = String.format(title, titleParams);
                content = String.format(content, contentParams);
            }
            // 推送消息
            PushUtil.pushMessage(title, content, account.getPlatNo(), account.getDeviceNo(), account.getLordId(), packId);
        } catch (Exception e) {
            LogUtil.error(e, "添加推送消息出错, account:", account, ", pushId:", pushId, ", params:", Arrays.asList(params));
        }
    }

    /**
     * 计算字符串中需要替换的参数个数
     *
     * @param str
     * @return
     */
    private static int getStringReplaceCount(String str) {
        if (!CheckNull.isNullTrim(str)) {
            return str.split(REPLACE_MATCH).length - 1;
        }
        return 0;
    }
}
