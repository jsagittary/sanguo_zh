package com.gryphpoem.game.zw.mgr;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.gryphpoem.game.zw.cmd.base.Cmd;
import com.gryphpoem.game.zw.cmd.base.Command;

/**
 * @ClassName CmdMgr.java
 * @Description
 * @author QiuKun
 * @date 2019年4月30日
 */
@Component
public class CmdMgr implements ApplicationContextAware {

    /** <rqCmd,Command> */
    private final Map<Integer, Command> commandCache = new HashMap<>();

    private ApplicationContext ac;

    public void init() {
        Map<String, Command> allCommand = ac.getBeansOfType(Command.class);
        for (Command c : allCommand.values()) {
            Cmd cmdAnnotation = c.getClass().getAnnotation(Cmd.class);
            if (cmdAnnotation != null) {
                int rqCmd = cmdAnnotation.rqCmd();
                int rsCmd = cmdAnnotation.rsCmd();
                c.init(rqCmd, rsCmd);
                registerCmd(rqCmd, rsCmd, c);
            }
        }
    }

    public Command getCommand(int rqCmd) {
        return commandCache.get(rqCmd);
    }

    private void registerCmd(int rqCmd, int rsCmd, Command command) {
        if (command != null && rqCmd > 0) {
            commandCache.put(rqCmd, command);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ac = applicationContext;
    }

}
