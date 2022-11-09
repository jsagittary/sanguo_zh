package com.gryphpoem.game.zw.core.util;

import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.TextFormat;
import com.gryphpoem.game.zw.core.exception.ExceptionMessage;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.thread.ServerThread;
import com.gryphpoem.game.zw.pb.BasePb;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.logging.log4j.Level;

import java.util.HashSet;
import java.util.Set;

/**
 * @author TanDonghai
 * @ClassName LogUtil.java
 * @Description 日志记录工具类
 * @date 创建时间：2016年9月2日 下午3:29:19
 */
public class LogUtil {
    private LogUtil() {
    }

    /**
     * 用于异步日志线程
     */
    private static ServerThread logThread;

    static {
        logThread = new ServerThread(new ThreadGroup("LOG_GROUP"), "LOG_THREAD", 0);
        logThread.start();
    }

    private static Logger PAY_LOGGER = LogManager.getLogger("PAY");
    private static Logger CHAT_LOGGER = LogManager.getLogger("CHAT");
    private static Logger SAVE_LOGGER = LogManager.getLogger("SAVE");
    private static Logger WARN_LOGGER = LogManager.getLogger("WARN");
    private static Logger ERROR_LOGGER = LogManager.getLogger("ERROR");
    private static Logger COMMON_LOGGER = LogManager.getLogger("COMMON");
    private static Logger MESSAGE_LOGGER = LogManager.getLogger("MESSAGE");
    private static Logger ACTIVITY_LOGGER = LogManager.getLogger("ACTIVITY");
    private static Logger STATISTICS_LOGGER = LogManager.getLogger("STATISTICS");
    private static Logger HOTFIX_LOGGER = LogManager.getLogger("HOTFIX");
    private static Logger ROBOT_LOGGER = LogManager.getLogger("ROBOT");
    private static Logger WORLD_LOGGER = LogManager.getLogger("WORLD");
    private static Logger BATTLE_LOGGER = LogManager.getLogger("BATTLE");
    private static Logger CALCULATE_LOGGER = LogManager.getLogger("CALCULATE");

    /**
     * 项目的日志打印等级
     */
    private static final Level LEVEL = Level.DEBUG;

    /**
     * 根据传入的日志等级，返回当前是否允许打印这个等级的日志
     *
     * @param level 日志等级{@link Level}
     * @return 如果传入的日志等级不小于系统设置的等级，返回true
     */
    private static boolean canPrint(Level level) {
        if (LEVEL == Level.OFF) {
            return false;
        }

        return LEVEL.isLessSpecificThan(level);
    }

    public static void setLevel(String logName, org.apache.log4j.Level level) {
        LogManager.getLogger(logName).setLevel(level);
    }

    public static void calculate(Object... message) {
        logThread.addCommand(() -> {
            CALCULATE_LOGGER.debug(getClassPath() + ExceptionMessage.spliceMessage(message));
        });
    }

    /**
     * 异步打印战斗日志
     *
     * @param message
     */
    public static void fight(Object... message) {
        if (canPrint(Level.DEBUG)) {
            COMMON_LOGGER.info("[debug] " + getClassPath() + ExceptionMessage.spliceMessage(message));
        }
    }

    public static void debug(Object... message) {
        if (canPrint(Level.DEBUG)) {
            getLogThread().addCommand(() -> {
                COMMON_LOGGER.info("[debug] " + getClassPath() + ExceptionMessage.spliceMessage(message));
            });
        }
    }

    public static void error(Object message, Throwable t) {
        ERROR_LOGGER.error("[error] " + getClassPath() + message, t);
        sentry(t);
    }

    public static void error(Throwable t, Object... message) {
        ERROR_LOGGER.error("[error] " + getClassPath() + ExceptionMessage.spliceMessage(message), t);
        sentry(t);
    }

    public static void error(Object... message) {
        ERROR_LOGGER.error("[error] " + getClassPath() + ExceptionMessage.spliceMessage(message));
    }

    public static void error2Sentry(Object... message) {
        String msg = "[error] " + getClassPath() + ExceptionMessage.spliceMessage(message);
        ERROR_LOGGER.error(msg);
        sentry(msg);
    }

    public static void start(Object message) {
        ERROR_LOGGER.info("[start] " + getClassPath() + message);
    }

    public static void stop(Object message) {
        ERROR_LOGGER.info("[stop] " + getClassPath() + message);
    }

    public static void warn(Object... message) {
        String msg = "[warn] " + getClassPath() + ExceptionMessage.spliceMessage(message);
        WARN_LOGGER.info(msg);
        sentry(msg);
    }

    public static void sentry(Throwable t) {
        if (needSendException(t)) {
            SentryHelper.sendToSentry(t);
        }
    }

    public static void sentry(String message) {
        SentryHelper.sendToSentry(message);
    }

    public static void common(Object... message) {
        COMMON_LOGGER.info("[common] " + getClassPath() + ExceptionMessage.spliceMessage(message));
    }

    public static void boss(Object message) {
        COMMON_LOGGER.info("[boss] " + getClassPath() + message);
    }

    public static void war(Object message) {
        COMMON_LOGGER.info("[war] " + getClassPath() + message);
    }

    public static void channel(Object message) {
        STATISTICS_LOGGER.info("[channel] " + getClassPath() + message);
    }

    public static void haust(Object message) {
        STATISTICS_LOGGER.info("[haust] " + getClassPath() + message);
    }

    public static void flow(Object message) {
        STATISTICS_LOGGER.info("[flow] " + getClassPath() + message);
    }

    public static void gm(Object... message) {
        STATISTICS_LOGGER.info("[GM] " + getClassPath() + ExceptionMessage.spliceMessage(message));
    }

    @SuppressWarnings("unused")
    public static void robot(Object... message) {
        if (false) {
            ROBOT_LOGGER.info("[robot]" + getClassPath() + ExceptionMessage.spliceMessage(message));
        }
    }

    public static void robot(Throwable t, Object... message) {
        ROBOT_LOGGER.error("[robot]" + getClassPath() + ExceptionMessage.spliceMessage(message), t);
        if (needSendException(t)) {
            SentryHelper.sendToSentry(t);
        }
    }

    /**
     * 判断异常是否需要发送到远端记录
     *
     * @param t
     * @return
     */
    private static boolean needSendException(Throwable t) {
        return !(t instanceof MwException);
    }

    public static void hotfix(Object message) {
        HOTFIX_LOGGER.info("[hotfix]" + getClassPath() + message);
    }

    public static void hotfix(Object message, Throwable t) {
        HOTFIX_LOGGER.error("[hotfix] " + getClassPath() + message, t);
        if (needSendException(t)) {
            SentryHelper.sendToSentry(t);
        }
    }

    public static void world(Object... message) {
        StringBuilder sb = new StringBuilder();
        sb.append("[world]").append(getClassPath()).append(ExceptionMessage.spliceMessage(message));
        // final String msg = "[world]" + getClassPath() + ExceptionMessage.spliceMessage(message);
        final String msg = sb.toString();
        logThread.addCommand(() -> {
            WORLD_LOGGER.info(msg);
        });

    }

    /**
     * 将协议日志打印成短格式的日志
     *
     * @param obj
     * @return
     */
    public static Object obj2ShortStr(Object obj) {
        if (!FileUtil.isWindows() && (obj instanceof BasePb.Base)) {
            return TextFormat.shortDebugString((MessageOrBuilder) obj);
        }
        return obj;
    }

    public static void c2sReqMessage(Object message, Long roleId) {
        MESSAGE_LOGGER.info("[c2s] " + getClassPath() + "roleId:" + roleId + ", " + obj2ShortStr(message));
    }

//    public static void c2sMessage(Object message, Long roleId) {
//        MESSAGE_LOGGER.info("[s2c] " + getClassPath(4) + "roleId:" + roleId + ", " + lineSeparator() + obj2ShortStr(message));
//    }

    public static void c2sMessage(Object message, Long roleId) {
        MESSAGE_LOGGER.info("[s2c] " + getClassPath(4) + "roleId:" + roleId + ", " + obj2ShortStr(message));
    }

    private static String lineSeparator() {
        return System.getProperty("line.separator");
    }

    public static void s2sMessage(Object message) {
        MESSAGE_LOGGER.info("[s2s] " + getClassPath() + obj2ShortStr(message));
    }

    public static void innerMessage(Object message) {
        MESSAGE_LOGGER.info("[inner] " + getClassPath() + obj2ShortStr(message));
    }

    public static void save(Object message) {
        SAVE_LOGGER.info("[save] " + getClassPath() + message);
    }

    public static void activity(Object message) {
        ACTIVITY_LOGGER.info("[activity] " + getClassPath() + message);
    }

    public static void pay(Object message) {
        PAY_LOGGER.info("[pay] " + getClassPath() + message);
    }

    public static void chat(Object message) {
        // CHAT_LOGGER.info("[chat] " + getClassPath() + message);
        CHAT_LOGGER.info(message);
    }

    /**
     * 聊天日志记录
     *
     * @param channel  聊天渠道
     * @param style    样式
     * @param serverId 角色服务器Id
     * @param nick     昵称
     * @param roleId   角色Id
     * @param content  内容
     * @param server   区服id
     * @param area     区域聊天记录，默认为0
     * @param nick2    私聊对象昵称 私聊时有值
     * @param roleId2  私聊对象角色Id 私聊时有值
     */
    public static void commonChat(String channel, int style, int serverId, String nick, Long roleId, String content,
                                  int server, int area, String nick2, Long roleId2) {
        StringBuilder sb = new StringBuilder();
        sb.append(channel).append("|").append(style).append("|").append(serverId).append("|").append(nick).append("|")
                .append(roleId).append("|").append(content);

        sb.append("|").append(server);
        sb.append("|").append(area);
        sb.append("|").append(nick2 == null ? "" : nick2);
        sb.append("|").append(roleId2 == null ? "" : roleId2);

        CHAT_LOGGER.info(sb);
    }

    public static void battle(String battleId, Object... msgs) {
        StringBuilder sb = new StringBuilder();
        sb.append("battle|").append(battleId);
        for (Object msg : msgs) {
            sb.append("|").append(msg);
        }
        logThread.addCommand(() -> BATTLE_LOGGER.info(sb.toString()));
    }

    public static void silence(Object message) {
        STATISTICS_LOGGER.info("[SILENCE] " + getClassPath() + message);
    }

    private static String getClassPath() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StackTraceElement ele = stackTraceElements[3];
        return getSimpleClassName(ele.getFileName()) + "." + ele.getMethodName() + "():" + ele.getLineNumber() + " - ";
    }

    private static String getClassPath(int stackIndex) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        if (stackTraceElements.length <= stackIndex) {
            stackIndex = 3;
        }
        StackTraceElement ele = stackTraceElements[stackIndex];
        return getSimpleClassName(ele.getFileName()) + "." + ele.getMethodName() + "():" + ele.getLineNumber() + " - ";
    }

    public static String getSimpleClassName(String fileName) {
        int index = fileName.indexOf(".");
        if (index > 0) {
            return fileName.substring(0, index);
        }
        return fileName;
    }

    public static ServerThread getLogThread() {
        return logThread;
    }

    public static String getSetValStr(Set<Integer> set) {
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        int size = set.size();
        int temp = 1;
        for (Integer val : set) {
            sb.append(val);
            if (temp == size) {
                sb.append("]");
            } else {
                sb.append(",");
            }
            temp++;
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        HashSet<Integer> set = new HashSet<>();
        set.add(1);
        set.add(2);
        set.add(3);
        set.add(4);
        System.out.println(getSetValStr(set));
    }
}
