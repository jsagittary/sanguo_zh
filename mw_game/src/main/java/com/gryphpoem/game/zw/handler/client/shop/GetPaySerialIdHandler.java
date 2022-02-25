package com.gryphpoem.game.zw.handler.client.shop;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.GamePb3.GetPaySerialIdRq;
import com.gryphpoem.game.zw.pb.HttpPb.PayApplyRq;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.PayService;

/**
 * 
 * @Description 获取支付内部支付订单号
 * @author QiuKun
 *
 */
public class GetPaySerialIdHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetPaySerialIdRq req = msg.getExtension(GetPaySerialIdRq.ext);
        PayService service = getService(PayService.class);
        PayApplyRq message = service.payApplyRqProcess(req);
        if (message != null) {
            // 向支付服务区请求订单号
            Base.Builder baseBuilder = PbHelper.createRqBase(PayApplyRq.EXT_FIELD_NUMBER, null, PayApplyRq.ext,
                    message);
            AppGameServer.getInstance().sendMsgToPublicPay(baseBuilder);
        }

    }

//    public static void main(String[] args) throws Exception {
//        ExtensionRegistry PB_EXTENDSION_REGISTRY = ExtensionRegistry.newInstance();
//        HttpPb.registerAllExtensions(PB_EXTENDSION_REGISTRY);
//        PayApplyRq.Builder builder = PayApplyRq.newBuilder();
//        builder.setRoleId(1225752195);
//        builder.setPayType(6);
//        builder.setPlatName("muzhi");
//        builder.setServerId(6);
//        builder.setAmount(100);
//        builder.setAccountKey(4895);
//        Base.Builder baseBuilder = PbHelper.createRqBase(PayApplyRq.EXT_FIELD_NUMBER, null, PayApplyRq.ext,
//                builder.build());
//        Base msg = baseBuilder.build();
//        String url = "http://192.168.2.80:8080/modernwar_account/account/inner.do";
//        byte[] result = HttpUtils.sendPbByte(url, msg.toByteArray());
//
//        short len = MessageUtil.getShort(result, 0);
//        byte[] data = new byte[len];
//        System.arraycopy(result, 2, data, 0, len);
//
//        Base rs = Base.parseFrom(data, PB_EXTENDSION_REGISTRY);
//        // System.out.println(rs);
//        PayApplyRs rs2 = rs.getExtension(PayApplyRs.ext);
//        System.out.println(rs2);
//        System.out.println("获得的的id :" + rs2.getSerialId());
//    }
}
