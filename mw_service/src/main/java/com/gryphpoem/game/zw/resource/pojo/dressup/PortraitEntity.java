package com.gryphpoem.game.zw.resource.pojo.dressup;

import com.gryphpoem.game.zw.dataMgr.StaticLordDataMgr;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.AwardType;
import com.gryphpoem.game.zw.resource.domain.s.StaticPortrait;

import java.util.List;
import java.util.Objects;

/**
 * 头像装扮实体
 * @description:
 * @author: zhou jie
 * @time: 2021/3/4 17:34
 */
public class PortraitEntity extends BaseDressUpEntity {

    public PortraitEntity(int id) {
        super(AwardType.PORTRAIT, id);
    }

    public PortraitEntity(int id, boolean permanentHas) {
        super(AwardType.PORTRAIT, id, permanentHas);
    }

    public PortraitEntity(int id, int duration) {
        super(AwardType.PORTRAIT, id, duration);
    }

    public PortraitEntity(CommonPb.DressUpEntity data) {
        super(data);
    }

    @Override
    public List<List<Integer>> convertProps() {
        StaticPortrait staticPortrait = StaticLordDataMgr.getPortrait(getId());
        if (Objects.nonNull(staticPortrait)) {
            return staticPortrait.getConsume();
        }
        return null;
    }
}
