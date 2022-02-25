package com.gryphpoem.game.zw.resource.pojo.treasureware;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.pojo.Equip;
import com.gryphpoem.game.zw.resource.pojo.GamePb;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.Turple;
import org.springframework.util.ObjectUtils;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class TreasureWare extends Equip implements GamePb<CommonPb.TreasureWare> {
    /** 专属属性配置表id*/
    private Integer specialId;
    /** 品质*/
    private int quality;
    /** 等级*/
    private int level;
    /** 获取时间*/
    private int getTime;
    /** 级别(最高，高，中。。。)*/
    private int rank;
    /** 基本属性百分比*/
    private int attrPercentage;
    /** 宝具获取状态*/
    private int status;
    /** 宝具分解时间*/
    private int decomposeTime;
    /** 宝具名id*/
    private int profileId;

    private List<Turple<Integer, Integer>> specialAttr;

    public Integer getSpecialId() {
        return specialId;
    }

    public void setSpecialId(Integer specialId) {
        this.specialId = specialId;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public List<Turple<Integer, Integer>> getSpecialAttr() {
        return specialAttr;
    }

    public void setSpecialAttr(List<Turple<Integer, Integer>> specialAttr) {
        this.specialAttr = specialAttr;
    }

    public int getGetTime() {
        return getTime;
    }

    public void setGetTime(int getTime) {
        this.getTime = getTime;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getAttrPercentage() {
        return attrPercentage;
    }

    public void setAttrPercentage(int attrPercentage) {
        this.attrPercentage = attrPercentage;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getDecomposeTime() {
        return decomposeTime;
    }

    public void setDecomposeTime(int decomposeTime) {
        this.decomposeTime = decomposeTime;
    }

    public int getProfileId() {
        return profileId;
    }

    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }

    public TreasureWare() {
        super();
        this.specialAttr = new CopyOnWriteArrayList<>();
    }

    public TreasureWare(CommonPb.TreasureWare treasureWare) {
        this();
        setKeyId(treasureWare.getKeyId());
        setEquipId(treasureWare.getTreasureWareId());
        for (CommonPb.TwoInt twoInt : treasureWare.getAttrList()) {
            super.getAttrAndLv().add(new Turple<>(twoInt.getV1(), twoInt.getV2()));
        }
        if (treasureWare.hasHeroId()) {
            setHeroId(treasureWare.getHeroId());
        }
        setEquipLocked(treasureWare.getTreasureWareLocked());

        if (treasureWare.hasLevel()) {
            setLevel(treasureWare.getLevel());
        }
        if (treasureWare.hasGetTime()) {
            setGetTime(treasureWare.getGetTime());
        }
        if (treasureWare.hasRank()) {
            setRank(treasureWare.getRank());
        }
        if (!ObjectUtils.isEmpty(treasureWare.getAttrSpecialList())) {
            for (CommonPb.TwoInt twoInt : treasureWare.getAttrSpecialList()) {
                specialAttr.add(new Turple<>(twoInt.getV1(), twoInt.getV2()));
            }
        }
        if (treasureWare.hasAttrPercentage()) {
            setAttrPercentage(treasureWare.getAttrPercentage());
        }
        if (treasureWare.hasStatus()) {
            setStatus(treasureWare.getStatus());
        }
        if (treasureWare.hasSpecialId()) {
            setSpecialId(treasureWare.getSpecialId());
        }
        if (treasureWare.hasQuality()) {
            setQuality(treasureWare.getQuality());
        }
        if (treasureWare.hasDecomposeTime()) {
            setDecomposeTime(treasureWare.getDecomposeTime());
        }
    }

    @Override
    public CommonPb.TreasureWare createPb(boolean isSaveDb) {
        CommonPb.TreasureWare.Builder builder = CommonPb.TreasureWare.newBuilder();
        //基本属性
        for (int i = 0; i < this.getAttrAndLv().size(); i++) {
            Turple<Integer, Integer> attrLv = this.getAttrAndLv().get(i);
            builder.addAttr(PbHelper.createTwoIntPb(attrLv.getA(), attrLv.getB()));
        }

        //专属属性
        if (!ObjectUtils.isEmpty(this.getSpecialAttr())) {
            for (int i = 0; i < this.getSpecialAttr().size(); i++) {
                Turple<Integer, Integer> attrArr = this.getSpecialAttr().get(i);
                builder.addAttrSpecial(PbHelper.createTwoIntPb(attrArr.getA(), attrArr.getB()));
            }
        }

        builder.setKeyId(this.getKeyId());
        builder.setQuality(this.getQuality());
        builder.setLevel(this.getLevel());
        builder.setTreasureWareId(this.getEquipId());
        builder.setHeroId(this.getHeroId());
        builder.setTreasureWareLocked(this.getEquipLocked());
        builder.setGetTime(this.getGetTime());
        builder.setRank(this.getRank());
        if (Objects.nonNull(specialId))
            builder.setSpecialId(this.getSpecialId());

        if (isSaveDb) {
            builder.setAttrPercentage(this.getAttrPercentage());
            builder.setStatus(this.getStatus());
            builder.setDecomposeTime(this.getDecomposeTime());
        }
        return builder.build();
    }

    public CommonPb.TreasureWare createPb() {
        CommonPb.TreasureWare.Builder builder = CommonPb.TreasureWare.newBuilder();
        builder.setKeyId(getKeyId());
        builder.setTreasureWareId(getEquipId());
        builder.setHeroId(getHeroId());

        return builder.build();
    }

    @Override
    public String toString() {
        return "TreasureWare{" +
                "specialId=" + specialId +
                ", quality=" + quality +
                ", level=" + level +
                ", getTime=" + getTime +
                ", rank=" + rank +
                ", attrPercentage=" + attrPercentage +
                ", status=" + status +
                ", decomposeTime=" + decomposeTime +
                ", profileId=" + profileId +
                ", specialAttr=" + specialAttr +
                "} " + super.toString();
    }
}
