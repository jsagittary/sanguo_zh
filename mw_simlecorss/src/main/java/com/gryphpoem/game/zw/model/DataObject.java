package com.gryphpoem.game.zw.model;

public class DataObject {
    // 状态
    private short op;

    public synchronized void setOp(short option) {
        if (op != Option.INSERT) {
            op = option;
        }
    }

    public synchronized int getOp() {
        return op;
    }

    public synchronized boolean beginAdd() {
        if (getOp() != Option.INSERT) {
            return false;
        }
        op = Option.NONE;
        return true;
    }

    public synchronized void commitAdd(boolean result) {
        if (!result) {
            op = Option.INSERT;
        }
    }

    public synchronized boolean beginUpdate() {
        if (getOp() != Option.UPDATE) {
            return false;
        }
        op = Option.NONE;
        return true;
    }

    public synchronized void commitUpdate(boolean result) {
        if (!result) {
            op = Option.UPDATE;
        }
    }

    public static interface Option {

        public static final short NONE = 0;
        public static final short INSERT = 1;
        public static final short UPDATE = 2;
        public static final short DELETE = 3;

    }

}
