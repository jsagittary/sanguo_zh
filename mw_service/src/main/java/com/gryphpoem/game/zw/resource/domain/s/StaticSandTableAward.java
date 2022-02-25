package com.gryphpoem.game.zw.resource.domain.s;

import com.gryphpoem.game.zw.resource.constant.AwardType;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * s_sandtable_award
 */
public class StaticSandTableAward {
    private int id;
    private int type;
    private int param;
    private List<List<Integer>> award;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getParam() {
        return param;
    }

    public void setParam(int param) {
        this.param = param;
    }

    public List<List<Integer>> getAward() {
        return award;
    }

    public void setAward(List<List<Integer>> award) {
        this.award = award;
    }

//    public StaticSandTableAward clone() {
//        StaticSandTableAward award = new StaticSandTableAward();
//        award.setAward(new ArrayList<>());
//        award.getAward().addAll(this.award);
//        return award;
//    }

    public int getSandTableScoreAward() {
        if (ObjectUtils.isEmpty(this.award)) {
            return 0;
        }

        int result = 0;
        for (List<Integer> singleAward : this.award) {
            if (singleAward.get(0) != AwardType.SANDTABLE_SCORE) {
                continue;
            }

            result += singleAward.get(2);
        }

        return result;
    }

//    public List<List<Integer>> getMultipleAward(double multiple) {
//        if (ObjectUtils.isEmpty(this.award)) {
//            return award;
//        }
//
//        this.award.forEach(singleAward -> {
//            if (singleAward.get(0) != AwardType.SANDTABLE_SCORE) {
//                return;
//            }
//
//            Integer count = singleAward.remove(2);
//            singleAward.add(2, (int) (count * multiple));
//        });
//
//        return this.award;
//    }
}
