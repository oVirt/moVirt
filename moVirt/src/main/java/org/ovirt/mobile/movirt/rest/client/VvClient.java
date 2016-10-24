package org.ovirt.mobile.movirt.rest.client;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.rest.spring.annotations.RestService;
import org.androidannotations.rest.spring.api.RestClientHeaders;
import org.androidannotations.rest.spring.api.RestClientRootUrl;
import org.androidannotations.rest.spring.api.RestClientSupport;
import org.ovirt.mobile.movirt.rest.OvirtSimpleClientHttpRequestFactory;
import org.ovirt.mobile.movirt.rest.Request;
import org.ovirt.mobile.movirt.rest.RequestHandler;
import org.ovirt.mobile.movirt.rest.Response;
import org.ovirt.mobile.movirt.rest.VvFileHttpMessageConverter;
import org.ovirt.mobile.movirt.rest.dto.ConsoleConnectionDetails;
import org.ovirt.mobile.movirt.util.Version;
import org.ovirt.mobile.movirt.util.properties.AccountPropertiesManager;
import org.ovirt.mobile.movirt.util.properties.AccountProperty;
import org.ovirt.mobile.movirt.util.properties.PropertyChangedListener;

import static org.ovirt.mobile.movirt.rest.RestHelper.initClient;
import static org.ovirt.mobile.movirt.rest.RestHelper.setAcceptHeader;
import static org.ovirt.mobile.movirt.rest.RestHelper.setupVersionHeader;

@EBean(scope = EBean.Scope.Singleton)
public class VvClient {
    private static final String TAG = VvClient.class.getSimpleName();

    @RestService
    OVirtVvRestClient vvRestClient;

    @Bean
    AccountPropertiesManager accountPropertiesManager;

    @Bean
    RequestHandler requestHandler;

    @Bean
    OvirtSimpleClientHttpRequestFactory requestFactory;

    private final PropertyChangedListener<Version> versionChangedListener = new PropertyChangedListener<Version>() {
        @Override
        public void onPropertyChange(Version property) {
            setupVersionHeader(vvRestClient, property);
        }
    };

    @AfterInject
    public void init() {
        initClient(vvRestClient, requestFactory);
        setAcceptHeader(vvRestClient, VvFileHttpMessageConverter.X_VIRT_VIEWER_MEDIA_TYPE);

        accountPropertiesManager.notifyListener(AccountProperty.VERSION, versionChangedListener);
        accountPropertiesManager.registerListener(AccountProperty.VERSION, versionChangedListener);
    }

    public void getConsoleConnectionDetails(final String vmId, final String consoleId, Response<ConsoleConnectionDetails> response) {
        requestHandler.fireRestRequest(new VVRestClientRequest<ConsoleConnectionDetails>() {
            @Override
            public ConsoleConnectionDetails fire() {
                return vvRestClient.getConsoleFile(vmId, consoleId);
            }
        }, response);
    }

    private abstract class VVRestClientRequest<T> implements Request<T> {
        @Override
        @SuppressWarnings("unchecked")
        public <U extends RestClientRootUrl & RestClientHeaders & RestClientSupport> U getRestClient() {
            return (U) vvRestClient;
        }
    }
}
