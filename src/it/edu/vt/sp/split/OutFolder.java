package it.edu.vt.sp.split;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class OutFolder {
	static String DIRECT_OUTPUT_MODE = "DIRECT_OUTPUT";
	static String ERROR_MODE = "ERROR";
	private static String INDIVIDUAL_MODE = "INDIVIDUAL";

	private String paddingInputFlderPath="";
	private String paddingInputWMFlderPath="";
	private String paddingQueueFlderPath="";
	private String archiveFlderPath="";
	private String backPageFlderPath="";
	private String outputMainFlderPath="";
	private String jmpFlderPath="";
	private String jarPath="";
	public String jarFilePath = "";

	private File paddingInput_Flder;
	private File paddingInputWM_Flder;
	private File paddingQueue_Flder;
	private File output_Flder;
	private File archive_Flder;
	private File jmp_Flder;
	private String pathSlash = "/";
	
	public int genAlertThreshold = 3000;

	public String paddingInputFlderPath() {return paddingInputFlderPath;}
	public String paddingInputWMFlderPath() {return paddingInputWMFlderPath;}
	public String paddingQueueFlderPath() {return paddingQueueFlderPath;}
	public String outputMainFlderPath() {return outputMainFlderPath;}
	public String archiveFlderPath() {return archiveFlderPath;}
	public String backPageFlderPath() {return backPageFlderPath;}
	public String jarPath() {return jarPath;}

	public File paddingInput_Flder() {return paddingInput_Flder;}
	public File paddingInputWM_Flder() {return paddingInputWM_Flder;}
	public File paddingQueue_Flder() {return paddingQueue_Flder;}
	public File output_Flder() {return output_Flder;}
	public File archive_Flder() {return archive_Flder;}
	public File jmpFlderPath() {return jmp_Flder;}
	
	private File mkFolderIfNotExist (String path) {
    	File flder = new File( path );
    	if(!flder.exists() && !flder.mkdirs()){
    	    throw new IllegalStateException("Couldn't create dir: " + flder);
    	}
    	return flder;
	}
	
	private String getProgramSetting(String progSettingFilePath) {
		String jsonDataSrc = "";
		try {
			File jsonFile = new File(progSettingFilePath);
			if (jsonFile.exists()) {

				BufferedReader br = null;
				try {
					String line;
					br = new BufferedReader(new FileReader(jsonFile));
					while ((line = br.readLine()) != null) {
						jsonDataSrc += line;
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						if (br != null)
							br.close();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
				jsonDataSrc = (jsonDataSrc.indexOf(",") == 0) ? jsonDataSrc.substring(1):jsonDataSrc;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return jsonDataSrc;
	}
	
	public OutFolder(File sourcedir, String mode) {
		jarFilePath = "/your-root-folder/script/";
		String serverSettingFilePath = "/your-root-folder/setting/server_config.xml";
		String progSettingFilePath = "/your-root-folder/setting/programSetting.json";
		
		
		if (System.getProperty("os.name").equals("Linux")) {
        	this.jmpFlderPath  = "/your-root-folder/jmp/log/";
        	this.jarPath = jarFilePath + "splitPDF.jar";
    	} else {
    		jarFilePath = "C:\\your-root-folder\\script\\";
    		serverSettingFilePath = "C:\\your-root-folder\\setting\\server_config.xml";
    		progSettingFilePath = "C:\\your-root-folder\\setting\\programSetting.json";
        	this.jmpFlderPath  = "C:\\your-root-folder\\jmp\\log";
        	this.jarPath = "\\\"C:\\\\your-root-folder\\\\script\\\\splitPDF.jar\\\"";
	    	pathSlash = "\\";
    	}

		String scheduled_file_path = "";
		try {
			File xmlFile = new File(serverSettingFilePath);
			if (xmlFile.exists()) {
				Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile);
				doc.getDocumentElement().normalize();
				Element ParentNode = (Element) doc.getElementsByTagName("server_config").item(0);
				
				if (System.getProperty("os.name").equals("Linux")) {
					scheduled_file_path = ParentNode.getElementsByTagName("scheduled_file_path").item(0).getFirstChild().getNodeValue();
					jarFilePath = ParentNode.getElementsByTagName("jarFilePath").item(0).getFirstChild().getNodeValue();
				} else {
					scheduled_file_path = ParentNode.getElementsByTagName("scheduled_file_path_local").item(0).getFirstChild().getNodeValue();
					jarFilePath = ParentNode.getElementsByTagName("jarFilePath_local").item(0).getFirstChild().getNodeValue();
				}
			} else {
				System.out.println("Setting file not exist: " + serverSettingFilePath);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		String docType_folderName = sourcedir.getParentFile().getName();
		if (mode.equals(INDIVIDUAL_MODE)) {
	    	this.outputMainFlderPath = sourcedir.getParentFile().getAbsolutePath() + pathSlash + "output" + pathSlash;
	    	if (System.getProperty("os.name").equals("Linux")) {
		    	this.backPageFlderPath = "/your-root-folder/split_pdf/"+docType_folderName+"/back_page/";
	    	} else {
		    	this.backPageFlderPath = "C:\\your-root-folder\\split_pdf\\"+docType_folderName+"\\back_page\\";
	    	}
		} else {
			this.outputMainFlderPath = scheduled_file_path + docType_folderName + pathSlash;
	    	this.backPageFlderPath = sourcedir.getParentFile().getAbsolutePath() + pathSlash + "back_page" + pathSlash;
		}
    	output_Flder = mkFolderIfNotExist( outputMainFlderPath );
    	
    	this.paddingInputFlderPath  = sourcedir.getParentFile().getAbsolutePath() + pathSlash + "padding" + pathSlash;
    	paddingInput_Flder = mkFolderIfNotExist( paddingInputFlderPath );
    	
    	this.paddingInputWMFlderPath  = sourcedir.getParentFile().getAbsolutePath() + pathSlash + "padding" + pathSlash + "with_watermark" + pathSlash;
    	paddingInputWM_Flder = mkFolderIfNotExist( paddingInputWMFlderPath );
    	
    	this.paddingQueueFlderPath  = sourcedir.getParentFile().getAbsolutePath() + pathSlash + "padding" + pathSlash + "queue" + pathSlash;
    	paddingQueue_Flder = mkFolderIfNotExist( paddingQueueFlderPath );

    	this.archiveFlderPath  = sourcedir.getParentFile().getAbsolutePath() + pathSlash + "archive_files" + pathSlash;
    	archive_Flder = mkFolderIfNotExist( archiveFlderPath );

    	jmp_Flder = mkFolderIfNotExist( jmpFlderPath );
    	
		try {
			JSONObject jsonObj = new JSONObject(getProgramSetting(progSettingFilePath));
			genAlertThreshold = jsonObj.getJSONObject("pdf").getInt("split_large_amount_pdf");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
