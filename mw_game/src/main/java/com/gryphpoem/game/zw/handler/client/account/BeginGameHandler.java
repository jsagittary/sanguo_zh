package com.gryphpoem.game.zw.handler.client.account;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.GamePb1.BeginGameRq;
import com.gryphpoem.game.zw.pb.HttpPb.VerifyRq;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.domain.p.TargetServerCamp;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.server.AppGameServer;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description 客户端向游戏服务器请求所选服武器角色状态
 * @author TanDonghai
 */
public class BeginGameHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        BeginGameRq req = msg.getExtension(BeginGameRq.ext);

        // 客户端发过来的登陆验证请求，这里转发给账号服务器做验证
        long keyId = req.getKeyId();
        String token = req.getToken();
        int serverId = req.getServerId();
        String curVersion = req.getCurVersion();
        String deviceNo = req.getDeviceNo();

        LogUtil.debug("keyId:" + keyId + ", token:" + token + ", serverId:" + serverId + ", curVersion:" + ", deviceNo:"
                + deviceNo + ", channelId:" + getChannelId());

        // 检测区服是否合法
        ServerSetting serverSetting = AppGameServer.ac.getBean(ServerSetting.class);
        List<TargetServerCamp> tscList = serverSetting.getAllowJoinServerIdCampList().stream()
                .filter(tsc -> tsc.getOriginServerId() == serverId).collect(Collectors.toList());
        if (CheckNull.isEmpty(tscList)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "服务器id不合法  serverId:", serverId, ", keyId:", keyId,
                    ", allowServerIdCamp:", serverSetting.getAllowJoinServerCampStr());
        }

        VerifyRq.Builder builder = VerifyRq.newBuilder();
        builder.setKeyId(keyId);
        builder.setServerId(serverId);
        builder.setToken(token);
        builder.setCurVersion(curVersion);
        builder.setDeviceNo(deviceNo);
        builder.setChannelId(getChannelId());

        Base.Builder baseBuilder = PbHelper.createRqBase(VerifyRq.EXT_FIELD_NUMBER, null, VerifyRq.ext,
                builder.build());

        // 发给账号服务器
        sendMsgToPublic(baseBuilder);
    }
}
