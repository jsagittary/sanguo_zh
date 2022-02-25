package com.gryphpoem.game.zw.resource.pojo.dressup;

import com.gryphpoem.game.zw.dataMgr.StaticLordDataMgr;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.AwardType;
import com.gryphpoem.game.zw.resource.domain.s.StaticMarchLine;
import com.gryphpoem.game.zw.resource.domain.s.StaticNameplate;

import java.util.List;
import java.util.Objects;

/**
 * 行军特效装扮实体
 * @description:
 * @author: zhou jie
 * @time: 2021/3/9 9:56
 */
public class MarchSpecialEffectEntity extends BaseDressUpEntity {

    public MarchSpecialEffectEntity(int id, int addTime) {
        super(AwardType.MARCH_SPECIAL_EFFECTS, id, addTime);
    }

    public MarchSpecialEffectEntity(int id, boolean permanentHas) {
        super(AwardType.MARCH_SPECIAL_EFFECTS, id, permanentHas);
    }

    public MarchSpecialEffectEntity(CommonPb.DressUpEntity data) {
        super(data);
    }

    @Override
    public List<List<Integer>> convertProps() {
        StaticMarchLine sMarchLine = StaticLordDataMgr.getMarchLine(getId());
        if (Objects.nonNull(sMarchLine)) {
            return sMarchLine.getConsume();
        }
        return null;
    }
}
