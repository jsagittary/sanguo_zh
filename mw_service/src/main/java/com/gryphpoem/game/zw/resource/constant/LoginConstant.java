package com.gryphpoem.game.zw.resource.constant;

/**
 * @ClassName LoginConstant.java
 * @Description 帐号与登录相关功能常量定义类
 * @author TanDonghai
 * @date 创建时间：2017年3月23日 下午7:17:03
 *
 */
public class LoginConstant {
	/** 角色状态：未创建 */
	public static final int ROLE_NOT_CREATE = 0;
	/** 角色状态：已创建 */
	public static final int ROLE_CREATED = 1;
	
	/** BeginGame返回状态：角色未创建 */
	public static final int BEGIN_STATE_NOT_CREATE = 1;
	/** BeginGame返回状态：角色已创建 */
	public static final int BEGIN_STATE_CREATED = 2;
	/** BeginGame返回状态：禁止登陆 */
	public static final int BEGIN_STATE_FORBID = 3;
	/** BeginGame返回状态：不属于白名单，无法登陆 */
	public static final int BEGIN_STATE_NOT_WHITE = 4;
    /** BeginGame返回状态：禁止创建新角色 */
	public static final int BEGIN_STATE_FORBID_CREATE = 5;
	/**
	 * BeginGame返回状态：登陆失败
	 */
	public static final int BEGIN_STATE_ERROR = 5;
	
	/** 玩家选择阵营：由系统随机阵营 */
	public static final int CAMP_RANDOM = 0;
	
	/** 创建角色返回状态：失败 */
	public static final int CREATE_STATE_FAIL = 0;
	/** 创建角色返回状态：成功 */
	public static final int CREATE_STATE_SUCCESS = 1;
	/** 创建角色返回状态：角色已创建 */
	public static final int CREATE_STATE_CREATED = 2;
    /** 创建角色返回状态: 禁止创建 */
	public static final int CREATE_STATE_FORBID = 3;

}
