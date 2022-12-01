package com.gryphpoem.game.zw.manager;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.Scheduler;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.pb.BattlePb;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pojo.p.FightLogic;
import com.gryphpoem.game.zw.resource.dao.impl.p.FightRecordDao;
import com.gryphpoem.game.zw.resource.domain.p.DbFightRecord;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.server.SaveMailReportServer;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 邮件战报管理器
 *
 * @author zhou jie
 * @time 2022/9/19 15:18
 */
@Component
public class FightRecordDataManager {

    /**
     * 空战报的占位符
     */
    private static DbFightRecord PLACE_HOLDER = new DbFightRecord();

    /**
     * 战报一级缓存, 缓存时间5分钟, 过期会从cache中移除, 需要加载的时候根据战报id去数据库查询
     */
    private final LoadingCache<Long, BattlePb.BattleRoundPb> recordCache;

    @Autowired
    private FightRecordDao fightRecordDao;

    public FightRecordDataManager() {
        recordCache = Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)// 5分钟后失效
                .maximumSize(100)
                .removalListener((key, value, cause) -> {
                    // TODO: 2022/9/22 缓存卸载监听函数
                    LogUtil.common("[缓存卸载] remove removalCause={}, key=" + key + ", value=" + value);
                })
                .scheduler(Scheduler.systemScheduler())
                .build(recordId -> {
                    LogUtil.common("[缓存加载] load value = " + recordId);
                    DbFightRecord dbFightRecord = fightRecordDao.selectFightRecord(recordId);

                    // TODO: 2022/9/22 缓存穿透问题可以用占位符缓解
                    return Objects.nonNull(dbFightRecord) ? BattlePb.BattleRoundPb.parseFrom(dbFightRecord.getRecord()) : null;
                });
    }


    /**
     * 生成玩家战报
     *
     * @param rpt 玩家战报
     * @param now 创建时间
     * @return 战报
     */
    public CommonPb.Report generateReport(CommonPb.RptAtkPlayer rpt, FightLogic fightLogic, int now) {
        return generateReport(rpt, null, fightLogic, now);
    }

    /**
     * 生成流寇战报
     *
     * @param rpt 流寇战报
     * @param now 创建时间
     * @return 战报
     */
    public CommonPb.Report generateReport(CommonPb.RptAtkBandit rpt, FightLogic fightLogic, int now) {
        return generateReport(null, rpt, fightLogic, now);
    }


    /**
     * 生成战报
     *
     * @param playerRpt 玩家战报
     * @param banditRpt 流寇战报
     * @param now       创建时间
     * @return 战报
     */
    private CommonPb.Report generateReport(CommonPb.RptAtkPlayer playerRpt, CommonPb.RptAtkBandit banditRpt, FightLogic fightLogic, int now) {
        if (Objects.isNull(playerRpt) && Objects.isNull(banditRpt)) {
            return null;
        }

        CommonPb.Report.Builder reportBuilder = CommonPb.Report.newBuilder();
        reportBuilder.setTime(now);
        // 唯一id
        long id = fightLogic.fightId;
        reportBuilder.setKeyId(String.valueOf(id));

        if (Objects.nonNull(playerRpt)) {
            reportBuilder.setRptPlayer(playerRpt);
        }
        if (Objects.nonNull(banditRpt)) {
            reportBuilder.setRptBandit(banditRpt);
        }

        if (fightLogic.isRecordFlag()) {
            BattlePb.BattleRoundPb record = fightLogic.generateRecord();
            reportBuilder.setRecordId(record.getKeyId());

            // 添加战报
            insertRecordContainer(record, now);
        }

        return reportBuilder.build();
    }

    /**
     * 添加战报到缓存中, 并将战报放入执行队列
     *
     * @param record     战报
     * @param createTime 创建时间
     */
    private void insertRecordContainer(BattlePb.BattleRoundPb record, int createTime) {
        long reportId = Long.parseLong(record.getKeyId());
        // insert to cache
        recordCache.put(reportId, record);
        // insert to db
        SaveMailReportServer.getIns().saveData(new DbFightRecord(reportId, TimeHelper.secondToDate(createTime), record.toByteArray()));
    }


    /**
     * 查询战报
     * 先查询缓存, 缓存中不存在, 会查询数据库
     *
     * @param recordId 战报id
     * @return 战报 有可能是null
     */
    public BattlePb.@Nullable BattleRoundPb selectRecord(long recordId) {
        long beginTime = System.currentTimeMillis();

        BattlePb.BattleRoundPb record = recordCache.get(recordId);

        long endTime = System.currentTimeMillis();
        LogUtil.debug(String.format("查询耗时:%d!", endTime - beginTime));
        return record;
    }


}
