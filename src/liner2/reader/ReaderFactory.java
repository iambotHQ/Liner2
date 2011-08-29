package liner2.reader;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

import liner2.reader.CclStreamReader;
import liner2.reader.IobStreamReader;

import liner2.LinerOptions;

public class ReaderFactory {

	private static final ReaderFactory factory = new ReaderFactory();
	
	private ReaderFactory(){
		
	}
	
	public static ReaderFactory get(){
		return ReaderFactory.factory; 
	}
	
	/**
	 * TODO
	 * @return
	 */
	public StreamReader getStreamReader(String inputFile, String inputFormat) throws Exception {
		if (inputFormat.equals("ccl"))
			return new CclStreamReader(getInputStream(inputFile));
		else if (inputFormat.equals("iob"))
			return new IobStreamReader(getInputStream(inputFile));
		else
			throw new Exception("Input format " + inputFormat + " not recognized.");
	}
	
	private InputStream getInputStream(String inputFile) throws Exception {
		if (inputFile.isEmpty())
			return System.in;
		else {
			try {
				return new FileInputStream(inputFile);
			} catch (IOException ex) {
				throw new Exception("Unable to read input file: " + inputFile);
			}
		}
	}
}
