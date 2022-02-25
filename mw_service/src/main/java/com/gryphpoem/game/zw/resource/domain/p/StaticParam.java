package com.gryphpoem.game.zw.resource.domain.p;

public class StaticParam {
    private int paramId;
    private String title;
    private String paramName;
    private String paramValue;
    private String descs;

    public int getParamId() {
        return paramId;
    }

    public void setParamId(int paramId) {
        this.paramId = paramId;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescs() {
        return descs;
    }

    public void setDescs(String descs) {
        this.descs = descs;
    }

    @Override
    public String toString() {
        return "StaticParam [paramId=" + paramId + ", title=" + title + ", paramName=" + paramName + ", paramValue="
                + paramValue + ", descs=" + descs + "]";
    }

}
