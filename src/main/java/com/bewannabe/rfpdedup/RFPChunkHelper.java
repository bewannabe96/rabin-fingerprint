package com.bewannabe.rfpdedup;

import java.util.Map;
import java.util.HashMap;
import java.util.Arrays; 

import java.lang.Math;

import me.tongfei.progressbar.*;

public class RFPChunkHelper {
    static class Chunk {
        String hash; int succession;

        Chunk(String hash) { this.hash = hash; this.succession = 1; }

        String getHash() { return this.hash; }
        void extend() { this.succession += 1; }
    }

    private static final int irfp = 0;          // interesting RFP values                   0
    private static final int d = 257;           // multiplier                               257
    private static final int m = 524288;        // min chunk size                           512KB
    private static final int mx = 2097152;      // max chunk size                           2MB
    private static final int am = 1048576-1;    // anchor mask (average chunk size - 1)     1MB

    private final byte[] t; // input file

    private int lChunkCount;            // number of logical chunks
    private Map<String,byte[]> chunks;  // unique chunks
    private FileRecipe recipe;          // file recipe

    public RFPChunkHelper(byte[] t) { 
        this.t = t;

        this.lChunkCount = 0;
        this.chunks = new HashMap<String,byte[]>();
        this.recipe = new FileRecipe();

        chunk();
    }

    public int getLogicalChunkCount() { return this.lChunkCount; }

    public Map<String,byte[]> getChunks() { return this.chunks; }

    public FileRecipe getRecipe() { return this.recipe; }

    // [d^x mod q]
    private int dpxmq(int x) {
        int o = 1;
        for(int i=0; i<x; i++) { o *= d & am; o = o & am; };
        return o;
    }

    // [p_s (caculated)]
    private int ps(int s) {
        int ps = 0;
        for(int i=s; i<s+m; i++) { ps += ((t[i] & am) * dpxmq(m-1-i)) & am; ps = ps & am; };
        return ps;
    }

    // [p_s (constant)]
    // s:       s value
    // psm1:    ps value of s-1 (ps of s minus 1)
    private int ps(int s, int psm1) {
        return ((((d & am) * (psm1 - ((dpxmq(m-1) * (t[s-1] & am)) & am))) & am) + (t[s+m-1] & am)) & am;
    }

    // la:  last anchor point
    // na:  new anchor point
    // return (<na):    is not zero run
    // return (>=na):   is zero run and return value is the new anchor point
    private int identifyZeroRun(int la, int na) {
        int p = la + 1; for(; p<t.length && t[p]==0; p++);
        return p - 1;
    }

    // la:  last anchor point
    // na:  new anchor point
    // zr:  is zero run
    private void anchor(int la, int na, boolean zr) {
        this.lChunkCount += 1;
        if(zr) { recipe.appendZeroChunk(na-la); return; }

        byte[] chunk = Arrays.copyOfRange(t, la+1, na+1);
        String hash = MD5.hash(chunk);

        recipe.appendChunk(hash);
        chunks.put(hash, chunk);
    }

    // process chunk 
    private void chunk() {
        int la = -1, zr = 0;

        try (ProgressBar pb = new ProgressBar("", t.length-m+1)) {
            for(int s=0, na=0, psv=irfp; s<t.length-m+1; s++) {
                na = s + m - 1;
                if(na==la+mx) {
                    anchor(la, na, false); la = s = na;
                    psv=irfp; continue;
                }

                psv = psv==irfp ? ps(s) : ps(s, psv);
                if(psv==irfp) {
                    zr = identifyZeroRun(la, na);
                    if(zr>=na) { anchor(la, zr, true); la = s = zr; }
                    else { anchor(la, na, false); la = s = na; }
                }

                pb.step();
            }
            if(la < t.length-1) {
                zr = identifyZeroRun(la, t.length-1);
                anchor(la, t.length-1, zr==t.length-1);

                pb.step();
            }
        }
    }
}