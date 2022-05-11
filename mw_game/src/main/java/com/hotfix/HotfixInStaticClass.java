package com.hotfix;

import com.gryphpoem.game.zw.core.util.FileUtil;
import com.gryphpoem.game.zw.core.util.JavaHotUpdateUtil;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.resource.common.ServerHotfix;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.mchange.io.FileUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.util.Date;


/**
 * 静态类动态更新
 *
 * @author zhangdh
 */
public class HotfixInStaticClass {

    private static Instrumentation inst = GameAgent.inst;

    /**
     * 重新定义静态类,注意不可新加方法与属性
     *
     * @param newClsFullName
     */
    public static boolean redefineClass(String hotfixId, String newClsFullName, Date now) {
        //记录热更的类
        ServerHotfix hotfix = new ServerHotfix(hotfixId, newClsFullName, now);
        try {
            LogUtil.hotfix("Hotfix Class : " + newClsFullName);
            Class<?> cls = Class.forName(newClsFullName);
            LogUtil.hotfix(String.format("class loader: %s, class: %s", cls.getClassLoader().toString(), cls.getName()));
            Resource resource = new FileSystemResource("hotfix/" + newClsFullName.replace(".", "/") + ".class");
            byte[] theClassFile = FileUtils.getBytes(resource.getFile());
            if (inst != null) {
                ClassDefinition cd = new ClassDefinition(cls, theClassFile);
                inst.redefineClasses(cd);
                hotfix.setResult(1);
            } else {
                hotfix.setResult(2);
                hotfix.setResultInfo("Not Found Game.Agent...");
                LogUtil.hotfix("Not Found Game.Agent...");
            }
        } catch (Exception e) {
            LogUtil.hotfix("", e);
            hotfix.setResult(3);
            hotfix.setResultInfo(printStackTraceToString(e));
        } finally {
            try {
                // DataRepairDao dataRepairDao = DataResource.ac.getBean(DataRepairDao.class);
                // dataRepairDao.insertHotfifxResult(hotfix);
                LogUtil.hotfix(String.format("hotfixId :%s, hotfix :%s, finish", hotfix.getHotfixId(), hotfix.getClassName()));
            } catch (Exception e) {
                LogUtil.hotfix("", e);
            }
        }
        return true;
    }

    public static boolean runJavaFile(String hotfixId, File javaFile, Date now, String fileNameSuffix) {
        //记录热更的类
        ServerHotfix hotfix = new ServerHotfix(hotfixId, javaFile.getName(), now);
        try {
            LogUtil.hotfix("Hotfix Java : " + javaFile.getName());
            JavaHotUpdateUtil.hotUpdate(FileUtil.readFile(javaFile));
            hotfix.setResult(1);
        } catch (Exception e) {
            LogUtil.hotfix("", e);
            hotfix.setResult(3);
            hotfix.setResultInfo(printStackTraceToString(e));
        } finally {
            LogUtil.hotfix(String.format("hotfixId :%s, hotfix :%s, finish", hotfix.getHotfixId(), hotfix.getClassName()));
        }
        return true;
    }

    public static String printStackTraceToString(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw, true));
        return sw.getBuffer().toString();
    }


    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, InterruptedException {
        HotfixInStaticClass.redefineClass(String.valueOf(TimeHelper.getCurrentSecond()), "com.gryphpoem.game.zw.server.timer.RobotTimer", new Date());
    }
}
