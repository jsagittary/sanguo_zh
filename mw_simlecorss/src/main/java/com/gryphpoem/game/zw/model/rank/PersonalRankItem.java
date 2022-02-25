package com.gryphpoem.game.zw.model.rank;

import com.gryphpoem.game.zw.pb.CommonPb.CrossRankItem;
import com.gryphpoem.game.zw.util.RankLinkedList;

/**
 * @ClassName PersonalRankItem.java
 * @Description 个人排行榜
 * @author QiuKun
 * @date 2019年5月29日
 */
public class PersonalRankItem extends RankLinkedList.AbsRankItem {
    private long lordId;

    public PersonalRankItem(long lordId) {
        super();
        this.lordId = lordId;
    }

    public long getLordId() {
        return lordId;
    }

    public void setLordId(long lordId) {
        this.lordId = lordId;
    }

    public CrossRankItem toCrossRankItemPb() {
        CrossRankItem.Builder builder = CrossRankItem.newBuilder();
        builder.setRoleId(lordId);
        builder.setVal(val);
        builder.setTime(mTime);
        return builder.build();
    }

    public PersonalRankItem(CrossRankItem pb) {
        this.lordId = pb.getRoleId();
        this.mTime = pb.getTime();
        this.val = pb.getVal();
    }
}
