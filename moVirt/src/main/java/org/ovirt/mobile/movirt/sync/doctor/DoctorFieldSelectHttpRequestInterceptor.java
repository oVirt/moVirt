package org.ovirt.mobile.movirt.sync.doctor;

import android.text.TextUtils;
import android.util.Log;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * Interceptor that wraps HTTP requests to Doctor Rest by adding query parameter that restricts
 * the returned fields to those that will actually be used (mapped to business entities).
 * </p>
 *
 * <p>It determines the required fields by reflecting over {@link com.fasterxml.jackson.annotation.JsonProperty} annotated fields.</p>
 * Please note that current implementation expects the {@link com.fasterxml.jackson.annotation.JsonProperty#value()} to be used
 * even in the simple cases such as
 * <pre>{@code
 *      &#064;JsonProperty("name")
 *      private String name;
 * }</pre>
 */
public class DoctorFieldSelectHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private static final String TAG = DoctorFieldSelectHttpRequestInterceptor.class.getSimpleName();

    private static Map<String, String> cachedFields = new HashMap<>();

    private static final String PREFIX = "/entities/";
    private static final Pattern ENTITY_PATTERN = Pattern.compile(PREFIX + "(\\w+)");

    private static ObjectMapper mapper = new ObjectMapper();

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        if (!request.getMethod().equals(HttpMethod.GET) || !TextUtils.isEmpty(request.getURI().getQuery())) {
            return execution.execute(request, body);
        }

        Matcher matcher = ENTITY_PATTERN.matcher(request.getURI().getPath());
        if (!matcher.matches()) {
            return execution.execute(request, body);
        }

        String entity = matcher.group(1);

        if (!cachedFields.containsKey(entity)) {
            determineUsedJsonProperties(entity);
        }

        final String query = cachedFields.get(entity);
        HttpRequestWrapper wrapper = new HttpRequestWrapper(request) {
            @Override
            public URI getURI() {
                URI uri = super.getURI();
                return withUriQuery(uri, query);
            }
        };
        Log.i(TAG, "Intercepting HTTP GET " + request.getURI().getPath() + " with " + query);

        return execution.execute(wrapper, body);
    }

    private static void determineUsedJsonProperties(String entity) throws JsonProcessingException {
        try {
            Class<?> entityClass = Class.forName(DoctorFieldSelectHttpRequestInterceptor.class.getPackage().getName() + "." + StringUtils.capitalize(entity));
            Field[] fields = entityClass.getDeclaredFields();
            List<String> doctorFields = new ArrayList<>();
            for (Field field : fields) {
                if (field.isAnnotationPresent(JsonProperty.class)) {
                    JsonProperty property = field.getAnnotation(JsonProperty.class);
                    doctorFields.add(property.value());
                }
            }
            String entityQuery = mapper.writeValueAsString(Select.fields(doctorFields.toArray(new String[doctorFields.size()])));
            cachedFields.put(entity, entityQuery);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static URI withUriQuery(URI uri, String query) {
        try {
            return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), "q=" + query, uri.getFragment());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
