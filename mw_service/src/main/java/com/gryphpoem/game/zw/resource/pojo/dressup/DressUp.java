package com.gryphpoem.game.zw.resource.pojo.dressup;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.AwardType;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.pojo.Task;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 玩家的装扮
 *
 * @description:
 * @author: zhou jie
 * @time: 2021/3/4 11:45
 */
public class DressUp {

    /**
     * 当前使用的头像框
     */
    private int curPortraitFrame = Constant.DEFAULT_PORTRAIT_FRAME_ID;
    /**
     * 当前使用的铭牌
     */
    private int curNamePlate = Constant.DEFAULT_NAME_PLATE_ID;
    /**
     * 当前使用的行军特效
     */
    private int curMarchEffect = Constant.DEFAULT_MARCH_LINE_ID;
    /**
     * 当前使用的气泡
     */
    private int currChatBubble = 0;

    /**
     * 当前使用的称号 0没有称号
     */
    private int curTitle = 0;
    /**
     * 所有类型的装扮 <br/>
     * key值定义：{@link com.gryphpoem.game.zw.resource.constant.AwardType}
     */
    private Map<Integer, Map<Integer, BaseDressUpEntity>> allDressUpEntity = new HashMap<>();

    /**
     * 根据类型获取装扮
     *
     * @param type 类型
     * @return 装扮对象
     */
    public Map<Integer, BaseDressUpEntity> getDressUpEntityMapByType(int type) {
        return allDressUpEntity.computeIfAbsent(type, (t) -> new HashMap<>());
    }

    /**
     * 获取所有的装扮
     *
     * @return 所有的装扮数据
     */
    public Map<Integer, Map<Integer, BaseDressUpEntity>> getAllDressUpEntity() {
        return allDressUpEntity;
    }

    public int getCurPortraitFrame() {
        return curPortraitFrame;
    }

    public void setCurPortraitFrame(int curPortraitFrame) {
        this.curPortraitFrame = curPortraitFrame;
    }

    public int getCurNamePlate() {
        return curNamePlate;
    }

    public void setCurNamePlate(int curNamePlate) {
        this.curNamePlate = curNamePlate;
    }

    public int getCurMarchEffect() {
        if (curMarchEffect == 0 && Constant.DEFAULT_MARCH_LINE_ID != 0) {
            this.curMarchEffect = Constant.DEFAULT_MARCH_LINE_ID;
        }
        return curMarchEffect;
    }

    public void setCurMarchEffect(int curMarchEffect) {
        this.curMarchEffect = curMarchEffect;
    }

    public int getCurTitle() {
        return curTitle;
    }

    public void setCurTitle(int curTitle) {
        this.curTitle = curTitle;
    }

    /**
     * 序列化
     *
     * @return 装扮数据
     */
    public CommonPb.DressUpData ser() {
        CommonPb.DressUpData.Builder builder = CommonPb.DressUpData.newBuilder();
        builder.setCurPortraitFrame(this.curPortraitFrame);
        builder.setCurNamePlate(this.curNamePlate);
        builder.setCurMarchEffect(this.curMarchEffect);
        builder.setCurrChatBubble(this.currChatBubble);
        builder.setCurTitle(this.curTitle);
        builder.addAllData(this.allDressUpEntity.values().stream().flatMap(map -> map.values().stream()).map(entity -> entity.toData().build()).collect(Collectors.toList()));
        return builder.build();
    }

    /**
     * 反序列化
     *
     * @param duData 装扮数据
     */
    public void dser(CommonPb.DressUpData duData) {
        this.curPortraitFrame = duData.getCurPortraitFrame();
        this.curNamePlate = duData.getCurNamePlate();
        this.curMarchEffect = duData.getCurMarchEffect();
        this.currChatBubble = duData.getCurrChatBubble();
        this.curTitle = duData.getCurTitle();
        this.allDressUpEntity = duData.getDataList().stream().collect(Collectors.groupingBy(CommonPb.DressUpEntity::getType, HashMap::new, Collectors.toMap(CommonPb.DressUpEntity::getId, pbToDressUpEntity(), (oldV, newV) -> newV)));
    }

    /**
     * pb转成装扮的实体对象
     * @return 函数
     */
    private Function<CommonPb.DressUpEntity, BaseDressUpEntity> pbToDressUpEntity() {
        return data -> {
            int type = data.getType();
            BaseDressUpEntity dressUpEntity = null;
            if (type == AwardType.PORTRAIT) {
                dressUpEntity = new PortraitEntity(data);
            } else if (type == AwardType.CASTLE_SKIN) {
                dressUpEntity = new CastleSkinEntity(data);
            } else if (type == AwardType.PORTRAIT_FRAME) {
                dressUpEntity = new PortraitFrameEntity(data);
            } else if (type == AwardType.NAMEPLATE) {
                dressUpEntity = new NameplateEntity(data);
            } else if (type == AwardType.MARCH_SPECIAL_EFFECTS) {
                dressUpEntity = new MarchSpecialEffectEntity(data);
            } else if (type == AwardType.CHAT_BUBBLE) {
                dressUpEntity = new ChatBubbleEntity(data);
            } else if (type == AwardType.TITLE) {
                dressUpEntity = new TitleEntity(data);
            }else {
                LogUtil.error("反序列化错误, type：", type, ", id：", data.getId());
            }
            return dressUpEntity;
        };
    }

    public int getCurrChatBubble() {
        return currChatBubble;
    }

    public void setCurrChatBubble(int currChatBubble) {
        this.currChatBubble = currChatBubble;
    }
}
