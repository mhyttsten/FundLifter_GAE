package com.pf.fl.shared.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Compresser {

	//----------------------------------------------
	public static final void main(String[] args) throws Exception {
		try {
			StringBuffer strb = new StringBuffer();
			for(int i=0; i < 1000; i++) {
				strb.append("0123456789");
			}
			byte[] uc_data = strb.toString().getBytes();
			System.out.println("Size of uncompressed: " + uc_data.length);
			byte[] c_data = dataCompress("file", uc_data);
			System.out.println("Size of compressed: " + c_data.length);
			uc_data = dataUncompress(c_data);
			System.out.println("Size of uncompressed_2: " + uc_data.length);			
			System.out.println("First 30 characters: " + (new String(uc_data).substring(0,30)));
		} catch(Exception exc) {
			System.out.println(exc);
			exc.printStackTrace();
		}
	}
	
	//----------------------------------------------
	public static byte[] dataCompress(String name, byte[] data) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ZipOutputStream zos = new ZipOutputStream(bos);
		zos.putNextEntry(new ZipEntry(name));
		zos.write(data);
		zos.closeEntry();
		zos.close();
		byte[] result = bos.toByteArray();
		return result;
	}
	
	//-------------------------------------------------------------------------
	public static byte[] dataUncompress(byte[] data) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		ZipInputStream zis = new ZipInputStream(bis);
		try {
			ZipEntry ze = zis.getNextEntry();
			int read = 0;
			do {
				byte[] part = new byte[1024 * 1024 * 10];
				read = zis.read(part);
				if (read > 0) {
					bos.write(part, 0, read);
				}
			} while (read > 0);
		} catch(IOException exc) {
			return null;
		}
		byte[] result = bos.toByteArray();

		// This would indicate an error condition, we think
		if (data != null && data.length > 0 && (result == null || result.length == 0)) {
			return null;
		}
		
		return result;
  }
}
