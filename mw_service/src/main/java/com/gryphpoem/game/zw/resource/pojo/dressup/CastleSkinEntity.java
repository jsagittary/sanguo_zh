package com.gryphpoem.game.zw.resource.pojo.dressup;

import com.gryphpoem.game.zw.dataMgr.StaticLordDataMgr;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.AwardType;
import com.gryphpoem.game.zw.resource.domain.s.StaticCastleSkin;

import java.util.List;
import java.util.Objects;

/**
 * 城堡皮肤装扮实体
 * @description:
 * @author: zhou jie
 * @time: 2021/3/5 15:22
 */
public class CastleSkinEntity extends BaseDressUpEntity {

    /**
     * 星级
     */
    private int star;

    public CastleSkinEntity(int id) {
        super(AwardType.CASTLE_SKIN, id);
    }

    public CastleSkinEntity(int id, boolean permanentHas) {
        super(AwardType.CASTLE_SKIN, id, permanentHas);
    }

    public CastleSkinEntity(int id, int duration) {
        super(AwardType.CASTLE_SKIN, id, duration);
    }

    public CastleSkinEntity(CommonPb.DressUpEntity data) {
        super(data);
        CommonPb.CastleSkinEntity cs = data.getCs();
        this.star = cs.getStar();
    }

    public int getStar() {
        return star;
    }

    public void setStar(int star) {
        this.star = star;
    }

    @Override
    public CommonPb.DressUpEntity.Builder toData() {
        CommonPb.DressUpEntity.Builder builder = super.toData();
        builder.setCs(CommonPb.CastleSkinEntity.newBuilder().setStar(star).build());
        return builder;
    }

    @Override
    public List<List<Integer>> convertProps() {
        StaticCastleSkin castleSkinCfg = StaticLordDataMgr.getCastleSkinMapById(getId());
        if (Objects.nonNull(castleSkinCfg)) {
            return castleSkinCfg.getConsume();
        }
        return null;
    }
}
