package com.compass.vinyl.interceptor;

import com.compass.vinyl.Data;
import com.compass.vinyl.Scenario;
import com.compass.vinyl.Vinyl;
import okhttp3.*;
import okio.Buffer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VinylInterceptor {

    public static class OkHttpInterceptor implements Interceptor {

        private static final String MEDIA_TYPE_JSON = "application/json; charset=utf-8";

        private static final String  REQUEST = "requestBody";

        private static final String  HEADER = "header";

        private static final String  RESPONSE = "response";

        private final long THRESHOLD_5MB = 5 * 1024 * 1024L;

        private static final String COMPASS_RESPONSE_FILTER = "X-Compass-Response-Filter";

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
                String responseJSON = (String) recordedScenario.getOutput().getValue();
                response = new Response.Builder()
                        .request(request)
                        .protocol(Protocol.HTTP_1_1)
                        .code(201)
                        .message("")
                        .body(ResponseBody.create(responseJSON, okhttp3.MediaType.parse(MEDIA_TYPE_JSON)))
                        .build();
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