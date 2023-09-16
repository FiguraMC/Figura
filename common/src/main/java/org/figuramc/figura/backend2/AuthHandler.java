package org.figuramc.figura.backend2;

import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.InsufficientPrivilegesException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import com.mojang.authlib.exceptions.UserBannedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.network.chat.Component;
import org.figuramc.figura.FiguraMod;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
                minecraft.getMinecraftSessionService().joinServer(user.getProfileId(), user.getAccessToken(), serverID);
                NetworkStuff.authSuccess(getToken(serverID));
            // cringe exceptions
            } catch (AuthenticationUnavailableException e) {
                NetworkStuff.authFail(Component.translatable("disconnect.loginFailedInfo.serversUnavailable").getString());
            } catch (InvalidCredentialsException e) {
                NetworkStuff.authFail(Component.translatable("disconnect.loginFailedInfo.invalidSession").getString());
            } catch (InsufficientPrivilegesException e) {
                NetworkStuff.authFail(Component.translatable("disconnect.loginFailedInfo.insufficientPrivileges").getString());
            } catch (UserBannedException e) {
                NetworkStuff.authFail(Component.translatable("disconnect.loginFailedInfo.userBanned").getString());
            } catch (Exception e) {
                NetworkStuff.authFail(e.getMessage());
            }
        });
    }

    // requests // 

    protected static String request(HttpRequest request) throws Exception {
        HttpResponse<String> response = NetworkStuff.client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() != 200)
            throw new Exception(response.body());
        return response.body();
    }

    private static String getServerID(String username) throws Exception {
        return request(HttpRequest.newBuilder(HttpAPI.getUri("/auth/id?username=" + username)).build());
    }

    private static String getToken(String serverID) throws Exception {
        return request(HttpRequest.newBuilder(HttpAPI.getUri("/auth/verify?id=" + serverID)).build());
    }
}
