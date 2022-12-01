package com.gryphpoem.game.zw.resource.constant;

import com.gryphpoem.game.zw.resource.util.CheckNull;
import org.springframework.util.ObjectUtils;

import java.util.List;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-06-20 14:07
 */
public class HeroUpgradeConstant {
    public enum Condition {
        /**
         * 等级条件
         */
        LEVEL(1, new Compare[]{Compare.GreaterAndEqual}),
        ;
        private int type;
        private Compare[] compare;

        public Compare[] getCompare() {
            return compare;
        }

        public int getType() {
            return type;
        }

        Condition(int type, Compare[] compare) {
            this.type = type;
            this.compare = compare;
        }

        /**
         * 校验升级品阶条件
         *
         * @param configList
         * @param params
         * @return
         */
        public static boolean checkCondition(List<List<Integer>> configList, int[] params) {
            if (CheckNull.isEmpty(configList))
                return false;
            if (ObjectUtils.isEmpty(params))
                return false;
            int index = 0;
            for (List<Integer> tmp : configList) {
                if (CheckNull.isEmpty(tmp))
                    continue;
                Condition condition = convertTo(tmp.get(0));
                if (CheckNull.isNull(condition))
                    return false;
                if (CheckNull.isEmpty(condition.getCompare()))
                    continue;
                for (int i = 0; i < condition.getCompare().length; i++) {
                    Compare tmpC = condition.getCompare()[i];
                    switch (tmpC) {
                        case Less:
                            if (params[index++] >= tmp.get(i + 1))
                                return false;
                            break;
                        case LessAndEqual:
                            if (params[index++] > tmp.get(i + 1))
                                return false;
                            break;
                        case Equal:
                            if (params[index++] != tmp.get(i + 1))
                                return false;
                            break;
                        case GreaterAndEqual:
                            if (params[index++] < tmp.get(i + 1))
                                return false;
                            break;
                        case Greater:
                            if (params[index++] <= tmp.get(i + 1))
                                return false;
                            break;
                    }
                }
            }
            return true;
        }

        public static Condition convertTo(int type) {
            for (Condition tmp : values()) {
                if (tmp.getType() == type)
                    return tmp;
            }
            return null;
        }
    }

    public enum Compare {
        Less(), LessAndEqual(), Equal(), GreaterAndEqual(), Greater(),
        ;
    }
}
