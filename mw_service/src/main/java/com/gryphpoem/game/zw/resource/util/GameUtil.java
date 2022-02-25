package com.gryphpoem.game.zw.resource.util;

import com.gryphpoem.game.zw.resource.domain.p.AwardItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 游戏通用工具类
 * @author xwind
 * @date 2021/4/21
 */
public class GameUtil {

    /**
     * 根据权重随机一个奖励
     * @param awardList
     * @param sum
     * @return
     */
    public static List<AwardItem> randomAwardByWeight(List<List<Integer>> awardList, int sum){
        List<AwardItem> retList = new ArrayList<>();
        int rdm = RandomUtil.randomIntExcludeEnd(0,sum);
        int swap = 0;
        for (List<Integer> list : awardList) {
            int w = list.get(3);
            if(rdm >= swap && rdm < w + swap){
                List<Integer> tmp = new ArrayList<>(3);
                retList.add(new AwardItem(list.get(0),list.get(1),list.get(2)));
                break;
            }
            swap += w;
        }
        return retList;
    }

    public static List<AwardItem> randomAwardByWeight(List<List<Integer>> awardList){
        int sum = awardList.stream().mapToInt(tmps -> tmps.get(3)).sum();
        return randomAwardByWeight(awardList,sum);
    }


}
