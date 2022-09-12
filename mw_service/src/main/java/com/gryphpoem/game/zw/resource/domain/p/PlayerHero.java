package com.gryphpoem.game.zw.resource.domain.p;

import com.google.protobuf.InvalidProtocolBufferException;
import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.pojo.GamePb;
import com.gryphpoem.game.zw.resource.util.CheckNull;

import java.io.Serializable;
import java.util.Objects;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-08-12 17:13
 */
public class PlayerHero implements Serializable, Cloneable, GamePb<DbPlayerHero> {
    private long lordId;
    /** 拥有兵力的武将*/
    private PlayerHeroBiography biography;

    public long getLordId() {
        return lordId;
    }

    public void setLordId(long lordId) {
        this.lordId = lordId;
    }

    public PlayerHeroBiography getBiography() {
        return biography;
    }

    public void setBiography(PlayerHeroBiography biography) {
        this.biography = biography;
    }

    public PlayerHero() {
    }

    public PlayerHero(long roleId) {
        this.lordId = roleId;
        this.biography = new PlayerHeroBiography();
    }

    public PlayerHero(DbPlayerHero dbPlayerHero) throws InvalidProtocolBufferException {
        this.lordId = dbPlayerHero.getLordId();
        this.biography = new PlayerHeroBiography();
        if (Objects.nonNull(dbPlayerHero.getHeroBiography())) {
            SerializePb.SerHeroBiographyData data = SerializePb.SerHeroBiographyData.parseFrom(dbPlayerHero.getHeroBiography());
            if (Objects.nonNull(data) && CheckNull.nonEmpty(data.getDataList())) {
                data.getDataList().forEach(pb -> {
                    biography.getLevelMap().put(pb.getV1(), pb.getV2());
                });
            }
        }
    }

    @Override
    public DbPlayerHero createPb(boolean isSaveDb) {
        DbPlayerHero db = new DbPlayerHero();
        db.setLordId(lordId);
        if (Objects.nonNull(biography)) {
            db.setHeroBiography(biography.createPb(true).toByteArray());
        }
        return db;
    }
}
