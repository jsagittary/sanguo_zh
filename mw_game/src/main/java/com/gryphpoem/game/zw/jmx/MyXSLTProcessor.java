package com.gryphpoem.game.zw.jmx;

import mx4j.tools.adaptor.http.XSLTProcessor;

import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource(objectName = "A_Social:name=MyXSLTProcessor", description = "processor")
public class MyXSLTProcessor extends XSLTProcessor{

}
