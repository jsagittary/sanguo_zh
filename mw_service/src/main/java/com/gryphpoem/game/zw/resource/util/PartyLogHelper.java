package com.gryphpoem.game.zw.resource.util;

import java.util.HashMap;
import java.util.Map;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.manager.CampDataManager;
import com.gryphpoem.game.zw.pb.CommonPb.PartyLog;
import com.gryphpoem.game.zw.resource.constant.PartyConstant;
import com.gryphpoem.game.zw.resource.pojo.party.Camp;

/**
 * @ClassName PartyLogHelper.java
 * @Description 军团日志记录相关操作
 * @author TanDonghai
 * @date 创建时间：2017年5月8日 上午10:06:50
 *
 */
public class PartyLogHelper {

	private static CampDataManager campDataManager = DataResource.ac.getBean(CampDataManager.class);

	private static Map<Integer, Integer> logParamMap = new HashMap<Integer, Integer>();

	static {
		registerPartyLogParamNum();
	}

	/**
	 * 注册军团日志参数个数信息
	 */
	private static void registerPartyLogParamNum() {
		logParamMap.put(PartyConstant.LOG_CITY_REBUILD, 4);
		logParamMap.put(PartyConstant.LOG_CITY_CONQUERED, 4);
		logParamMap.put(PartyConstant.LOG_CITY_LIMIT, 4);
		logParamMap.put(PartyConstant.LOG_LEAVE_CITY, 4);
		logParamMap.put(PartyConstant.LOG_CITY_BREACHED, 5);
		logParamMap.put(PartyConstant.LOG_PROMOTE_RANKS, 2);
	}

	/**
	 * 获取军团日志的参数个数
	 * 
	 * @param logId
	 * @return 如果日志id未注册，返回-1
	 */
	public static int getPartyLogParamNum(int logId) {
		Integer num = logParamMap.get(logId);
		return num == null ? -1 : num;
	}

	/**
	 * 添加军团日志
	 * 
	 * @param camp
	 * @param logId
	 * @param params
	 * @throws MwException
	 */
	public static void addPartyLog(int camp, int logId, Object... params) throws MwException {
		int paramNum = getPartyLogParamNum(logId);
		if (paramNum < 0) {
			throw new MwException("添加军团日志，日志id未注册, logId:" + logId);
		}

		int paramLen = null == params ? 0 : params.length;
		if (paramNum != paramLen) {
			throw new MwException("添加军团日志，日志参数不正确, logId:" + logId + ", need:" + paramNum + ", 传入个数:" + paramLen);
		}

		Camp party = campDataManager.getParty(camp);
		if (null == party) {
			throw new MwException("添加军团日志，未找到军团对象, camp:" + camp);
		}

		party.getLog().addFirst(createPartyLogPb(logId, TimeHelper.getCurrentSecond(), params));
	}

	/**
	 * 创建军团日志pb对象
	 * 
	 * @param logId
	 * @param time
	 * @param params
	 * @return
	 */
	public static PartyLog createPartyLogPb(int logId, int time, Object... params) {
		PartyLog.Builder builder = PartyLog.newBuilder();
		builder.setLogId(logId);
		builder.setTime(time);
		if (!CheckNull.isEmpty(params)) {
			for (Object param : params) {
				builder.addParam(String.valueOf(param));
			}
		}
		return builder.build();
	}
}
