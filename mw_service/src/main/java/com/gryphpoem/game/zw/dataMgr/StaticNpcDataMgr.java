package com.gryphpoem.game.zw.dataMgr;

import java.util.Map;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.resource.dao.impl.s.StaticDataDao;
import com.gryphpoem.game.zw.resource.domain.s.StaticNpc;

/**
 * @ClassName StaticNpcDataMgr.java
 * @Description NPC相关配置信息管理
 * @author TanDonghai
 * @date 创建时间：2017年4月1日 下午2:05:20
 *
 */
public class StaticNpcDataMgr {

	private static StaticDataDao staticDataDao = DataResource.ac.getBean(StaticDataDao.class);

	private static Map<Integer, StaticNpc> npcMap;

	public static void init() {
		Map<Integer, StaticNpc> npcMap = staticDataDao.selectNpcMap();
		StaticNpcDataMgr.npcMap = npcMap;
	}

	public static Map<Integer, StaticNpc> getNpcMap() {
		return npcMap;
	}

}
