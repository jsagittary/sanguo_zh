package com.gryphpoem.game.zw.resource.pojo.rpc;

import com.gryphpoem.game.zw.pb.CommonPb;

import java.util.Objects;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-08-30 10:47
 */
public class RpcPlayer {
    //rpc服务器中的lord信息
    private RpcLord rpcLord;

    public RpcPlayer(){}

    public RpcPlayer(CommonPb.RpcPlayerPb pb){
        dSer(pb);
    }

    public void dSer(CommonPb.RpcPlayerPb pb){
        if (Objects.nonNull(pb.getLord())){
            rpcLord = new RpcLord();
            rpcLord.deSer(pb.getLord());
        }
    }

    public CommonPb.RpcPlayerPb ser(){
        CommonPb.RpcPlayerPb.Builder builder = CommonPb.RpcPlayerPb.newBuilder();
        if (Objects.nonNull(rpcLord)){
            builder.setLord(rpcLord.ser());
        }
        return builder.build();
    }

    public RpcLord getRpcLord() {
        return rpcLord;
    }

    public void setRpcLord(RpcLord rpcLord) {
        this.rpcLord = rpcLord;
    }
}
