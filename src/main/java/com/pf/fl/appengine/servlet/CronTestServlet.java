package com.pf.appengine;

import java.util.logging.Logger;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

public class CronTestServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(CronTestServlet.class.getSimpleName());

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {  	
  	log.info("CronTestServlet.doGet on: " + new java.util.Date().toString());
  	log.info("Will now read fund file");

    Storage storage = null;
    storage = StorageOptions.getDefaultInstance().getService();
    Bucket bucket = storage.get("ql-magnushyttsten.appspot.com");
    Blob b = bucket.get("backend/fundinfo-db-master.bin");
    if (b == null) {
    	log.info("Error: Blog object was null");
    } else {
	    byte[] fundDBFile = b.getContent();
   		String size = "null";
    	if (fundDBFile != null) {
    		size = String.valueOf(fundDBFile.length);
    	}
    	log.info("Size of fund file: " + size);
  	}
  }
}
