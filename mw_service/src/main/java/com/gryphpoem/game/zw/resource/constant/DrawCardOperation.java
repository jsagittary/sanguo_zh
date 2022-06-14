package com.gryphpoem.game.zw.resource.constant;

import javax.swing.plaf.PanelUI;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-06-14 11:20
 */
public class DrawCardOperation {

    /**
     * 抽卡次数枚举
     */
    public enum DrawCardCount {
        /** 一次*/
        ONCE(1, 1),
        /** 十次*/
        TEN(2, 10),;
        private int type;
        private int count;
        public int getType() {
            return type;
        }

        public int getCount() {
            return count;
        }

        DrawCardCount(int type, int count) {
            this.type = type;
            this.count = count;
        }

        public static DrawCardCount convertTo(int type) {
            for (DrawCardCount tmp : values()) {
                if (tmp.getType() == type)
                    return tmp;
            }

            return null;
        }
    }

    /**
     * 抽卡使用类型枚举
     */
    public enum DrawCardCostType {
        /** 免费*/
        FREE(1),
        /** 玉璧*/
        MONEY(2),;

        private int type;

        public int getType() {
            return type;
        }

        DrawCardCostType(int type) {
            this.type = type;
        }

        public static DrawCardCostType convertTo(int type) {
            for (DrawCardCostType tmp : values()) {
                if (tmp.getType() == type)
                    return tmp;
            }

            return null;
        }
    }
}
