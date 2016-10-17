package org.ovirt.mobile.movirt.rest;

import org.androidannotations.rest.spring.api.RestClientHeaders;
import org.androidannotations.rest.spring.api.RestClientRootUrl;
import org.androidannotations.rest.spring.api.RestClientSupport;

public interface Request<T> {
    T fire();

    <U extends RestClientRootUrl & RestClientHeaders & RestClientSupport> U getRestClient();
}
