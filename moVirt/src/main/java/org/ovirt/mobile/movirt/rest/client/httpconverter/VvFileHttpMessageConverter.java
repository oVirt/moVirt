package org.ovirt.mobile.movirt.rest.client.httpconverter;

import org.ovirt.mobile.movirt.model.enums.ConsoleProtocol;
import org.ovirt.mobile.movirt.rest.ParseUtils;
import org.ovirt.mobile.movirt.rest.dto.ConsoleConnectionDetails;
import org.ovirt.mobile.movirt.util.ObjectUtils;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VvFileHttpMessageConverter extends AbstractHttpMessageConverter<Object> {
    private static final String TAG = VvFileHttpMessageConverter.class.getSimpleName();

    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
    public static final String X_VIRT_VIEWER_MEDIA_TYPE = "application/x-virt-viewer";
    public static final String VIRT_VIEWER_DEFINITION = "virt-viewer";

    public VvFileHttpMessageConverter() {
        super(new MediaType("application", "x-virt-viewer", DEFAULT_CHARSET));
    }

    @Override
    protected Object readInternal(Class<? extends Object> clazz,
                                  HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        Object result = null;
        InputStream inputStream = inputMessage.getBody();
        try {
            result = convertStreamToConsoleConnectionDetails(inputStream);
        } catch (Exception x) {
            throw new IllegalStateException("Couldn't parse .vv file response", x);
        } finally {
            ObjectUtils.closeSilently(inputStream);
        }

        return result;
    }

    @Override
    public boolean canRead(Class<?> clazz, MediaType mediaType) {
        return super.canRead(clazz, mediaType);
    }

    @Override
    protected boolean canRead(MediaType mediaType) {
        return super.canRead(mediaType);
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return false;
    }

    @Override
    protected boolean canWrite(MediaType mediaType) {
        return false;
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return clazz == ConsoleConnectionDetails.class;
    }

    @Override
    protected void writeInternal(Object t,
                                 HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        throw new UnsupportedOperationException();
    }

    private ConsoleConnectionDetails convertStreamToConsoleConnectionDetails(InputStream is) throws IOException {
        Map<String, String> vvFileMap = new HashMap<>();
        Scanner sc = new Scanner(is, DEFAULT_CHARSET.name());
        Pattern pattern = Pattern.compile("^([^#\\[][^=]*)=(.*)$");
        Pattern definitionPattern = Pattern.compile("^\\[([^\\]]*)\\]$");
        boolean virtViewerBlockFound = false;

        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            Matcher definitionMatcher = definitionPattern.matcher(line);
            if (definitionMatcher.matches()) {
                String definition = definitionMatcher.group(1);
                virtViewerBlockFound = definition.equals(VIRT_VIEWER_DEFINITION);
                continue;
            }

            if (!virtViewerBlockFound) {
                continue;
            }

            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                MatchResult result = matcher.toMatchResult();
                vvFileMap.put(result.group(1), result.group(2));
            }
        }

        ConsoleProtocol protocol = ConsoleProtocol.mapProtocol(vvFileMap.get(VvFileParam.TYPE));
        String address = vvFileMap.get(VvFileParam.HOST);
        String password = vvFileMap.get(VvFileParam.PASSWORD);
        String subject = vvFileMap.get(VvFileParam.HOST_SUBJECT);
        String unescapedCa = vvFileMap.get(VvFileParam.CA);
        String certificate = unescapedCa == null ? "" : unescapedCa.replace("\\n", "\n");
        int port = ParseUtils.intOrDefault(vvFileMap.get(VvFileParam.PORT));
        int tlsPort = ParseUtils.intOrDefault(vvFileMap.get(VvFileParam.TLS_PORT));

        return new ConsoleConnectionDetails(protocol, address, port, tlsPort, password, subject, certificate);
    }

    interface VvFileParam {
        String TYPE = "type";
        String HOST = "host";
        String PORT = "port";
        String TLS_PORT = "tls-port";
        String PASSWORD = "password";
        String HOST_SUBJECT = "host-subject";
        String CA = "ca";
    }
}

