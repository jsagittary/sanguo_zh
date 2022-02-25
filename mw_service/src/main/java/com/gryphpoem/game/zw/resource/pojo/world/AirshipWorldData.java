package com.gryphpoem.game.zw.resource.pojo.world;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.BattleRole;
import com.gryphpoem.game.zw.resource.pojo.fight.NpcForce;
import com.gryphpoem.game.zw.resource.util.CheckNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName AirshipWorldData.java
 * @Description 飞艇在世界的数据
 * @author QiuKun
 * @date 2019年1月16日
 */
public class AirshipWorldData implements WorldEntity {

    public static int SEQ_ID = 0;

    /** 存活 */
    public static final int STATUS_LIVE = 0;
    /** 逃跑刷新 */
    public static final int STATUS_REFRESH = 1;
    /** 被干掉刷新 */
    public static final int STATUS_DEAD_REFRESH = 2;

    private int id; // 配置id
    private int keyId;// 唯一id
    private int pos = -1;// 飞艇的位置
    private List<NpcForce> npc = new LinkedList<>(); // npc信息
    private long belongRoleId;// 所属玩家id
    private int status;// 状态 0 存活 1 刷新
    private int triggerTime; // 根据状态,读取不同的截止时间含义
    private Map<Integer, List<BattleRole>> joinRoles = new HashMap<>(); // <camp,List<BattleRole>>有归属玩家时, 加入玩家的部队信息
    private int areaId; // 区域id
    // 邀请过的玩家
    private HashSet<Long> invites = new HashSet();

    public AirshipWorldData(CommonPb.Airship ser) {
        this.id = ser.getId();
        this.keyId = ser.getKeyId();
        this.pos = ser.getPos();
        for (CommonPb.Force f : ser.getNpcList()) {
            npc.add(new NpcForce(f.getNpcId(), f.getHp(), f.getCurLine()));
        }
        this.belongRoleId = ser.getBelongRoleId();
        this.status = ser.getStatus();
        this.triggerTime = ser.getTriggerTime();
        this.areaId = ser.getAreaId();
        if (!CheckNull.isEmpty(ser.getCamp1ArmyList())) {
            List<BattleRole> br1 = ser.getCamp1ArmyList().stream().collect(Collectors.toList());
            joinRoles.put(1, br1);
        }
        if (!CheckNull.isEmpty(ser.getCamp2ArmyList())) {
            List<BattleRole> br2 = ser.getCamp2ArmyList().stream().collect(Collectors.toList());
            joinRoles.put(2, br2);
        }
        if (!CheckNull.isEmpty(ser.getCamp3ArmyList())) {
            List<BattleRole> br3 = ser.getCamp3ArmyList().stream().collect(Collectors.toList());
            joinRoles.put(3, br3);
        }
        if (!CheckNull.isEmpty(ser.getInvitesList())) {
            invites.addAll(ser.getInvitesList());
        }
    }

    /**
     * 配置id
     * 
     * @param id
     */
    public AirshipWorldData(int id) {
        this.keyId = ++SEQ_ID;
        this.id = id;
    }

    public AirshipWorldData() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getKeyId() {
        return keyId;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public List<NpcForce> getNpc() {
        return npc;
    }

    public long getBelongRoleId() {
        return belongRoleId;
    }

    public void setBelongRoleId(long belongRoleId) {
        this.belongRoleId = belongRoleId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getTriggerTime() {
        return triggerTime;
    }

    public void setTriggerTime(int triggerTime) {
        this.triggerTime = triggerTime;
    }

    public Map<Integer, List<BattleRole>> getJoinRoles() {
        return joinRoles;
    }

    public boolean isLiveStatus() {
        return getStatus() == STATUS_LIVE;
    }

    public boolean isRefreshStatus() {
        return getStatus() == STATUS_REFRESH || getStatus() == STATUS_DEAD_REFRESH;
    }

    public int getAreaId() {
        return areaId;
    }

    public void setAreaId(int areaId) {
        this.areaId = areaId;
    }

    public int getRemainHp() {
        int hp = 0;
        for (NpcForce n : this.npc) {
            if (n.getHp() > 0) {
                hp += n.getHp();
            }
        }
        return hp;
    }

    public HashSet<Long> getInvites() {
        return invites;
    }
}
