package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.dataMgr.StaticBannerDataMgr;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.domain.s.StaticMergeBanner;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: BannerService
 * Date:      2020/12/9 9:57
 * author     shi.pei
 */
@Service
public class BannerService {

    /**
     * 获取合服banner信息
     */
    public GamePb4.GetBannerRs getBannerInfo(GamePb4.GetBannerRq rq){
        GamePb4.GetBannerRs.Builder builder = GamePb4.GetBannerRs.newBuilder();
        List<StaticMergeBanner> mbList = StaticBannerDataMgr.getMergeBannerList();
        if (mbList== null || mbList.size() == 0){
            builder.setIsShow(0);
        }else {
            builder.setIsShow(1);
            List<CommonPb.MergeBannerData> pbList = new ArrayList<>(mbList.size());
            for (StaticMergeBanner smb:mbList){
                pbList.add(PbHelper.createMergeBannerData(smb));
            }
            builder.addAllMergeBannerData(pbList);
        }
        
        return builder.build();
    }
}
