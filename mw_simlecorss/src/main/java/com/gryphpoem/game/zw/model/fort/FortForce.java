package com.gryphpoem.game.zw.model.fort;

/**
 * @ClassName FortForce.java
 * @Description
 * @author QiuKun
 * @date 2019年5月27日
 */
public interface FortForce {

    /**
     * 是否为NPC势力
     * 
     * @return
     */
    default boolean isNpcFortForce() {
        return false;
    }
}
