package com.gryphpoem.game.zw.core.timer;

import com.gryphpoem.game.zw.core.ICommand;

public interface ITimerEvent extends ICommand {
	long remain();
}
