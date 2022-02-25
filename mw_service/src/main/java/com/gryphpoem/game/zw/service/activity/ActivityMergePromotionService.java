package com.gryphpoem.game.zw.service.activity;

import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.pb.GamePb3;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.s.StaticPromotion;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import org.springframework.stereotype.Service;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-04-07 1:44
 */
@Service
public class ActivityMergePromotionService {

    public void buildActivity(GamePb3.GetActivityRs.Builder builder, Player player, Activity activity){
        for (StaticPromotion promotion : StaticActivityDataMgr
                .getStaticPromotionListByActId(activity.getActivityId())) {
            // 购买打折礼包次数
            int cnt = activity.getStatusMap().getOrDefault(promotion.getPromotionId(), 0);
            builder.addActivityCond(PbHelper.createActivityCondPb(promotion, cnt > 0 ? 1 : 0, cnt));
        }
    }
}
