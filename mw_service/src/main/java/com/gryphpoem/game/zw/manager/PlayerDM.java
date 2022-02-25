package com.gryphpoem.game.zw.manager;

import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Account;

public interface PlayerDM {
	/**
	 * 
	 * Method: loadAllPlayer
	 * 
	 * @Description: 加载全部玩家数据
	 * @return void
	 * @throws
	 */
	void loadAllPlayer();

	/**
	 * 
	 * Method: getPlayer
	 * 
	 * @Description: 根据roleId获取Player数据
	 * @param roleId
	 * @return
	 * @return Player
	 * @throws
	 */
	Player getPlayer(Long roleId);

	/**
	 * 
	 * Method: createPlayer
	 * 
	 * @Description: 创建玩家数据
	 * @param account
	 * @return
	 * @return Player
	 * @throws
	 */
	Player createPlayer(Account account);
}
