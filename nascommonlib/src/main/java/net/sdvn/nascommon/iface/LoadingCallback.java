package net.sdvn.nascommon.iface;

public abstract class LoadingCallback implements ILoadingCallback{
    public void showLoading() {
    }

    public void showLoading(int msgId) {
    }

    public void showLoading(int msgId, boolean isCancellable) {
    }

    public void dismissLoading() {
    }

    public void showTipView(int msgId, boolean isPositive) {
    }

    public void showTipView(String msg, boolean isPositive) {
    }
}