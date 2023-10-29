package org.figuramc.figura.lua.api.net;

import net.minecraft.network.chat.Component;
import org.figuramc.figura.lua.api.data.FiguraBuffer;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.ReadOnlyLuaTable;
import org.figuramc.figura.lua.api.data.FiguraFuture;
import org.figuramc.figura.lua.api.data.FiguraInputStream;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaTypeDoc;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@LuaWhitelist
@LuaTypeDoc(
        name = "HttpAPI",
        value = "http"
)
public class HttpRequestsAPI {
    private final NetworkingAPI parent;
    private final HttpClient httpClient;
    HttpRequestsAPI(NetworkingAPI parent) {
        this.parent = parent;
        httpClient = HttpClient.newBuilder().build();
    }

    @LuaWhitelist
    @LuaMethodDoc("http.request")
    public HttpRequestBuilder<?, ?> request(@LuaNotNil String uri) {
        return new HttpRequestBuilder<>(this, uri);
    }

    @LuaWhitelist
    @LuaTypeDoc(
            name = "HttpResponse",
            value = "http_response"
    )
    public static class HttpResponse <T> {
        private final T data;
        private final int responseCode;
        private final ReadOnlyLuaTable headersTable;
        public HttpResponse(T data, int responseCode, Map<String, List<String>> headers) {
            this.data = data;
            this.responseCode = responseCode;
            LuaTable headersTable = new LuaTable();
            for (Map.Entry<String, List<String>> entry :
                    headers.entrySet()) {
                LuaTable headerTable = new LuaTable();
                for (String headerVal :
                        entry.getValue()) {
                    headerTable.set(headerTable.length() + 1, LuaValue.valueOf(headerVal));
                }
                headersTable.set(entry.getKey(), new ReadOnlyLuaTable(headerTable));
            }
            this.headersTable = new ReadOnlyLuaTable(headersTable);
        }

        @LuaWhitelist
        @LuaMethodDoc("http_response.get_data")
        public Object getData() {
            return data;
        }

        @LuaWhitelist
        @LuaMethodDoc("http_response.get_response_code")
        public int getResponseCode() {
            return responseCode;
        }

        @LuaWhitelist
        @LuaMethodDoc("http_response.get_headers")
        public ReadOnlyLuaTable getHeaders() {
            return headersTable;
        }
    }

    @LuaWhitelist
    @LuaTypeDoc(
            name = "HttpRequestBuilder",
            value = "http_request_builder"
    )
    public static class HttpRequestBuilder <R, P> {
        private final HttpRequestsAPI parent;
        private String uri;
        private String method = "GET";
        private Object data;
        private final HashMap<String, String> headers = new HashMap<>();

        public HttpRequestBuilder(HttpRequestsAPI parent, String uri) {
            this.parent = parent;
            this.uri = uri;
        }


        @LuaWhitelist
        @LuaMethodDoc("http_request_builder.uri")
        public HttpRequestBuilder<R, P> uri(@LuaNotNil String uri) {
            this.uri = uri;
            return this;
        }

        @LuaWhitelist
        @LuaMethodDoc("http_request_builder.method")
        public HttpRequestBuilder<R, P> method(String method) {
            this.method = Objects.requireNonNullElse(method, "GET");
            return this;
        }

        @LuaWhitelist
        @LuaMethodDoc("http_request_builder.body")
        public HttpRequestBuilder<R, P> body(Object data) {
            if (data == null || data instanceof InputStream || data instanceof FiguraBuffer) {
                this.data = data;
            }
            else {
                throw new LuaError("Invalid request body type");
            }
            return this;
        }

        @LuaWhitelist
        @LuaMethodDoc("http_request_builder.header")
        public HttpRequestBuilder<R, P> header(@LuaNotNil String header, String value) {
            if (value == null) {
                headers.remove(header);
            } else {
                headers.put(header, value);
            }
            return this;
        }

        @LuaWhitelist
        @LuaMethodDoc("http_request_builder.get_uri")
        public String getUri() {
            return uri;
        }

        @LuaWhitelist
        @LuaMethodDoc("http_request_builder.get_method")
        public String getMethod() {
            return method;
        }


        @LuaWhitelist
        @LuaMethodDoc("http_request_builder.get_data")
        public Object getBody() {
            return data;
        }


        @LuaWhitelist
        @LuaMethodDoc("http_request_builder.get_headers")
        public HashMap<String, String> getHeaders() {
            return headers;
        }

        private InputStream inputStreamSupplier() {
            return data instanceof InputStream  is ? is : ((FiguraBuffer) data).asInputStream();
        }

        private HttpRequest getRequest() {
            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(uri));
            HttpRequest.BodyPublisher bp = data != null ?
                    HttpRequest.BodyPublishers.ofInputStream(this::inputStreamSupplier) : HttpRequest.BodyPublishers.noBody();
            for (Map.Entry<String, String> entry :
                    getHeaders().entrySet()) {
                builder.header(entry.getKey(), entry.getValue());
            }
            builder.method(getMethod(), bp);
            return builder.build();
        }

        @LuaWhitelist
        @LuaMethodDoc("http_request_builder.send")
        public FiguraFuture<HttpResponse<FiguraInputStream>> send() {
            String uri = this.getUri();
            try {
                parent.parent.securityCheck(uri);
            } catch (NetworkingAPI.LinkNotAllowedException e) {
                parent.parent.error(NetworkingAPI.LogSource.HTTP, Component.literal("Tried to send %s request to not allowed link %s".formatted(method, uri)));
                throw e;
            }
            parent.parent.log(NetworkingAPI.LogSource.HTTP, Component.literal("Sent %s request to %s".formatted(method, uri)));
            HttpRequest req = this.getRequest();
            FiguraFuture<HttpResponse<FiguraInputStream>> future = new FiguraFuture<>();
            var asyncResponse = parent.httpClient.sendAsync(req, java.net.http.HttpResponse.BodyHandlers.ofInputStream());
            asyncResponse.thenAcceptAsync((response) -> future.complete(new HttpResponse<>(new FiguraInputStream(response.body()),
                    response.statusCode(), response.headers().map())));
            return future;
        }
    }

}