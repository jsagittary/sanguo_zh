package com.gryphpoem.game.zw.core.rank;

/**
 * @Description
 * @Author zhangdh
 * @Date 2020-12-21 9:42
 */
public enum RankType {
    ASC("ASC"),
    DESC("DESC");

    private RankType(String name) {
        this.name = name;
    }

    private String name;
}
