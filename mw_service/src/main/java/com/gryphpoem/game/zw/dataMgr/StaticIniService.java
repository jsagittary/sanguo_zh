package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.core.structs.Priority;

/**
 * 提供加载配表数据的接口
 * <p>
 *     注意：配置数据的处理逻辑中不要有依赖关系，如：A中需要用到B中的数据，不要在A中通过B调用拿数据，请在A中自己查一次库；
 *          若一定要依赖，请设置加载类的优先级{@link Priority}
 * </p>
 *
 * @author xwind
 * @date 2021/7/1
 */
public interface StaticIniService {
    void load();

    void check();

    Priority priority();
}
