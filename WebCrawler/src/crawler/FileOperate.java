package crawler;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Date;
import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

public class FileOperate implements Comparator<File>{
	static Constant constant = new Constant();//initial properties Constant(讀properties檔)
    /**默認排序的方式， 按目錄，文件排序TYPE_DIR*/
    public static final int TYPE_DEFAULT             = -1;
    /**按修改時間，降序*/
    public static final int TYPE_MODIFIED_DATE_DOWN = 1;
    /**按修改時間，升序*/
    public static final int TYPE_MODIFIED_DATE_UP    = 2;
    /**按文件大小，降序*/
    public static final int TYPE_SIZE_DOWN            = 3;
    /**按文件大小，升序*/
    public static final int TYPE_SIZE_UP            = 4;
/*  public static final int TYPE_NAME_DOWN            = 5;
    public static final int TYPE_NAME_UP            = 6;*/
    /**按文件名*/
    public static final int TYPE_NAME                 = 5;
    /**按目錄，文件排序*/
    public static final int TYPE_DIR                = 7;
    
    private int mType = -1;
    
    public FileOperate(int type) {
        if (type < 0 || type > 7) {
            type = TYPE_DIR;
        }
        mType = type;
    }
    @Override
    public int compare(File object1, File object2) {
        
        int result = 0;
        
        switch (mType) {
        
        case TYPE_MODIFIED_DATE_DOWN://last modified date down
            result = compareByModifiedDateDown(object1, object2);
            break;
            
        case TYPE_MODIFIED_DATE_UP://last modified date up
            result = compareByModifiedDateUp(object1, object2);
            break;
            
        case TYPE_SIZE_DOWN:    // file size down
            result = compareBySizeDown(object1, object2);
            break;
            
        case TYPE_SIZE_UP:        //file size up
            result = compareBySizeUp(object1, object2);
            break;
            
        case TYPE_NAME:            //name 
            result = compareByName(object1, object2);
            break;
            
        case TYPE_DIR:            //dir or file
            result = compareByDir(object1, object2);
            break;
        default:
            result = compareByDir(object1, object2);
            break;
        }
        return result;
    }
    private int compareByModifiedDateDown(File object1, File object2) {
        
        long d1 = object1.lastModified();
        long d2 = object2.lastModified();
        
        if (d1 == d2){
            return 0;
        } else {
            return d1 < d2 ? 1 : -1;
        }
    }
    private int compareByModifiedDateUp(File object1, File object2) {
        
        long d1 = object1.lastModified();
        long d2 = object2.lastModified();
        
        if (d1 == d2){
            return 0;
        } else {
            return d1 > d2 ? 1 : -1;
        }
    }
    private int compareBySizeDown(File object1, File object2) {
        
        if (object1.isDirectory() && object2.isDirectory()) {
            return 0;
        }
        if (object1.isDirectory() && object2.isFile()) {
            return -1;
        }
        if (object1.isFile() && object2.isDirectory()) {
            return 1;
        }
        long s1 = object1.length();
        long s2 = object2.length();
        
        if (s1 == s2){
            return 0;
        } else {
            return s1 < s2 ? 1 : -1;
        }
    }
    private int compareBySizeUp(File object1, File object2) {
        
        if (object1.isDirectory() && object2.isDirectory()) {
            return 0;
        }
        if (object1.isDirectory() && object2.isFile()) {
            return -1;
        }
        if (object1.isFile() && object2.isDirectory()) {
            return 1;
        }
        
        long s1 = object1.length();
        long s2 = object2.length();
        
        if (s1 == s2){
            return 0;
        } else {
            return s1 > s2 ? 1 : -1;
        }
    }
    private int compareByName(File object1, File object2) {
        
        Comparator<Object> cmp = Collator.getInstance(java.util.Locale.TAIWAN);
        
        return cmp.compare(object1.getName(), object2.getName());
    }
    private int compareByDir(File object1, File object2) {
        
        if (object1.isDirectory() && object2.isFile()) {
            return -1;
        } else if (object1.isDirectory() && object2.isDirectory()) {
            return compareByName(object1, object2);
        } else if (object1.isFile() && object2.isDirectory()) {
            return 1;
        } else {  //object1.isFile() && object2.isFile()) 
            return compareByName(object1, object2);
        }
    }
    public static File[] FileSorter(String FolderPath){
        
		File[] list = new File(FolderPath).listFiles();
		//      File[] list = new File(FolderPath).listFiles();
		Arrays.sort(list, new FileOperate(FileOperate.TYPE_MODIFIED_DATE_DOWN));
		//      printFileArray(list);
		//      printFileArray(list,FilterFileType);
		return list;
    }  
    //for test
//    public static void main(String[] args){
//         
//        File[] list = new File("cfg").listFiles();
//        Arrays.sort(list, new FileSorter(FileSorter.TYPE_MODIFIED_DATE_DOWN));
////        printFileArray(list);
//        printFileArray(list,".txt");
//    }
    //for test
    private static void printFileArray(File[] list) {
        
    	log.getLogger().info("文件大小\t\t文件修改日期\t\t文件類型\t\t文件名稱");
        
        for (File f : list) {
            log.getLogger().info(f.length() + "\t\t" + new Date(f.lastModified()).toString() + "\t\t" + (f.isDirectory() ? "目錄" : "文件") + "\t\t" +  f.getName() );
        }
    }
    private static void printFileArray(File[] list , String FilterFileType) {
        
        log.getLogger().info("文件大小\t\t文件修改日期\t\t文件類型\t\t文件名稱");
        
        for (File f : list) {
        	if(f.toString().indexOf(FilterFileType)>0)
        		log.getLogger().info(f.length() + "\t\t" + new Date(f.lastModified()).toString() + "\t\t" + (f.isDirectory() ? "目錄" : "文件") + "\t\t" +  f.getName() );
        }
    }
	public static void compressGzipFile(String file, String gzipFile) {
        try {
            FileInputStream fis = new FileInputStream(file);
            FileOutputStream fos = new FileOutputStream(gzipFile);
            GZIPOutputStream gzipOS = new GZIPOutputStream(fos);
            byte[] buffer = new byte[1024];
            int len;
            while((len=fis.read(buffer)) != -1){
                gzipOS.write(buffer, 0, len);
            }
            //close resources
            gzipOS.close();
            fos.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
         
    }	
	public static void File_Delete(String file_name) {
		File file = new File(file_name);
		if(file.delete()){
			log.getLogger().info(file.getName() + " is deleted!");
		}else{
			log.getLogger().info("Delete operation is failed.");
		}		
	}    
	public static void File_Writer_Set(String file_name , Set<String> set) {
		PrintWriter writer;
		try {
			writer = new PrintWriter(file_name);
			//解析set，並儲存
			for(String line: set){
				writer.println(line);
				writer.flush();
			}
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	public static void File_Writer(String file_name, String sOutPut) throws IOException {
		PrintWriter writer = new PrintWriter(file_name);
		writer.println(sOutPut);
		writer.flush();
		writer.close();	
	}	
	public static void File_Reader(String file_name) throws IOException {
		try{
			// Open the file that is the first 
			// command line parameter
			FileInputStream fstream = new FileInputStream(file_name);
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   {
				// Print the content on the console
				log.getLogger().info(strLine);
			}
			//Close the input stream
			in.close();
		}catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}

	public static Set<String> File_Reader_GetSection(String file_name , String sDelimiter, int iSection) throws IOException {
		Set<String> his_url = new HashSet<String>();
		try{
			// Open the file that is the first 
			// command line parameter
			FileInputStream fstream = new FileInputStream(file_name);
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   {
				// Print the content on the console
				String[] StrArray= CommonFunc.splite_String(strLine, sDelimiter);
//				log.getLogger().info (StrArray[iSection]);
				his_url.add(StrArray[iSection]);
			}
			//Close the input stream
			in.close();
		}catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		return his_url;
	}
}