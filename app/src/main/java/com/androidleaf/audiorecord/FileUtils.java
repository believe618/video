package com.androidleaf.audiorecord;

import java.io.File;

import android.os.Environment;

public class FileUtils {

	
	public static String getAudioRecordFilePath(){
		if(isSDCardAvaliable()){
			File mFile = new File(Constants.AUDIO_DIRECTORY);
			if(!mFile.exists()){
				try {
					mFile.mkdirs();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return mFile.getAbsolutePath();
		}
		return null;
	}
	
	
	public static boolean isSDCardAvaliable(){
		return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
	}
	
	public static String getAAcFilePath(String audioRecordFileName){
		return getAudioRecordFilePath() + "/" + audioRecordFileName + Constants.AAC_SUFFIX;
	}
	
	public static String getPcmFilePath(String audioRecordFileName){
		return getAudioRecordFilePath() + "/" + audioRecordFileName + Constants.PCM_SUFFIX;
	}
	
	public static String getM4aFilePath(String audioRecordFileName){
		return getAudioRecordFilePath() + "/" + audioRecordFileName + Constants.M4A_SUFFIX;
	}
}
