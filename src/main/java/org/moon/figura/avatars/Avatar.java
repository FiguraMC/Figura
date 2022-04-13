package org.moon.figura.avatars;

//the avatar class
//contains all things related to the avatar
//and also related to the owner, like trust settings
public class Avatar {

    private AvatarMetadata metadata;

    public record AvatarMetadata(String name, String author, String version) {}
}
