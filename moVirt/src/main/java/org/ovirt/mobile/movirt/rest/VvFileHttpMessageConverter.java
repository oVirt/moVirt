package org.ovirt.mobile.movirt.rest;

/**
 * Created by suomiy on 10/17/16.
 */

import android.util.Log;

import org.ovirt.mobile.movirt.model.ConsoleProtocol;
import org.ovirt.mobile.movirt.rest.dto.ConsoleConnectionDetails;
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
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "failed to close input stream");
                }
            }
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
        Pattern p = Pattern.compile("^([^#\\[][^=]*)=(.*)$");


        while (sc.hasNextLine()) {
            Matcher matcher = p.matcher(sc.nextLine());
            if (matcher.matches()) {
                MatchResult result = matcher.toMatchResult();
                vvFileMap.put(result.group(1), result.group(2));
            }
        }
        ConsoleProtocol protocol = ConsoleProtocol.mapProtocol(vvFileMap.get(VvFileParam.TYPE));
        String address = vvFileMap.get(VvFileParam.HOST);
        String password = vvFileMap.get(VvFileParam.PASSWORD);
        String subject = vvFileMap.get(VvFileParam.HOST_SUBJECT);
        int port = ParseUtils.intOrDefault(vvFileMap.get(VvFileParam.PORT));
        int tlsPort = ParseUtils.intOrDefault(vvFileMap.get(VvFileParam.TLS_PORT));

        return new ConsoleConnectionDetails(protocol, address, port, tlsPort, subject, password);
    }

    interface VvFileParam {
        String TYPE = "type";
        String HOST = "host";
        String PORT = "port";
        String TLS_PORT = "tls-port";
        String PASSWORD = "password";
        String HOST_SUBJECT = "host-subject";
    }
}

