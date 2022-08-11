package com.gryphpoem.game.zw.resource.pojo.treasureware;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.TreasureWareConst;
import com.gryphpoem.game.zw.resource.pojo.GamePb;

/**
 * @Description: 宝具打造，用于记录是否是首次打造
 * @Author: DuanShQ
 * @CreateTime: 2022-06-13
 */
public class MakeTreasureWare implements GamePb<CommonPb.TwoInt> {

    private byte orangeFirstMakeTw;

    private byte ancientFirstMakeTw;

    public byte getOrangeFirstMakeTw() {
        return orangeFirstMakeTw;
    }

    public void setOrangeFirstMakeTw(byte orangeFirstMakeTw) {
        this.orangeFirstMakeTw = orangeFirstMakeTw;
    }

    public byte getAncientFirstMakeTw() {
        return ancientFirstMakeTw;
    }

    public void setAncientFirstMakeTw(byte ancientFirstMakeTw) {
        this.ancientFirstMakeTw = ancientFirstMakeTw;
    }

    public int getMakeCount(int quality) {
        if (quality == TreasureWareConst.ORANGE_QUALITY)
            return orangeFirstMakeTw;
        if (quality == TreasureWareConst.RED_QUALITY)
            return ancientFirstMakeTw;

        return 1;
    }

    public void updateMakeCount(int quality) {
        if (quality == TreasureWareConst.ORANGE_QUALITY)
            orangeFirstMakeTw = 1;
        if (quality == TreasureWareConst.RED_QUALITY)
            ancientFirstMakeTw = 1;
    }

    public void dsePb(CommonPb.TwoInt pb) {
        this.orangeFirstMakeTw = (byte) pb.getV1();
        this.ancientFirstMakeTw = (byte) pb.getV2();
    }

    @Override
    public CommonPb.TwoInt createPb(boolean isSaveDb) {
        CommonPb.TwoInt.Builder builder = CommonPb.TwoInt.newBuilder();
        builder.setV1(orangeFirstMakeTw);
        builder.setV2(ancientFirstMakeTw);
        return builder.build();
    }
}
