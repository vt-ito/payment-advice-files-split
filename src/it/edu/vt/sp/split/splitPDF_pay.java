package it.edu.vt.sp.split;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.Splitter;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;


public class splitPDF_pay{
	private static SimpleDateFormat yyyymmdd = new SimpleDateFormat("yyyyMMdd");
	private static SimpleDateFormat hhmmssSS = new SimpleDateFormat("HHmmssSS");
	private static SimpleDateFormat yyyymmdd_hhmmss = new SimpleDateFormat("yyyyMMdd_HHmmss");
	private static SimpleDateFormat yyyymmdd_hhmmss_2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static SimpleDateFormat yyyy_mm_dd_format = new SimpleDateFormat("yyyy-MM-dd");
	private static byte[] OWNERPW = "[please change to your token]".getBytes();
	private static String DIRECT_OUTPUT_MODE = "DIRECT_OUTPUT";
	private static String INDIVIDUAL_MODE = "INDIVIDUAL";
	private static String RETRY_MODE = "RETRY";
	private static String CANCEL_MODE = "CANCEL";
	private static String HIDDENSTR_HEADER_DETAIL_ASCII = "[please change to your ASCII value]";
	private static String HIDDENSTR_HEADER_DETAIL_HEX = "[please change to your HEX value]";
	private static PrintWriter w;
	private static PrintWriter w_jmp;
	private static PrintWriter w_pdf;
	private static File logflder;
	private static File jmplogflder;
	private static SimpleDateFormat hhmmss = new SimpleDateFormat("HH:mm:ss");
	private static String hostname;
	private static OutFolder of;
	private static String jmp_log_path;
	private static String w_log_path;
	private static String comma = "";
	private static String pid = "";
	private static String jid = "";
	private static String [] jidArr;
	private static String [] logPathArr;
	private static String pathSlash = "/";
	private static boolean jid_provided = false;
	private static String docType = "Payment Advice";

    private static void markLog(String logmsg) {
    	if (w_log_path != null) {
        	try {
    			w = new PrintWriter(new FileOutputStream(new File(w_log_path), true));
    	        System.out.println(logmsg);
    	        w.println(logmsg);
                w.close();
    		} catch (Exception e) {
    			e.printStackTrace();
    		};
    	}
    }

    private static void markLog_jmp_append(String objName,String objVal) {
    	if (jmp_log_path != null) {
        	try {
    			w_jmp = new PrintWriter(new FileOutputStream(new File(jmp_log_path), true));
                w_jmp.println(comma + "\"" + objName + "\" : \"" + objVal + "\"");
                w_jmp.close();
    		} catch (Exception e) {
    			e.printStackTrace();
    		};
    	}
    }
    
    private static void markLog_jmp_update(String objName,String objVal) {
    	if (jmp_log_path != null) {
        	try {
    			String line = null;
    			String logContent = "";
    			FileReader fileReader = new FileReader(jmp_log_path);
            	BufferedReader bufferedReader = new BufferedReader(fileReader);
            	boolean notMarked = true;
                while((line = bufferedReader.readLine()) != null) {
                	if (line.indexOf(objName)>=0) {
                		if (logContent != "") {
                			comma = ",";
                		}
    	            	logContent = logContent + comma + "\"" + objName + "\" : \"" + objVal + "\"" + "\n";
    	            	notMarked = false;
                	} else {
    	            	logContent = logContent + line + "\n";
                	}
                }
                if (notMarked) {
                	logContent = logContent + comma + "\"" + objName + "\" : \"" + objVal + "\"" + "\n";
                }
                bufferedReader.close();

    			w_jmp = new PrintWriter(new FileOutputStream(new File(jmp_log_path)));
                w_jmp.print(logContent);
                w_jmp.close();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    	}
    }
    
    private static void markLog_eachPDF(String logmsg) {
        System.out.println(logmsg);
        w_pdf.println(logmsg);
    }

	private static String getStudNo_ASCII(byte[] b) {
    	String hiddenStr = null;
		try {
	    	String contentStr = new String(b, "UTF8").replaceAll("[\u0000]", "");
	    	
	    	int start_pos = contentStr.indexOf(HIDDENSTR_HEADER_DETAIL_ASCII);

        	if (start_pos > 0) {
        		hiddenStr = contentStr.substring(start_pos, contentStr.indexOf("ct", start_pos));
        	}
		} catch (Exception e){
        	markLog("---------------- Error ----------------");
        	markLog(e.toString());
            e.printStackTrace();
        }
    	return hiddenStr;
	}

	private static String getStudNo_Hex(byte[] b) {
    	String hiddenStr = null;
		try {
	    	String contentStr = new String(b);
			
	    	int start_pos = contentStr.indexOf(HIDDENSTR_HEADER_DETAIL_HEX);
        	if (start_pos > 0) {
        		
                String hiddenStrHex = contentStr.substring(start_pos, contentStr.indexOf("this", start_pos)).replace("\n", "");
                StringBuilder output = new StringBuilder();
                for (int j = 0; j < hiddenStrHex.length(); j+=2) {
                    String str = hiddenStrHex.substring(j, j+2);
                    output.append((char)Integer.parseInt(str, 16));
                }
                hiddenStr = output.toString();
        	}
		} catch (Exception e){
        	markLog("---------------- Error ----------------");
        	markLog(e.toString());
            e.printStackTrace();
        	
        }
    	return hiddenStr;
	}

    public static boolean isValidDate(String inDate) {
        if (inDate == null)
            return false;
        
        if(!inDate.matches("([0-9]{4})-([0-9]{2})-([0-9]{2})"))
            return false;

        yyyy_mm_dd_format.setLenient(false);
        try {
            yyyy_mm_dd_format.parse(inDate);
            return true;
        }
        catch (ParseException pe) {
            return false;
        }
    }

    private static boolean splitFile_retry(File sourcedir, String fileName) {
        boolean isSuccess = true;
    	markLog("Start retry generate pdf:");
    	
        final String inputFileName = fileName;
        
        // check existing of retry file
    	File currFile = new File(of.paddingInputFlderPath() + fileName);

    	if (currFile.exists()) {
    		// check existing of queue files
        	File[] qFiles = of.paddingQueue_Flder().listFiles(new FilenameFilter (){
        		public boolean accept(File dir, String name) {
        			return name.startsWith(inputFileName);
        		}
        	});

        	if (qFiles.length > 0) {
        		splitFile_withQueue(sourcedir, fileName, currFile);
        	} else {
        		split_On_Individual_PDF (sourcedir, currFile, RETRY_MODE);
        	}
    	} else {
        	markLog("  > Source file does not exist: " + currFile.getAbsolutePath());
    		return false;
    	}
        return isSuccess;
    }
    
    private static boolean splitFile_withQueue(File sourcedir, final String fileName, File currFile) {

        boolean isSuccess = true;
        String publishDate;
        String outputPDFName;
        String outputFlderPath;
        File outputFlder;

    	int genCount = 0;
    	int errCount = 0;
        String errStr = "";
        
        PdfReader reader = null;
        PdfReader reader_bp = null;
        
    	markLog("  > Start split pdf with .queue files...");
    	
        try {
    		// Get the first queue filename
        	
        	// Create log file
        	markLog("  > Create log file...");
        	
        	File tmpFile = null;
        	
        	jmp_log_path = jmplogflder.getAbsolutePath() + pathSlash + "splitPDF_" + hostname + "_" + jid + ".log";
        	tmpFile = new File(jmp_log_path);
        	if (tmpFile.exists()) {
            	int i = 0;
        		String logPath = logflder.getAbsolutePath() + pathSlash;
        		String logName = "";
        		try {
        			do {
        				logName = fileName.replace(".pdf", "_") + hostname + "_" + jid + "_" + i + ".log";
        				tmpFile = new File (logPath + logName);
        				i++;
        			} while  (tmpFile.exists());

            		Date date = new Date();
        			w_pdf = new PrintWriter(logPath + logName, "UTF-8");
                    w_pdf.println("--- Start at: " + yyyymmdd_hhmmss_2.format(date) + " ---\r\n");

        	        // 1. Get file list from input folder
                	File[] qFiles = of.paddingQueue_Flder().listFiles(new FilenameFilter (){
                		public boolean accept(File dir, String name) {
                			return name.startsWith(fileName);
                		}
                	});
                	

                	if (qFiles.length > 0) {
        	    		String[] qFileDetail = qFiles[0].getName().replace(".queue", "").split("~");
                		publishDate = qFileDetail[3];

                		outputFlderPath = of.outputMainFlderPath() + publishDate;
                		outputFlder = new File( outputFlderPath);
            	    	if(!outputFlder.exists() && !outputFlder.mkdirs()){
            	    	    throw new IllegalStateException("Couldn't create dir: " + outputFlder);
            	    	}
            	    	
            	    	if (qFileDetail.length > 4) {
            	    		reader_bp = new PdfReader(of.backPageFlderPath() + qFileDetail[4]);
            	    	}
                    	
        	            reader = new PdfReader(currFile.getAbsolutePath());
        	            Document document = new Document(reader.getPageSize(1));

        	            PdfCopy copier = null;
        	            
        	            int startPage;
        	            int endPage;
        	            String[] pageRange;

        	            errCount = 0;
        	            errStr = "";
        	        	markLog_eachPDF("  > Export " + fileName + " to " +  outputFlderPath);
        	        	markLog_jmp_update("status", "Running");

        	        	int j = 0;
                    	for (File each_queue_file :qFiles) {
        		            markLog_jmp_update("progress", String.valueOf( ( (double) j) / qFiles.length * 100 ));
                    		j++;
        	        		// get page number
        	        		pageRange = each_queue_file.getName().split("~")[1].split("-");
        	        		startPage = Integer.parseInt(pageRange[0]);
        	        		endPage = Integer.parseInt(pageRange[1]);
        	        		
        	        		// output pdf name
        	        		outputPDFName = each_queue_file.getName().split("~")[2];
        	        		
        		        	markLog_eachPDF("  >>  Exporting " + outputPDFName);

        	        		document = new Document(reader.getPageSize(1));
        	        		try {
        		        		copier = new PdfCopy(document, new FileOutputStream( outputFlderPath + "/" + outputPDFName));
        		        		copier.setEncryption(null,OWNERPW,
        		        				PdfWriter.ALLOW_PRINTING | PdfWriter.ALLOW_DEGRADED_PRINTING,
        		        				PdfWriter.STANDARD_ENCRYPTION_128);
        		        		document.open();
        		        		for (int p = startPage; p<=endPage ; p++) {
    			            		copier.addPage(copier.getImportedPage(reader, p));
        		        		}
		                    	// add back page
        		        		if (reader_bp!=null) copier.addPage(copier.getImportedPage(reader_bp, 1));
        			        	document.close();
        			        	
        			        	each_queue_file.delete();
        			        	genCount++;
        	        			
        	        		} catch (Exception e) {
        	                	errCount++;
        	        			errStr += " " + errCount + ". : Cannot generate : " + outputFlderPath + "/" + outputPDFName + "\r\n";
        	        			errStr += "    > " + e.toString() + "\r\n";
        	        		} finally {
        	        			if (document!=null)	document.close();
        	        			if (copier!=null)	copier.close();
        	        		}
                    	}

        	        	reader.close();
        	        	if (reader_bp != null) reader_bp.close();
        	        	
        	        	markLog_eachPDF("File generated: " + genCount + "\r\n");

        	        	if (errCount > 0) {
        		        	markLog_eachPDF("--------------------------------------------");
        		        	markLog_eachPDF("No. of Error: " + errCount);
        		        	markLog_eachPDF("Error detail: \r\n" + errStr);
        		        	
        			    	markLog("\r\n--------------------------------------------");
    			        	markLog("  > Error occur when retry generate: " + fileName);
        	        		
        		        	isSuccess = false;
        	        		markLog_jmp_update("status", "Error");
        	        	} else {
        		        	markLog_eachPDF("--------------------------------------------");
        	        		markLog_eachPDF("Success to rerun padding job.");
        		        	
        					// Delete source pdf
        		        	currFile.delete();
        	        		markLog_jmp_update("status", "Success");
        	        	}
                	}
                	
                    w_pdf.println("--- End at: " + yyyymmdd_hhmmss_2.format(new Date()) + " ---");
        	    	w_pdf.close();
        		} catch (Exception e) {
        			e.printStackTrace();
            		markLog(e.toString());
            		isSuccess = false;
        		} 
        	} else {
        		markLog("Error: JMP log " + jmp_log_path + " not exist.");
        		isSuccess = false;
        	}
        } catch (Exception e){
        	markLog("---------------- Error ----------------");
        	markLog(e.toString());
            e.printStackTrace();
        	
        	isSuccess = false;
        } finally {
        	if (w_pdf != null) w_pdf.close();
        	if (reader != null) reader.close();
        	if (reader_bp != null) reader_bp.close();
        }
        return isSuccess;
    }
    
	private static File[] getSortedFileList (File sourcedir, final String inputFileName) {
        File[] files = null;

        // 1. Get file list from input folder
    	if (inputFileName.equals("")) {
        	files = sourcedir.listFiles(new FilenameFilter (){
        		public boolean accept(File dir, String name) {
        			return name.endsWith(".pdf");
        		}
        	});

            // 2. sort files in directory by date
            for(int i = 0; i < files.length; i++)
                for(int j = i+1; j < files.length; j++)
                    if(files[i].lastModified() > files[j].lastModified())
                    {
                        File temp = files[i];
                        files[i] = files[j];
                        files[j] = temp;
                    }
            
    	} else {
        	files = sourcedir.listFiles(new FilenameFilter (){
        		public boolean accept(File dir, String name) {
        			return name.equals(inputFileName);
        		}
        	});
    	}
		return files;
	}
		
	private static boolean split_On_Individual_PDF (File sourcedir, File currFile, String mode){
		// 1. check string in Hex String or ASCII
		// 2. Create water marked pdf file
        boolean isSuccess = true;
        boolean isASCII = true;
        boolean isHex = true;
        List<?> documents = null;
        PDDocument pdfdoc = null;
    	File queueFile = null;
        PdfReader reader = null;
        PdfReader reader_bp = null;

    	String[] hiddenStrArray;
    	String publishDate;
    	String bp_pdf_name="";
        String outputFlderPath = "";
        String outputPDFName = "";
        String prev_stud_no = "";
        String curr_stud_no = "";
        String errStr = "";
    	String queueFileName;

        PdfCopy copier = null;
        Document document = null;
    	int genCount = 0;
        int errCount = 0;
        int ttl_no_of_page = 0;
    	int page_start = 1;
        boolean errInPrev = false;
        
        String src_pdfName = currFile.getName();

    	// Create log file
    	try {
    		Date date = new Date();
	        long startTime = System.nanoTime();
    		String logName = src_pdfName.replace(".pdf", "_") + hostname + "_" + jid + "_0.log";
    		String logPath = logflder.getAbsolutePath() + pathSlash + logName;
    		w_pdf = new PrintWriter(logPath, "UTF-8");
            w_pdf.println("--- Start at: " + yyyymmdd_hhmmss_2.format(date) + " ---\r\n");

            markLog_jmp_append("startTime", yyyy_mm_dd_format.format(date) + " " + hhmmss.format(date));
            markLog_jmp_update("status", "Running");
            markLog_jmp_update("progress", "1");

    		// 1. check string in Hex String or ASCII
    		reader = new PdfReader(currFile.getAbsolutePath());
            byte[] b = reader.getPageContent(1);
            String hiddenStr = getStudNo_ASCII(b);
    		if (hiddenStr == null) {
    			hiddenStr = getStudNo_Hex(b);
    			if (hiddenStr == null) {
    				isHex = false;
    				hiddenStr = getStudNo_ASCII (b);
    				if (hiddenStr == null) {
        				isASCII = false;
	                    if (documents == null) {
	                    	pdfdoc = parseDocument( new FileInputStream( currFile.getAbsolutePath() ) );
		                    Splitter splitter = new Splitter();
		                    splitter.setSplitAtPage(1);
		                    documents = splitter.split( pdfdoc );
	                    }
	                    
	                    if (documents.size()>0) {
		            		PDFTextStripper stripper = new PDFTextStripper();
		            		stripper.setStartPage( 1 );
		            		stripper.setEndPage( 1 );
		            		String docText = stripper.getText( pdfdoc );
		                	int pos = docText.indexOf("[your ASCII code]");
		                	int pos_end = docText.indexOf("\r\n", pos);
		                	if (pos_end < 0) {
		                		pos_end = docText.indexOf("\n", pos);
		                	}
		                	if (pos > 0 && pos_end > 0) {
			                	hiddenStr = docText.substring(pos, pos_end);
		                	}
	                    }
    				} else {
        				isASCII = true;
    				}
    			} else {
    				isASCII = false;
    			}
    		}

        	if (hiddenStr == null) {
        		markLog_eachPDF("  > Skip this pdf : Cannot found hidden string at page 1 in " + src_pdfName);
        		reader.close();
        	} else {
        		// 2. set prop
        		// PdfObj_Pay(String src_pdfName, String publishDate, String fileCreateDate, String hiddenStr)
        		PdfObj_Pay pdfObj = new PdfObj_Pay(src_pdfName,"","",hiddenStr);
        		markLog_eachPDF(pdfObj.getLogMsg());
		        	
	        	// 3 Get publish date
        		// 3.1 check string in Hex String or ASCII
	        	hiddenStrArray = hiddenStr.split("_");

                if (hiddenStrArray.length>10) {
                	publishDate = yyyy_mm_dd_format.format(date);
                } else {
                	publishDate = hiddenStrArray[hiddenStrArray.length-1];
                }
	        	
                // 3.2 Get publish date
        		if (!isValidDate(publishDate)) {
                	markLog_eachPDF("  > Error: No publish date, skip this pdf.");
        			throw new Exception("  > Error: Cannot find valid publish date in : " + hiddenStr);
        		} else {
        			markLog_eachPDF("  > Publish Date             : " + publishDate);
        		}

        		// 3.3 Get Back page
        		
        		// get back page file name
        		bp_pdf_name = pdfObj.getBackPageName();
	            markLog_eachPDF("  > Back page                : " + bp_pdf_name);
        		
	            File back_page_file = null;
	            if(pdfObj.needBackPage()) {
		            // check back page
		            back_page_file = new File(of.backPageFlderPath() + bp_pdf_name.replace("\\", ""));
		            markLog_eachPDF("  > back_page_file                 : " + back_page_file);
		            if (back_page_file.exists()) {
			        	reader_bp = new PdfReader(back_page_file.getAbsolutePath());
		            } else {
		            	markLog_eachPDF("Error: Cannot find back page file: " + back_page_file.getAbsolutePath());
		            	throw new Exception("Error: Cannot find back page file: " + back_page_file.getAbsolutePath());
		            }
	            }


        		ttl_no_of_page = reader.getNumberOfPages();
                markLog_eachPDF("  > Total Pages              : " + ttl_no_of_page + "\r\n");

                // 3.4 Create output folder
                File outputFlder = null;
        		outputFlderPath = of.outputMainFlderPath() + publishDate;
        		if (mode.equals(INDIVIDUAL_MODE)){
        			File outputMainFolder = new File (of.outputMainFlderPath() + src_pdfName.replace(".pdf", ""));
		        	if (outputMainFolder.exists()) {
			            markLog_eachPDF("  > Delete existing output folder: " + outputMainFolder.getAbsolutePath());
			            try {
			        	    FileUtils.forceDelete(outputMainFolder);
			            } catch (Exception e){
			            }
		        	}
	        		outputFlderPath = of.outputMainFlderPath() + src_pdfName.replace(".pdf", "") + pathSlash + publishDate;
		        	outputFlder = new File( outputFlderPath );
        		} else {
		        	outputFlder = new File( outputFlderPath );
        		}
        		
	            markLog_eachPDF("  > Export " + src_pdfName + " to " +  outputFlderPath);
	        	if(!outputFlder.exists() && !outputFlder.mkdirs()){
	        	    throw new IllegalStateException("Couldn't create dir: " + outputFlder);
	        	}
		        	

	        	// 4. Split pdf 
	        	// 4.1 Start to read file - first page
	        	// prepare output object for first student
	            prev_stud_no = hiddenStrArray[1];
	            curr_stud_no = hiddenStrArray[1];
		        outputPDFName = curr_stud_no + pdfObj.getOutputFileName();
		            
		        markLog_eachPDF("  >>  Exporting " + outputPDFName);

		        document = new Document(reader.getPageSize(1));
	            try {
	        		copier   = new PdfCopy(document, new FileOutputStream( outputFlderPath + pathSlash + outputPDFName));
	        		copier.setEncryption(null,OWNERPW,
	        				PdfWriter.ALLOW_PRINTING | PdfWriter.ALLOW_DEGRADED_PRINTING,
	        				PdfWriter.STANDARD_ENCRYPTION_128);
	        		document.open();	
	            } catch (Exception e) {
                	errCount++;
        			errStr += " " + errCount + ". : Cannot generate : " + outputFlderPath + pathSlash + outputPDFName + "\r\n";
        			errStr += "    > " + e.toString() + "\r\n";
        			errInPrev = true;
        		}
		            
		        // 4.2 Split pdf in for-loop
	            for (int i = 1; i <= ttl_no_of_page; i++) {
		            markLog_jmp_update("progress", String.valueOf( ( (double) i) / ttl_no_of_page * 100 ));
		            try {
		            	// Get hidden String from src_pdf
		            	b = reader.getPageContent(i);
		            	if(isASCII) {
		            		hiddenStr = getStudNo_ASCII (b);
		            		if (hiddenStr == null) {
		            			hiddenStr = getStudNo_Hex (b);
		            			if (hiddenStr != null) {
		            				isASCII = false;
		            			}
		            		}
		            	} else if (isHex) {
		            		hiddenStr = getStudNo_Hex (b);
		            		if (hiddenStr == null) {
		            			hiddenStr = getStudNo_ASCII (b);
		            			if (hiddenStr != null) {
		            				isASCII = true;
		            			}
		            		}
		            	} else {
		                    if (documents == null) {
		                    	pdfdoc = parseDocument( new FileInputStream( currFile.getAbsolutePath() ) );
			                    Splitter splitter = new Splitter();
			                    splitter.setSplitAtPage(1);
			                    documents = splitter.split( pdfdoc );
		                    }
		                    if (documents.size()>0) {
			            		PDFTextStripper stripper = new PDFTextStripper();
			            		stripper.setStartPage( i );
			            		stripper.setEndPage( i );
			            		String docText = stripper.getText( pdfdoc );
			                	int pos = docText.indexOf("[your ASCII code]");
			                	int pos_end = docText.indexOf("\r\n", pos);
			                	if (pos_end < 0) {
			                		pos_end = docText.indexOf("\n", pos);
			                	}

			                	if (pos > 0 && pos_end > 0) {
				                	hiddenStr = docText.substring(pos, pos_end);
			                	}
		                    }
		            	}

		            	// if hidden string exist in src_pdf
		            	// save this page content from source pdf to output pdf
		            	if (hiddenStr == null) {
		            		markLog_eachPDF("Error : no hidden string at page: " + i );
		            	} else {
		            		// get content from hidden string
		                    hiddenStrArray = hiddenStr.split("_");
		                    curr_stud_no = hiddenStrArray[1];

		                    if (!curr_stud_no.equals(prev_stud_no)) {
		                    	if (errInPrev) {
		                    		errInPrev = false;
			                    	queueFileName = src_pdfName
					                    			+ "~" + String.format("%05d", page_start) + "-" + String.format("%05d", i-1)		
					                    			+ "~" + outputPDFName + "~" + publishDate
					                    			+ (bp_pdf_name.equals("") ? "" : ("~" + bp_pdf_name))
					                    			+ ".queue"; 
			                    	queueFile = new File(of.paddingQueueFlderPath() + queueFileName);
			                    	queueFile.createNewFile();
		                    	}
		                    	
		                    	if(pdfObj.needBackPage()) {
			                    	copier.addPage(copier.getImportedPage(reader_bp, 1));
		                    	}
		    			        document.close();
		    			        genCount++;

		                    	prev_stud_no = curr_stud_no;
		                    	outputPDFName = curr_stud_no + pdfObj.getOutputFileName();
					            document = new Document(reader.getPageSize(1));
		                    	page_start = i;

		                    	if (pdfObj.isNSSS()) {
		                    		if (!bp_pdf_name.equals(hiddenStrArray[2])) {
			                    		// open new back page
			                    		reader_bp.close();

			                    		bp_pdf_name = hiddenStrArray[2].replace("\\", "");	// back page code
		        			            back_page_file = new File(of.backPageFlderPath() + bp_pdf_name + ".pdf");
		        			            if (back_page_file.exists()) {
		        				        	reader_bp = new PdfReader(back_page_file.getAbsolutePath());
		        			            } else {
		        			            	markLog_eachPDF("Error: Cannot find back page file: " + back_page_file.getAbsolutePath());
		        			            	throw new Exception("Error: Cannot find back page file: " + back_page_file.getAbsolutePath());
		        			            }
		                    		}
		                    	}
		                    	
		                    	// create new blank pdf
				        		copier   = new PdfCopy(document, new FileOutputStream( outputFlderPath + pathSlash + outputPDFName));
				        		copier.setEncryption(null,OWNERPW,
				        				PdfWriter.ALLOW_PRINTING | PdfWriter.ALLOW_DEGRADED_PRINTING,
				        				PdfWriter.STANDARD_ENCRYPTION_128);
		                    	document.open();
					        	markLog_eachPDF("  >>  Exporting " + outputPDFName);
		                    }
		                    // add this page content to output pdf
		                    if (!errInPrev) copier.addPage(copier.getImportedPage(reader, i));
		            	}
		            } catch (Exception e) {
	                	errCount++;
	        			errStr += " " + errCount + ". : Cannot generate : " + outputFlderPath + pathSlash + outputPDFName + "\r\n";
	        			errStr += "    > " + e.toString() + "\r\n";
	        			errInPrev = true;
		            }
	            }
		            
	            // for the end page of a pdf
                if (!errInPrev) {
                	if(pdfObj.needBackPage()) {
                		copier.addPage(copier.getImportedPage(reader_bp, 1));
                	}
                	if (reader_bp != null) {
                		reader_bp.close();
                	}
            	}

            	// if there is error on previous pdf, create queue file for re-generation
            	if (errInPrev) {
            		errInPrev = false;
                	queueFileName = src_pdfName
	                    			+ "~" + String.format("%05d", page_start) + "-" + String.format("%05d", ttl_no_of_page)		// page range
	                    			+ "~" + outputPDFName + "~" + publishDate
	                    			+ (bp_pdf_name.equals("") ? "" : ("~" + bp_pdf_name))
	                    			+ ".queue"; 
                	queueFile = new File(of.paddingQueueFlderPath() + queueFileName.replace("\\", ""));
                	System.out.println(queueFile.getAbsolutePath());
                	queueFile.createNewFile();
            	}
	            
            	// close this pdf
	            document.close();
	            if (reader != null) reader.close();
		        genCount++;
		        

	            markLog_jmp_update("progress", "100");
	        	markLog_eachPDF("\r\nFile generated: " + genCount+ "\r\n");
				
	        	if (errCount > 0) {
	        		isSuccess = false;
    	            markLog_jmp_update("status", "Error");
		        	markLog_eachPDF("--------------------------------------------");
		        	markLog_eachPDF("No. of Error: " + errCount);
		        	markLog_eachPDF("Error detail: \r\n" + errStr);

		        	// move water marked file to padding input folder
		        	try {
		    			if (!mode.equals(RETRY_MODE)) {
			        		markLog_eachPDF("Copying source file " + currFile.toPath() + " to " + of.paddingInputFlderPath() + "\r\n");
							Files.copy(currFile.toPath(), Paths.get( of.paddingInputFlderPath() + currFile.getName()), StandardCopyOption.REPLACE_EXISTING);	
		    			}
		    		} catch (IOException e) {
		    			e.printStackTrace();
		    		}
		        	
	        	}
        	}

			isSuccess = archive_src(of.archiveFlderPath(), currFile) && isSuccess;

        	date = new Date();
            w_pdf.println("--- End at: " + yyyymmdd_hhmmss_2.format(date) + " ---");
            markLog_jmp_append("endTime", yyyy_mm_dd_format.format(date) + " " + hhmmss.format(date));

	        date = new Date();
	        long difference = System.nanoTime() - startTime;
	    	String ttl_time = String.format("%d min, %d sec",
                    TimeUnit.NANOSECONDS.toMinutes(difference),
                    TimeUnit.NANOSECONDS.toSeconds(difference) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.NANOSECONDS.toMinutes(difference)));
            markLog_jmp_append("totalTime", ttl_time);
        	w_pdf.close();

        	if (reader != null) reader.close();
        	
        	if (isSuccess) {
                markLog_jmp_update("status", "Success");
        	}
        	
		} catch (Exception e) {
            markLog_jmp_update("status", "Error");
			markLog_eachPDF("---------------- Error in split_On_Individual_PDF ----------------");
        	markLog_eachPDF("Error: During splitting on  " + src_pdfName);

        	if (reader != null) reader.close();
        	if (reader_bp != null) reader_bp.close();
        	try {
				Files.copy(currFile.toPath(), Paths.get( of.archiveFlderPath() + currFile.getName()), StandardCopyOption.REPLACE_EXISTING);
				Files.move(currFile.toPath(), Paths.get( of.paddingInputFlderPath() + currFile.getName()), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();

        	isSuccess = false;
		} finally {

			if (!isSuccess) {

				String pathSlash2 = "";
    	    	if (!System.getProperty("os.name").equals("Linux")) {
        	    	pathSlash2 = "\\\\";
    	    	}
	            markLog_jmp_update("retryCmd",  "java -Xmx2048m -classpath " + of.jarPath() + " it.edu.vt.sp.split.splitPDF " + "\\\"" + of.paddingInputFlderPath().replace("\\", "\\\\") + pathSlash2 + "\\\" " + jid + " retry " + currFile.getName());
	            markLog_jmp_update("cancelCmd", "java -Xmx2048m -classpath " + of.jarPath() + " it.edu.vt.sp.split.splitPDF " + "\\\"" + of.paddingInputFlderPath().replace("\\", "\\\\") + pathSlash2 + "\\\" " + jid + " cancel " + currFile.getName());
			}
			
        	if (w_pdf != null) w_pdf.close();
        	if (reader != null) reader.close();
        	if (reader_bp != null) reader_bp.close();
		}
		return isSuccess;
	}
	
    private static boolean batchSplitFile_direct_mode(File sourcedir, String inputFolderName, String mode) {
        boolean isSuccess = true;
    	markLog("Start generate pdf in direct output mode:");
		
		// Init parameter
        File currFile = null;
        String src_pdfName = "";
        boolean successInCurrBatch = false;
        boolean isExist = false;
		
        try {
	        // 1. Get file list from input folder
        	File[] files = getSortedFileList (sourcedir, inputFolderName);
        	Date d = new Date();
        	String yyyymmdd_str = yyyymmdd.format(d);
        	String hhmmssSS_str = hhmmssSS.format(d);
        	if (jid.equals("")) {
    	    	jid = yyyymmdd_str + "_" + hhmmssSS_str;
            	jid_provided = false;
        	}
        	String [] paddingFileName = new String[files.length];
	    	jidArr = new String[files.length];
	    	logPathArr = new String[files.length];

	        // 2. Generate log file
	        for(int f = 0; f < files.length; f++) {
    	    	jmp_log_path = jmplogflder.getAbsolutePath() + pathSlash + "splitPDF_" + hostname + "_" + jid + ".log";
    	    	
    	    	if (!jid_provided) {
        	    	File logFile = new File(jmp_log_path);
        	    	while (logFile.exists()){
        	    		hhmmssSS_str = Long.toString(Long.parseLong(hhmmssSS_str) + 1);
        	    		jid = yyyymmdd_str + "_" + hhmmssSS_str;
            	    	jmp_log_path = jmplogflder.getAbsolutePath() + pathSlash + "splitPDF_" + hostname + "_" + jid + ".log";
            	    	logFile = new File(jmp_log_path);
        	    	}
    	    	}
    	    	
    	    	paddingFileName[f] = files[f].getName();
    	    	jidArr[f] = jid;
    	    	logPathArr[f] = jmp_log_path;
	        }
	        
	        // 3. Do job for each file
	        while(files.length>0){

	            try {
	            	// Get the first file from the list which ordered by last modified date
		        	currFile = files[0];
		        	src_pdfName = currFile.getName();
		        	
	            	// if there is a new pdf in the list, create a log file for jmp
			        for(int f = 0; f < files.length; f++) {
			        	isExist = false;
			        	for (int j = 0; j < paddingFileName.length; j++){
			        		if (files[f].getName().equals(paddingFileName[j])) {
					        	isExist = true;
					        	break;
			        		}
			        	}
				        
			    	    jmp_log_path = jmplogflder.getAbsolutePath() + pathSlash + "splitPDF_" + hostname + "_" + jid + ".log";

				        if (!isExist){
			        		
			    	    	if (!jid_provided) {
			        	    	File logFile = new File(jmp_log_path);
			        	    	while (logFile.exists()){
			        	    		hhmmssSS_str = Long.toString(Long.parseLong(hhmmssSS_str) + 1);
			        	    		jid = yyyymmdd_str + "_" + hhmmssSS_str;
			        	    		jmp_log_path = jmplogflder.getAbsolutePath() + pathSlash + "splitPDF_" + hostname + "_" + jid + ".log";
			            	    	logFile = new File(jmp_log_path);
			        	    	}
			    	    	}

			    	        paddingFileName = append(paddingFileName,files[f].getName());
			    	        jidArr = append(jidArr,jid);
			    	        logPathArr = append(logPathArr,jmp_log_path);
			        	}
			        }

			        // set jid and jmp_log_path for this pdf
		        	for (int j = 0; j < paddingFileName.length; j++){
		        		if(src_pdfName.equals(paddingFileName[j])){
				        	jid = jidArr[j];
				        	jmp_log_path = logPathArr[j];
				        	break;
		        		}
		        	}

			        
		        	markLog("--------------------------------------------------------------------");
		        	markLog("Start to process on pdf: " + src_pdfName);
		        	successInCurrBatch = split_On_Individual_PDF(sourcedir, currFile, mode);
		        	isSuccess = isSuccess && successInCurrBatch;

		        	if (successInCurrBatch) {
			        	markLog("  > Success!");
		        	} else {
			        	markLog("  > Error occur when generate: " + src_pdfName);
		        	}
		        	markLog("Finish to process on pdf: " + src_pdfName);
		        	markLog("--------------------------------------------------------------------\r\n");

		        	files = getSortedFileList (sourcedir, inputFolderName);
		        	
	            } catch (Exception e){
	            	markLog("---------------- Error ----------------");
	            	markLog("Error: Cannot process on " + src_pdfName);
	            	markLog(e.toString());
	                e.printStackTrace();
	            	isSuccess = false;
	            } finally {
	    			
	            }
	        }
	        
        } catch (Exception e){
        	markLog("---------------- Error in batchSplitFile_direct_mode ----------------");
        	markLog(e.toString());
            e.printStackTrace();
        	isSuccess = false;
        } finally {
			
        }
		return isSuccess;
		
	}
    
    static <T> T[] append(T[] arr, T element) {
        final int N = arr.length;
        arr = Arrays.copyOf(arr, N + 1);
        arr[N] = element;
        return arr;
    }
    
    private static boolean archive_src (String archiveFlderPath, File workingPDF) {
    	
    	try {
    		markLog_eachPDF("Archive source file " + workingPDF.getName() + " to " + archiveFlderPath + "\r\n");
			Files.move(workingPDF.toPath(), Paths.get( archiveFlderPath + workingPDF.getName()), StandardCopyOption.REPLACE_EXISTING);
			
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
    }
    
    public static boolean delete_padding_file(File sourcedir, final String fileName) {
    	Boolean isSuccess = true;
    	

		File tmpFile = null;
		
    	jmp_log_path = jmplogflder.getAbsolutePath() + pathSlash + "splitPDF_" + hostname + "_" + jid + ".log";
    	tmpFile = new File(jmp_log_path);
    	if (tmpFile.exists()) {

        	int i = 0;
    		String logPath = logflder.getAbsolutePath() + pathSlash;
    		String logName = "";
    		
    		try {
    			do {
    				logName = fileName.replace(".pdf", "_") + hostname + "_" + jid + "_" + i + ".log";
    				tmpFile = new File (logPath + logName);
    				i++;
    			} while  (tmpFile.exists());

    	    	markLog("Start to delete padding file ...");
        		Date date = new Date();
    			w_pdf = new PrintWriter(logPath + logName, "UTF-8");
                w_pdf.println("--- Start at: " + yyyymmdd_hhmmss_2.format(date) + " ---\r\n");

    	    	
                tmpFile = new File(sourcedir + pathSlash + fileName);
                if (tmpFile.exists()) {
                	w_pdf.println("Deleting : " + tmpFile.getAbsolutePath());
                	if( tmpFile.delete() ) {
                    	w_pdf.println("Deleting Success.");
                	} else {
                		isSuccess = false;
                    	w_pdf.println("Delete failed: " + tmpFile.getAbsolutePath());
                	}
                }

                tmpFile = new File(sourcedir + pathSlash + "with_watermark" + pathSlash + fileName);
                if (tmpFile.exists()) {
                	w_pdf.println("Deleting : " + tmpFile.getAbsolutePath());
                	if( tmpFile.delete() ) {
                    	w_pdf.println("Deleting Success.");
                	} else {
                		isSuccess = false;
                    	w_pdf.println("Delete failed: " + tmpFile.getAbsolutePath());
                	}
                }
                
                tmpFile = new File(sourcedir + pathSlash + "queue");
                if (tmpFile.exists() && tmpFile.isDirectory()) {
                	File[] listOfFiles = tmpFile.listFiles(new FilenameFilter (){
                		public boolean accept(File dir, String name) {
                			return name.startsWith(fileName);
                		}
                	});

                	for (int j = 0; j < listOfFiles.length; j++) {
                    	w_pdf.println("Deleting : " + listOfFiles[j].getAbsolutePath());
                    	if( listOfFiles[j].delete() ) {
                        	w_pdf.println("Deleting Success.");
                    	} else {
                    		isSuccess = false;
                        	w_pdf.println("Delete failed: " + listOfFiles[j].getAbsolutePath());
                    	}
                	}
                }
                

    			tmpFile = null;

                w_pdf.println("--- End at: " + yyyymmdd_hhmmss_2.format(date) + " ---");
            	w_pdf.close();

            	if (isSuccess) {
            		markLog_jmp_update("status", "Cancelled");
        	    	markLog("Finish to delete padding file.");
            	} else {
        	    	markLog("Error occur during deleting padding file.");
            	}
    			
    		} catch (Exception e) {
    			e.printStackTrace();
        		markLog(e.toString());
        		isSuccess = false;
    		} 
    		
    	} else {
    		markLog("Error: JMP log " + jmp_log_path + " not exist.");
    		isSuccess = false;
    	}
    	
		return isSuccess;
    }

    private static PDDocument parseDocument( InputStream input )throws IOException
    {
        PDFParser parser = new PDFParser( input );
        parser.parse();
        return parser.getPDDocument();
    }
    
    public static void main( String[] args ) throws Exception
    {
    	boolean isSuccess = false;
    	hostname = InetAddress.getLocalHost().getHostName();
		
    	// Check input arg
    	if (args.length < 1 || args.length == 2) {
    		System.out.println("Please input arg: ");
    		System.out.println("  - 1. PDF Absolute Path ");
    		System.out.println("  - 2. Job ID");
    		System.out.println("  - 3. Mode: Retry - re-generate pdf in which queue file in padding folder");
    		System.out.println("       Mode: cancel - delete queue file in padding folder");
    		System.out.println("  - 4. filename of retry file 'xxxxxxx.pdf'");
    	}
    	else{
        	String path = args[0];
        	String mode = DIRECT_OUTPUT_MODE;
        	String fileName = "";
        	if (args.length>1) {
            	jid = args[1];
            	if (!jid.equals("")) {
                	jid_provided = true;
            	}
        	}
        	
        	try {
        		System.out.println(path);
        		System.out.println("sadasdsa");
    	        File inputPath = new File(path);
    	        File sourcedir = null;
    	        
    	        if (inputPath.isDirectory()) {
        	    	sourcedir = new File(path);
    	        } else {
    	        	// if specific file name provided
    	        	sourcedir = inputPath.getParentFile();
    	        	fileName = inputPath.getName();
    	        }

            	if (args.length>2) {
            		mode = args[2].toUpperCase();
            	}
    	    	of = new OutFolder(sourcedir, mode);
    	        long startTime = System.nanoTime();
    	        Date date = new Date();
				
            	// Create log folder
    	    	logflder = new File(sourcedir.getParentFile().getAbsolutePath() + "/log/");
    	    	if(!logflder.exists() && !logflder.mkdirs()){
    	    	    throw new IllegalStateException("Couldn't create dir: " + logflder);
    	    	}
    	    	pid = yyyymmdd_hhmmss.format(date);
    	    	w_log_path = logflder.getAbsolutePath() + pathSlash + "splitPDF_" + hostname + "_" + pid + ".log";

    	    	if (!System.getProperty("os.name").equals("Linux")) {
        	    	pathSlash = "\\";
    	    	}

    	    	// JMP
    	    	jmplogflder = new File(of.jmpFlderPath().toString());
    	    	if(!jmplogflder.exists() && !jmplogflder.mkdirs()){
    	    	    throw new IllegalStateException("Couldn't create dir: " + jmplogflder);
    	    	}
				// Start process
            	try {
        	    	markLog("--- Start at " + yyyymmdd_hhmmss_2.format(date) + " ---\r\n");
        	    	markLog("Input file folder: " + path);
        	    	
	            	if (args.length>2) {
	            		fileName = args[3];
	            		if (mode.equals(RETRY_MODE)) {
	            	    	markLog("Mode : Retry split pdf job\r\n");
	            		} else if (mode.equals(CANCEL_MODE)) {
	            	    	markLog("Mode : Cancel split pdf job\r\n");
	            		} else if (mode.equals(INDIVIDUAL_MODE)) {
	            	    	markLog("Mode : Split Individual pdf\r\n");
	            		}
	            	}
				    
			        // split file
	            	if (mode.equals(RETRY_MODE)){
	            		isSuccess = splitFile_retry(sourcedir, fileName);
	            	} else if (mode.equals(CANCEL_MODE)) {
	            		isSuccess = delete_padding_file(sourcedir, fileName);
	            	} else if (mode.equals(INDIVIDUAL_MODE)) {
	                	File currFile = new File(path + fileName);
	                	if (currFile.exists()) {
		            		isSuccess = split_On_Individual_PDF(sourcedir, currFile, mode);
	                	} else {
	                    	markLog("  > Source file does not exist: " + currFile.getAbsolutePath());
	                    	isSuccess = false;
	                	}
	            	} else {
	            		isSuccess = batchSplitFile_direct_mode(sourcedir, fileName, mode);
	            	}
		        	
			        // End of Program
			        date = new Date();
			        long difference = System.nanoTime() - startTime;
        	    	markLog("\r\n--------------------------------------------");
        	    	String ttl_time = String.format("%d min, %d sec",
	                        TimeUnit.NANOSECONDS.toMinutes(difference),
	                        TimeUnit.NANOSECONDS.toSeconds(difference) -
	                        TimeUnit.MINUTES.toSeconds(TimeUnit.NANOSECONDS.toMinutes(difference)));
        	    	markLog("Total execution time: " + ttl_time + "\r\n");
        	    	markLog("--- End at " + yyyymmdd_hhmmss_2.format(date) + " ---");
            	} catch (Exception e){
        	    	markLog("---------------- Error in Main ----------------");
    			//	markLog_jmp_update("status", "Error");
        	    	markLog(e.toString());
    	            e.printStackTrace();
    	        }
            	
            	if (w != null) w.close();
            	if (w_jmp != null) w_jmp.close();
            	
        	} catch (Exception e){
	            e.printStackTrace();
	        } finally {
	        	if (w!=null) w.close();
	        }
			
			if (!isSuccess) {
				// send alert
				System.out.println("End with error!");
			}
    	}
    }
}
