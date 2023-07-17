package org.figuramc.figura.lua.api.networking;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaFieldDoc;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.permissions.Permissions;

import java.util.ArrayList;

@LuaWhitelist
@LuaTypeDoc(
        name = "NetworkingAPI",
        value = "net"
)
public class NetworkingAPI {
    private static final String NETWORKING_DISABLED_ERROR_TEXT = "Networking is disabled in config";
    private static final String NO_PERMISSION_ERROR_TEXT = "This avatar doesn't have networking permissions";
    private static final String NETWORKING_DISALLOWED_FOR_LINK_ERROR = "Networking disallowed for link %s";
    private final Avatar owner;
    @LuaWhitelist
    @LuaFieldDoc("net.http")
    public final HttpRequestsAPI http;

    public NetworkingAPI(Avatar owner) {
        this.owner = owner;
        http = new HttpRequestsAPI(this);
    }

    public void securityCheck(String link) {
        if (!Configs.ALLOW_NETWORKING.value)
            throw new LuaError(NETWORKING_DISABLED_ERROR_TEXT);
        if (owner.permissions.get(Permissions.NETWORKING) < 1) {
            owner.noPermissions.add(Permissions.NETWORKING);
            throw new LuaError(NO_PERMISSION_ERROR_TEXT);
        }
        if (!isLinkAllowed(link)) {
            throw new LuaError(NETWORKING_DISALLOWED_FOR_LINK_ERROR.formatted(link));
        }
    }

    @LuaWhitelist
    @LuaMethodDoc("net.is_networking_allowed")
    public boolean isNetworkingAllowed() {
        return Configs.ALLOW_NETWORKING.value && owner.permissions.get(Permissions.NETWORKING) >= 1;
    }

    @LuaWhitelist
    @LuaMethodDoc("net.is_link_allowed")
    public boolean isLinkAllowed(String link) {
        RestrictionLevel level = RestrictionLevel.getById(Configs.NETWORKING_RESTRICTION.value);
        if (level == null) return false;
        ArrayList<Filter> filters = Configs.NETWORK_FILTER.getFilters();
        return switch (level) {
            case WHITELIST -> filters.stream().anyMatch(f -> f.matches(link));
            case BLACKLIST -> filters.stream().noneMatch(f -> f.matches(link));
            case NONE -> true;
        };
    }

    public static class Filter {

        private String filterSource;
        private FilterMode filterMode;

        public Filter(String source, FilterMode mode) {
            setFilterSource(source);
            setFilterMode(mode);
        }

        public String getFilterSource() {
            return filterSource;
        }

        public void setFilterSource(String filterSource) {
            this.filterSource = filterSource;
        }

        public FilterMode getFilterType() {
            return filterMode;
        }

        public void setFilterMode(FilterMode filterMode) {
            this.filterMode = filterMode;
        }

        public boolean matches(String s) {
            return switch (filterMode) {
                case EQUAL -> s.equals(filterSource);
                case REGEX -> s.matches(filterSource);
                case STARTS_WITH -> s.startsWith(filterSource);
                case ENDS_WITH -> s.endsWith(filterSource);
            };
        }

        public enum FilterMode {
            EQUAL(0),
            REGEX(1),
            STARTS_WITH(2),
            ENDS_WITH(3);
            private final int id;
            FilterMode(int id) {
                this.id = id;
            }

            public int getId() {
                return id;
            }

            public static FilterMode getById(int id) {
                for (FilterMode t :
                        FilterMode.values()) {
                    if (t.id == id) return t;
                }
                return null;
            }
        }
    }

    public enum RestrictionLevel {
        WHITELIST(0),
        BLACKLIST(1),
        NONE(2);
        private final int id;
        RestrictionLevel(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static RestrictionLevel getById(int id) {
            for (RestrictionLevel t :
                    RestrictionLevel.values()) {
                if (t.id == id) return t;
            }
            return null;
        }
    }

    @LuaWhitelist
    public Object __index(LuaValue key) {
        if (!key.isstring()) return null;
        return switch (key.tojstring()) {
            case "http" -> http;
            default -> null;
        };
    }
}
