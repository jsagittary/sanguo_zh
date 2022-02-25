package com.gryphpoem.game.zw.resource.pojo.dressup;

import com.gryphpoem.game.zw.dataMgr.StaticLordDataMgr;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.AwardType;
import com.gryphpoem.game.zw.resource.domain.s.StaticPortraitFrame;

import java.util.List;
import java.util.Objects;

/**
 * 头像框装扮实体
 * @description:
 * @author: zhou jie
 * @time: 2021/3/9 9:54
 */
public class PortraitFrameEntity extends BaseDressUpEntity {

    public PortraitFrameEntity(int id) {
        super(AwardType.PORTRAIT_FRAME, id);
    }

    public PortraitFrameEntity(int id, boolean permanentHas) {
        super(AwardType.PORTRAIT_FRAME, id, permanentHas);
    }

    public PortraitFrameEntity(int id, int duration) {
        super(AwardType.PORTRAIT_FRAME, id, duration);
    }

    public PortraitFrameEntity(CommonPb.DressUpEntity data) {
        super(data);
    }

    @Override
    public List<List<Integer>> convertProps() {
        StaticPortraitFrame staticPortraitFrame = StaticLordDataMgr.getPortraitFrame(getId());
        if (Objects.nonNull(staticPortraitFrame)) {
            return staticPortraitFrame.getConsume();
        }
        return null;
    }
}
