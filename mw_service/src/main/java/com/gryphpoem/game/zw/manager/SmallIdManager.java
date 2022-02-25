package com.gryphpoem.game.zw.manager;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticLordDataMgr;
import com.gryphpoem.game.zw.resource.dao.impl.p.AccountDao;
import com.gryphpoem.game.zw.resource.dao.impl.p.SmallIdDao;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Account;
import com.gryphpoem.game.zw.resource.domain.p.Lord;
import com.gryphpoem.game.zw.resource.domain.p.SmallId;
import com.gryphpoem.game.zw.resource.domain.s.StaticSmallClear;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class SmallIdManager {

    @Autowired
    private SmallIdDao smallDao;
    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private WorldDataManager worldDataManager;
    @Autowired
    private AccountDao accountDao;

    private Map<Long, SmallId> smallIdCache = new HashMap<>();

    public void init() {
        List<SmallId> list = smallDao.load();
        for (SmallId smallId : list) {
            smallIdCache.put(smallId.getLordId(), smallId);
        }
    }

    /**
     * 判断是否小号
     *
     * @param lordId
     * @return
     */
    public boolean isSmallId(long lordId) {
        return lordId == 0 || smallIdCache.containsKey(lordId);
    }

    /**
     * 在线清理小号
     *
     * @param player
     */
    public void addSmallIdOnline(Player player) {
        if (player == null) {
            return;
        }
        long lordId = player.lord.getLordId();
        boolean result = addSmallId(lordId, player.account.getAccountKey());
        if (!result) {
            return;
        }
        playerDataManager.rmPlayer(player);
        worldDataManager.rmPlayer(player);
    }

    /**
     * 服务器启动时清理小号
     *
     * @param list
     */
    public void processSmallIdLogic(List<Lord> list) {
        if (CheckNull.isEmpty(list)) {
            return;
        }
        // 获取所有账号
        List<Account> accountList = accountDao.load();
        Map<Long, Account> accountMap = accountList.stream().collect(Collectors.toMap(Account::getLordId, (p) -> p, (oldV, newV) -> newV));

        // 现在的second
        int nowSecond = TimeHelper.getCurrentSecond();

        long count = list.stream()
                // 判断是否满足清理小号的条件
                .filter(lord -> checkSmallLord(lord, nowSecond))
                .peek(lord -> {
                    Optional.ofNullable(accountMap.get(lord.getLordId()))
                            .ifPresent(account -> {
                                addSmallId(lord.getLordId(), account.getAccountKey());
                                LogUtil.debug("启动加入小号=" + lord.getLordId());
                            });
                }).count();
        LogUtil.start("共清除小号Lord数据 " + count + " 条");
    }


    /**
     * 检测是否是小号
     *
     * @param lord      玩家的基本信息
     * @param nowSecond 现在的时间
     * @return true 小号 false 不是小号
     */
    private boolean checkSmallLord(Lord lord, int nowSecond) {
        // 有充值
        if (lord.getTopup() > 0) {
            return false;
        }
        StaticSmallClear sConf = StaticLordDataMgr.getSmallClearListByLv(lord.getLevel());
        // 没找到该等级的小号配置, 或者这个等级的玩家不清除
        if (Objects.isNull(sConf)) {
            return false;
        }
        int log = sConf.getLog();
        // 最近多少天不登录
        int duTime = TimeHelper.DAY_S * log;
        // 离线时间
        int offTime = lord.getOffTime();
        // 超出了配置的天数
        return lord.getOffTime() > 0 && nowSecond - offTime >= duTime;
    }

    /**
     * 服务器启动时清理小号
     *
     * @param lordId 玩家的唯一ID
     * @return 清除成功
     */
    private boolean addSmallId(long lordId, long accountKey) {
        if (smallIdCache.containsKey(lordId)) {
            return false;
        }
        SmallId s = new SmallId();
        s.setLordId(lordId);
        s.setAccountKey(accountKey);
        smallIdCache.put(lordId, s);
        smallDao.insertSmallId(s);
        return true;
    }

    public Map<Long, SmallId> getSmallIdCache() {
        return smallIdCache;
    }
}
