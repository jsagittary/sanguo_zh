package com.gryphpoem.game.zw.quartz.jobs;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.impl.SiLiDominateWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.impl.StateDominateWorldMap;
import com.gryphpoem.game.zw.pb.WorldPb;
import org.quartz.JobExecutionContext;

import java.text.ParseException;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-11-24 15:51
 */
public class DominateSideJob extends AbsMainLogicThreadJob {

    public static final String PREVIEW = "preview";
    public static final String BEGIN = "begin";
    public static final String END = "end";


    @Override
    protected void executeInMain(JobExecutionContext context) {
        String jobName = context.getJobDetail().getKey().getName();
        String[] jobNameArr = jobName.split("_");
        switch (Integer.parseInt(jobNameArr[1])) {
            case WorldPb.WorldFunctionDefine.STATES_AND_COUNTIES_DOMINATE_VALUE:
                switch (jobNameArr[2]) {
                    case PREVIEW:
                        StateDominateWorldMap.getInstance().onPreview(Integer.parseInt(jobNameArr[3]));
                        break;
                    case BEGIN:
                        StateDominateWorldMap.getInstance().onBegin(Integer.parseInt(jobNameArr[3]));
                        break;
                    case END:
                        try {
                            StateDominateWorldMap.getInstance().onEnd(Integer.parseInt(jobNameArr[3]));
                        } catch (ParseException e) {
                            LogUtil.error("州郡雄踞一方结束定时器报错, e: ", e);
                        }
                        break;
                    default:
                        break;
                }
            case WorldPb.WorldFunctionDefine.SI_LI_DOMINATE_SIDE_VALUE:
                switch (jobNameArr[2]) {
                    case PREVIEW:
                        SiLiDominateWorldMap.getInstance().onPreview();
                        break;
                    case BEGIN:
                        SiLiDominateWorldMap.getInstance().onBegin();
                        break;
                    case END:
                        try {
                            SiLiDominateWorldMap.getInstance().onEnd();
                        } catch (ParseException e) {
                            LogUtil.error("州郡雄踞一方结束定时器报错, e: ", e);
                        }
                        break;
                    default:
                        break;
                }
        }
    }
}
