package com.compass.vinyl.interceptor;

import com.compass.vinyl.Data;
import com.compass.vinyl.Scenario;
import com.compass.vinyl.Vinyl;
import okhttp3.*;
import okio.Buffer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

public class VinylInterceptor {

    public static class OkHttpInterceptor implements Interceptor {

        private static final boolean HEAD_PEEK = true ;

        public static String checkSum(String string)  {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] thedigest = md.digest(string.getBytes());
                return DatatypeConverter.printHexBinary(thedigest);
            } catch (NoSuchAlgorithmException e){
                return "0";
            }
        }

        private static Response peekAndGet( Scenario recordedScenario, Chain chain ) throws IOException {
            Request original = chain.request();
            String responseJSON = (String) recordedScenario.getOutput().getValue();
            Response cachedResponse = new Response.Builder()
                    .request(original)
                    .protocol(Protocol.HTTP_1_1)
                    .code(201)
                    .message("")
                    .body(ResponseBody.create(responseJSON, okhttp3.MediaType.parse(MEDIA_TYPE_JSON)))
                    .build();
            if ( HEAD_PEEK ){
                // create another one from the original
                Request headRequest = original.newBuilder().head().build();
                Response headResponse = chain.proceed(headRequest);
                String checkSum = headResponse.header( COMPASS_HEAD_CHECKSUM , "");
                if ( checkSum.isEmpty() ){
                    return cachedResponse;
                }
                String cachedCheckSum  = checkSum(responseJSON);
                //  obvious... yes?
                if ( cachedCheckSum.equals( checkSum ) ){
                    return cachedResponse;
                }
                // now, here, we go for the actual call
                Response response = chain.proceed(original);
                return response;
            }
            return cachedResponse;
        }

        private static final String MEDIA_TYPE_JSON = "application/json; charset=utf-8";

        private static final String  REQUEST = "requestBody";

        private static final String  HEADER = "header";

        private static final String  RESPONSE = "response";

        private final long THRESHOLD_5MB = 5 * 1024 * 1024L;

        private static final String COMPASS_RESPONSE_FILTER = "X-Compass-Response-Filter";

        private static final String COMPASS_HEAD_CHECKSUM = "X-Compass-CHECKSUM";

        private static final Logger LOG = LoggerFactory.getLogger(OkHttpInterceptor.class);

        private final Vinyl vinyl;

        private long recordLengthThreshold = THRESHOLD_5MB;

        public OkHttpInterceptor(Vinyl vinyl) {
            this.vinyl = vinyl;
        }

        @NotNull
        @Override
        public Response intercept(@NotNull Chain chain) throws IOException {

            Request request = chain.request();
            String url = request.url().url().getFile();
            String method = request.method();
            Data input = new Data(REQUEST, extractBody(request));

            String headerData = request.header(COMPASS_RESPONSE_FILTER);
            Data header = new Data(HEADER, (headerData != null) ? headerData : "");

            List<Data> inputs = new ArrayList<>();
            inputs.add(header);
            inputs.add(input);
            Scenario inputScenario = new Scenario(url, method, inputs);
            Scenario recordedScenario = vinyl.playback(inputScenario);

            Response response;
            if (recordedScenario == null) {
                response = chain.proceed(request);

                String responseJSON = response.peekBody(recordLengthThreshold).string();
                Data output = new Data(RESPONSE, responseJSON);

                vinyl.record(new Scenario(url, method, inputs, output));
            }
            else {
                response = peekAndGet(recordedScenario,chain);
            }
            return response;
        }

        private String extractBody(Request request) {
            try {
                Request copy = request.newBuilder().build();
                Buffer buffer = new Buffer();
                String content = "";
                if (copy.body() != null) {
                    copy.body().writeTo(buffer);
                    content = buffer.readUtf8();
                }
                return content;
            } catch (IOException e) {
                LOG.error("Error occurred while extracting request body.", e);
                throw new RuntimeException("Error occurred while extracting request body");
            }
        }

        public void setRecordLengthThreshold(long recordLengthThreshold) {
            this.recordLengthThreshold = recordLengthThreshold;
        }
    }
}