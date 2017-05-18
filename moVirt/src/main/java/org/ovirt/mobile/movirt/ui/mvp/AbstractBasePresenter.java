package org.ovirt.mobile.movirt.ui.mvp;

import org.ovirt.mobile.movirt.util.ObjectUtils;

import java.lang.ref.WeakReference;

public abstract class AbstractBasePresenter<Presenter extends AbstractBasePresenter, View extends BaseView> implements BasePresenter {

    private WeakReference<View> view;

    @Override
    @SuppressWarnings("unchecked")
    public Presenter initialize() {
        ObjectUtils.requireNotNull(view, "view");
        return (Presenter) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Presenter setView(BaseView view) {
        this.view = new WeakReference<>((View) view);
        return (Presenter) this;
    }

    protected View getView() {
        return view.get();
    }
}
