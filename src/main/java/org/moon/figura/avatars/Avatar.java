package org.moon.figura.avatars;

//the avatar class
//contains things related to a single avatar
public class Avatar {

    private final String name;
    private final String author;
    private final String version;

    public Avatar(String name, String author, String version) {
        this.name = name;
        this.author = author;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public String getVersion() {
        return version;
    }
}
