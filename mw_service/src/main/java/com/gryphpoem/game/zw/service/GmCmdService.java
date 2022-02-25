package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.resource.domain.Player;

/**
 * GM命令接口
 * @author xwind
 * @date 2021/7/28
 */
public interface GmCmdService {
    /**
     * gm接口
     * @param player {@link Player}
     * @param params 参数，如执行命令 sandtable fight，传到这里的就是["fight"]
     */
    void handleGmCmd(Player player,String...params) throws Exception;
}
