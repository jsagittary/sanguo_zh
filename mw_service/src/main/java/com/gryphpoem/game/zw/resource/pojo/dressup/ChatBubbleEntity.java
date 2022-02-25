package com.gryphpoem.game.zw.resource.pojo.dressup;

import com.gryphpoem.game.zw.dataMgr.StaticLordDataMgr;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.AwardType;
import com.gryphpoem.game.zw.resource.domain.s.StaticChatBubble;

import java.util.List;
import java.util.Objects;

/**
 * @author xwind
 * @date 2021/5/31
 */
public class ChatBubbleEntity extends BaseDressUpEntity{
    public ChatBubbleEntity(int id) {
        super(AwardType.CHAT_BUBBLE, id);
    }

    public ChatBubbleEntity(int id, boolean permanentHas) {
        super(AwardType.CHAT_BUBBLE, id, permanentHas);
    }

    public ChatBubbleEntity(int id, int duration) {
        super(AwardType.CHAT_BUBBLE, id, duration);
    }

    public ChatBubbleEntity(CommonPb.DressUpEntity data) {
        super(data);
    }

    @Override
    public List<List<Integer>> convertProps() {
        StaticChatBubble staticChatBubble = StaticLordDataMgr.getChatBubbleMapById(getId());
        if (Objects.nonNull(staticChatBubble)) {
            return staticChatBubble.getConsume();
        }
        return null;
    }
}
