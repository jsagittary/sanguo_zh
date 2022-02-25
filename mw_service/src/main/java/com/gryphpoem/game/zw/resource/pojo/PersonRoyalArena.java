package com.gryphpoem.game.zw.resource.pojo;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.domain.s.StaticRoyalArenaTask;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * 阵营对拼的玩家数据
 * User:        zhoujie
 * Date:        2020/4/2 13:02
 * Description:
 */
public class PersonRoyalArena {


    // 玩家的id
    private long roleId;

    // 贡献度
    private int contribution;

    // 完成任务数量
    private int fulfilTask;

    // 额外可以完成任务数量(金币购买)
    private int extraTask;

    // 大国风范购买次数
    private int countryStyleCnt;

    // 刺探购买次数
    private int detectCnt;

    // 当前的任务
    private RoyalArenaTask task;

    // 奖励的领取状态, key: awardId, val: 0 未领取, 1 已领取
    private Map<Integer, Integer> awardStatus = new HashMap<>();


    /**
     * 转点清除数据
     */
    public void clearTaskAndData() {
        this.fulfilTask = 0;
        this.extraTask = 0;
        this.countryStyleCnt = 0;
        this.detectCnt = 0;
        if (!CheckNull.isNull(task)) {
            task.clearTaskAndData();
        }
    }

    /**
     * 反序列化
     * @param personInfo
     */
    public PersonRoyalArena(CommonPb.PersonRoyalData personInfo) {
        this.roleId = personInfo.getRoleId();
        this.contribution = personInfo.getContribution();
        this.fulfilTask = personInfo.getFulfilTask();
        this.extraTask = personInfo.getExtraTask();
        this.countryStyleCnt = personInfo.getCountryStyleCnt();
        this.detectCnt = personInfo.getDetectCnt();
        this.task = new RoyalArenaTask(personInfo.getTask());
        for (CommonPb.TwoInt twoInt : personInfo.getAwardStatusList()) {
            this.awardStatus.put(twoInt.getV1(), twoInt.getV2());
        }
    }

    /**
     * 增加贡献值
     *
     * @param add 增加贡献值
     * @return 最后的贡献值
     */
    public int addPst(int add) {
        this.contribution += add;
        return this.contribution;
    }

    /**
     * 自增任务的完成数量
     * @return 完成数量
     */
    public int incrementTaskcnt() {
        return ++this.fulfilTask;
    }

    /**
     * 自增额外购买任务的数量
     * @return 完成数量
     */
    public int incrementExtraTaskcnt() {
        return ++this.extraTask;
    }

    /**
     * 自增大国风范购买的次数
     * @return 购买大国风范的次数
     */
    public int incrementCountryStyleCnt() {
        return ++this.countryStyleCnt;
    }


    /**
     * 自增刺探购买的次数
     * @return 购买刺探的次数
     */
    public int incrementDetectStyleCnt() {
        return ++this.detectCnt;
    }

    /**
     * 初始化玩家的数据
     *
     * @param roleId
     */
    public PersonRoyalArena(long roleId) {
        this.roleId = roleId;
        this.contribution = 0;
        this.fulfilTask = 0;
        this.extraTask = 0;
        this.countryStyleCnt = 0;
        this.detectCnt = 0;
    }

    /**
     * 更新任务
     * @param sTask 任务
     */
    public void refreshTask(StaticRoyalArenaTask sTask) {
        if (!CheckNull.isNull(sTask)) {
            this.task = new RoyalArenaTask(sTask);
        }
    }

    /**
     * 序列化
     * @return
     */
    public CommonPb.PersonRoyalData ser() {
        CommonPb.PersonRoyalData.Builder builder = CommonPb.PersonRoyalData.newBuilder();
        builder.setRoleId(this.roleId);
        builder.setContribution(this.contribution);
        builder.setFulfilTask(this.fulfilTask);
        builder.setExtraTask(this.extraTask);
        builder.setCountryStyleCnt(this.countryStyleCnt);
        builder.setDetectCnt(this.detectCnt);
        builder.setTask(this.task.ser());
        this.awardStatus.forEach((key, value) -> builder.addAwardStatus(PbHelper.createTwoIntPb(key, value)));
        return builder.build();
    }

    public long getRoleId() {
        return roleId;
    }

    public int getContribution() {
        return contribution;
    }

    public int getFulfilTask() {
        return fulfilTask;
    }

    public int getExtraTask() {
        return extraTask;
    }

    public int getCountryStyleCnt() {
        return countryStyleCnt;
    }

    public int getDetectCnt() {
        return detectCnt;
    }

    public RoyalArenaTask getTask() {
        return task;
    }

    public Map<Integer, Integer> getAwardStatus() {
        return awardStatus;
    }

}
