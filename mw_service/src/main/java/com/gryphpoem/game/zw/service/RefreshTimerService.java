package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.resource.domain.Player;

/**
 * 通用刷新接口，跑秒timer用于处理定时刷新的业务，在{@link PlayerService#handlePlayerState}中调用，业务中需要处理定时刷新时实现该接口即可
 * @author xwind
 * @date 2021/9/15
 */
public interface RefreshTimerService {
    void checkAndRefresh(Player player);
}
