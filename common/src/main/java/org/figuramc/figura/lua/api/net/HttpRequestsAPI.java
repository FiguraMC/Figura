package org.figuramc.figura.lua.api.net;

import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.figuramc.figura.FiguraMod;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.HttpClients;
import org.figuramc.figura.lua.api.data.FiguraBuffer;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@LuaWhitelist
@LuaTypeDoc(
        name = "HttpAPI",
        value = "http"
)
public class HttpRequestsAPI {
    private final NetworkingAPI parent;
    private final HttpClient httpClient;
    private static final List<String> disallowedHeaders = Arrays.asList("Host", "X-Forwarded-Host", "X-Host");

    HttpRequestsAPI(NetworkingAPI parent) {
        this.parent = parent;
        httpClient = HttpClients.createDefault();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "http.request",
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "uri",
                    returnType = HttpRequestBuilder.class
            )
    )
    public HttpRequestBuilder request(@LuaNotNil String uri) {
        return new HttpRequestBuilder(this, uri);
    }

    @LuaWhitelist
    @LuaTypeDoc(
            name = "HttpResponse",
            value = "http_response"
    )
    public static class HttpResponse {
        private final FiguraInputStream data;
        private final int responseCode;
        private final ReadOnlyLuaTable headersTable;
        public HttpResponse(FiguraInputStream data, int responseCode, Map<String, List<String>> headers) {
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
        @LuaMethodDoc(
                value = "http_response.get_data",
                overloads = @LuaMethodOverload(
                        returnType = FiguraInputStream.class
                )
        )
        public FiguraInputStream getData() {
            return data;
        }

        @LuaWhitelist
        @LuaMethodDoc(
                value = "http_response.get_response_code",
                overloads = @LuaMethodOverload(
                        returnType = int.class
                )
        )
        public int getResponseCode() {
            return responseCode;
        }

        @LuaWhitelist
        @LuaMethodDoc(
                value = "http_response.get_headers",
                overloads = @LuaMethodOverload(
                        returnType = ReadOnlyLuaTable.class
                )
        )
        public ReadOnlyLuaTable getHeaders() {
            return headersTable;
        }

        @Override
        public String toString() {
            return String.format("HttpResponse(%s)", responseCode);
        }
    }
    @LuaWhitelist
    @LuaTypeDoc(
            name = "HttpRequestBuilder",
            value = "http_request_builder"
    )
    public static class HttpRequestBuilder {

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
        @LuaMethodDoc(
                value = "http_request_builder.uri",
                overloads = @LuaMethodOverload(
                        argumentTypes = String.class,
                        argumentNames = "uri",
                        returnType = HttpRequestBuilder.class
                )
        )
        public HttpRequestBuilder uri(@LuaNotNil String uri) {
            this.uri = uri;
            return this;
        }

        @LuaWhitelist
        @LuaMethodDoc(
                value = "http_request_builder.method",
                overloads = @LuaMethodOverload(
                        argumentTypes = String.class,
                        argumentNames = "method",
                        returnType = HttpRequestBuilder.class
                )
        )
        public HttpRequestBuilder method(String method) {
            this.method = (method != null) ? method : "GET";
            return this;
        }

        @LuaWhitelist
        @LuaMethodDoc(
                value = "http_request_builder.body",
                overloads = {
                        @LuaMethodOverload(
                            argumentTypes = FiguraInputStream.class,
                            argumentNames = "data",
                            returnType = HttpRequestBuilder.class
                        ),
                        @LuaMethodOverload(
                                argumentTypes = FiguraBuffer.class,
                                argumentNames = "data",
                                returnType = HttpRequestBuilder.class
                        )
                }
        )
        public HttpRequestBuilder body(Object data) {
            if (data == null || data instanceof InputStream || data instanceof FiguraBuffer) {
                this.data = data;
            }
            else {
                throw new LuaError("Invalid request body type");
            }
            return this;
        }

        @LuaWhitelist
        @LuaMethodDoc(
                value = "http_request_builder.header",
                overloads = @LuaMethodOverload(
                        argumentNames = {"header","value"},
                        argumentTypes = {String.class, String.class},
                        returnType = HttpRequestBuilder.class
                )
        )
        public HttpRequestBuilder header(@LuaNotNil String header, String value) {
            if (value == null) {
                headers.remove(header);
            } else {
                headers.put(header, value);
            }
            return this;
        }

        @LuaWhitelist
        @LuaMethodDoc(
                value = "http_request_builder.get_uri",
                overloads = @LuaMethodOverload(
                        returnType = String.class
                )
        )
        public String getUri() {
            return uri;
        }

        @LuaWhitelist
        @LuaMethodDoc(
                value = "http_request_builder.get_method",
                overloads = @LuaMethodOverload(
                        returnType = String.class
                )
        )
        public String getMethod() {
            return method;
        }


        @LuaWhitelist
        @LuaMethodDoc(
                value = "http_request_builder.get_data",
                overloads = @LuaMethodOverload(
                        returnType = Objects.class
                )
        )
        public Object getBody() {
            return data;
        }


        @LuaWhitelist
        @LuaMethodDoc(
                value = "http_request_builder.get_headers",
                overloads = @LuaMethodOverload(
                        returnType = LuaTable.class
                )
        )
        public HashMap<String, String> getHeaders() {
            return headers;
        }

        private InputStream inputStreamSupplier() {
            return data instanceof InputStream ? (InputStream) data : ((FiguraBuffer) data).asInputStream();
        }

        private HttpUriRequest getRequest() {
            RequestBuilder requestBuilder = RequestBuilder.create(getMethod())
                    .setUri(URI.create(uri));

            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if (disallowedHeaders.stream().anyMatch(s -> s.equalsIgnoreCase(entry.getKey()))) {
                    if (parent.parent.owner.isHost) {
                        FiguraMod.sendChatMessage(new TranslatableComponent("figura.network.header_disabled", entry.getKey()));
                    }
                    continue;
                }
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }

            if (data != null) {
                HttpEntity entity = new InputStreamEntity(this.inputStreamSupplier());
                requestBuilder.setEntity(entity);
            } else {
                requestBuilder.setEntity(null);
            }


            return requestBuilder.build();
        }

        @LuaWhitelist
        @LuaMethodDoc("http_request_builder.send")
        public FiguraFuture<HttpResponse> send() {
            String uri = this.getUri();
            try {
                parent.parent.securityCheck(uri);
            } catch (NetworkingAPI.LinkNotAllowedException e) {
                parent.parent.error(NetworkingAPI.LogSource.HTTP, new TextComponent(String.format("Tried to send %s request to not allowed link %s", method, uri)));
                throw e.luaError;
            }
            parent.parent.log(NetworkingAPI.LogSource.HTTP, new TextComponent(String.format("Sent %s request to %s", method, uri)));
            HttpUriRequest req = this.getRequest();
            FiguraFuture<HttpResponse> future = new FiguraFuture<>();
            CompletableFuture<org.apache.http.HttpResponse> asyncResponse = sendAsyncRequest(req);
            asyncResponse.whenCompleteAsync((response, t) -> {
                if (t != null) future.error(t);
                else {
                    try {
                        future.complete(new HttpResponse(new FiguraInputStream(response.getEntity().getContent()),
                                response.getStatusLine().getStatusCode(), convertHeaders(response.getAllHeaders())));
                    } catch (IOException e) {
                        parent.parent.log(NetworkingAPI.LogSource.HTTP, new TextComponent("Error while trying to read input from stream"));
                    }
                }
            });
            return future;
        }

        @Override
        public String toString() {
            return String.format("HttpRequestBuilder(%s:%s)", method, uri);
        }

        public CompletableFuture<org.apache.http.HttpResponse> sendAsyncRequest(HttpUriRequest request) {
            CompletableFuture<org.apache.http.HttpResponse> future = new CompletableFuture<>();
            CompletableFuture.runAsync(() -> {
                try {
                    parent.httpClient.execute(request, response -> {
                        try {
                            future.complete(response);
                        } catch (Exception e) {
                            future.completeExceptionally(e);
                        }
                        /// ????
                        return null;
                    });
                } catch (IOException e) {
                    future.completeExceptionally(e);
                }
            });

            return future;
        }


        private static Map<String, List<String>> convertHeaders(Header[] headers) {
            Map<String, List<String>> headersMap = new HashMap<>();

            for (Header header : headers) {
                String name = header.getName();
                String value = header.getValue();

                headersMap.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
            }

            return headersMap;
        }

    }

    @Override
    public String toString() {
        return "HttpAPI";
    }
}