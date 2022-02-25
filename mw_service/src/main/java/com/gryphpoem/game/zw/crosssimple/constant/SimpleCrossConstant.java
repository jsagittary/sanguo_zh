package com.gryphpoem.game.zw.crosssimple.constant;

/**
 * @ClassName SimpleCrossConstant.java
 * @Description
 * @author QiuKun
 * @date 2019年5月16日
 */
public interface SimpleCrossConstant {

    /** 登陆跨服 */
    public static final int OP_TYPE_LOGIN = 1;
    /** 关闭跨服界面 */
    public static final int OP_TYPE_CLOSE = 2;
    /** 玩家下线 */
    public static final int OP_TYPE_OFFLINE = 3;
    /** 信息同步 */
    public static final int OP_TYPE_SYNCINFO = 4;
    /** 退出跨服 */
    public static final int OP_TYPE_LOGOUT = 5;

    /** 将领操作,前往 */
    public static final int HERO_OPERATE_GOTO = 1;
    /** 将领操作,回防 */
    public static final int HERO_OPERATE_BACK_COURT = 2;
    /** 将领操作,进攻 */
    public static final int HERO_OPERATE_ATTACK = 3;
    /** 将领操作,偷袭 */
    public static final int HERO_OPERATE_SNEAK = 4;
    /** 将领操作,复活 */
    public static final int HERO_OPERATE_REVIVE = 5;
    /** 将领操作,单挑 */
    public static final int HERO_OPERATE_SOLO = 6;

}
