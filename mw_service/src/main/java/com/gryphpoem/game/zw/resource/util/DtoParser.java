package com.gryphpoem.game.zw.resource.util;

import com.gryphpoem.cross.chat.dto.CrossRoleChat;
import com.gryphpoem.cross.gameplay.player.common.CrossHero;
import com.gryphpoem.cross.gameplay.player.common.CrossLord;
import com.gryphpoem.cross.gameplay.player.common.CrossMedal;
import com.gryphpoem.cross.gameplay.player.common.CrossSeasonTalent;
import com.gryphpoem.cross.player.dto.PlayerLordDto;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.dataMgr.StaticHeroDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticIniDataMgr;
import com.gryphpoem.game.zw.manager.MedalDataManager;
import com.gryphpoem.game.zw.manager.TechDataManager;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.AwardType;
import com.gryphpoem.game.zw.resource.constant.HeroConstant;
import com.gryphpoem.game.zw.resource.constant.MedalConst;
import com.gryphpoem.game.zw.resource.constant.SeasonConst;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Lord;
import com.gryphpoem.game.zw.resource.domain.s.StaticHero;
import com.gryphpoem.game.zw.resource.domain.s.StaticSeasonTalent;
import com.gryphpoem.game.zw.resource.pojo.chat.RoleChat;
import com.gryphpoem.game.zw.resource.pojo.dressup.DressUp;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.medal.Medal;
import com.gryphpoem.game.zw.resource.pojo.season.SeasonTalent;
import com.gryphpoem.game.zw.service.FightService;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;

import java.util.*;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-08-30 14:15
 */
public final class DtoParser {
    public static PlayerLordDto toPlayerLordDto(Player player, int serverId) {
        PlayerLordDto dto = new PlayerLordDto();
        Lord lord = player.lord;
        dto.setLordId(player.getLordId());
        dto.setOriginalServerId(player.account.getServerId());
        dto.setServerId(serverId);
        dto.setCamp(player.getCamp());
        dto.setRoleName(lord.getNick());
        dto.setVip(lord.getVip());
        dto.setLv(lord.getLevel());
        dto.setMapId(0);
        dto.setPos(lord.getPos());
        dto.setCe(lord.getFight());
        dto.setAppearance(getAppearance(player));
        return dto;
    }

    public static Map<Integer, Integer> getAppearance(Player player) {
        Map<Integer, Integer> appMap = new HashMap<>();
        //皮肤
        appMap.put(AwardType.CASTLE_SKIN, player.getCurCastleSkin());
        //头像
        appMap.put(AwardType.PORTRAIT, player.lord.getPortrait());
        //头像框
        DressUp dress = player.getDressUp();
        appMap.put(AwardType.PORTRAIT_FRAME, dress.getCurPortraitFrame());
        //铭牌
        appMap.put(AwardType.NAMEPLATE, dress.getCurNamePlate());
        //行军特效
        appMap.put(AwardType.MARCH_SPECIAL_EFFECTS, dress.getCurMarchEffect());
        //行军特效
        appMap.put(AwardType.MARCH_SPECIAL_EFFECTS, dress.getCurTitle());
        return appMap;
    }

    public static CrossRoleChat getCrossRoleChat(long roomId, int chlId, RoleChat chat) {
        CrossRoleChat crossRoleChat = new CrossRoleChat();
        crossRoleChat.setRoomId(roomId);
        crossRoleChat.setChlId(chlId);
        crossRoleChat.setMsg(chat.getMsg());
        crossRoleChat.setStyle(chat.getStyle());

        Lord lord = chat.getPlayer().lord;
        crossRoleChat.setLordId(lord.getLordId());
        crossRoleChat.setLevel(lord.getLevel());
        crossRoleChat.setNick(lord.getNick());
        ServerSetting serverSetting = DataResource.ac.getBean(ServerSetting.class);
        crossRoleChat.setServerId(serverSetting.getServerID());
        crossRoleChat.setCamp(lord.getCamp());
        crossRoleChat.setCampJob(lord.getJob());
        crossRoleChat.setRanks(lord.getRanks());
        crossRoleChat.setMapId(lord.getArea());
        crossRoleChat.setPos(lord.getPos());
        crossRoleChat.setPortrait(lord.getPortrait());
        //头像框
        DressUp dress = chat.getPlayer().getDressUp();
        crossRoleChat.setPortraitFrame(dress.getCurPortraitFrame());
        crossRoleChat.setBubbleId(dress.getCurrChatBubble());

        //额外参数
        if (chat.getMyParam() != null) {
            crossRoleChat.setExtParam(Arrays.asList(chat.getMyParam()));
        }
        return crossRoleChat;
    }

    public static CrossLord buildCrossFightLord(Player player) {
        CrossLord cld = new CrossLord();
        Lord lord = player.lord;
        cld.setLordId(lord.getLordId());
        cld.setNick(lord.getNick());
        cld.setLevel(lord.getLevel());
        cld.setCamp(lord.getCamp());
        cld.setCommandLv(player.building.getCommand());
        ServerSetting serverSetting = DataResource.ac.getBean(ServerSetting.class);
        cld.setServerId(serverSetting.getServerID());
        cld.setOriginalServerId(player.account.getServerId());
        return cld;
    }

    public static CrossHero buildCrossFightHero(Player player, Hero hero) {
        CrossHero heroDto = new CrossHero();
        StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
        heroDto.setLordId(player.getLordId());
        heroDto.setHeroId(hero.getHeroId());
        FightService fightService = DataResource.ac.getBean(FightService.class);
        //英雄血条数目
        int line = fightService.calcHeroLine(player, hero, staticHero.getLine());
        heroDto.setMaxLine(line);
        //单排兵力
        int lead = (int) Math.ceil(hero.getAttr()[HeroConstant.ATTR_LEAD] * 1.0 / line);// 当兵力不能被整除时，向上取整
        heroDto.setLead(lead);
        TechDataManager techDataManager = DataResource.ac.getBean(TechDataManager.class);
        //兵种等级
        int intensifyLv = techDataManager.getIntensifyLv4HeroType(player, staticHero.getType());
        heroDto.setIntensifyLv(intensifyLv);
        //目前战斗计算中没使用
//        int restrain = techDataManager.getIntensifyRestrain4HeroType(player, staticHero.getType());

        //兵书
        MedalDataManager medalDataManager = DataResource.ac.getBean(MedalDataManager.class);
        Medal medal = medalDataManager.getHeroMedalByHeroIdAndIndex(player, hero.getHeroId(), MedalConst.HERO_MEDAL_INDEX_0);
        if (Objects.nonNull(medal)) {
            heroDto.setMedal(buildCrossFightMedal(medal));
        }
        //赛季英雄技能
        List<Integer> skillActionList = buildHeroSkill(hero);
        if (CheckNull.nonEmpty(skillActionList)) {
            heroDto.setSkillAction(skillActionList);
        }
        //战斗属性
        Map<Integer, Integer> attrMap = CalculateUtil.processAttr(player, hero);
        heroDto.setAttrMap(attrMap);
        return heroDto;
    }

    public static CrossSeasonTalent buildCrossFightSeasonTalent(Player player) {
        SeasonTalent talent = player.getPlayerSeasonData().getSeasonTalent();
        SeasonTalentService talentService = DataResource.ac.getBean(SeasonTalentService.class);
        if (talentService.checkTalentBuffOpen(player)) {
            CrossSeasonTalent talentDto = new CrossSeasonTalent();
            Map<Integer, StaticSeasonTalent> sTalentMap = StaticIniDataMgr.getSeasonTalentMap();
            for (Integer learnId : talent.getLearns()) {
                StaticSeasonTalent staticSeasonTalent = sTalentMap.get(learnId);
                if (Objects.nonNull(staticSeasonTalent)) {
                    if (Arrays.binarySearch(SeasonConst.fightEffects, staticSeasonTalent.getEffect()) >= 0) {
                        Set<Integer> tidSet = talentDto.computeIfAbsent();
                        tidSet.add(staticSeasonTalent.getId());
                    }
                }
            }
            //此处可以优化, 只传递战斗属性
            return talentDto;
        }
        return null;
    }

    public static CrossMedal buildCrossFightMedal(Medal medal) {
        return new CrossMedal(medal.getAuraSkillId(), medal.getSpecialSkillId());
    }

    private static List<Integer> buildHeroSkill(Hero hero) {
//        List<Integer> skillActionList = null;
//        for (Map.Entry<Integer, Integer> entry : hero.getSkillLevels().entrySet()) {
//            StaticHeroSeasonSkill heroSkill = StaticHeroDataMgr.getHeroSkill(hero.getHeroId(), entry.getKey(), entry.getValue());
//            if (Objects.nonNull(heroSkill)) {
//                StaticSkillAction ska = StaticFightDataMgr.getSkillAction(heroSkill.getSkillActionId());
//                if (Objects.nonNull(ska)) {
//                    if (skillActionList == null) {
//                        skillActionList = new ArrayList<>();
//                    }
//                    skillActionList.add(ska.getId());
//                }
//            }
//        }
        return null;
    }
}
