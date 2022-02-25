package com.gryphpoem.game.zw.core.handler;

public abstract class HttpHandler extends  AbsHttpHandler{
	public DealType dealType() {
		return DealType.PUBLIC;
	}
}
