package org.figuramc.figura.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version implements Comparable<Version> {

    // slightly modified regex of semver
    // difference only is that build metadata can be anything
    // also minor and patch are optionals
    private static final Pattern PATTERN = Pattern.compile(
        "^"+
        // #.#.#, with the minor and patch versions being optional.
        "(?<major>0|[1-9]\\d*)(?:\\.(?<minor>0|[1-9]\\d*)(?:\\.(?<patch>0|[1-9]\\d*))?)?"+
        // from the -, grab all characters until the +.
        "(?:-(?<pre>(?:0|[1-9]\\d*|\\d*[a-zA-Z-][\\da-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][\\da-zA-Z-]*))*))?"+
        // from the +, grab all characters until the end of the string.
        "(?:\\+(?<build>[\\da-zA-Z-]+(?:\\.[\\da-zA-Z-]+)*))?"+
        "$"
    );

    private final String src;

    public final int major, minor, patch;
    public final String pre, build;
    public final boolean invalid;

    public Version(String version) {
        // temp vars
        int major = 0, minor = 0, patch = 0;
        String pre = null, build = null;
        boolean invalid = true;

        // try parse version
        try {
            Matcher matcher = PATTERN.matcher(version);
            if (matcher.matches()) {
                major = Integer.parseInt(matcher.group("major"));

                String m = matcher.group("minor");
                if (m != null)
                    minor = Integer.parseInt(m);

                String p = matcher.group("patch");
                if (p != null)
                    patch = Integer.parseInt(p);

                pre = matcher.group("pre");
                build = matcher.group("build");

                invalid = false;
            }
        } catch (Exception ignored) {}

        // store vars
        this.src = version;

        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.pre = pre == null ? "" : pre;
        this.build = build == null ? "" : build;
        this.invalid = invalid;
    }

    @Override
    public int compareTo(Version o) {
        if (this.invalid || o.invalid)
            return 0;

        int ret;

        if (major != o.major)
            ret = major - o.major;
        else if (minor != o.minor)
            ret = minor - o.minor;
        else if (patch != o.patch)
            ret = patch - o.patch;

        else if (pre.isBlank() && !o.pre.isBlank())
            ret = 1;
        else if (!pre.isBlank() && o.pre.isBlank())
            ret = -1;

        else {
            String[] split1 = pre.split("\\.");
            String[] split2 = o.pre.split("\\.");
            ret = 0;

            for (int i = 0; i < split1.length; i++) {
                if (i >= split2.length) {
                    ret = 1;
                    break;
                }

                String s1 = split1[i];
                String s2 = split2[i];

                try {
                    int i1 = Integer.parseInt(s1);
                    int i2 = Integer.parseInt(s2);
                    ret = i1 - i2;
                } catch (Exception ignored) {
                    ret = s1.compareTo(s2);
                }

                if (ret != 0)
                    break;
            }

            if (split1.length < split2.length)
                ret = -1;
        }

        return (int) Math.signum(ret);
    }

    public String noBuildString() {
        if (invalid)
            return src;

        String ver = major + "." + minor + "." + patch;
        if (!pre.isBlank())
            ver += "-" + pre;

        return ver;
    }

    @Override
    public String toString() {
        if (invalid)
            return src;

        String ver = major + "." + minor + "." + patch;
        if (!pre.isBlank())
            ver += "-" + pre;
        if (!build.isBlank())
            ver += "+" + build;
        return ver;
    }
}
