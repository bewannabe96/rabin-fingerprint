package com.bewannabe.rfpdedup;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;

public class FileRecipe {
    static abstract class Chunk {
        int size = 0;
        public int getSize() { return size; }
    }

    static class NormalChunk extends Chunk {
        private String hash;

        NormalChunk(String hash) { this.hash = hash; this.size = 1; }
        NormalChunk(String hash, int size) { this.hash = hash; this.size = size; }

        public String getHash() { return this.hash; }

        public boolean hashEquals(String hash) { return this.hash.equals(hash); }
        public void extend() { this.size += 1; }
        public void extend(int size) { this.size += size; }

        public String toString() { return String.format("%s:%d", hash, size); }
    }

    static class ZeroChunk extends Chunk {
        ZeroChunk(int size) { this.size = size; }

        public String toString() { return String.format("0:%d", size); }
    }

    private Set<String> uniqueChunks;
    private List<Chunk> chunks; 
    private Chunk prevChunk;    

    public FileRecipe() {
        this.uniqueChunks = new HashSet<String>();
        this.chunks = new ArrayList<Chunk>();
        this.prevChunk = null;
    }

    public Set<String> getUniqueChunks() { return this.uniqueChunks; }
    public List<Chunk> getChunks() { return this.chunks; }

    public void appendChunk(String hash) {
        uniqueChunks.add(hash);
        if(prevChunk!=null
            && prevChunk instanceof NormalChunk
            && ((NormalChunk)prevChunk).hashEquals(hash))
            ((NormalChunk)prevChunk).extend();
        else { prevChunk = new NormalChunk(hash); chunks.add(prevChunk); }
    }

    public void appendChunk(String hash, int size) {
        uniqueChunks.add(hash);
        if(prevChunk!=null
            && prevChunk instanceof NormalChunk
            && ((NormalChunk)prevChunk).hashEquals(hash))
            ((NormalChunk)prevChunk).extend();
        else { prevChunk = new NormalChunk(hash, size); chunks.add(prevChunk); }
    }

    public void appendZeroChunk(int size) {
        prevChunk = new ZeroChunk(size);
        chunks.add(prevChunk);
    }
}