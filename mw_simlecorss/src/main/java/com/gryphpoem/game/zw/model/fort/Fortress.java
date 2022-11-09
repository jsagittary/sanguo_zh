package com.gryphpoem.game.zw.model.fort;

import com.gryphpoem.game.zw.dataMgr.StaticCrossDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticNpcDataMgr;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.SerializePb.DbFortress;
import com.gryphpoem.game.zw.pojo.p.NpcForce;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.domain.s.StaticCrossFort;
import com.gryphpoem.game.zw.resource.domain.s.StaticNpc;
import com.gryphpoem.game.zw.resource.util.CheckNull;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author QiuKun
 * @ClassName Fortress.java
 * @Description 堡垒信息
 * @date 2019年5月14日
 */
public class Fortress {
    // 堡垒id
    private int id;
    // 堡垒的类型
    private int type;
    /**
     * 攻击方兵力
     */
    private int atkCnt;
    /**
     * 防守方兵力
     */
    private int defCnt;
    /**
     * 所属阵的营
     */
    private int camp;
    /**
     * npc队列,npc都属于防守方的
     */
    private final NpcFortForce<NpcForce> npcQueue = new NpcFortForce<>();
    /**
     * 玩家队列
     */
    private final LinkedList<RoleForce> roleQueue = new LinkedList<>();

    public static Fortress createFortressByCfg(StaticCrossFort cfg) {
        Fortress fortress = new Fortress();
        fortress.id = cfg.getId();
        fortress.type = cfg.getType();
        fortress.camp = cfg.getCamp();
        if (!CheckNull.isEmpty(cfg.getForm())) {
            for (Integer id : cfg.getForm()) {
                StaticNpc staticNpc = StaticNpcDataMgr.getNpcMap().get(id);
                if (staticNpc != null) {
                    fortress.npcQueue.add(new NpcForce(id, staticNpc.getTotalArm()));
                }
            }
        }
        fortress.reCalcCnt();
        return fortress;
    }

    public static Fortress createFortressByDb(DbFortress pb) {
        Fortress fortress = new Fortress();
        fortress.id = pb.getId();
        fortress.type = pb.getType();
        fortress.camp = pb.getCamp();
        if (!CheckNull.isEmpty(pb.getNpcForceList())) {
            for (CommonPb.Force fPb : pb.getNpcForceList()) {
                fortress.npcQueue.add(new NpcForce(fPb.getNpcId(), fPb.getHp()));
            }
        }
        fortress.reCalcCnt();
        return fortress;
    }

    public List<RoleForce> getAtkQueue() {
        return roleQueue.stream().filter(r -> !isDefCamp(r.getCamp())).collect(Collectors.toList());
    }

    public List<RoleForce> getDefQueue() {
        return roleQueue.stream().filter(r -> isDefCamp(r.getCamp())).collect(Collectors.toList());
    }

    /**
     * 是否是相邻
     *
     * @param fortId
     * @return
     */
    public boolean checkNeighbor(int fortId) {
        StaticCrossFort cfgCrossFort = StaticCrossDataMgr.getFortMap().get(this.id);
        return cfgCrossFort.getNeighbor().contains(fortId);
    }

    /**
     * 是否可以发起单挑
     *
     * @param camp
     * @return true 可以发起单挑
     */
    public boolean canSolo(int camp) {
        if (isDefCamp(camp)) {
            if (getAtkCnt() <= 0) {
                return false;
            }
        } else {
            if (getDefCnt() <= 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * 重新计算兵力
     */
    public void reCalcCnt() {
        int tmpAtkCnt = 0;
        int tmpDefCnt = 0;
        for (Iterator<NpcForce> it = npcQueue.iterator(); it.hasNext(); ) {
            NpcForce f = it.next();
            if (f.getHp() <= 0) {
                it.remove();
                continue;
            }
            tmpDefCnt += f.getHp();
        }

        for (Iterator<RoleForce> it = roleQueue.iterator(); it.hasNext(); ) {
            RoleForce f = it.next();
            if (!f.isLive()) {
                it.remove();
                continue;
            }
            if (isDefCamp(f.getCamp())) {
                tmpDefCnt += f.getCount();
            } else {
                tmpAtkCnt += f.getCount();
            }
        }
        this.atkCnt = tmpAtkCnt;
        this.defCnt = tmpDefCnt;
    }

    /**
     * 只能用于加入
     *
     * @param force
     */
    public void joinRoleForce(RoleForce force) {
        roleQueue.addLast(force);
        addCnt(force.getCount(), force.getCamp());
        if (!isDefCamp(force.getCamp()) && getDefCnt() <= 0) { // 是进攻方并且没有防守方就直接切换阵营
            changeCamp(force.getCamp());
        }
    }

    /**
     * 查找RoleForce
     *
     * @param lordId
     * @param heroId
     * @return
     */
    public RoleForce findRoleForce(long lordId, int heroId) {
        return roleQueue.stream()
                .filter(f -> f.getCrossHero().getLordId() == lordId && f.getCrossHero().getHeroId() == heroId)
                .findFirst().orElse(null);
    }

    /**
     * 只能用于移除
     *
     * @param lordId
     * @param heroId
     * @return
     */
    public RoleForce removeRoleForce(long lordId, int heroId) {
        for (Iterator<RoleForce> it = roleQueue.iterator(); it.hasNext(); ) {
            RoleForce f = it.next();
            if (f.getCrossHero().getLordId() == lordId && f.getCrossHero().getHeroId() == heroId) {
                it.remove();
                subCnt(f.getCount(), f.getCamp());
                return f;
            }
        }
        return null;
    }

    /**
     * 是否是大本营
     */
    public boolean isBaseCamp() {
        return this.type == StaticCrossFort.TYPE_CAMP;
    }

    public boolean isNpcCamp() {
        return this.camp == Constant.Camp.NPC;
    }

    public boolean isDefCamp(int camp) {
        return this.camp == camp;
    }

    public void addCnt(int cnt, int camp) {
        if (isDefCamp(camp)) {
            addDefCnt(cnt);
        } else {
            addAtkCnt(cnt);
        }
    }

    public void subCnt(int cnt, int camp) {
        addCnt(-cnt, camp);
    }

    private void addAtkCnt(int cnt) {
        this.atkCnt += cnt;
        if (atkCnt < 0) {
            this.atkCnt = 0;
        }
    }

    private void addDefCnt(int cnt) {
        this.defCnt += cnt;
        if (this.defCnt < 0) {
            this.defCnt = 0;
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAtkCnt() {
        return atkCnt;
    }

    public void setAtkCnt(int atkCnt) {
        this.atkCnt = atkCnt;
    }

    public int getDefCnt() {
        return defCnt;
    }

    public void setDefCnt(int defCnt) {
        this.defCnt = defCnt;
    }

    public NpcFortForce<NpcForce> getNpcQueue() {
        return npcQueue;
    }

    public LinkedList<RoleForce> getRoleQueue() {
        return roleQueue;
    }

    public int getCamp() {
        return camp;
    }

    public void setCamp(int camp) {
        this.camp = camp;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    /**
     * 交换阵营
     *
     * @param camp
     */
    public void changeCamp(int camp) {
        setCamp(camp);
        reCalcCnt();
    }

    public CommonPb.FortressPb toFortressPb() {
        CommonPb.FortressPb.Builder builder = CommonPb.FortressPb.newBuilder();
        builder.setId(id);
        builder.setAtkCnt(atkCnt);
        builder.setDefCnt(defCnt);
        builder.setCamp(camp);
        return builder.build();
    }

    public DbFortress toDbFortressPb() {
        DbFortress.Builder builder = DbFortress.newBuilder();
        builder.setId(id);
        builder.setType(type);
        builder.setCamp(camp);
        if (!npcQueue.isEmpty()) {
            builder.addAllNpcForce(npcQueue.stream().map(NpcForce::toForcePb).collect(Collectors.toList()));
        }
        return builder.build();
    }

}
