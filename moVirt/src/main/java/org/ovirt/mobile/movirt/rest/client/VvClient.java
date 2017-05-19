package org.ovirt.mobile.movirt.rest.client;

import org.androidannotations.annotations.EBean;
import org.androidannotations.rest.spring.annotations.RestService;
import org.androidannotations.rest.spring.api.RestClientHeaders;
import org.androidannotations.rest.spring.api.RestClientRootUrl;
import org.androidannotations.rest.spring.api.RestClientSupport;
import org.ovirt.mobile.movirt.auth.account.AccountEnvironment;
import org.ovirt.mobile.movirt.auth.properties.AccountProperty;
import org.ovirt.mobile.movirt.auth.properties.manager.AccountPropertiesManager;
import org.ovirt.mobile.movirt.auth.properties.property.version.Version;
import org.ovirt.mobile.movirt.rest.Request;
import org.ovirt.mobile.movirt.rest.RequestHandler;
import org.ovirt.mobile.movirt.rest.Response;
import org.ovirt.mobile.movirt.rest.client.httpconverter.VvFileHttpMessageConverter;
import org.ovirt.mobile.movirt.rest.client.requestfactory.OvirtSimpleClientHttpRequestFactory;
import org.ovirt.mobile.movirt.rest.dto.ConsoleConnectionDetails;
import org.ovirt.mobile.movirt.util.DestroyableListeners;
import org.ovirt.mobile.movirt.util.IdHelper;
import org.ovirt.mobile.movirt.util.ObjectUtils;

import static org.ovirt.mobile.movirt.rest.RestHelper.setAcceptEncodingHeaderAndFactory;
import static org.ovirt.mobile.movirt.rest.RestHelper.setAcceptHeader;
import static org.ovirt.mobile.movirt.rest.RestHelper.setFilterHeader;
import static org.ovirt.mobile.movirt.rest.RestHelper.setVersionHeader;
import static org.ovirt.mobile.movirt.rest.RestHelper.setupAuth;

@EBean
public class VvClient implements AccountEnvironment.EnvDisposable {
    private RequestHandler requestHandler;
    private DestroyableListeners listeners;

    @RestService
    OVirtVvRestClient vvRestClient;

    public VvClient init(AccountPropertiesManager propertiesManager, OvirtSimpleClientHttpRequestFactory requestFactory,
                         RequestHandler requestHandler) {
        ObjectUtils.requireAllNotNull(propertiesManager, requestFactory, requestHandler);

        this.requestHandler = requestHandler;

        setAcceptEncodingHeaderAndFactory(vvRestClient, requestFactory);
        setAcceptHeader(vvRestClient, VvFileHttpMessageConverter.X_VIRT_VIEWER_MEDIA_TYPE);

        listeners = new DestroyableListeners(propertiesManager)
                .notifyAndRegisterListener(new AccountProperty.VersionListener() {
                    @Override
                    public void onPropertyChange(Version newVersion) {
                        setVersionHeader(vvRestClient, newVersion);
                        setupAuth(vvRestClient, newVersion);
                    }
                }).notifyAndRegisterListener(new AccountProperty.ApiUrlListener() {
                    @Override
                    public void onPropertyChange(String apiUrl) {
                        vvRestClient.setRootUrl(apiUrl);
                    }
                }).notifyAndRegisterListener(new AccountProperty.HasAdminPermissionsListener() {
                    @Override
                    public void onPropertyChange(Boolean hasAdminPermissions) {
                        setFilterHeader(vvRestClient, hasAdminPermissions);
                    }
                });

        return this;
    }

    @Override
    public void dispose() {
        listeners.destroy();
    }

    public void getConsoleConnectionDetails(final String vmId, final String consoleId, Response<ConsoleConnectionDetails> response) {
        requestHandler.fireRestRequestSafe(new VVRestClientRequest<ConsoleConnectionDetails>() {
            @Override
            public ConsoleConnectionDetails fire() {
                return vvRestClient.getConsoleFile(IdHelper.getIdPart(vmId), IdHelper.getIdPart(consoleId));
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
