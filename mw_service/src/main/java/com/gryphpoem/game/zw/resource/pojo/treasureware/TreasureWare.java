package com.gryphpoem.game.zw.resource.pojo.treasureware;

import com.gryphpoem.game.zw.core.util.Turple;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.pojo.Equip;
import com.gryphpoem.game.zw.resource.pojo.GamePb;
import com.gryphpoem.game.zw.resource.pojo.attr.TreasureWareAttrItem;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.pb.TreasureWarePbUtil;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class TreasureWare extends Equip implements GamePb<CommonPb.TreasureWare> {
    /**
     * 专属属性配置表id
     */
    private Integer specialId;
    /**
     * 品质
     */
    private int quality;
    /**
     * 等级
     */
    private int level;
    /**
     * 获取时间
     */
    private int getTime;
    /**
     * 级别(最高，高，中。。。)
     */
    private int rank;
    /**
     * 宝具获取状态
     */
    private int status;
    /**
     * 宝具分解时间
     */
    private int decomposeTime;
    /**
     * 宝具名id
     */
    private int profileId;
    /**
     * KEY: 属性栏目位置
     */
    private final Map<Integer, TreasureWareAttrItem> attrs = new TreeMap<>();
    private List<Turple<Integer, Integer>> specialAttr;

    //临时洗练属性
    private TreasureWareAttrItem trainAttr;

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

    public Map<Integer, TreasureWareAttrItem> getAttrs() {
        return attrs;
    }

    public TreasureWareAttrItem getTrainAttr() {
        return trainAttr;
    }

    public void setTrainAttr(TreasureWareAttrItem trainAttr) {
        this.trainAttr = trainAttr;
    }

    public TreasureWare() {
        super();
        this.specialAttr = new CopyOnWriteArrayList<>();
    }

    public TreasureWare(CommonPb.TreasureWare treasureWare) {
        this();
        setKeyId(treasureWare.getKeyId());
        setEquipId(treasureWare.getTreasureWareId());
        //基础属性
        treasureWare.getAttrList().forEach(attrItem -> {
            attrs.put(attrItem.getAttr().getIndex(), new TreasureWareAttrItem(attrItem));
        });

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

        if (treasureWare.hasTrainAttr()) {
            setTrainAttr(new TreasureWareAttrItem(treasureWare.getTrainAttr()));
        }
        if (treasureWare.hasProfileId()) {
            setProfileId(treasureWare.getProfileId());
        }
    }

    @Override
    public CommonPb.TreasureWare createPb(boolean isSaveDb) {
        CommonPb.TreasureWare.Builder builder = CommonPb.TreasureWare.newBuilder();
        //基本属性
        attrs.forEach((k, v) -> builder.addAttr(TreasureWarePbUtil.createTreasureWareAttrItemPb(v)));

        //专属属性
        if (!ObjectUtils.isEmpty(this.getSpecialAttr())) {
            for (int i = 0; i < this.getSpecialAttr().size(); i++) {
                Turple<Integer, Integer> attrArr = this.getSpecialAttr().get(i);
                builder.addAttrSpecial(PbHelper.createTwoIntPb(attrArr.getA(), attrArr.getB()));
            }
        }

        //洗练临时属性
        if (Objects.nonNull(trainAttr)) {
            builder.setTrainAttr(TreasureWarePbUtil.createTreasureWareAttrItemPb(trainAttr));
        }

        builder.setKeyId(this.getKeyId());
        builder.setQuality(this.getQuality());
        builder.setLevel(this.getLevel());
        builder.setTreasureWareId(this.getEquipId());
        builder.setHeroId(this.getHeroId());
        builder.setTreasureWareLocked(this.getEquipLocked());
        builder.setGetTime(this.getGetTime());
        builder.setRank(this.getRank());
        builder.setProfileId(this.getProfileId());
        if (Objects.nonNull(specialId))
            builder.setSpecialId(this.getSpecialId());

        if (isSaveDb) {
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

    public String logAttrs() {
        if (CheckNull.isEmpty(attrs))
            return "";

        StringBuilder sb = new StringBuilder();
        attrs.values().forEach(attrItem -> {
            if (CheckNull.isNull(attrItem)) return;
            sb.append(attrItem.logAttr());
        });
        return sb.toString();
    }

    /**
     * 获取属性相同条数
     *
     * @param attrId
     * @param cnt
     * @return
     */
    public boolean getSameAttrCnt(int attrId, int cnt, int quality) {
        if (CheckNull.isEmpty(attrs) || this.quality != quality)
            return false;

        Map<Integer, Integer> cntMap = new HashMap<>(attrs.size());
        attrs.values().forEach(attr -> {
            cntMap.merge(attr.getAttrId(), 1, Integer::sum);
        });

        if (attrId == 0) {
            return Objects.nonNull(cntMap.values().stream().filter(count -> count >= cnt).findFirst().orElse(null));
        } else {
            return cntMap.getOrDefault(attrId, 0) >= cnt;
        }
    }

    @Override
    public String toString() {
        return "TreasureWare{" +
                "specialId=" + specialId +
                ", quality=" + quality +
                ", level=" + level +
                ", getTime=" + getTime +
                ", rank=" + rank +
                ", status=" + status +
                ", decomposeTime=" + decomposeTime +
                ", profileId=" + profileId +
                ", specialAttr=" + specialAttr +
                "} " + super.toString();
    }
}
