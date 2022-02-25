package com.gryphpoem.game.zw.manager;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticMailDataMgr;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb2.SyncMailRs;
import com.gryphpoem.game.zw.resource.constant.ActivityConst;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.MailConstant;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticMail;
import com.gryphpoem.game.zw.resource.pojo.Mail;
import com.gryphpoem.game.zw.resource.util.*;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component public class MailDataManager {

    @Autowired
    private ActivityDataManager activityDataManager;

    /**
     * 添加玩家发件
     *
     * @param player
     * @param state
     * @param title
     * @param content
     * @param sendName
     * @return
     */
    public Mail addPlayerSendMail(Player player, int state, String title, String content, String sendName) {
        if (player == null) {
            return null;
        }
        Mail mail = new Mail(player.maxKey(), MailConstant.TYPE_ROLE, state, TimeHelper.getCurrentSecond(), title,
                content, sendName, player.lord.getLevel(), player.lord.getVip());
        player.mails.put(mail.getKeyId(), mail);
        return mail;
    }

    /**
     * 发送普通邮件，不包含奖励附件和战报等信息，可以有参数<br/>
     * <i>注意：</i>使用该方法 <tt>必须 </tt>在 {@link MailHelper#registerMailParamNum()}
     * 方法中注册邮件的标题和内容参数个数
     *
     * @param recvPlayer 接收玩家
     * @param moldId     邮件id
     * @param time
     * @param param      邮件额外参数，包括邮件标题和内容的所有参数，按参数顺序排列
     * @return
     */
    public Mail sendNormalMail(Player recvPlayer, int moldId, int time, Object... param) {
        StaticMail staticMail = StaticMailDataMgr.getStaticMail(moldId);
        if (staticMail == null) {
            LogUtil.error(String.format("roleId :%d, 未找到邮件模版 :%d", recvPlayer != null ? recvPlayer.getLordId() : 0L, moldId));
            return null;
        }

        if (recvPlayer == null) {
            LogUtil.debug("sendNormalMail recvPlayer is null");
            return null;
        }

        List<String> tParam = new ArrayList<>();
        List<String> cParam = new ArrayList<>();
        // 填充邮件参数
        fillMailParam(moldId, tParam, cParam, param);

        int type = staticMail.getType();
        Mail mail = new Mail(recvPlayer.maxKey(), type, moldId, MailConstant.STATE_UNREAD, time);
        mail.settParam(tParam);
        mail.setcParam(cParam);

        recvPlayer.mails.put(mail.getKeyId(), mail);

        // 发送邮件
        synMailToPlayer(recvPlayer, mail);

        return mail;
    }

    /**
     * 带list填充mail
     *
     * @param recvPlayer
     * @param moldId
     * @param time
     * @param param
     * @return
     */
    public Mail sendCrossNormalMail(Player recvPlayer, int moldId, int time, List<String> param) {
        StaticMail staticMail = StaticMailDataMgr.getStaticMail(moldId);
        if (staticMail == null) {
            LogUtil.error(String.format("roleId :%d, 未找到邮件模版 :%d", recvPlayer != null ? recvPlayer.getLordId() : 0L, moldId));
            return null;
        }

        if (recvPlayer == null) {
            LogUtil.debug("sendNormalMail recvPlayer is null");
            return null;
        }

        List<String> tParam = new ArrayList<>();
        List<String> cParam = new ArrayList<>();
        // 填充邮件参数
        fillMailParam(moldId, tParam, cParam, param);

        int type = staticMail.getType();
        Mail mail = new Mail(recvPlayer.maxKey(), type, moldId, MailConstant.STATE_UNREAD, time);
        mail.settParam(tParam);
        mail.setcParam(cParam);

        recvPlayer.mails.put(mail.getKeyId(), mail);
        mail.setCross(true);
        // 发送邮件
        synMailToPlayer(recvPlayer, mail);

        return mail;
    }

    /**
     * 发送带附件的邮件<br/>
     * <i>注意：</i>使用该方法 <tt>必须 </tt>在 {@link MailHelper#registerMailParamNum()}
     * 方法中注册邮件的标题和内容参数个数
     *
     * @param recvPlayer
     * @param awards
     * @param mailId
     * @param from
     * @param now
     * @param param
     * @return
     */
    public Mail sendAttachMail(Player recvPlayer, List<CommonPb.Award> awards, int mailId, AwardFrom from, int now,
            Object... param) {
        StaticMail staticMail = StaticMailDataMgr.getStaticMail(mailId);
        if (staticMail == null) {
            //邮件id未配置，就用这个保证正常发奖励
            staticMail = StaticMailDataMgr.getStaticMail(MailConstant.MOLD_GM_CUSTOM);//MOLD_NON_CONFIG_MAIL_ID
            param = new Object[]{"奖励补发","尊敬的领主，您在活动中获得以下奖励，请查收！[" + mailId + "]"};
            mailId = MailConstant.MOLD_GM_CUSTOM;
        }
        if(Objects.isNull(staticMail)){
            return null;
        }

        List<String> tParam = new ArrayList<>();
        List<String> cParam = new ArrayList<>();
        // 填充邮件参数
        fillMailParam(mailId, tParam, cParam, param);

        int type = staticMail.getType();
        Mail mail = new Mail(recvPlayer.maxKey(), type, mailId, awards != null ? MailConstant.STATE_UNREAD_ITEM : MailConstant.STATE_UNREAD, now);
        mail.settParam(tParam);
        mail.setcParam(cParam);
        if (awards != null) {
            mail.setRewardList(awards);
        }

        recvPlayer.mails.put(mail.getKeyId(), mail);
        LogUtil.common("带附件邮件roleId:", recvPlayer.roleId, ", mailId:", mailId,", from:", Objects.isNull(from)?"NULL":from.getCode(), ", tParam:", tParam, ", cParam:",cParam);
        if (ArrayUtils.contains(MailConstant.CROSS_MAIL_TEMPLATE_ID, mailId))
            mail.setCross(true);

        // 发送邮件
        synMailToPlayer(recvPlayer, mail);

        return mail;
    }


    /**
     * 发送带附件的邮件和获取的奖励的邮件<br/>
     * <i>注意：</i>使用该方法 <tt>必须 </tt>在 {@link MailHelper#registerMailParamNum()}
     * 方法中注册邮件的标题和内容参数个数
     *  这个方法现在没有用到
     * @param recvPlayer
     * @param awards
     * @param mailId
     * @param from
     * @param now
     * @param drop
     * @param param
     * @return
     */
    @Deprecated
    public Mail sendAttachMail(Player recvPlayer, List<CommonPb.Award> awards, int mailId, AwardFrom from, int now,
            List<CommonPb.Award> drop, Object... param) {
        StaticMail staticMail = StaticMailDataMgr.getStaticMail(mailId);
        if (staticMail == null) {
            return null;
        }

        List<String> tParam = new ArrayList<>();
        List<String> cParam = new ArrayList<>();
        // 填充邮件参数
        fillMailParam(mailId, tParam, cParam, param);

        int type = staticMail.getType();
        Mail mail = new Mail(recvPlayer.maxKey(), type, mailId, awards != null ? MailConstant.STATE_UNREAD_ITEM : MailConstant.STATE_UNREAD, now);
        mail.settParam(tParam);
        mail.setcParam(cParam);
        if (awards != null) {
            mail.setRewardList(awards);
        }
        if (null != drop) {
            mail.setDropList(drop);
        }

        recvPlayer.mails.put(mail.getKeyId(), mail);
        LogUtil.common("带附件邮件 roleId:", recvPlayer.roleId, ", mailId:", mailId, ", tParam:", tParam, ", cParam:",
                cParam);
        // 发送邮件
        synMailToPlayer(recvPlayer, mail);

        return mail;
    }

    /**
     * 发送采集邮件<br/>
     * <i>注意：</i>使用该方法 <tt>必须 </tt>在 {@link MailHelper#registerMailParamNum()}
     * 方法中注册邮件的标题和内容参数个数
     *
     * @param recvPlayer
     * @param report
     * @param moldId
     * @param collect    采集信息
     * @param now
     * @param param      邮件额外参数，包括邮件标题和内容的所有参数，按参数顺序排列
     * @return
     */
    public Mail sendCollectMail(Player recvPlayer, CommonPb.Report.Builder report, int moldId,
            CommonPb.MailCollect collect, int now, Map<Long, List<CommonPb.Award>> recoverArmyAwardMap,
            Object... param) {
        StaticMail staticMail = StaticMailDataMgr.getStaticMail(moldId);
        if (staticMail == null) {
            return null;
        }

        List<String> tParam = new ArrayList<>();
        List<String> cParam = new ArrayList<>();
        // 填充邮件参数
        fillMailParam(moldId, tParam, cParam, param);

        int type = staticMail.getType();
        Mail mail = new Mail(recvPlayer.maxKey(), type, moldId, MailConstant.STATE_UNREAD, now);
        mail.settParam(tParam);
        mail.setcParam(cParam);

        if (null != report) {
            report.setKeyId(mail.getKeyId());
        }
        if (null != collect) {
            List<CommonPb.Award> grabList = collect.getGrabList();
            if (!CheckNull.isEmpty(grabList)) {
                for (CommonPb.Award award : grabList) {
                    int key = award.getType() * 10000 + award.getId();
                    // 更新活动采集资源
                    activityDataManager.updActivity(recvPlayer, ActivityConst.ACT_COLLECT_RESOURCES, award.getCount(), key, true);
                }
            }
            mail.setCollect(collect);
        }
        List<CommonPb.Award> recoverList = null;
        if (!CheckNull.isEmpty(recoverArmyAwardMap) && recoverArmyAwardMap.containsKey(recvPlayer.roleId)) {
            recoverList = recoverArmyAwardMap.get(recvPlayer.roleId);
        }
        if (null != recoverList) {
            mail.setRecoverList(recoverList);
        }

        recvPlayer.addMail(mail, CheckNull.isNull(report) ? null : report.build());

        // 发送采集邮件
        synMailToPlayer(recvPlayer, mail);

        return mail;
    }

    public Mail sendCrossCollectMail(Player recvPlayer, CommonPb.Report.Builder report, int moldId,
                                CommonPb.MailCollect collect, int now, Map<Long, List<CommonPb.Award>> recoverArmyAwardMap,
                                List<String> param) {
        StaticMail staticMail = StaticMailDataMgr.getStaticMail(moldId);
        if (staticMail == null) {
            return null;
        }

        List<String> tParam = new ArrayList<>();
        List<String> cParam = new ArrayList<>();
        // 填充邮件参数
        fillMailParam(moldId, tParam, cParam, param);

        int type = staticMail.getType();
        Mail mail = new Mail(recvPlayer.maxKey(), type, moldId, MailConstant.STATE_UNREAD, now);
        mail.settParam(tParam);
        mail.setcParam(cParam);

        if (null != report) {
            report.setKeyId(mail.getKeyId());
        }
        if (null != collect) {
            List<CommonPb.Award> grabList = collect.getGrabList();
            if (!CheckNull.isEmpty(grabList)) {
                for (CommonPb.Award award : grabList) {
                    int key = award.getType() * 10000 + award.getId();
                    // 更新活动采集资源
                    activityDataManager.updActivity(recvPlayer, ActivityConst.ACT_COLLECT_RESOURCES, award.getCount(), key, true);
                }
            }
            mail.setCollect(collect);
        }
        List<CommonPb.Award> recoverList = null;
        if (!CheckNull.isEmpty(recoverArmyAwardMap) && recoverArmyAwardMap.containsKey(recvPlayer.roleId)) {
            recoverList = recoverArmyAwardMap.get(recvPlayer.roleId);
        }
        if (null != recoverList) {
            mail.setRecoverList(recoverList);
        }

        recvPlayer.addMail(mail, CheckNull.isNull(report) ? null : report.build());
        mail.setCross(true);
        // 发送采集邮件
        synMailToPlayer(recvPlayer, mail);

        return mail;
    }

    /**
     * 兼容以前的方法
     *
     * @param recvPlayer
     * @param report
     * @param moldId
     * @param collect
     * @param now
     * @param param
     * @return
     */
    public Mail sendCollectMail(Player recvPlayer, CommonPb.Report.Builder report, int moldId,
            CommonPb.MailCollect collect, int now, Object... param) {
        return sendCollectMail(recvPlayer, report, moldId, collect, now, null, param);
    }

    private void fillMailParam(int moldId, List<String> tParam, List<String> cParam, Object... param) {
        if (!CheckNull.isEmpty(param)) {
            Turple<Integer, Integer> paramNum = MailHelper.getMailParamNum(moldId);
            for (int i = 0; i < paramNum.getA(); i++) {
                tParam.add(String.valueOf(param[i]));
            }
            for (int i = paramNum.getA(); i < param.length; i++) {
                cParam.add(String.valueOf(param[i]));
            }
        }
    }

    private void fillMailParam(int moldId, List<String> tParam, List<String> cParam, List<String> param) {
        if (!CheckNull.isEmpty(param)) {
            Turple<Integer, Integer> paramNum = MailHelper.getMailParamNum(moldId);
            for (int i = 0; i < paramNum.getA(); i++) {
                tParam.add(param.get(i));
            }
            for (int i = paramNum.getA(); i < param.size(); i++) {
                cParam.add(param.get(i));
            }
        }
    }

    /**
     * 发送侦查邮件，<i>注意：</i>使用该方法 <tt>必须 </tt>在
     * {@link MailHelper#registerMailParamNum()} 方法中注册邮件的标题和内容参数个数
     *
     * @param recvPlayer 接收邮件的玩家
     * @param mailId     邮件模版id
     * @param scout      侦查结果详情
     * @param now
     * @param param      邮件额外参数，包括邮件标题和内容的所有参数，按参数顺序排列
     * @return 返回邮件对象
     */
    public Mail sendScoutMail(Player recvPlayer, int mailId, CommonPb.MailScout scout, int now, Object... param) {
        StaticMail staticMail = StaticMailDataMgr.getStaticMail(mailId);
        if (staticMail == null) {
            return null;
        }

        List<String> tParam = new ArrayList<>();
        List<String> cParam = new ArrayList<>();
        // 填充邮件参数
        fillMailParam(mailId, tParam, cParam, param);

        int type = staticMail.getType();
        Mail mail = new Mail(recvPlayer.maxKey(), type, mailId, MailConstant.STATE_UNREAD, now);
        mail.settParam(tParam);
        mail.setcParam(cParam);
        if (null != scout) {
            mail.setScout(scout);

        }
        recvPlayer.mails.put(mail.getKeyId(), mail);

        // 发送侦查邮件
        synMailToPlayer(recvPlayer, mail);
        return mail;
    }

    public Mail sendCrossScoutMail(Player recvPlayer, int mailId, CommonPb.MailScout scout, int now, List<String> param) {
        StaticMail staticMail = StaticMailDataMgr.getStaticMail(mailId);
        if (staticMail == null) {
            return null;
        }

        List<String> tParam = new ArrayList<>();
        List<String> cParam = new ArrayList<>();
        // 填充邮件参数
        fillMailParam(mailId, tParam, cParam, param);

        int type = staticMail.getType();
        Mail mail = new Mail(recvPlayer.maxKey(), type, mailId, MailConstant.STATE_UNREAD, now);
        mail.settParam(tParam);
        mail.setcParam(cParam);
        if (null != scout) {
            mail.setScout(scout);

        }
        recvPlayer.mails.put(mail.getKeyId(), mail);
        mail.setCross(true);

        // 发送侦查邮件
        synMailToPlayer(recvPlayer, mail);
        return mail;
    }

    /**
     * 发送战报邮件，<i>注意：</i>使用该方法 <tt>必须 </tt>在
     * {@link MailHelper#registerMailParamNum()} 方法中注册邮件的标题和内容参数个数
     *
     * @param recvPlayer          接收邮件的玩家
     * @param report              战报
     * @param mailId              邮件模版id
     * @param drop                掉落物品，仅用于客户端显示
     * @param now
     * @param recoverArmyAwardMap 伤兵恢复
     * @param param               邮件额外参数，包括邮件标题和内容的所有参数，按参数顺序排列
     * @return 返回邮件对象
     */
    public Mail sendReportMail(Player recvPlayer, CommonPb.Report.Builder report, int mailId, List<CommonPb.Award> drop,
            int now, Map<Long, List<CommonPb.Award>> recoverArmyAwardMap, Object... param) {
        Turple<Integer, Integer> paramNum = MailHelper.getMailParamNum(mailId);
        List<String> tParam = new ArrayList<>();
        List<String> cParam = new ArrayList<>();
        if (!CheckNull.isEmpty(param)) {
            for (int i = 0; i < paramNum.getA(); i++) {
                tParam.add(String.valueOf(param[i]));
            }
            for (int i = paramNum.getA(); i < param.length; i++) {
                cParam.add(String.valueOf(param[i]));
            }

            int total = paramNum.getA() + paramNum.getB();
            if (total != param.length) {
                LogUtil.error("战报邮件参数个数传入不正确, roleId:", recvPlayer.roleId, ", mailId:", mailId, ", need:", total,
                        ", 传入个数:", param.length);
            }
        }
        List<CommonPb.Award> recoverList = null;
        if (!CheckNull.isEmpty(recoverArmyAwardMap) && recoverArmyAwardMap.containsKey(recvPlayer.roleId)) {
            recoverList = recoverArmyAwardMap.get(recvPlayer.roleId);
        }
        return sendReportMail(recvPlayer, report, mailId, drop, now, tParam, cParam, recoverList);
    }

    /**
     * 带List发邮件
     *
     * @param recvPlayer
     * @param report
     * @param mailId
     * @param drop
     * @param now
     * @param recoverArmyAwardMap
     * @param param
     * @return
     */
    public Mail sendCrossReportMail(Player recvPlayer, CommonPb.Report.Builder report, int mailId, List<CommonPb.Award> drop,
                               int now, Map<Long, List<CommonPb.Award>> recoverArmyAwardMap, List<String> param) {
        Turple<Integer, Integer> paramNum = MailHelper.getMailParamNum(mailId);
        List<String> tParam = new ArrayList<>();
        List<String> cParam = new ArrayList<>();
        if (!CheckNull.isEmpty(param)) {
            for (int i = 0; i < paramNum.getA(); i++) {
                tParam.add(param.get(i));
            }
            for (int i = paramNum.getA(); i < param.size(); i++) {
                cParam.add(param.get(i));
            }

            int total = paramNum.getA() + paramNum.getB();
            if (total != param.size()) {
                LogUtil.error("战报邮件参数个数传入不正确, roleId:", recvPlayer.roleId, ", mailId:", mailId, ", need:", total,
                        ", 传入个数:", param.size());
            }
        }
        List<CommonPb.Award> recoverList = null;
        if (!CheckNull.isEmpty(recoverArmyAwardMap) && recoverArmyAwardMap.containsKey(recvPlayer.roleId)) {
            recoverList = recoverArmyAwardMap.get(recvPlayer.roleId);
        }
        return sendCrossReportMail(recvPlayer, report, mailId, drop, now, tParam, cParam, recoverList);
    }

    /**
     * 兼容之前的方法
     *
     * @param recvPlayer
     * @param report
     * @param mailId
     * @param drop
     * @param now
     * @param param
     * @return
     */
    public Mail sendReportMail(Player recvPlayer, CommonPb.Report.Builder report, int mailId, List<CommonPb.Award> drop,
            int now, Object... param) {
        return sendReportMail(recvPlayer, report, mailId, drop, now, null, param);
    }

    /**
     * 添加并发送战报邮件,不能包含奖励道具
     *
     * @param recvPlayer 接收邮件的玩家
     * @param report     战报
     * @param moldId     邮件模版id
     * @param drop       掉落物品，仅用于客户端显示
     * @param now
     * @param tParam     邮件标题参数
     * @param cParam     邮件内容参数
     * @return
     */
    public Mail sendReportMail(Player recvPlayer, CommonPb.Report.Builder report, int moldId, List<CommonPb.Award> drop,
            int now, List<String> tParam, List<String> cParam) {
        return sendReportMail(recvPlayer, report, moldId, drop, now, tParam, cParam, null);
    }

    /**
     * 添加并发送战报邮件,不能包含奖励道具
     *
     * @param recvPlayer 接收邮件的玩家
     * @param report     战报
     * @param moldId     邮件模版id
     * @param drop       掉落物品，仅用于客户端显示
     * @param now
     * @param tParam     邮件标题参数
     * @param cParam     邮件内容参数
     * @param recover    恢复物品, 仅用于客户端显示
     * @return
     */
    public Mail sendReportMail(Player recvPlayer, CommonPb.Report.Builder report, int moldId, List<CommonPb.Award> drop,
            int now, List<String> tParam, List<String> cParam, List<CommonPb.Award> recover) {
        Mail mail = createReportMail(recvPlayer, report, moldId, drop, now, tParam, cParam, recover);

        if (null != mail) {
            // 发送战报邮件
            synMailToPlayer(recvPlayer, mail);
        }

        return mail;
    }

    public Mail sendCrossReportMail(Player recvPlayer, CommonPb.Report.Builder report, int moldId, List<CommonPb.Award> drop,
                               int now, List<String> tParam, List<String> cParam, List<CommonPb.Award> recover) {
        Mail mail = createCrossReportMail(recvPlayer, report, moldId, drop, now, tParam, cParam, recover);

        if (null != mail) {
            // 发送战报邮件
            synMailToPlayer(recvPlayer, mail);
        }

        return mail;
    }

    public Mail createCrossReportMail(Player recvPlayer, CommonPb.Report.Builder report, int moldId,
                                 List<CommonPb.Award> drop, int now, List<String> tParam, List<String> cParam,
                                 List<CommonPb.Award> recover) {
        StaticMail staticMail = StaticMailDataMgr.getStaticMail(moldId);
        if (staticMail == null) {
            return null;
        }
        if (recvPlayer == null) {
            LogUtil.error("createReportMail can not found recvPlayer=" + recvPlayer);
            return null;
        }

        int type = staticMail.getType();
        Mail mail = new Mail(recvPlayer.maxKey(), type, moldId, MailConstant.STATE_UNREAD, now);
        if (null != tParam) {
            mail.settParam(tParam);
        }
        if (null != cParam) {
            mail.setcParam(cParam);
        }

        if (report != null) {
            report.setKeyId(mail.getKeyId());
        }
        if (null != drop) {
            mail.setDropList(drop);
        }
        if (recover != null) {
            mail.setRecoverList(recover);
        }
        mail.setCross(true);

        recvPlayer.addMail(mail, CheckNull.isNull(report) ? null : report.build());
        return mail;
    }

    /**
     * 创建并返回战报邮件，<i>注意：</i>使用该方法 <tt>必须 </tt>在
     * {@link MailHelper#registerMailParamNum()} 方法中注册邮件的标题和内容参数个数
     *
     * @param recvPlayer 接收邮件的玩家
     * @param report     战报
     * @param moldId     邮件模版id
     * @param drop       掉落物品，仅用于客户端显示
     * @param now
     * @param tParam     邮件额外参数，包括邮件标题的所有参数，按参数顺序排列
     * @param cParam     邮件额外参数，包括内容的所有参数，按参数顺序排列
     * @param recover
     * @return 返回邮件对象
     */
    public Mail createReportMail(Player recvPlayer, CommonPb.Report.Builder report, int moldId,
            List<CommonPb.Award> drop, int now, List<String> tParam, List<String> cParam,
            List<CommonPb.Award> recover) {
        StaticMail staticMail = StaticMailDataMgr.getStaticMail(moldId);
        if (staticMail == null) {
            return null;
        }
        if (recvPlayer == null) {
            LogUtil.error("createReportMail can not found recvPlayer=" + recvPlayer);
            return null;
        }

        int type = staticMail.getType();
        Mail mail = new Mail(recvPlayer.maxKey(), type, moldId, MailConstant.STATE_UNREAD, now);
        if (null != tParam) {
            mail.settParam(tParam);
        }
        if (null != cParam) {
            mail.setcParam(cParam);
        }

        if (report != null) {
            report.setKeyId(mail.getKeyId());
        }
        if (null != drop) {
            mail.setDropList(drop);
        }
        if (recover != null) {
            mail.setRecoverList(recover);
        }

        recvPlayer.addMail(mail, CheckNull.isNull(report) ? null : report.build());
        return mail;
    }

    /**
     * 同步邮件到客户端
     *
     * @param target
     * @param mail
     */
    public void synMailToPlayer(Player target, Mail mail) {
        if (target != null && target.isLogin && target.ctx != null) {
            SyncMailRs.Builder builder = SyncMailRs.newBuilder();
            builder.setShow(PbHelper.createMailShowPb(mail));
            Base.Builder msg = PbHelper.createSynBase(SyncMailRs.EXT_FIELD_NUMBER, SyncMailRs.ext, builder.build());
            MsgDataManager.getIns().add(new Msg(target.ctx, msg.build(), target.roleId));
        }
    }

    private Mail createSandTableEnrollMail(Player recvPlayer, int moldId, int now, Object[] param,
                                           CommonPb.SandTableEnrollMailInfo mailInfo0,
                                           CommonPb.SandTableRoundOverMailInfo mailInfo1){
        StaticMail staticMail = StaticMailDataMgr.getStaticMail(moldId);
        if (staticMail == null) {
            return null;
        }
        int type = staticMail.getType();
        Mail mail = new Mail(recvPlayer.maxKey(), type, moldId, MailConstant.STATE_UNREAD, now);
        List<String> tParam = new ArrayList<>();
        List<String> cParam = new ArrayList<>();
        // 填充邮件参数
        fillMailParam(moldId, tParam, cParam, param);
        mail.settParam(tParam);
        mail.setcParam(cParam);
        if(Objects.nonNull(mailInfo0))
            mail.setEnrollMailInfo(mailInfo0);
        if(Objects.nonNull(mailInfo1))
            mail.setRoundOverMailInfo(mailInfo1);

        recvPlayer.mails.put(mail.getKeyId(), mail);
        return mail;
    }

    public void sendSandTableEnrollMail(Player recvPlayer, int moldId, int now, Object[] param, CommonPb.SandTableEnrollMailInfo mailInfo){
        Mail mail = this.createSandTableEnrollMail(recvPlayer,moldId,now,param,mailInfo,null);
        if (Objects.nonNull(mail)) {
            synMailToPlayer(recvPlayer, mail);
        }
    }

    public void sendSandTableRoundOverMail(Player recvPlayer,int moldId,int now,Object[] param,CommonPb.SandTableRoundOverMailInfo mailInfo){
        Mail mail = this.createSandTableEnrollMail(recvPlayer,moldId,now,param,null,mailInfo);
        if (Objects.nonNull(mail)) {
            synMailToPlayer(recvPlayer, mail);
        }
    }
}
