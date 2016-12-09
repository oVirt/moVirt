package org.ovirt.mobile.movirt.rest.client;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.rest.spring.annotations.RestService;
import org.androidannotations.rest.spring.api.RestClientHeaders;
import org.androidannotations.rest.spring.api.RestClientRootUrl;
import org.androidannotations.rest.spring.api.RestClientSupport;
import org.ovirt.mobile.movirt.auth.properties.manager.AccountPropertiesManager;
import org.ovirt.mobile.movirt.auth.properties.AccountProperty;
import org.ovirt.mobile.movirt.auth.properties.property.Version;
import org.ovirt.mobile.movirt.rest.OvirtSimpleClientHttpRequestFactory;
import org.ovirt.mobile.movirt.rest.Request;
import org.ovirt.mobile.movirt.rest.RequestHandler;
import org.ovirt.mobile.movirt.rest.Response;
import org.ovirt.mobile.movirt.rest.VvFileHttpMessageConverter;
import org.ovirt.mobile.movirt.rest.dto.ConsoleConnectionDetails;

import static org.ovirt.mobile.movirt.rest.RestHelper.setAcceptEncodingHeaderAndFactory;
import static org.ovirt.mobile.movirt.rest.RestHelper.setAcceptHeader;
import static org.ovirt.mobile.movirt.rest.RestHelper.setFilterHeader;
import static org.ovirt.mobile.movirt.rest.RestHelper.setVersionHeader;
import static org.ovirt.mobile.movirt.rest.RestHelper.setupAuth;

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

    @AfterInject
    public void init() {
        setAcceptEncodingHeaderAndFactory(vvRestClient, requestFactory);
        setAcceptHeader(vvRestClient, VvFileHttpMessageConverter.X_VIRT_VIEWER_MEDIA_TYPE);

        accountPropertiesManager.notifyAndRegisterListener(new AccountProperty.VersionListener() {
            @Override
            public void onPropertyChange(Version version) {
                setVersionHeader(vvRestClient, version);
                setupAuth(vvRestClient, version);
            }
        });

        accountPropertiesManager.notifyAndRegisterListener(new AccountProperty.ApiUrlListener() {
            @Override
            public void onPropertyChange(String apiUrl) {
                vvRestClient.setRootUrl(apiUrl);
            }
        });

        accountPropertiesManager.notifyAndRegisterListener(new AccountProperty.HasAdminPermissionsListener() {
            @Override
            public void onPropertyChange(Boolean hasAdminPermissions) {
                setFilterHeader(vvRestClient, hasAdminPermissions);
            }
        });
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
