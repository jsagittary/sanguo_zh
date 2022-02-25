package com.gryphpoem.game.zw.resource.domain.s;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-04-15 10:29
 */
public class StaticHeroSeason {
    //英雄ID
    private int heroId;
    //合成该英雄所需玩家等级
    private int synthLv;
    //碎片ID
    private int pieceId;
    //合成所需碎片数量
    private int pieceCount;

    public int getHeroId() {
        return heroId;
    }

    public void setHeroId(int heroId) {
        this.heroId = heroId;
    }

    public int getSynthLv() {
        return synthLv;
    }

    public void setSynthLv(int synthLv) {
        this.synthLv = synthLv;
    }

    public int getPieceId() {
        return pieceId;
    }

    public void setPieceId(int pieceId) {
        this.pieceId = pieceId;
    }

    public int getPieceCount() {
        return pieceCount;
    }

    public void setPieceCount(int pieceCount) {
        this.pieceCount = pieceCount;
    }
}
