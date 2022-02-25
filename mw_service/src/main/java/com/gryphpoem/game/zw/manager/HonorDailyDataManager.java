package com.gryphpoem.game.zw.manager;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.constant.HonorDailyConstant;
import com.gryphpoem.game.zw.resource.constant.PlayerConstant;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticHonorDailyCond;
import com.gryphpoem.game.zw.resource.pojo.daily.HonorDaily;
import com.gryphpoem.game.zw.resource.pojo.daily.HonorReport2;
import com.gryphpoem.game.zw.resource.util.PbHelper;

/**
 * @ClassName HonorDailyDataManager.java
 * @Description 荣耀日报相关逻辑
 * @author QiuKun
 * @date 2018年8月28日
 */
@Component
public class HonorDailyDataManager {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private GlobalDataManager globalDataManager;

    /**
     * 检测是否满足等级条件
     * 
     * @param player
     * @param condId 条件id
     * @param paramIndex 参数的下标 从0开始,(如果该参数没有填写,默认表示满足)
     * @return false条件不满足
     */
    public boolean checkHonorDailyCondLordLv(int lordLv, int condId, int paramIndex) {
        StaticHonorDailyCond sHonorDailyCond = StaticWorldDataMgr.getHonorDailyCondMapById(condId);
        if (sHonorDailyCond == null) {
            return false;
        }
        if (sHonorDailyCond.getParam() == null) {
            return false;
        }
        if (sHonorDailyCond.getParam().size() < paramIndex + 1) { // 如果没填默认满足
            return true;
        }
        Integer paramLv = sHonorDailyCond.getParam().get(paramIndex);
        return lordLv >= paramLv.intValue();
    }

    /**
     * 添加并检测荣耀日报(其他类型)
     * 
     * @param player
     * @param condId
     * @param sch
     * @param params
     */
    public void addAndCheckHonorReport2s(Player player, int condId, int sch, String... params) {
        if (player == null || sch <= 0) return;
        StaticHonorDailyCond sHonorDailyCond = StaticWorldDataMgr.getHonorDailyCondMapById(condId);
        if (sHonorDailyCond == null) {
            LogUtil.error("荣耀日报,配置未找到,condId:", condId);
            return;
        }
        Integer playerConstantKey = HonorDailyConstant.getPlayerConstantKeyIdByCondId(condId);
        if (playerConstantKey == null) {
            LogUtil.error("荣耀日报,condId与playerConstant没找到存储对应关系,condId:", condId);
            return;
        }
        if (!checkHonorDailyCondLordLv(player.lord.getLevel(), condId, 1)) {
            return;
        }
        Integer condParam = sHonorDailyCond.getParam().get(0);
        int val = player.getMixtureDataById(playerConstantKey);
        boolean isFirstFinish = val < condParam; // 是否是首次达成
        val += sch; // 加上进度值
        // 保存进度值
        player.setMixtureData(playerConstantKey, val);
        // 荣耀日报触发
        if (isFirstFinish && val >= condParam) {
            HonorDaily honorDaily = getHonorDaily();
            // 额外参数添加
            List<String> paramList = null;
            if (params != null && params.length > 0) {
                paramList = new ArrayList<>(params.length);
                for (String p : params) {
                    paramList.add(p);
                }
            }
            HonorReport2 honorReport2 = honorDaily.addHonorReport2(player, condId, 0, paramList);
            // 全服玩家红点更新
            playerDataManager.getPlayers().values().forEach(p -> {
                if (updateReportTips(p)) {
                    syncHonorReport(p, honorReport2);
                }
            });
        }
    }

    public HonorDaily getHonorDaily() {
        HonorDaily honorDaily = globalDataManager.getGameGlobal().getHonorDaily();
        return honorDaily;
    }

    /**
     * 荣耀日报红点推送(其他类型)
     * 
     * @param target
     * @param hr2
     */
    private void syncHonorReport(Player target, HonorReport2 hr2) {
        if (target != null && target.isLogin && target.ctx != null) {
            GamePb4.SyncHonorReportRs.Builder builder = GamePb4.SyncHonorReportRs.newBuilder();
            builder.setReports2(hr2.ser());
            BasePb.Base.Builder msg = PbHelper.createSynBase(GamePb4.SyncHonorReportRs.EXT_FIELD_NUMBER,
                    GamePb4.SyncHonorReportRs.ext, builder.build());
            MsgDataManager.getIns().add(new Msg(target.ctx, msg.build(), target.roleId));
        }
    }

    /**
     * 玩家字段上面加上tips
     * 
     * @param player
     * @return true加成功 false加失败
     */
    public boolean updateReportTips(Player player) {
        int tips = player.getMixtureDataById(PlayerConstant.HONOR_REPORT_TIPS);
        if (tips < HonorDaily.MAX_REPORTS_NUM) {
            tips++;
            player.setMixtureData(PlayerConstant.HONOR_REPORT_TIPS, tips);
            return true;
        }
        return false;
    }
}
