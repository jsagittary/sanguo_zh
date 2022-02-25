package com.gryphpoem.game.zw.resource.pojo;

import com.gryphpoem.game.zw.resource.util.CheckNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @ClassName ChangeInfo.java
 * @Description 记录要同步到客户端的玩家资源变更类型
 * @author TanDonghai
 * @date 创建时间：2017年4月18日 下午5:15:55
 *
 */
public class ChangeInfo {
	private List<Integer> awardTypeList = new ArrayList<>();
	private List<Integer> awardIdList = new ArrayList<>();
	private Set<String> distinctSet = new HashSet<>();

	private ChangeInfo() {
	}

	public static ChangeInfo newIns() {
		return new ChangeInfo();
	}

	public void addChangeType(int awardType, int awardId) {
		StringBuffer sb = new StringBuffer();
		final String award = sb.append(awardType).append(awardId).toString();
		if (distinctSet.contains(award)) {
			return;// 避免重复
		}
		distinctSet.add(award);
		awardTypeList.add(awardType);
		awardIdList.add(awardId);
	}

	public List<Integer> getAwardTypeList() {
		return awardTypeList;
	}

	public List<Integer> getAwardIdList() {
		return awardIdList;
	}

	public boolean isEmpty() {
		return CheckNull.isEmpty(awardTypeList) && CheckNull.isEmpty(awardIdList);
	}

	public int getChangeLen() {
		return awardTypeList.size();
	}

	public int getType(int index) {
		return awardTypeList.get(index);
	}

	public int getId(int index) {
		return awardIdList.get(index);
	}
}
