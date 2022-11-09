package com.gryphpoem.game.zw.model.fort;

import com.gryphpoem.game.zw.pojo.p.NpcForce;

import java.util.LinkedList;

/**
 * @author QiuKun
 * @ClassName NpcFortForce.java
 * @Description
 * @date 2019年5月27日
 */
public class NpcFortForce<E extends NpcForce> extends LinkedList<E> implements FortForce {

    private static final long serialVersionUID = -2681500559129230225L;

    @Override
    public boolean isNpcFortForce() {
        return true;
    }
}
