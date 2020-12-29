package com.bewannabe.rfpdedup;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

public class Core {
    /*
    **  USAGE: upload(file_path, file_data, cloud);
    **  DESCRIPTION:
    **      Uploads a file.
    **  @param
    **      file_path   String      file path to be uploaded
    **      file_data   byte[]      raw binary data of a file to be uploaded
    **      cloud       StorageManager.Cloud.LOCAL | StorageManage.Cloud.AZURE
    **  @throws
    **      Exception("FileAlreadyExist");
    */
    public static void upload(String filePath, byte[] fileData, StorageManager.Cloud cloud) throws Exception {
        StorageManager sm = new StorageManager(cloud);
        IndexManager im = new IndexManager(sm);
        RecipeManager rm = new RecipeManager(sm);

        im.importIndex();

        String fileHash = MD5.hash(filePath);
        if(im.checkFileRegistry(fileHash)) throw new Exception("FileAlreadyExist");

        RFPChunkHelper ch = new RFPChunkHelper(fileData);

        im.register(fileHash, filePath, fileData.length, ch.getLogicalChunkCount(), ch.getChunks());
        rm.exportRecipe(fileHash, ch.getRecipe());
        im.printStats();

        im.exportIndex();
    }

    /*
    **  USAGE: download(file_path);
    **  DESCRIPTION:
    **      Downloads a file.
    **  @param
    **      file_path   String      file path to be uploaded
    **      cloud       StorageManager.Cloud.LOCAL | StorageManage.Cloud.AZURE
    **  @return
    **      Returns raw binary data of downloaded file
    **  @throws
    **      Exception("FileNotExist");
    */
    public static byte[] download(String filePath, StorageManager.Cloud cloud) throws Exception {
        StorageManager sm = new StorageManager(cloud);
        IndexManager im = new IndexManager(sm);
        RecipeManager rm = new RecipeManager(sm);

        im.importIndex();

        String fileHash = MD5.hash(filePath);
        if(!im.checkFileRegistry(fileHash)) throw new Exception("FileNotExist");

        IndexManager.File file = im.getFile(fileHash);
        FileRecipe recipe = rm.importRecipe(fileHash);
        List<FileRecipe.Chunk> chunks = recipe.getChunks();

        byte[] fileData = new byte[file.getSize()];
        Map<String,byte[]> downloaded = new HashMap<String,byte[]>();

        int cursor = 0;
        String hash; byte[] data; int size;
        for(FileRecipe.Chunk chunk : chunks) {
            size = chunk.getSize();
            if(chunk instanceof FileRecipe.NormalChunk) {
                hash = ((FileRecipe.NormalChunk)chunk).getHash();

                if(!downloaded.containsKey(hash)) downloaded.put(hash, sm.readChunkData(hash));
                data = downloaded.get(hash);

                for(int i=0; i<size; i++) {
                    System.arraycopy(data, 0, fileData, cursor, data.length);
                    cursor += data.length;
                }
            }
            else {
                Arrays.fill(fileData, cursor, cursor+size, (byte)0);
                cursor += size;
            }
        }
        return fileData;
    }

    /*
    **  USAGE: delete(file_path);
    **  DESCRIPTION:
    **      Deletes a file.
    **  @param
    **      file_path   String      file path to be uploaded
    **      cloud       StorageManager.Cloud.LOCAL | StorageManage.Cloud.AZURE
    **  @throws
    **      Exception("FileNotExist");
    */
    public static void delete(String filePath, StorageManager.Cloud cloud) throws Exception {
        StorageManager sm = new StorageManager(cloud);
        IndexManager im = new IndexManager(sm);
        RecipeManager rm = new RecipeManager(sm);

        im.importIndex();

        String fileHash = MD5.hash(filePath);
        if(!im.checkFileRegistry(fileHash)) throw new Exception("FileNotExist");

        im.unregister(fileHash, rm.importRecipe(fileHash).getUniqueChunks());
        rm.deleteRecipe(fileHash);
        im.printStats();

        im.exportIndex();
    }
}