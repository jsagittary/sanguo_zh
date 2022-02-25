package com.gryphpoem.game.zw.resource.pojo.dressup;

import com.gryphpoem.game.zw.dataMgr.StaticLordDataMgr;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.AwardType;
import com.gryphpoem.game.zw.resource.domain.s.StaticNameplate;

import java.util.List;
import java.util.Objects;

/**
 * 铭牌装扮实体
 * @description:
 * @author: zhou jie
 * @time: 2021/3/9 9:56
 */
public class NameplateEntity extends BaseDressUpEntity {

    public NameplateEntity(int id) {
        super(AwardType.NAMEPLATE, id);
    }

    public NameplateEntity(int id, boolean permanentHas) {
        super(AwardType.NAMEPLATE, id, permanentHas);
    }

    public NameplateEntity(int id, int duration) {
        super(AwardType.NAMEPLATE, id, duration);
    }

    public NameplateEntity(CommonPb.DressUpEntity data) {
        super(data);
    }

    @Override
    public List<List<Integer>> convertProps() {
        StaticNameplate staticNameplate = StaticLordDataMgr.getNameplate(getId());
        if (Objects.nonNull(staticNameplate)) {
            return staticNameplate.getConsume();
        }
        return null;
    }

}
