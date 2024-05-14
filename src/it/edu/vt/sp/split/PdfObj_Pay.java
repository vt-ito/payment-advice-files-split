package it.edu.vt.sp.split;

public class PdfObj_Pay {

	private String logMsg = "";
	private String backPageName="";
	private boolean isNSSS = false;
	private boolean needBackPage = true;

	public String getOutputFileName() { return "_payment.pdf"; }
	public String getBackPageName() { return backPageName; }
	public String getLogMsg() { return logMsg;}
	public boolean isNSSS() { return isNSSS; }
	public boolean needBackPage() { return needBackPage; }
	
	public PdfObj_Pay(String src_pdfName, String publishDate, String fileCreateDate, String hiddenStr){
		
        System.out.println(hiddenStr);
		String[] hiddenStrArray = hiddenStr.split("_");

		
        if ( src_pdfName.indexOf("01TSSS")>-1 ) {
        	needBackPage = false;
        	this.logMsg= "  > Type                     : TSSS";
        }else if ( src_pdfName.indexOf("MMPRR")>-1 ) {
        	isNSSS = true;
        	backPageName = hiddenStrArray[2].replace("\\", "") + ".pdf";	// back page code
        	this.logMsg= "  > Type                     : NSSS";
        } else if ( src_pdfName.indexOf("SSB_2")>-1
                 || src_pdfName.indexOf("SSB_3")>-1 
                 || src_pdfName.indexOf( "SSB_")>-1 ) {
        	backPageName = "ssb.pdf";
        	this.logMsg= "  > Type                     : SSSS";
        } else if ( src_pdfName.indexOf("SS_2")>-1
                 || src_pdfName.indexOf("SS_3")>-1
                 || src_pdfName.indexOf("SS_6")>-1
                 || src_pdfName.indexOf("SS_8")>-1
                 || src_pdfName.indexOf( "SS_")>-1  ){
        	backPageName = "normal.pdf";
        	this.logMsg= "  > Type                     : Normal";
        }
	}
}
