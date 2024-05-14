# payment-advice-file-split
This project is used to split master payment advice file with multiple records into individual payment advice by using a Java command line program.  It provides a convenient solution for efficiently processing and organizing large payment advice files. 

## Features
* Split payment advice files into separate files based on specified criteria.
* Easily customize the splitting criteria according to your requirements.
* Process large files quickly and efficiently.
* Maintain original file integrity while generating new split files.

## Prerequisites
* Java Development Kit (JDK) version 8 or above.
* Setup a pre-defined files structure 
* Input payment advice file(s) in the supported format.

## Usage
1. Clone or download the repository to your local machine.
2. Update the parameters in source files (i.e. OutFolder.java / PdfObj_Pay.java / splitPDF_pay.java) 
3. Compile the Java program into splitPDF.jar
4. Run the program with the following command:

```
java -Xmx2048m -classpath splitPDF.jar 'it.edu.vt.sp.split' '[your input folder path]'
```

## License
This project is licensed under [AGPL open-source license](LICENSE).
