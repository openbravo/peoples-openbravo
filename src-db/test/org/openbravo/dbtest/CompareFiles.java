package org.openbravo.dbtest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.Vector;
import java.util.Arrays;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.Task;
public class CompareFiles extends Task {
	private String dir1;
	private String dir2;
    protected Log _log;
    protected boolean success=true;
	
	public CompareFiles()
	{
		
	}

    /**
     * Initializes the logging.
     */
    private void initLogging() {
        // For Ant, we're forcing DdlUtils to do logging via log4j to the console
        Properties props = new Properties();
        String     level = "INFO";

        props.setProperty("log4j.rootCategory", level + ",A");
        props.setProperty("log4j.appender.A", "org.apache.log4j.ConsoleAppender");
        props.setProperty("log4j.appender.A.layout", "org.apache.log4j.PatternLayout");
        props.setProperty("log4j.appender.A.layout.ConversionPattern", "%m%n");
        // we don't want debug logging from Digester/Betwixt
        props.setProperty("log4j.logger.org.apache.commons", "WARN");

        LogManager.resetConfiguration();
        PropertyConfigurator.configure(props);

        _log = LogFactory.getLog(getClass());
    }
	public void execute()
	{

        initLogging();
		
		File fdir1=new File(dir1);
		File fdir2=new File(dir2);
		if(!fdir1.isDirectory())
		{
			_log.error("Directory1 is not a directory.");
			System.exit(1);
		}
		if(!fdir2.isDirectory())
		{
			_log.error("Directory2 is not a directory.");
			System.exit(1);
		}

		Vector<File> files1=new Vector<File>();
		recursiveRead(fdir1, files1);
		Vector<File> files2=new Vector<File>();
		recursiveRead(fdir2, files2);
		
		for(int i=0;i<files1.size();i++)
		{
			File vecFile=files1.get(i);
			File corrFile=findCorrespondingFile(files2,vecFile);
			if(corrFile==null)
			{
				_log.error("DIFF: File "+vecFile.getParent()+"/"+vecFile.getName()+" in source directory doesn't exist in export directory.");
				success=false;
				files1.remove(vecFile);
				i--;
			}
			else
			{
				compareFiles(vecFile, corrFile);
				files1.remove(vecFile);
				i--;
				files2.remove(corrFile);
			}
		}
		for(int i=0;i<files2.size();i++)
		{
			File vecFile=files2.get(i);
			success=false;
			_log.error("DIFF: File "+vecFile.getParent()+"/"+vecFile.getName()+" in export directory doesn't exist in source directory.");
		}
		if(success)
			_log.info("Comparison completely successful");
		else
			_log.error("Comparison NOT successful.");
		
	}
	
	
	public void compareFiles(File sourceFile, File exportFile)
	{
		Vector<String> linesSourceFile=readContents(sourceFile);
		Vector<String> linesExportFile=readContents(exportFile);
		if(linesSourceFile.size()!=linesExportFile.size())
		{
			success=false;
			_log.error("DIFF: Files "+sourceFile.getParent()+"/"+sourceFile.getName()+"(sources) and "+exportFile.getParent()+"/"+exportFile.getName()+"(export) have different number of lines.");
			return;
		}
		for(int i=0;i<linesSourceFile.size();i++)
		{
			if(!linesSourceFile.get(i).equals(linesExportFile.get(i)))
			{
				success=false;
				_log.error("DIFF: Files "+sourceFile.getParent()+"/"+sourceFile.getName()+"(sources) and "+exportFile.getParent()+"/"+exportFile.getName()+"(export) are not equal. First difference in line "+i+".");
				return;
			}
		}
		//_log.info("Files "+sourceFile.getParent()+"/"+sourceFile.getName()+"(sources) and "+exportFile.getParent()+"/"+exportFile.getName()+"(export) are equal.");
	}
	
	
	void recursiveRead(File dir, Vector<File> vector)
	{
		if(dir.isFile())
		{
			if(dir.getName().endsWith(".xml") && !dir.getName().startsWith(".") && !dir.getParent().contains("sampledata"))
			{
				vector.add(dir);
			}
		}
		else if(dir.isDirectory())
		{
			File[] files=dir.listFiles();
			Arrays.sort(files);
			for(int i=0;i<files.length;i++)
			{
				recursiveRead(files[i],vector);
			}
		}
	}
	
	File findCorrespondingFile(Vector<File> vector, File file)
	{
		File auxFile=null;
		int i=0;
		while(auxFile==null && i<vector.size())
		{
			File vecFile=vector.get(i);
			if(file.getName().equals(vecFile.getName()) &&
					(file.getParentFile().getName().equals(vecFile.getParentFile().getName())) ||
					(file.getParent().equals(getDir1()) && vecFile.getParent().equals(getDir2())))
				auxFile=vecFile;
			i++;
			
		}
		return auxFile;
	}
	
	
	static public Vector<String> readContents(File aFile) {
		Vector<String> contents = new Vector<String>();
	    try {
	      BufferedReader input =  new BufferedReader(new FileReader(aFile));
	      try {
	        String line = null;
	        while (( line = input.readLine()) != null){
	          contents.add(line);
	        }
	      }
	      finally {
	        input.close();
	      }
	    }
	    catch (IOException ex){
	      ex.printStackTrace();
	    }
	    return contents;
	  }
	
	
	public String getDir1() {
		return dir1;
	}
	public void setDir1(String dir1) {
		this.dir1 = dir1;
	}
	public String getDir2() {
		return dir2;
	}
	public void setDir2(String dir2) {
		this.dir2 = dir2;
	}

}