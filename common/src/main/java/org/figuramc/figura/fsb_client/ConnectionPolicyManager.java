package org.figuramc.figura.fsb_client;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.utils.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Read, write, and query connection policies to/from the disk.
 */
public class ConnectionPolicyManager {
    public static final HashMap<String, Function<CompoundTag, Matcher>> REGISTRY = new HashMap<>();
    public static final String CACHE_FILE_NAME = "fsb_connections";
    public final String cacheName;

    private @NotNull ConnectionPolicy fallback = ConnectionPolicy.CONNECT;
    private final List<Matcher> matchers = new ArrayList<>();

    private static ConnectionPolicyManager INSTANCE;

    /**
     * Get the policy manager attached to the default cache location.
     */
    public static ConnectionPolicyManager get() {
        if (INSTANCE == null) INSTANCE = new ConnectionPolicyManager();
        return INSTANCE;
    }

    public ConnectionPolicyManager() {
        this(CACHE_FILE_NAME);
    }

    public ConnectionPolicyManager(String cacheName) {
        this.cacheName = cacheName;

        CompoundTag[] leaky = new CompoundTag[1];
        IOUtils.readCacheFile(cacheName, it -> leaky[0] = it);
        CompoundTag content = leaky[0];

        if (content != null) loadFromTag(content);
    }

    public void save() {
        // this API sucks EVEN MORE than the reading one
        IOUtils.saveCacheFile(cacheName, this::saveToTag);
    }

    // ---- serde ----
    private static final String K_FALLBACK = "fallback";
    private static final String K_MATCHER_TYPE = "matcher_type";
    private static final String K_MATCHERS = "matchers";

    private void loadFromTag(CompoundTag tag) {
        fallback = ConnectionPolicy.getOrElse(tag.getString(K_FALLBACK), ConnectionPolicy.CONNECT);
        final Logger logger = FiguraMod.LOGGER;

        matchers.clear();
        AtomicBoolean broken = new AtomicBoolean(false);
        Tag maybeMatList = tag.get(K_MATCHERS);
        if (maybeMatList instanceof ListTag l) {
            l.forEach(t -> {
                if (t instanceof CompoundTag comp) {
                    String type = comp.getString(K_MATCHER_TYPE);
                    if (REGISTRY.containsKey(type)) {
                        try {
                            matchers.add(REGISTRY.get(type).apply(comp));
                        } catch (RuntimeException e) {
                            logger.error("bad connection policy entry", e);
                            broken.set(true);
                        }
                    } else {
                        logger.warn("bad connection policy entry: matcher type '{}' not recognized", type);
                        broken.set(true);
                    }
                } else {
                    logger.error("bad connection policy file: non-compound matcher: {}", t);
                    broken.set(true);
                }
            });
        } else {
            if (maybeMatList != null)
                logger.error("bad connection policy file: matchers isn't a list: {}", maybeMatList);
            broken.set(true);
        }
        if (broken.get()) {
            logger.warn("broken connection policy entries will be dropped if policy is edited");
        }
    }

    private void saveToTag(CompoundTag tag) {
        tag.putString(K_FALLBACK, fallback.name());

        ListTag matList = new ListTag();
        for (Matcher matcher : matchers) {
            matList.add(matcher.serialize());
        }
        tag.put(K_MATCHERS, matList);
    }

    @Override
    public String toString() {
        return String.format("ConnPolMan(%s.nbt, %s +%d rules)", cacheName, fallback, 0);
    }

    public @NotNull ConnectionPolicy query(String ip) {
        for (Matcher matcher : matchers) {
            ConnectionPolicy policy = matcher.getPolicy(ip);
            if (policy != null) return policy;
        }
        return fallback;
    }

    public interface Matcher {

        /**
         * Obtain a policy preference for the provided IP.
         * Return {@code null} if the IP does not match this rule.
         */
        @Nullable ConnectionPolicy getPolicy(String ip);

        /**
         * Get the serialization key used to identify this type of matcher.
         */
        CompoundTag serialize();
    }

    public static class RegexMatcher implements Matcher {
        private final Pattern pattern;
        private final String patternRaw;
        private final ConnectionPolicy action;

        public static final String ID = "regex";
        public static final String K_PATTERN = "pattern";
        public static final String K_ACTION = "action";

        public static RegexMatcher deserialize(CompoundTag tag) {
            if (!tag.getString(K_MATCHER_TYPE).equals(ID))
                throw new IllegalStateException("bad dispatch to RegexMatcher::deserialize (wrong type)");
            String pat = Objects.requireNonNull(tag.getString(K_PATTERN), "missing pattern");
            String act = Objects.requireNonNull(tag.getString(K_ACTION), "missing action");
            ConnectionPolicy pol = Objects.requireNonNull(
                    ConnectionPolicy.get(act),
                    "%s is not a recognised Connection Policy".formatted(act)
            );
            return new RegexMatcher(pat, pol);
        }

        public RegexMatcher(String pattern, ConnectionPolicy action) {
            this.pattern = Pattern.compile(pattern);
            this.patternRaw = pattern;
            this.action = action;
        }

        @Override
        public @Nullable ConnectionPolicy getPolicy(String ip) {
            if (pattern.matcher(ip).find()) return action;
            return null;
        }

        @Override
        public CompoundTag serialize() {
            CompoundTag tag = new CompoundTag();
            tag.putString(K_MATCHER_TYPE, ID);
            tag.putString(K_PATTERN, patternRaw);
            tag.putString(K_ACTION, action.name());
            return tag;
        }
    }

    public static class LiteralMatcher implements Matcher {
        private final String normIp;
        private final ConnectionPolicy action;

        public static final String ID = "literal";
        public static final String K_PATTERN = "pattern";
        public static final String K_ACTION = "action";

        public static LiteralMatcher deserialize(CompoundTag tag) {
            if (!tag.getString(K_MATCHER_TYPE).equals(ID))
                throw new IllegalStateException("bad dispatch to LiteralMatcher::deserialize (wrong type)");
            String ip = Objects.requireNonNull(tag.getString(K_PATTERN), "missing pattern");
            String act = Objects.requireNonNull(tag.getString(K_ACTION), "missing action");
            ConnectionPolicy pol = Objects.requireNonNull(
                    ConnectionPolicy.get(act),
                    "%s is not a recognised Connection Policy".formatted(act)
            );
            return new LiteralMatcher(ip, pol);
        }

        public LiteralMatcher(String ip, ConnectionPolicy action) {
            if (ip.endsWith(":25565")) ip = ip.substring(0, ip.length() - 6);
            this.normIp = ip;
            this.action = action;
        }

        @Override
        public @Nullable ConnectionPolicy getPolicy(String ip) {
            if (ip.endsWith(":25565")) ip = ip.substring(0, ip.length() - 6);
            if (normIp.equals(ip)) return action;
            return null;
        }

        @Override
        public CompoundTag serialize() {
            CompoundTag tag = new CompoundTag();
            tag.putString(K_MATCHER_TYPE, ID);
            tag.putString(K_PATTERN, normIp);
            tag.putString(K_ACTION, action.name());
            return tag;
        }
    }

    // ---- inners ----

    /**
     * Event result: How do we respond to the server connecting?
     */
    public enum ConnectionPolicy {
        /**
         * move to {@link ClientSession.StateMachine#CONNECTED}
         */
        CONNECT,
        /**
         * move to {@link ClientSession.StateMachine#INVISIBLE}
         */
        IGNORE,
        /**
         * move to {@link ClientSession.StateMachine#USER_REQUIRED}
         */
        ASK;

        private final Set<String> prev;

        ConnectionPolicy(String... prev) {
            this.prev = new HashSet<>(Arrays.asList(prev));
        }

        public static @Nullable ConnectionPolicy get(String name) {
            try {
                return ConnectionPolicy.valueOf(name);
            } catch (IllegalArgumentException e) {
                for (ConnectionPolicy value : ConnectionPolicy.values()) {
                    if (value.prev.contains(name)) return value;
                }
                return null;
            }
        }

        public static @NotNull ConnectionPolicy getOrElse(String name, @NotNull ConnectionPolicy otherwise) {
            ConnectionPolicy result = get(name);
            if (result != null) return result;
            return otherwise;
        }
    }

    // ---- <clinit> ----
    static {
        REGISTRY.put(RegexMatcher.ID, RegexMatcher::deserialize);
        REGISTRY.put(LiteralMatcher.ID, LiteralMatcher::deserialize);
    }
}
