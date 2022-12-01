package com.gryphpoem.game.zw.resource.pojo.global;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.dataMgr.StaticNpcDataMgr;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pojo.p.NpcForce;
import com.gryphpoem.game.zw.resource.domain.s.StaticNpc;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.service.FightService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author QiuKun
 * @ClassName ScheduleBoss.java
 * @Description 世界进度的boss
 * @date 2019年2月21日
 */
public class ScheduleBoss {

    /**
     * 进度id
     */
    private int scheduleId;

    /**
     * 阵型
     */
    private List<NpcForce> npc;

    /**
     * 原子操作对象
     */
    private AtomicInteger fightCnt = new AtomicInteger();

    /**
     * 构造
     */
    public ScheduleBoss() {
        this.npc = new ArrayList<>();
    }

    /**
     * 序列化
     *
     * @return
     */
    public CommonPb.ScheduleBoss ser() {
        CommonPb.ScheduleBoss.Builder builder = CommonPb.ScheduleBoss.newBuilder();
        builder.setId(this.scheduleId);
        builder.setFightCnt(this.fightCnt.get());
        if (!CheckNull.isEmpty(this.npc)) {
            for (NpcForce npcForce : this.npc) {
                builder.addBossNpc(PbHelper.createForcePb(npcForce));
            }
        }
        return builder.build();
    }

    /**
     * 反序列化
     *
     * @param boss
     */
    public ScheduleBoss(CommonPb.ScheduleBoss boss) {
        this();
        this.scheduleId = boss.getId();
        this.fightCnt.set(boss.getFightCnt());
        List<CommonPb.Force> bossNpcList = boss.getBossNpcList();
        if (!CheckNull.isEmpty(bossNpcList)) {
            for (CommonPb.Force force : bossNpcList) {
                this.npc.add(new NpcForce(force.getNpcId(), force.getHp(), force.getCurLine(), force.getDeputyNpcList()));
            }
        }
    }

    /**
     * 创建世界boss
     *
     * @param scheduleId
     * @param npcId
     * @return
     */
    public static ScheduleBoss createBoss(int scheduleId, List<List<Integer>> npcId) {
        ScheduleBoss scheduleBoss = new ScheduleBoss();
        scheduleBoss.scheduleId = scheduleId;
        scheduleBoss.npc = new ArrayList<>();
        if (!CheckNull.isEmpty(npcId)) {
            FightService fightService = DataResource.ac.getBean(FightService.class);
            for (List<Integer> id : npcId) {
                NpcForce npcForce = fightService.createCacheNpcForce(id);
                if (CheckNull.isNull(npcForce)) continue;
                scheduleBoss.npc.add(npcForce);
            }
        }
        return scheduleBoss;
    }

    /**
     * 获取boss剩余血量
     *
     * @return
     */
    public int getRemainHp() {
        int hp = 0;
        for (NpcForce n : this.npc) {
            if (n.getHp() > 0) {
                hp += n.getHp();
            }
        }
        return hp;
    }

    public int getScheduleId() {
        return scheduleId;
    }

    public List<NpcForce> getNpc() {
        return npc;
    }

    public AtomicInteger getFightCnt() {
        return fightCnt;
    }
}
