package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.resource.domain.Player;

/**
 * 充值之后的处理
 * @author xwind
 * @date 2021/7/7
 */
public interface RechargeService {
    void afterRecharge(Player player,int amount,int diamond);
}
