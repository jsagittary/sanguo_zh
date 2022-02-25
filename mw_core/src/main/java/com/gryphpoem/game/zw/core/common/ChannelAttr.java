package com.gryphpoem.game.zw.core.common;

import io.netty.util.AttributeKey;

public class ChannelAttr {
	public static AttributeKey<Long> heartTime = AttributeKey.valueOf("heart");
	public static AttributeKey<Long> roleId = AttributeKey.valueOf("roleId");
	public static AttributeKey<Long> ID = AttributeKey.valueOf("ID");
//	public static AttributeKey<Player> player = AttributeKey.valueOf("player");
    public static AttributeKey<Integer> SESSION_INDEX = AttributeKey.valueOf("Session_Index");
}
