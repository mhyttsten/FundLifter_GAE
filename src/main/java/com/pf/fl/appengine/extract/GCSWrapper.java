package com.pf.fl.appengine.extract;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.pf.fl.shared.Constants;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

public class GCSWrapper {
    private static final Logger log = Logger.getLogger(GCSWrapper.class.getName());

    //------------------------------------------------------------------------
    public static byte[] gcsReadFile(String fileName) throws IOException {
        Blob b = gcsReadBlob(fileName);
        if (b == null) {
            return null;
        }
        return b.getContent();
    }

    //------------------------------------------------------------------------
    public static void gcsWriteFile(String fileName, byte[] data) throws IOException {
        Blob blob = gcsReadBlob(fileName);
        if (blob != null) {
            blob.delete();
        }
        gcsWriteBlob(fileName, data);
    }

    // ***********************************************************************

    //------------------------------------------------------------------------
    public static Blob gcsReadBlob(String fileName) {
        Storage storage = null;
        storage = StorageOptions.getDefaultInstance().getService();
        Bucket bucket = storage.get(Constants.BUCKET);
        Blob b = bucket.get(fileName);
        return b;
    }

    //------------------------------------------------------------------------
    public static Blob gcsWriteBlob(String fileName, byte[] data) throws IOException {
        Storage storage = StorageOptions.getDefaultInstance().getService();
        Blob blob = storage.create(BlobInfo.newBuilder(Constants.BUCKET, fileName).build(), data);
        return blob;
    }

    //------------------------------------------------------------------------
    public static void gcsDeleteFiles(String startsWith, String doesNotContains) throws IOException {
        List<Blob> bs = gcsGetBlobsInAscendingOrder(startsWith);
        for (Blob b: bs) {
            if (!b.getName().contains(doesNotContains)) {
                b.delete();
            }
        }
    }


    //------------------------------------------------------------------------
    private static List<Blob> gcsGetBlobsInAscendingOrder(String prefix) throws IOException {
        Storage storage = null;
        storage = StorageOptions.getDefaultInstance().getService();

        try {
            Bucket bucket = storage.get(Constants.BUCKET);
            Page<Blob> pblob = bucket.list();
            Iterable<Blob> iterator = pblob.iterateAll();
            List<Blob> blobs = new ArrayList<>();
            for (Blob blob : iterator) {
                String bname = blob.getName();
                if (bname != null && bname.startsWith(prefix)) {
                    blobs.add(blob);
                }
            }

            Collections.sort(blobs, new Comparator<Blob>() {
                @Override
                public int compare(Blob o1, Blob o2) {
                    return -o1.getName().compareTo(o2.getName());
                }
            });
            return blobs;
        } catch(Exception exc) {
            throw new AssertionError(exc);
        }
    }
}
