package com.gryphpoem.game.zw.service;

/**
 * @author xwind
 * @date 2021/5/11
 */
public interface GameService {
    void handleOnStartup() throws Exception;

    void handleOnReloadConfig() throws Exception;
}
