package com.gryphpoem.game.zw.resource.util;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Account;

import java.util.Date;

public class AccountHelper {
	public static boolean isForbid(Account account) {
		Date nowDate = new Date();
		if(account.getForbid() == 1 || account.getForbid()>(nowDate.getTime()/1000))
		{
			return true;
		}
		return false;
	}

	/**
	 * 获取最大lordId的key值
	 * 
	 * @param platNo
	 * @param serverId
	 * @return
	 */
	public static String getMaxLordKey(int platNo, int serverId) {
		return platNo + "_" + serverId;
	}

	/**
	 * 获取当前服的 最大lordId的key值
	 * 
	 * @param platNo
	 * @return
	 */
	public static String getCurServerMaxLordKey(int platNo, int serverId) {
		return getMaxLordKey(platNo, serverId);
	}

	/**
	 * 获取lordId中 该渠道该服的Id
	 * 
	 * @param lordId
	 * @return
	 */
	public static int getIdByLordId(long lordId) {
		return (int) (lordId % Constant.ROLE_ID_MULTI);
	}

	/**
	 * 获取lordId中的serverId
	 * 
	 * @param lordId
	 * @return
	 */
	public static int getServerIdByLordId(long lordId) {
		int serverId = (int) (((lordId / Constant.ROLE_ID_MULTI) % 100000L) / 10l);
		return serverId;
	}

	/**
	 * 获取lordId中的platNo
	 * 
	 * @param lordId
	 * @return
	 */
	public static int getPlatNoByLordId(long lordId) {
		int plat = (int) (lordId / (100000L * Constant.ROLE_ID_MULTI));
		return plat;
	}

	/**
	 * 生成新的拼的roleId
	 * 
	 * @param platNo
	 * @param lordByPlatNo
	 * @return
	 */
	public static long createRoleIdByPlatNo(int platNo, long lordByPlatNo, int serverId, int camp) {
		return (platNo * 100000L + serverId * 10l + camp) * 1L * Constant.ROLE_ID_MULTI + lordByPlatNo;
	}

	/**
	 * 获取服务器第一位玩家的角色id，后面的玩家角色id在此基础上自增 <br>
	 * 为保证角色id在全区全服中唯一，角色id由服务器id（serverId）与角色创建序号决定
	 * 
	 * 渠道id 3位数 服务器id 4位数 1位阵营id 角色编号 6位数
	 * 
	 * @return 成功返回一个正整数，否则返回-1
	 */
	public static long getFirstRoleId(int platNo, int serverId, int camp) {
		if (serverId > 0) {
			// return (platNo * 100000L + serverId * 10l + camp) * 1L *
			// Constant.ROLE_ID_MULTI + 1;
			return createRoleIdByPlatNo(platNo, 1, serverId, camp);
		}
		return -1;
	}

	public static void main(String[] args) {
		// long firstRoleId = createRoleIdByPlatNo(81, 99, 101, 1);
		// long firstRoleId = getFirstRoleId(81, 1001, 1);
		// long firstRoleId = 55401020000114L;
		System.out.println("createRoleIdByPlatNo(1, 1, 1, 1) = " + createRoleIdByPlatNo(1, 100, 1, 1));
		// long firstRoleId = 100011000001L;
		long firstRoleId = 100031000001L;

		System.out.println(firstRoleId);
		long roleId = firstRoleId;
		System.out.println(getPlatNoByLordId(roleId));
		System.out.println(getServerIdByLordId(roleId));
		System.out.println(getIdByLordId(roleId));
		String sRoleId = String.valueOf(roleId);
		String id = sRoleId.substring(sRoleId.length() - 8);
		System.out.println("String.valueOf(roleId).substring(6) = " + id);
		System.out.println("Integer.toHexString(Integer.valueOf(id)) = " + Integer.toHexString(Integer.valueOf(id)));
	}

	/**
	 * 检查角色是否存在，如果不存在，抛出MwException异常
	 * 
	 * @param player
	 * @param roleId
	 * @throws MwException
	 */
	public static void checkPlayerIsExist(Player player, long roleId) throws MwException {
		if (null == player) {
			StringBuffer message = new StringBuffer();
			message.append("角色不存在, roleId:").append(roleId);
			throw new MwException(GameError.PLAYER_NOT_EXIST.getCode(), message.toString());
		}
	}
}
