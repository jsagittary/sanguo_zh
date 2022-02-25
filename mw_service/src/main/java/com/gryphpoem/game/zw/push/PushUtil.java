package com.gryphpoem.game.zw.push;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticPushDataMgr;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.push.PushFactory;
import com.gryphpoem.push.base.IPush;
import com.gryphpoem.push.bean.AppInfo;
import com.gryphpoem.push.message.SingleMessage;
import com.gryphpoem.push.target.ListTarget;
import com.gryphpoem.push.target.SingleTarget;
import com.gryphpoem.push.template.TextTemplate;

/**
 * @Description 消息推送工具类
 * @author TanDonghai
 * @date 创建时间：2017年9月4日 下午8:48:00
 *
 */
public class PushUtil {
    private PushUtil() {
    }

    private static PushServer pushServer;

    public static void setPushServer(PushServer pushServer) {
        PushUtil.pushServer = pushServer;
    }

    private static boolean isIosPlat(int platNo) {
        return platNo > 500;
    }

    /**
     * 添加推送消息
     * 
     * @param title 标题
     * @param content 内容
     * @param platNo 玩家所属渠道号
     * @param deviceNo 玩家设备id
     * @param roleId
     */
    public static void pushMessage(String title, String content, int platNo, String deviceNo, long roleId,
            String appId) {
        if (!StaticPushDataMgr.canPush(platNo, appId)) {
            return;// 如果渠道号未注册推送，跳过推送
        }

        LogUtil.debug("添加推送消息, title:" + title + ", content:" + content + ", platNo:" + platNo + ", deviceNo:"
                + deviceNo + ", roleId:" + roleId, ", appId:" + appId);
        PushMessage message = new PushMessage(title, content, platNo, roleId, appId, deviceNo);
        pushServer.addMessage(message);
    }

    /**
     * 推送消息
     * 
     * @param message
     */
    public static void pushMessage(PushMessage message) {
        if (null == message || !message.needPush()) {
            return;
        }

        LogUtil.debug("开始推送消息, message:" + message);

        AppInfo appInfo = new AppInfo(message.getAppId());

        /** 判断推送是否开启  1 开启 0 未开启 */
        if(Constant.PUSH_CONFIG_SWITCH == 1){
            if (message.singleDevice()) {
                pushSingleTextMessage(message, appInfo);
            } else {
                pushMessageToListDevice(message, appInfo);
            }
        }

    }

    /**
     * 推送单条消息给多个设备
     * 
     * @param message
     * @param appInfo
     */
    private static void pushMessageToListDevice(PushMessage message, AppInfo appInfo) {
        // 创建消息模板
        TextTemplate template = new TextTemplate();
        template.setTitle(message.getTitle());
        template.setContent(message.getContent());

        // 创建消息对象
        SingleMessage singleMessage = new SingleMessage();
        singleMessage.setTemplate(template);

        // 创建批量推送消息
        ListTarget target = new ListTarget(appInfo);
        target.addDeviceNo(message.getDeviceNoList());

        // 创建推送对象
        IPush pusher = PushFactory.newPusher();
        boolean ios = isIosPlat(message.getPlatNo());
        if (ios) {// IOS平台消息推送
            pusher.pushIOSSingleMessage(singleMessage, target);
        } else {
            pusher.pushSingleMessage(singleMessage, target);
        }
    }

    /**
     * 向单个玩家推送一条纯文本消息
     * 
     * @param message
     * @param appInfo
     */
    private static void pushSingleTextMessage(PushMessage message, AppInfo appInfo) {
        // 创建消息模板
        TextTemplate template = new TextTemplate();
        template.setTitle(message.getTitle());
        template.setContent(message.getContent());

        // 创建消息对象
        SingleMessage singleMessage = new SingleMessage();
        singleMessage.setTemplate(template);

        // 创建单个推送目标对象
        SingleTarget target = new SingleTarget(appInfo, message.getDeviceNoList().get(0));
        target.addDevice(message.getDeviceNoList().get(0), message.getRoleId());

        // 创建推送对象
        IPush pusher = PushFactory.newPusher();
        boolean ios = isIosPlat(message.getPlatNo());
        if (ios) {// IOS平台消息推送
            pusher.pushIOSSingleMessage(singleMessage, target);
        } else {
            pusher.pushSingleMessage(singleMessage, target);
        }
    }

}
