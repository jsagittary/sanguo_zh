package com.gryphpoem.game.zw.resource.pojo.sandtable;

import com.gryphpoem.game.zw.pb.SerializePb;

public class SandTableGroup {

    public int round;
    public int state;//0未开始 1已结束
    public int beginTime;
    public int camp1;
    public int camp2;

    public SandTableGroup(){

    }

    public SandTableGroup(int round, int beginTime, int camp1, int camp2) {
        this.round = round;
        this.beginTime = beginTime;
        this.camp1 = camp1;
        this.camp2 = camp2;
    }

    public void deser(SerializePb.SerSandTableGroup serSandTableGroup){
        this.round = serSandTableGroup.getRound();
        this.state = serSandTableGroup.getState();
        this.beginTime = serSandTableGroup.getBeginTime();
        this.camp1 = serSandTableGroup.getCamp1();
        this.camp2 = serSandTableGroup.getCamp2();
    }
}
