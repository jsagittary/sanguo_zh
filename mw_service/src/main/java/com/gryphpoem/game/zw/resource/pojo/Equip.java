package com.gryphpoem.game.zw.resource.pojo;

import com.gryphpoem.game.zw.pb.CommonPb.TwoInt;
import com.gryphpoem.game.zw.resource.util.Turple;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author TanDonghai
 * @ClassName Equip.java
 * @Description 装备
 * @date 创建时间：2017年3月27日 下午8:37:25
 */
public class Equip {
    /**
     * 装备私有id
     */
    private int keyId;
    /**
     * 装备id
     */
    private int equipId;
    /**
     * // 装备洗炼属性
     * // private Map<Integer, Integer> attr;
     */
    /**
     * // 临时洗炼属性
     * // private Map<Integer, Integer> tempBaptize;
     */
    /**
     * 穿戴了该装备的将领id，未穿戴为0
     */
    private int heroId;
    /**
     * A为属性和 B为等级
     */
    private List<Turple<Integer, Integer>> attrAndLv;
    /**
     * 有多少次洗练未升级的次数
     */
    private int notUpLvCnt;
    /**
     * 装备上锁(1-未上锁, 2-已上锁)
     */
    private int equipLocked;

    public Equip() {
        attrAndLv = new CopyOnWriteArrayList<>();
        // attr = new HashMap<>();
        // tempBaptize = new HashMap<>();
    }

    public Equip(com.gryphpoem.game.zw.pb.CommonPb.Equip equip) {
        this();
        setKeyId(equip.getKeyId());
        setEquipId(equip.getEquipId());
        // 装备洗炼属性
        for (TwoInt twoInt : equip.getAttrList()) {
            attrAndLv.add(new Turple<>(twoInt.getV1(), twoInt.getV2()));
        }
        // for (TwoInt twoInt : equip.getBaptizeList()) {// 临时洗炼属性
        // getAttr().put(twoInt.getV1(), twoInt.getV2());
        // }
        if (equip.hasHeroId()) {
            setHeroId(equip.getHeroId());
        }
        if (equip.hasNotUpLvCnt()) {
            setNotUpLvCnt(equip.getNotUpLvCnt());
        }
        setEquipLocked(equip.getEquipLocked());

    }

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getEquipId() {
        return equipId;
    }

    public void setEquipId(int equipId) {
        this.equipId = equipId;
    }

    public int getHeroId() {
        return heroId;
    }

    public void setHeroId(int heroId) {
        this.heroId = heroId;
    }

    /**
     * 装备是否被穿戴
     *
     * @return
     */
    public boolean isOnEquip() {
        return heroId > 0;
    }

    /**
     * 设置装备被穿戴
     *
     * @param heroId
     */
    public void onEquip(int heroId) {
        if (heroId <= 0 || heroId > Integer.MAX_VALUE) {
            return;
        }

        setHeroId(heroId);
    }

    /**
     * 是否全部属性都达到了某个等级
     *
     * @param maxLv 某个等级
     * @return true 全达到
     */
    public boolean isAllLvMax(int maxLv) {
        if (attrAndLv.isEmpty()) {
            return false;
        }
        for (Turple<Integer, Integer> attrLv : attrAndLv) {
            if (attrLv.getB() < maxLv) {
                return false;
            }
        }
        return true;
    }

    /**
     * 设置装备被卸下，闲置
     */
    public void downEquip() {
        setHeroId(0);
    }

    public List<Turple<Integer, Integer>> getAttrAndLv() {
        return attrAndLv;
    }

    public void setAttrAndLv(List<Turple<Integer, Integer>> attrAndLv) {
        this.attrAndLv = attrAndLv;
    }

    public int getNotUpLvCnt() {
        return notUpLvCnt;
    }

    public void setNotUpLvCnt(int notUpLvCnt) {
        this.notUpLvCnt = notUpLvCnt;
    }

    public int getEquipLocked() {
        return equipLocked;
    }

    public void setEquipLocked(int equipLocked) {
        this.equipLocked = equipLocked;
    }

    @Override
    public String toString() {
        return "Equip{" +
                "keyId=" + keyId +
                ", equipId=" + equipId +
                ", heroId=" + heroId +
                ", attrAndLv=" + attrAndLv +
                ", notUpLvCnt=" + notUpLvCnt +
                ", equipLocked=" + equipLocked +
                '}';
    }

}
