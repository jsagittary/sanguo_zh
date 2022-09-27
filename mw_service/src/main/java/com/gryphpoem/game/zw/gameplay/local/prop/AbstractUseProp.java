package com.gryphpoem.game.zw.gameplay.local.prop;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticProp;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.Prop;

import java.util.List;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-09-26 15:12
 */
public abstract class AbstractUseProp {

    public abstract int propType();

    /**
     * 校验道具使用
     *
     * @param count
     * @param staticProp
     * @param player
     * @param prop
     * @param params
     * @param roleId
     * @param propId
     * @param listAward
     * @param change
     * @param paramArr
     */
    public abstract void checkUseProp(int count, StaticProp staticProp, Player player, Prop prop,
                                      String params, long roleId, int propId, List<CommonPb.Award> listAward,
                                      ChangeInfo change, Object... paramArr) throws MwException;

    /**
     * 使用道具
     *
     * @param count
     * @param staticProp
     * @param player
     * @param prop
     * @param params
     * @param roleId
     * @param propId
     * @param listAward
     * @param change
     */
    public abstract List<CommonPb.Award> useProp(int count, StaticProp staticProp, Player player, Prop prop,
                                                 String params, long roleId, int propId, List<CommonPb.Award> listAward,
                                                 ChangeInfo change, Object... paramArr);
}
