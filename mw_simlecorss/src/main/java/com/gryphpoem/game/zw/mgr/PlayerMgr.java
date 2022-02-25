package com.gryphpoem.game.zw.mgr;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.model.CrossPlayer;
import com.gryphpoem.game.zw.util.CrossWarFinlishClear;

/**
 * @ClassName PlayerMgr.java
 * @Description
 * @author QiuKun
 * @date 2019年5月5日
 */
@Component
public class PlayerMgr implements CrossWarFinlishClear {
    /** 所有的跨服玩家数据 <lordId,CrossPlayer> */
    private Map<Long, CrossPlayer> playerMap = new ConcurrentHashMap<Long, CrossPlayer>();

    public Map<Long, CrossPlayer> getPlayerMap() {
        return playerMap;
    }

    public CrossPlayer getPlayer(long lordId) {
        return playerMap.get(lordId);
    }

    public void addPlayer(CrossPlayer crossPlayer) {
        this.playerMap.putIfAbsent(crossPlayer.getLordId(), crossPlayer);
    }

    public CrossPlayer removePlayer(long lordId) {
        return this.playerMap.remove(lordId);
    }

    public boolean containsPlayer(long lordId) {
        return playerMap.containsKey(lordId);
    }

    @Override
    public void clear() {
        playerMap.clear();
        LogUtil.debug("玩家数据清除");
    }

}
