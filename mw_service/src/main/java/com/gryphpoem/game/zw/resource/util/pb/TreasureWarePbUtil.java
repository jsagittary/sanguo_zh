package com.gryphpoem.game.zw.resource.util.pb;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.pojo.attr.AttrItem;
import com.gryphpoem.game.zw.resource.pojo.attr.TreasureWareAttrItem;

/**
 * 宝具序列化工具类
 * Description:
 * Author: zhangdh
 * createTime: 2022-03-02 11:52
 */
public final class TreasureWarePbUtil {

    public static CommonPb.TreasureWareAttrItem createTreasureWareAttrItemPb(TreasureWareAttrItem attr) {
        CommonPb.TreasureWareAttrItem.Builder builder = CommonPb.TreasureWareAttrItem.newBuilder();
        builder.setAttr(createAttrItemPb(attr));
        builder.setInitValue(attr.getInitValue());
        builder.setStage(attr.getStage());
        builder.setPercent(attr.getPercent());
        builder.setTrainTargetIndex(attr.getTrainTargetIndex());
        return builder.build();
    }

    public static CommonPb.AttrItem createAttrItemPb(AttrItem attr) {
        CommonPb.AttrItem.Builder builder = CommonPb.AttrItem.newBuilder();
        builder.setIndex(attr.getIndex());
        builder.setAttrId(attr.getAttrId());
        builder.setValue(attr.getValue());
        builder.setLevel(attr.getLevel());
        builder.setQuality(attr.getQuality());
        return builder.build();
    }
}
