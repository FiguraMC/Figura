package org.figuramc.figura.utils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class PermissivePath implements Path {
    public PermissivePath(String from) {
        List<String> chunks = List.of(from.split("/"));
        boolean isAbsolute = from.startsWith("/");
        if (!chunks.isEmpty() && isAbsolute) chunks = chunks.subList(1, chunks.size());
        if (!chunks.isEmpty() && chunks.get(chunks.size() - 1).isEmpty()) chunks = chunks.subList(0, chunks.size() - 1);
        this.chunks = chunks;
        this.isAbsolute = isAbsolute;
    }

    public static final PermissivePath ROOT = new PermissivePath(true, List.of());

    private final List<String> chunks;
    private final boolean isAbsolute;

    public PermissivePath(boolean isAbsolute, List<String> chunks) {
        // VERSIONING: replace with `new ArrayList<String>(chunks)` on 1.16.5
        this.chunks = List.copyOf(chunks);
        this.isAbsolute = isAbsolute;
    }

    public static PermissivePath ofOneChunk(String chunk) {
        return new PermissivePath(false, List.of(chunk));
    }

    private static final RuntimeException exc = new IllegalArgumentException(
            "PermissivePath is not compatible with other Path types");
    private static final RuntimeException fakeFilesystemException = new UnsupportedOperationException(
            "Not a real filesystem");

    private static final FileSystem fakeFS = new FileSystem() {
        @Override
        public FileSystemProvider provider() {
            throw fakeFilesystemException;
        }

        @Override
        public void close() {
        }

        @Override
        public boolean isOpen() {
            return true;
        }

        @Override
        public boolean isReadOnly() {
            return true;
        }

        @Override
        public String getSeparator() {
            return "/";
        }

        @Override
        public Iterable<Path> getRootDirectories() {
            return List.of(ROOT);
        }

        @Override
        public Iterable<FileStore> getFileStores() {
            return List.of();
        }

        @Override
        public Set<String> supportedFileAttributeViews() {
            return Set.of();
        }

        @Override
        public @NotNull Path getPath(@NotNull String first, @NotNull String @NotNull ... more) {
            String[] allOfIt = new String[more.length + 1];
            allOfIt[0] = first;
            System.arraycopy(more, 0, allOfIt, 1, more.length);
            return new PermissivePath(String.join("/", allOfIt));
        }

        @Override
        public PathMatcher getPathMatcher(String syntaxAndPattern) {
            throw fakeFilesystemException;
        }

        @Override
        public UserPrincipalLookupService getUserPrincipalLookupService() {
            throw fakeFilesystemException;
        }

        @Override
        public WatchService newWatchService() {
            throw fakeFilesystemException;
        }
    };

    @Override
    public @NotNull FileSystem getFileSystem() {
        return fakeFS;
    }

    @Override
    public boolean isAbsolute() {
        return isAbsolute;
    }

    @Override
    public PermissivePath getRoot() {
        if (isAbsolute) return ROOT;
        return null;
    }

    /**
     * get the last chunk as a Path, without normalizing
     */
    private PermissivePath getDirectFileName() {
        if (chunks.isEmpty()) return null;
        return ofOneChunk(chunks.get(chunks.size() - 1));
    }

    @Override
    public PermissivePath getFileName() {
        return normalize().getDirectFileName();
    }

    @Override
    public PermissivePath getParent() {
        if (chunks.isEmpty()) return null;
        return new PermissivePath(isAbsolute, chunks.subList(0, chunks.size() - 1));
    }

    @Override
    public int getNameCount() {
        return chunks.size();
    }

    @Override
    public @NotNull PermissivePath getName(int index) {
        return ofOneChunk(chunks.get(index));
    }

    @Override
    public @NotNull PermissivePath subpath(int beginIndex, int endIndex) {
        return new PermissivePath(false, chunks.subList(beginIndex, endIndex));
    }

    @Override
    public boolean startsWith(@NotNull Path other) {
        if (!(other instanceof PermissivePath)) return false;
        //noinspection PatternVariableCanBeUsed (Java 8 compat)
        PermissivePath other2 = (PermissivePath) other;
        if (other2.isAbsolute != this.isAbsolute) return false;
        if (other2.chunks.size() > chunks.size()) return false;
        for (int i = 0; i < other2.chunks.size(); i++) {
            if (!chunks.get(i).equals(other2.chunks.get(i))) return false;
        }
        return true;
    }

    @Override
    public boolean endsWith(@NotNull Path other) {
        if (!(other instanceof PermissivePath)) return false;
        //noinspection PatternVariableCanBeUsed (Java 8 compat)
        PermissivePath other2 = (PermissivePath) other;
        if (other2.isAbsolute) return this.equals(other2);
        if (other2.chunks.size() > chunks.size()) return false;
        for (int i = other2.chunks.size() - 1, j = chunks.size() - 1; i >= 0; i--, j--) {
            if (!other2.chunks.get(i).equals(chunks.get(j))) return false;
        }
        return true;
    }

    private static List<String> internalNormalize(List<String> chunks) {
        ArrayList<String> newChunks = new ArrayList<>(chunks.size());
        for (String chunk : chunks) {
            if (chunk.equals("..")) {
                if (!newChunks.isEmpty()) {
                    newChunks.remove(newChunks.size() - 1);
                    continue;
                }
            }
            if (chunk.equals(".")) continue;
            newChunks.add(chunk);
        }

        // VERSIONING: just `return newChunks;` on 1.16.5
        return List.copyOf(newChunks);
    }

    @Override
    public @NotNull PermissivePath normalize() {
        return new PermissivePath(isAbsolute, internalNormalize(chunks));
    }

    @Override
    public @NotNull PermissivePath resolve(@NotNull Path other) {
        if (!(other instanceof PermissivePath)) throw exc;
        //noinspection PatternVariableCanBeUsed (Java 8 compat)
        PermissivePath other2 = (PermissivePath) other;
        if (other2.isAbsolute) return other2;
        ArrayList<String> both = new ArrayList<>(chunks.size() + other2.chunks.size());
        both.addAll(chunks);
        both.addAll(other2.chunks);
        return new PermissivePath(isAbsolute, internalNormalize(both));
    }

    @Override
    public @NotNull PermissivePath relativize(@NotNull Path other) {
        if (!(other instanceof PermissivePath)) throw exc;
        //noinspection PatternVariableCanBeUsed (Java 8 compat)
        PermissivePath other2 = (PermissivePath) other;
        if (isAbsolute != other2.isAbsolute)
            throw new IllegalArgumentException("Cannot relativize two paths with different absoluteness");
        int upperBound = Math.min(chunks.size(), other2.chunks.size());
        int i;
        for (i = 0; i < upperBound; i++) {
            if (!chunks.get(i).equals(other2.chunks.get(i))) break;
        }
        return other2.subpath(i, other2.chunks.size());
    }

    @Override
    public @NotNull URI toUri() {
        return URI.create("permissive-fs:" + this);
    }

    @Override
    public @NotNull Path toAbsolutePath() {
        throw new RuntimeException("toAbsolutePath is not supported on PermissivePath");
    }

    @Override
    public @NotNull Path toRealPath(@NotNull LinkOption @NotNull ... options) throws IOException {
        throw new FileSystemException("PermissivePath is not a real path and is not backed by an actual filesystem");
    }

    @Override
    public @NotNull WatchKey register(@NotNull WatchService watcher,
                                      WatchEvent.Kind<?> @NotNull [] events,
                                      WatchEvent.Modifier @NotNull ... modifiers) throws IOException {
        throw new FileSystemException("PermissivePath is not a real path and is not backed by an actual filesystem");
    }

    @Override
    public int compareTo(@NotNull Path other) {
        if (this.equals(other)) return 0;
        if (!(other instanceof PermissivePath)) throw exc;
        //noinspection PatternVariableCanBeUsed (Java 8 compat)
        PermissivePath other2 = (PermissivePath) other;
        Iterator<String> it = this.chunks.iterator();
        Iterator<String> it2 = other2.chunks.iterator();
        while (it.hasNext() && it2.hasNext()) {
            String left = it.next();
            String right = it2.next();
            if (!left.equals(right)) return left.compareTo(right);
        }
        if (isAbsolute && !other2.isAbsolute) return -1;
        if (!isAbsolute && other2.isAbsolute) return 1;
        throw new IllegalStateException("the paths are equal, but equals() returns false. what's up with that");
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PermissivePath)) return false;
        //noinspection PatternVariableCanBeUsed (Java 8 compat)
        PermissivePath other = (PermissivePath) obj;
        return isAbsolute == other.isAbsolute && chunks.equals(other.chunks);
    }

    @Override
    @NotNull
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (isAbsolute) sb.append('/');
        sb.append(String.join("/", chunks));
        return sb.toString();
    }
}
