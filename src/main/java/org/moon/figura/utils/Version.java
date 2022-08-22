package org.moon.figura.utils;

import org.moon.figura.FiguraMod;

public class Version implements Comparable<Version> {

    public static final Version VERSION = new Version(FiguraMod.VERSION);

    public final int major, minor, patch;
    private final String pre, build;

    private Version(String version) throws IllegalArgumentException {
        String build = "";
        String pre = "";

        String[] buildSplit = version.split("\\+", 2);
        if (buildSplit.length > 1)
            build = buildSplit[1];

        String[] preSplit = buildSplit[0].split("-", 2);
        if (preSplit.length > 1)
            pre = preSplit[1];

        String[] ver = preSplit[0].split("\\.");

        int len = ver.length;
        if (len > 3 || len < 1)
            throw new IllegalArgumentException("Cannot parse version " + "\"" + version + "\"");

        try {
            this.major = Integer.parseInt(ver[0]);
            this.minor = len > 1 ? Integer.parseInt(ver[1]) : 0;
            this.patch = len > 2 ? Integer.parseInt(ver[2]) : 0;
            this.pre = pre;
            this.build = build;
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot parse version " + "\"" + version + "\"");
        }
    }

    public static Version of(Object version) throws IllegalArgumentException {
        return version instanceof Version v ? v : new Version(version.toString());
    }

    @Override
    public int compareTo(Version o) {
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

    @Override
    public String toString() {
        String ver = major + "." + minor + "." + patch;
        if (!pre.isBlank())
            ver += "-" + pre;
        if (!build.isBlank())
            ver += "+" + build;
        return ver;
    }
}
