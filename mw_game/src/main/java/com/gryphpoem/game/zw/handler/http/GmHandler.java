package com.gryphpoem.game.zw.handler.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.gryphpoem.game.zw.core.ICommand;
import com.gryphpoem.game.zw.core.handler.DealType;
import com.gryphpoem.game.zw.core.handler.HttpHandler;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CommonPb.AccountRoleInfo;
import com.gryphpoem.game.zw.pb.GamePb1.DoSomeRq;
import com.gryphpoem.game.zw.pb.HttpPb.SendAccountRoleRq;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.GmService;

/**
 * 
 * @Description 发送后台邮件
 * @author TanDonghai
 *
 */
public class GmHandler extends HttpHandler {
    private static int MaxUp = 300;

    @Override
    public void action() {
        final DoSomeRq req = msg.getExtension(DoSomeRq.ext);

        AppGameServer.getInstance().mainLogicServer.addCommand(new ICommand() {
            @Override
            public void action() {
                LogUtil.debug("=========DoSomeRq=" + req);
                String str = req.getStr();
                LogUtil.debug("GM-send------------" + str);
                String[] words = str.split(" ");
                if ("acRole".equals(words[0])) {
                    // 账号上传
                    sendAllRoleToAccount();
                } else {
                    // 其他的GM指令处理
                    GmService gmService = AppGameServer.ac.getBean(GmService.class);
                    try {
                        gmService.doSome(req, Long.valueOf(req.getRoleId()).longValue());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, DealType.PUBLIC);
    }

    /**
     * 发送角色信息给账号服
     */
    public static void sendRoleToAccount(Player p) {
        if (p == null) {
            return;
        }
        AccountRoleInfo ri = PbHelper.createAccountRoleInfo(p);

        // 推送角色信息给账户服
        SendAccountRoleRq.Builder push = SendAccountRoleRq.newBuilder();
        push.addArs(ri);
        Base.Builder baseBuilder = PbHelper.createRqBase(SendAccountRoleRq.EXT_FIELD_NUMBER, null,
                SendAccountRoleRq.ext, push.build());
        AppGameServer.getInstance().sendMsgToPublic(baseBuilder);
    }

    /**
     * 发送全部角色信息给账号服
     */
    public static void sendAllRoleToAccount() {
        PlayerDataManager playerDataManager = AppGameServer.ac.getBean(PlayerDataManager.class);
        Map<String, Player> ps = playerDataManager.getAllPlayer();
        Map<String, Player> m = new HashMap<>();
        m.putAll(ps);
        // 组装角色信息
        List<AccountRoleInfo> al = new ArrayList<>();
        Iterator<String> it = m.keySet().iterator();
        while (it.hasNext()) {
            Player p = m.get(it.next());
            if (p == null || p.account == null) {
                continue;
            }
            AccountRoleInfo ri = PbHelper.createAccountRoleInfo(p);
            al.add(ri);
        }

        // 一次最多发送1000个角色
        int num = MaxUp;
        SendAccountRoleRq.Builder push = SendAccountRoleRq.newBuilder();
        // 批量发送角色数据信息
        for (int i = 0; i < al.size(); i++) {
            num--;
            push.addArs(al.get(i));
            if (num == 0 || i == al.size() - 1) {
                // 推送角色信息给账户服
                Base.Builder baseBuilder = PbHelper.createRqBase(SendAccountRoleRq.EXT_FIELD_NUMBER, null,
                        SendAccountRoleRq.ext, push.build());
                AppGameServer.getInstance().sendMsgToPublic(baseBuilder);
                push = SendAccountRoleRq.newBuilder();
                num = MaxUp;
            }
        }

    }
}
