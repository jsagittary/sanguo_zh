package com.gryphpoem.game.zw.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.crosssimple.service.PlayerForCrossService;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.pojo.chat.RoleChat;

/**
 * @ClassName GmServiceExt.java
 * @Description gm命令扩展,之前的gmService类太大了
 * @author QiuKun
 * @date 2019年5月13日
 */
@Component
public class GmServiceExt {

    @Autowired
    private PlayerDataManager playerDataManager;

    @Autowired
    private ChatService chatService;

    @Autowired
    private PlayerForCrossService playerForCrossService;

    private Map<String, IDoSome> doSomeMap = new HashMap<>();

    private void initDoSomeMap() {
        if (doSomeMap.isEmpty()) {
            registDoSome("loginCross", this::loginCross); // 登陆跨服
            registDoSome("sendCrossChat", this::sendCrossChat); // 发送跨服聊天
            registDoSome("finlishCrossclear", this::finlishCrossclear); // 结束跨服清除
        }
    }

    private void registDoSome(String key, IDoSome d) {
        doSomeMap.put(key, d);
    }

    public void doSome(String[] words, long roleId) throws MwException {
        initDoSomeMap();
        if (words != null && words.length > 0) {
            String key = words[0];
            IDoSome iDoSome = doSomeMap.get(key);
            if (iDoSome != null) {
                iDoSome.doSomething(words, roleId);
            }
        }
    }

    private void sendCrossChat(String[] words, long roleId) throws MwException {
        Player player = playerDataManager.getPlayer(roleId);

        if (player != null && words.length > 2) {
            String channelStr = words[1];
            String content = words[2];
            int channel = Integer.parseInt(channelStr);
            RoleChat roleChat = (RoleChat) chatService.createRoleChat(player, content);
            roleChat.setChannel(channel);
            chatService.sendCrossChat(roleChat, player, false);
        }
    }

    private void loginCross(String[] words, long roleId) {
        Player player = playerDataManager.getPlayer(roleId);
        if (player != null) {
            playerForCrossService.loginCrossProcess(player);
        }
    }

    /**
     * 跨服结束清除
     * 
     * @param words
     * @param roleId
     */
    private void finlishCrossclear(String[] words, long roleId) {
        if (words.length >= 2 && "all".equalsIgnoreCase(words[1])) { // 清理全部
            playerDataManager.getAllPlayer().values().forEach(player -> {
                playerForCrossService.crossWarFinishClear(player);
            });
        } else {
            Player player = playerDataManager.getPlayer(roleId);
            if (player != null) {
                playerForCrossService.crossWarFinishClear(player);
            }
        }
    }

    @FunctionalInterface
    private static interface IDoSome {
        void doSomething(String[] words, long roleId) throws MwException;
    }
}
