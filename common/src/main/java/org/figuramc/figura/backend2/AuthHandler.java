package org.figuramc.figura.backend2;

import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.InsufficientPrivilegesException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.util.EntityUtils;
import org.figuramc.figura.FiguraMod;

import java.net.URI;
import java.nio.charset.StandardCharsets;

public class AuthHandler {

    public static void auth(boolean reAuth) {
        NetworkStuff.async(() -> {
            if (!reAuth && NetworkStuff.isConnected())
                return;

            FiguraMod.LOGGER.info("Authenticating with " + FiguraMod.MOD_NAME + " server...");
            NetworkStuff.backendStatus = 2;

            Minecraft minecraft = Minecraft.getInstance();
            User user = minecraft.getUser();
            try {
                String username = user.getName();
                String serverID = getServerID(username);
                FiguraMod.debug("Joining \"{}\" on server \"{}\"", username, serverID);
                minecraft.getMinecraftSessionService().joinServer(user.getGameProfile(), user.getAccessToken(), serverID);
                NetworkStuff.authSuccess(getToken(serverID));
            // cringe exceptions
            } catch (AuthenticationUnavailableException e) {
                NetworkStuff.authFail(new TranslatableComponent("disconnect.loginFailedInfo.serversUnavailable").getString());
            } catch (InvalidCredentialsException e) {
                NetworkStuff.authFail(new TranslatableComponent("disconnect.loginFailedInfo.invalidSession").getString());
            } catch (InsufficientPrivilegesException e) {
                NetworkStuff.authFail(new TranslatableComponent("disconnect.loginFailedInfo.insufficientPrivileges").getString());
            } catch (Exception e) {
                NetworkStuff.authFail(e.getMessage());
            }
        });
    }

    // requests // 

    protected static String request(HttpUriRequest request) throws Exception {
        HttpResponse response = NetworkStuff.client.execute(request);
        if (response.getStatusLine().getStatusCode() != 200)
            throw new Exception(EntityUtils.toString(response.getEntity()));
        return EntityUtils.toString(response.getEntity());
    }

    private static String getServerID(String username) throws Exception {
        RequestBuilder requestBuilder = RequestBuilder.get()
                .setUri(HttpAPI.getUri("/auth/id?username=" + username));
        return request(requestBuilder.build());
    }

    private static String getToken(String serverID) throws Exception {
        RequestBuilder requestBuilder = RequestBuilder.get()
                .setUri(HttpAPI.getUri("/auth/verify?id=" + serverID));
        return request(requestBuilder.build());
    }
}
