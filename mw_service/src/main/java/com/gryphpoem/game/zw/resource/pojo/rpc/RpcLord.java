package com.gryphpoem.game.zw.resource.pojo.rpc;

import com.gryphpoem.game.zw.pb.CommonPb;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-08-30 10:47
 */
public class RpcLord {
    private int syncDataTime;//最后lord数据到rpc-player 服务器时间

    public CommonPb.RpcLordPb ser(){
        CommonPb.RpcLordPb.Builder builder = CommonPb.RpcLordPb.newBuilder();
        builder.setSyncDataTime(syncDataTime);
        return builder.build();
    }

    public void deSer(CommonPb.RpcLordPb pb){
        this.syncDataTime = pb.getSyncDataTime();
    }

    public int getSyncDataTime() {
        return syncDataTime;
    }

    public void setSyncDataTime(int syncDataTime) {
        this.syncDataTime = syncDataTime;
    }
}
