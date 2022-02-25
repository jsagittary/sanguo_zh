package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.resource.domain.Player;

/**
 * @author xwind
 * @date 2021/5/31
 */
public interface LoginService {

    void afterLogin(Player player);
}
