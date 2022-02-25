package com.gryphpoem.game.zw.model.rank;

import com.gryphpoem.game.zw.pb.CommonPb.CrossRankItem;
import com.gryphpoem.game.zw.util.RankLinkedList;

/**
 * @ClassName CamRankItem.java
 * @Description 阵营排行
 * @author QiuKun
 * @date 2019年5月29日
 */
public class CamRankItem extends RankLinkedList.AbsRankItem {
    private int mainServerId; // 主服
    private int camp; // 阵营

    public CamRankItem(int mainServerId, int camp) {
        this.mainServerId = mainServerId;
        this.camp = camp;
    }

    public int getMainServerId() {
        return mainServerId;
    }

    public void setMainServerId(int mainServerId) {
        this.mainServerId = mainServerId;
    }

    public int getCamp() {
        return camp;
    }

    public void setCamp(int camp) {
        this.camp = camp;
    }

    public CrossRankItem toCrossRankItemPb() {
        CrossRankItem.Builder builder = CrossRankItem.newBuilder();
        builder.setCamp(camp);
        builder.setMainServerId(mainServerId);
        builder.setVal(val);
        builder.setTime(mTime);
        return builder.build();
    }

    public CamRankItem(CrossRankItem pb) {
        this.mainServerId = pb.getMainServerId();
        this.camp = pb.getCamp();
        this.mTime = pb.getTime();
        this.val = pb.getVal();
    }
}
