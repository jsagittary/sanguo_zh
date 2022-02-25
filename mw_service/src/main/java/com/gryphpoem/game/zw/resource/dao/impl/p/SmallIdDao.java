package com.gryphpoem.game.zw.resource.dao.impl.p;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.gryphpoem.game.zw.resource.dao.BaseDao;
import com.gryphpoem.game.zw.resource.domain.p.SmallId;

public class SmallIdDao extends BaseDao {
	public SmallId selectSmallId(long lordId) {
		return this.getSqlSession().selectOne("SmallIdDao.selectSmallId", lordId);
	}

	public void insertSmallId(SmallId smallId) {
		this.getSqlSession().insert("SmallIdDao.insertSmallId", smallId);
	}

	public List<SmallId> load() {
		List<SmallId> list = new ArrayList<SmallId>();
		long curIndex = 0L;
		int count = 1000;
		int pageSize = 0;
		while (true) {
			List<SmallId> page = load(curIndex, count);
			pageSize = page.size();
			if (pageSize > 0) {
				list.addAll(page);
				curIndex = page.get(pageSize - 1).getLordId();
			} else {
				break;
			}

			if (pageSize < count) {
				break;
			}
		}
		return list;
	}

	private List<SmallId> load(long curIndex, int count) {
		Map<String, Object> params = paramsMap();
		params.put("curIndex", curIndex);
		params.put("count", count);
		return this.getSqlSession().selectList("SmallIdDao.load", params);
	}
	
	public void insertAllNewSmallId() {
		this.getSqlSession().insert("SmallIdDao.insertAllNewSmallId");
	}
}
