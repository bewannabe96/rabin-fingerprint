package com.bewannabe.rfpdedup;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

public class IndexManager {
    static class File {
        private String path;
        private int size;
        private int chunks;

        public File(String path, int size, int chunks) { this.path = path; this.size = size; this.chunks = chunks; }

        public int getSize() { return this.size; }
        public int getChunks() { return this.chunks; }

        public String toString() { return String.format("%s:%d:%d", path, size, chunks); }
    }

    static class Chunk {
        private int size;
        private int count;

        public Chunk(int size, int count) { this.size = size; this.count = count; }
        
        public int getSize() { return this.size; }
        public int getCount() { return this.count; }
        public void increment() { this.count++; }
        public void decrement() { this.count--; }

        public String toString() { return String.format("%d:%d", size, count); }
    }

    StorageManager storageManager;

    private int storageFiles;           // number of files on storage
    private int logicalChunks;          // number of pre-duplicated chunks in storage
    private int logicalStorage;         // number of bytes of pre-duplicated chunks in storage
    private int physicalStorage;        // number of bytes of pre-duplicated chunks in storage

    private Map<String,File> files;     // stored files
    private Map<String,Chunk> chunks; // stored unique chunks

    public IndexManager(StorageManager storageManager) {
        this.storageManager = storageManager;
        this.storageFiles = 0;
        this.logicalStorage = 0;
        this.physicalStorage = 0;
        this.files = new HashMap<String,File>();
        this.chunks = new HashMap<String,Chunk>();
    }

    /*
    **  USAGE: importIndex();
    **  DESCRIPTION:
    **      Imports index file.
    */
    public void importIndex() {
        String data = storageManager.readIndexFile();
        if(data.length()==0) return;

        String[] lines = data.split("\n");
        String[] cols = lines[0].split(":");
        this.storageFiles = Integer.parseInt(cols[0]);
        this.logicalStorage = Integer.parseInt(cols[1]);
        this.physicalStorage = Integer.parseInt(cols[2]);
        this.logicalChunks = Integer.parseInt(cols[3]);
        for(int i=1; i<lines.length; i++) {
            cols = lines[i].split(":");
            if(i<storageFiles+1)
                files.put(cols[0], new File(cols[1], Integer.parseInt(cols[2]), Integer.parseInt(cols[3])));
            else
                chunks.put(cols[0], new Chunk(Integer.parseInt(cols[1]), Integer.parseInt(cols[2])));
        }
    }

    /*
    **  USAGE: exportIndex();
    **  DESCRIPTION:
    **      Exports index file.
    */
    public void exportIndex() {
        StringBuilder data = new StringBuilder();
        data.append(String.format("%d:%d:%d:%d\n", storageFiles, logicalStorage, physicalStorage, logicalChunks));
        for(Map.Entry<String,File> file : files.entrySet())
            data.append(String.format("%s:%s\n", file.getKey(), file.getValue()));
        for(Map.Entry<String,Chunk> chunk : chunks.entrySet())
            data.append(String.format("%s:%s\n", chunk.getKey(), chunk.getValue()));
        storageManager.writeIndexFile(data.toString());
    }

    /*
    **  USAGE: checkFileRegistry(file_hash);
    **  DESCRIPTION:
    **      Check whether file is registered.
    **  @param
    **      file_hash   String              file hash
    **  @return
    **      Returns true if file is registered, false otherwise
    */
    public boolean checkFileRegistry(String fileHash) { return files.containsKey(fileHash); }

    /*
    **  USAGE: getFile(file_hash);
    **  DESCRIPTION:
    **      Get file data identified by hash.
    **  @param
    **      file_hash   String              file hash
    **  @return
    **      Returns file data
    */
    public File getFile(String fileHash) { return files.get(fileHash); }

    /*
    **  USAGE: register(file_hash, file_path, file_size, l_chunks_c, pdd_chunks);
    **  DESCRIPTION:
    **      Registers a file to index file.
    **  @param
    **      file_hash   String                  file hash 
    **      file_path   String                  file path
    **      file_size   int                     size of file to be registered
    **      l_chunks_c  int                     number of logical chunks
    **      pdd_chunks  Map<String,byte[]>      pre-deduplicated chunks
    */
    public void register(String fileHash, String filePath, int fileSize, int lChunksCount, Map<String,byte[]> pddChunks) {
        // register file
        files.put(fileHash, new File(filePath, fileSize, lChunksCount));

        // update stats
        storageFiles += 1;
        logicalChunks += lChunksCount;
        logicalStorage += fileSize;

        // deduplication
        String hash; byte[] data;
        for(Map.Entry<String,byte[]> chunk : pddChunks.entrySet()) {
            hash = chunk.getKey();
            data = chunk.getValue();

            // chunk already stored
            if(chunks.containsKey(hash)) { chunks.get(hash).increment(); }
            // chunk is not stored
            else {
                chunks.put(hash, new Chunk(data.length, 1));
                storageManager.writeChunkData(hash, data);
                physicalStorage += data.length;
            }
        }
    }

    /*
    **  USAGE: unregister(file_hash, unq_chunks ;
    **  DESCRIPTION:
    **      Unregisters a file from index file.
    **  @param
    **      file_hash   String                  file hash 
    **      unq_chunks  Set<String>             set of unique chunks in file
    */
    public void unregister(String fileHash, Set<String> unqChunks) {
        // unregister file
        File file = files.get(fileHash);
        files.remove(fileHash);

        // update stats
        storageFiles -= 1;
        logicalChunks -= file.getChunks();
        logicalStorage -= file.getSize();
        
        // clear up chunks
        for(String hash : unqChunks) {
            chunks.get(hash).decrement();
            // if chunk is no more referenced
            if(chunks.get(hash).getCount()==0) {
                physicalStorage -= chunks.get(hash).getSize();
                chunks.remove(hash);
                storageManager.deleteChunkData(hash);
            }
        }
    }

    /*
    **  USAGE: printStats();
    **  DESCRIPTION:
    **      Prints statistics of index.
    */
    public void printStats() {
        System.out.printf("Total number of files that have been stored: %d\n", storageFiles);
		System.out.printf("Total number of pre-deduplicated chunks in storage: %d\n", logicalChunks);
		System.out.printf("Total number of unique chunks in storage: %d\n", chunks.size());
		System.out.printf("Total number of bytes of pre-deduplicated chunks in storage: %d\n", logicalStorage);
		System.out.printf("Total number of bytes of unique chunks in storage: %d\n", physicalStorage);
        System.out.printf("Deduplication ratio: %.2f\n", (double)logicalStorage/physicalStorage);
    }

}