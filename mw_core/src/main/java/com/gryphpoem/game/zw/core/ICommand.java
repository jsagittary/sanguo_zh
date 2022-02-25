package com.gryphpoem.game.zw.core;


@FunctionalInterface
public interface ICommand {

	void action() throws Exception;
}
