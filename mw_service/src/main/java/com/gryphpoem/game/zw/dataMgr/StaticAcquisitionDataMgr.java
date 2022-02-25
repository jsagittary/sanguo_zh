package com.gryphpoem.game.zw.dataMgr;

import java.util.Map;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.resource.dao.impl.s.StaticDataDao;
import com.gryphpoem.game.zw.resource.domain.s.StaticAcquisition;

/**
 * @ClassName StaticAcquisitionDataMgr.java
 * @Description 个人资源点配置数据管理类
 * @author TanDonghai
 * @date 创建时间：2017年5月9日 下午5:54:30
 *
 */
public class StaticAcquisitionDataMgr {
	private static StaticDataDao staticDataDao = DataResource.ac.getBean(StaticDataDao.class);

	private static Map<Integer, StaticAcquisition> acquisitionMap;

	public static void init() {
		Map<Integer, StaticAcquisition> acquisitionMap = staticDataDao.selectAcquisitionMap();
		StaticAcquisitionDataMgr.acquisitionMap = acquisitionMap;
	}

	public static Map<Integer, StaticAcquisition> getAcquisitionMap() {
		return acquisitionMap;
	}

	public static StaticAcquisition getAcquisitionById(int id) {
		return acquisitionMap.get(id);
	}
}
