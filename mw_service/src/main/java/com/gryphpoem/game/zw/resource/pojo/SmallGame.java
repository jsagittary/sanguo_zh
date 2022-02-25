package com.gryphpoem.game.zw.resource.pojo;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.util.CheckNull;

import java.util.HashSet;
import java.util.Set;

/**
 * 玩家小游戏记录
 * @Description
 * @Author zhangdh
 * @Date 2021-11-24 10:28
 */
public class SmallGame {
    //已经领取过的小游戏奖励
    private Set<Integer> awardSet = new HashSet<>();
    //已经领取过的小游戏额外奖励
    private Set<Integer> extAwardSet = new HashSet<>();

    public SmallGame(){}
    public SmallGame(CommonPb.SmallGame pb){
        if (CheckNull.nonEmpty(pb.getAwardList())){
            awardSet.addAll(pb.getAwardList());
        }
        if (CheckNull.nonEmpty(pb.getExtAwardList())){
            extAwardSet.addAll(pb.getExtAwardList());
        }
    }

    public CommonPb.SmallGame ser(){
        CommonPb.SmallGame.Builder builder = CommonPb.SmallGame.newBuilder();
        if (!awardSet.isEmpty()){
            builder.addAllAward(awardSet);
        }
        if (!extAwardSet.isEmpty()){
            builder.addAllExtAward(extAwardSet);
        }
        return builder.build();
    }

    public Set<Integer> getAwardSet() {
        return awardSet;
    }

    public Set<Integer> getExtAwardSet() {
        return extAwardSet;
    }
}
