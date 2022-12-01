package com.gryphpoem.game.zw.handler.client.mail;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.AsyncGameHandler;
import com.gryphpoem.game.zw.manager.FightRecordDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.BattlePb;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.constant.MailConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.pojo.Mail;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.util.FightUtil;

import java.util.Objects;

/**
 * 异步获取
 *
 * @author zhou jie
 * @time 2022/10/9 17:10
 */
public class GetFightRecordHandler extends AsyncGameHandler {

    @Override
    public void action() throws Exception {
        GamePb5.GetFightRecordRq req = msg.getExtension(GamePb5.GetFightRecordRq.ext);

        Long roleId = getRoleId();
        int mailKeyId = req.getMailKeyId();

        long curId = FightUtil.getFightIdGenerator();
        long recordId = Long.parseLong(req.getRecordId());

        if (recordId < 0 || recordId > curId) {
            // TODO: 2022/10/9 战报id检验
            throw new MwException(GameError.PARAM_ERROR.getCode(), "战报id校验失败! roleId:" + roleId + ",recordId:" + recordId);
        }
        Player player = DataResource.ac.getBean(PlayerDataManager.class).checkPlayerIsExist(roleId);
        Mail mail;
        if ((mail = player.mails.get(mailKeyId)) == null) {
            throw new MwException(GameError.MAIL_NOT_EXIST, "邮件不能存在, roleId: ", roleId, ", mailKeyId: ", mailKeyId);
        }
        if (mail.getReportStatus() == MailConstant.EXPIRED_REPORT) {
            throw new MwException(GameError.MAIL_REPORT_EXPIRED, "邮件战报过期, roleId: ", roleId, ", mailKeyId: ", mailKeyId);
        }

        FightRecordDataManager fightRecordDataManager = DataResource.ac.getBean(FightRecordDataManager.class);
        // 战况
        BattlePb.BattleRoundPb record = fightRecordDataManager.selectRecord(recordId);

        GamePb5.GetFightRecordRs.Builder builder = GamePb5.GetFightRecordRs.newBuilder();
        if (Objects.nonNull(record)) {
            builder.setRecord(record);
        }
        sendMsgToPlayer(PbHelper.createRsBase(GamePb5.GetFightRecordRs.EXT_FIELD_NUMBER, GamePb5.GetFightRecordRs.ext, builder.build()));
    }

}
