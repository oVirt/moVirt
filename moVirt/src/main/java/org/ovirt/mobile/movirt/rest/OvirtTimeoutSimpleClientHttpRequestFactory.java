package org.ovirt.mobile.movirt.rest;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;

@EBean(scope = EBean.Scope.Singleton)
public class OvirtTimeoutSimpleClientHttpRequestFactory extends OvirtSimpleClientHttpRequestFactory {

    private static final String TAG = OvirtTimeoutSimpleClientHttpRequestFactory.class.getSimpleName();

    @AfterInject
    void afterInject() {
        setConnectTimeout(20000); // 20s, but may take much longer (e.g. 2 times) together with the request
    }
}
