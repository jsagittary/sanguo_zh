package com.gryphpoem.game.zw.resource.constant.task;

import com.gryphpoem.game.zw.resource.constant.TaskType;

/**
 * <p>
 * 雇任任务相关
 * </p>
 *
 * @Description: TODO
 * @Author: huangxm
 * @CreateTime: 2022/6/10 17:43
 **/
public class CommandMultType {

    /**
     * 根据任务类型获取雇佣类型
     * 雇佣类型信息见 s_command_mult配置表
     * @param cond
     * @return
     */
    public static int getCommandMultType(int cond) {
        int type = 0;
        switch (cond) {
            case TaskType.COND_COMMAND_ADD:
                return 1;
            case TaskType.COND_30:
                return 11;
            case TaskType.COND_FACTORY_RECRUIT:
                return 16;
        }
        return type;
    }
}
