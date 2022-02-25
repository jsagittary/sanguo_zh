package com.gryphpoem.game.zw.network.util;

import com.gryphpoem.game.zw.network.session.SessionGroup;

import io.netty.util.AttributeKey;

/**
 * @ClassName AttrKey.java
 * @Description Netty 通道绑定的key值
 * @author QiuKun
 * @date 2019年5月5日
 */
public interface AttrKey {

    public static AttributeKey<SessionGroup> SESSION_KEY = AttributeKey.valueOf("Session_Key");
    public static AttributeKey<Integer> SESSION_INDEX = AttributeKey.valueOf("Session_Index");
    
    
}
