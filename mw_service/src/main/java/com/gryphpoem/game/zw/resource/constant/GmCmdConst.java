package com.gryphpoem.game.zw.resource.constant;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-08-06 12:03
 */
public interface GmCmdConst {
    //格式:crossStressTesting
    String crossStressTesting = "crossStressTesting";
    //修改跨服活动数据
    String crossRechargeActivity = "crossRechargeActivity";

    //完成音乐创作任务
    String musicCrt = "musicCrt";

    interface Cross{
        interface Activity{

        }

        interface Chat{
            String CrossChat ="CrossChat";
        }
    }
}
