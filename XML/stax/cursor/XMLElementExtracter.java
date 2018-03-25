//================================================================================
// [Function]
// Split a XML of large size into XML files containing certain number of elements.
//
// [Arguments] 
// - inputFile   : Input XML file.
// - elementName : The XML element name to extract.
// - start       : Start extracting XML element from the position of the elements.
// - stop        : Stop  extracting XML element at the position.  
//================================================================================
package stax.cursor;

import java.util.Vector;
import java.util.Enumeration;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;

import javax.xml.namespace.*;
import javax.xml.stream.*;
import javax.xml.stream.events.*;

public class XMLElementExtracter {
	//--------------------------------------------------------------------------------
	// Program arguments
	//--------------------------------------------------------------------------------
	private String elementName = null;
	public String getElementName() {
		return elementName;
	}
	public void setElementName(String elementName) {
		this.elementName = elementName;
	}
	private long start = -1;
	public long getStart() {
		return start;
	}
	public void setStart(long start) {
		this.start = start;
	}
	private long stop  = -1;
	public long getStop() {
		return stop;
	}
	public void setStop(long stop) {
		this.stop = stop;
	}
	private long units = -1;
	public long getUnits() {
		return units;
	}
	public void setUnits(long units) {
		this.units = units;
	}
	
	//--------------------------------------------------------------------------------
	// Control variables
	// - counter  : Current position of the XML element
	//--------------------------------------------------------------------------------
	private long counter = 0;	
	public long getCounter() {
		return counter;
	}
	public void setCounter(long counter) {
		this.counter = counter;
	}
	public void incrementCounter(){
		setCounter(getCounter() + 1);
	}
	public void resetCounter(){
		setCounter(0);
	}
	private long fileNumber = 1;
	public long getFileNumber() {
		return fileNumber;
	}
	public void setFileNumber(long fileNumber) {
		this.fileNumber = fileNumber;
	}
	public void incrementFileNumber(){
		setFileNumber(getFileNumber()+1);
	}


	//--------------------------------------------------------------------------------
	// XML input file handler
	//--------------------------------------------------------------------------------
	private File inputFile = null;
	private XMLEventReader reader = null;
	public File getInputFile() throws Exception{
		if(this.inputFile == null){
			throw(new Exception("getInputFile() called for null file"));
		}
		return inputFile;
	}
	public void setInputFile(String inputFileName) throws Exception{
		inputFile = new File(inputFileName);
		if (!inputFile.exists()) {
			System.out.println("File [" + inputFile.getAbsolutePath() + "] does not exist.");
			inputFile = null;
			throw(new Exception());
		} else {
			System.out.println("Process input XML [" + inputFile.getAbsolutePath() + "].");
		}
	}
	public XMLEventReader getParser() throws Exception{
		if(this.reader == null){
			XMLInputFactory xmlif = XMLInputFactory.newInstance();
			xmlif.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.TRUE);
			xmlif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
			// set the IS_COALESCING property to true , if application desires to get whole text data as one event.
			xmlif.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
			this.reader = xmlif.createXMLEventReader(new FileReader(getInputFile()));
		}
		return(this.reader);
	}
	
	//--------------------------------------------------------------------------------
	// XML output file handler
	//--------------------------------------------------------------------------------
	private StartElement rootElement = null;
	public StartElement getRootElement() {
		return rootElement;
	}
	public void setRootElement(StartElement rootElement) {
		this.rootElement = rootElement;
	}

	private XMLEventWriter writer = null;
	public XMLEventWriter getWriter() throws Exception{
		if(this.writer == null){
			//--------------------------------------------------------------------------------
			// Open the output XML file and add the XML declaration and a root element.
			// Using the outer most element, not the direct parent of the target XML, as the root. 
			//--------------------------------------------------------------------------------
			this.writer = XMLOutputFactory.newFactory().createXMLEventWriter(
					new FileWriter(new File("out/" + java.lang.Long.valueOf(getFileNumber()) + ".xml")));
			
			StartDocument startDocument = XMLEventFactory.newFactory().createStartDocument();
			this.writer.add(startDocument);
			this.writer.add(getRootElement());
		}
		return writer;
	}
	public void setWriter(XMLEventWriter writer) {
		this.writer = writer;
	}


	//--------------------------------------------------------------------------------
	// Log utility
	//--------------------------------------------------------------------------------
	private boolean isLogging = false;
	public boolean isLogging() {
		return isLogging;
	}

	public void setLogging(boolean isLogging) {
		this.isLogging = isLogging;
	}
	void log(String message){
		if(isLogging()){
			System.out.println(message);
		}
	}	


//====================================================================================================
	
	//--------------------------------------------------------------------------------
	// Commit the output XML
	//--------------------------------------------------------------------------------
	void commit() throws Exception{
		if(getCounter() > 0){
			EndDocument endDocument = XMLEventFactory.newFactory().createEndDocument();
			getWriter().add(XMLEventFactory.newFactory().createEndElement(getRootElement().getName(), null));
			getWriter().add(endDocument);
			getWriter().flush();
			getWriter().close();
			setWriter(null);
			
			log("Flushing file -----> " + getFileNumber());
			resetCounter();
			incrementFileNumber();
		} else {
			log("commit() is called but there is nothing to commit.");
		}
	}

	//--------------------------------------------------------------------------------
	// Append the XML element to the output file.
	//--------------------------------------------------------------------------------
	void append(XMLEventReader parser, XMLEvent startEvent) throws Exception{
		
		XMLEvent event = startEvent;
		do{
			//--------------------------------------------------------------------------------
			// Target XML element as start element
			//--------------------------------------------------------------------------------
			getWriter().add(event);
			//--------------------------------------------------------------------------------
			// Keep adding the child elements until end element is encountered.
			//--------------------------------------------------------------------------------
			event = parser.nextEvent();
		} while(!(event.isEndElement() && event.asEndElement().getName().equals(startEvent.asStartElement().getName())));
		//--------------------------------------------------------------------------------
		// Target XML element as end element
		//--------------------------------------------------------------------------------
		getWriter().add(event);
		
		//--------------------------------------------------------------------------------
		// When number of elements reached to the pre-defined unit number, commit to the file.
		//--------------------------------------------------------------------------------
		incrementCounter();
		if(getCounter() >= getUnits()){
			commit();
		}
	}
	
	//--------------------------------------------------------------------------------
	// Go through the input XML and extract target element into a file for each element.
	//--------------------------------------------------------------------------------
	void extractElements() throws Exception{
		XMLEventReader parser = getParser();
		try {
			//--------------------------------------------------------------------------------
			// Go to the outer-most XML element and use it as the root element. 
			//--------------------------------------------------------------------------------
			setRootElement(parser.nextTag().asStartElement());

			//--------------------------------------------------------------------------------
			// Start extracting the target element and its child elements.
			//--------------------------------------------------------------------------------
			long count = 0;
			while (parser.hasNext()) {
				XMLEvent event = parser.nextEvent();
				if (event.isStartElement()){
					String name = event.asStartElement().getName().getLocalPart();
					log(event.asStartElement().getName().toString());
					if(name.equals(getElementName())) {
						if(getStart() <= ++count){
							append(parser, event);
						} else {
							continue;
						}
						if(getStop() > 0 && count >= getStop()){
							break;
						}
					}
				}
			}
		} finally {
			commit();
		}
	}

	void run(String[] args) throws Exception{
		if(args.length > 5 && args[5].toLowerCase().equals("y")){
			isLogging = true;
		}
		setInputFile(args[0]);
		setElementName(args[1]);
		setStart(Long.parseLong(args[2]));
		setStop(Long.parseLong(args[3]));
		setUnits(Long.parseLong(args[4]));
	
		long starttime = System.currentTimeMillis();

		try	{
			extractElements();
		} finally {
			if (getParser() != null){
				getParser().close();
			}
		}
		long endtime = System.currentTimeMillis();
		System.out.println(" Parsing Time = " + (endtime - starttime));
	
	}

	private static void printUsage() {
		System.out.println("java -Djava.endorsed.dirs=<jaxp dist/lib directory> <ExtractXMLElement> <xmlfile> <element> <start> <end> <units> [verbose]");
	}

	public static void main(String[] args) throws Exception {
		String argv[] = null;
		if (args.length >= 5){
			argv = args;
		} else {
			argv = new String[6];
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	        System.out.print("XML filename to process?\n");
	        argv[0] = reader.readLine();
	        System.out.print("Entity name to extract?\n");
	        argv[1] = reader.readLine();
	        System.out.print("Start entity position to extract?\n");
	        argv[2] = reader.readLine();
	        System.out.print("Stop entity position to extract?\n");
	        argv[3] = reader.readLine();
	        System.out.print("Number of units per file?\n");
	        argv[4] = reader.readLine();
	        System.out.print("Verbose output? [Y|y]\n");
	        argv[5] = reader.readLine();
		}
		new XMLElementExtracter().run(argv);
	}
}
