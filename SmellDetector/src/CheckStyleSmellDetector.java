import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class CheckStyleSmellDetector extends SmellDetector {

    private String projectDirectory;
    private Map<SmellType, Set<Smell>> detectedSmells;

    public CheckStyleSmellDetector(String projectDirectory) {
        this.projectDirectory = projectDirectory;
        this.detectedSmells = new HashMap<>();
    }

    private static final Set<SmellType> SUPPORTED_SMELL_TYPES = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(SmellType.GOD_CLASS, SmellType.LONG_METHOD, SmellType.LONG_PARAMETER_LIST)));

    @Override
    public Set<SmellType> getSupportedSmellTypes() {
        return SUPPORTED_SMELL_TYPES;
    }

    @Override
    public String getDetectorName() {
        return "CheckStyle";
    }

    @Override
    public void findSmells(SmellType smellType, Map<SmellType, Set<Smell>> detectedSmells) throws URISyntaxException {
    	File checkStyleJarFile = new File(getClass().getClassLoader().getResource("checkstyle-10.12.1/checkstyle-10.12.1-all.jar").toURI());
    	File checkStyleConfigFile = new File(getClass().getClassLoader().getResource("checkstyle-10.12.1/checkstyle-config.xml").toURI());

		
		String toolOutput = null;
		try {
			toolOutput = Utils.runCommand(buildToolCommand(checkStyleJarFile, checkStyleConfigFile), null, true);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Document xmlDoc = Utils.getXmlDocument(toolOutput);
		
        try {
			extractSmells(smellType, xmlDoc, detectedSmells);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        this.detectedSmells = detectedSmells;
        System.out.println("End CheckStyle");

    }
    
    /**
	 * Builds a list that includes (in parts) the needed command to execute the tool via
	 * the command line and produce the smell detection results.
	 * 
	 * @param checkStyleJarFile the file of the tool
	 * @param checkStyleConfigFile the configuration file of the tool
	 * @return a list with the needed command
	 */
	private List<String> buildToolCommand(File checkStyleJarFile, File checkStyleConfigFile) {
		List<String> checkStyleCmdList = new ArrayList<>();
		
		try {
			checkStyleCmdList.add("cmd");
			checkStyleCmdList.add("/c");
			checkStyleCmdList.add("java");
			checkStyleCmdList.add("-jar");
			checkStyleCmdList.add(checkStyleJarFile.getPath());
			checkStyleCmdList.add("-c");
			checkStyleCmdList.add(checkStyleConfigFile.getPath());
			checkStyleCmdList.add("-f");
			checkStyleCmdList.add("xml");
			checkStyleCmdList.add(projectDirectory);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		return checkStyleCmdList;
	}
	
	/**
	 * A map that contains the code smells detected from the tool as the key, and their
	 * corresponding {@code SmellType} as the value.
	 */
	private static final Map<String, SmellType> MAP_FROM_DECTECTED_SMELLS_TO_SMELLTYPE;
	static {
		Map<String, SmellType> tempMap = new HashMap<String, SmellType>(3);
		tempMap.put("FileLengthCheck", SmellType.GOD_CLASS);
		tempMap.put("MethodLengthCheck", SmellType.LONG_METHOD);
		tempMap.put("ParameterNumberCheck", SmellType.LONG_PARAMETER_LIST);
		MAP_FROM_DECTECTED_SMELLS_TO_SMELLTYPE = Collections.unmodifiableMap(tempMap);
	}

	/**
	 * Extracts God Class, Long Method and Long Parameter List code smells and returns a set
	 * that contains all of them.
	 * 
	 * @param xmlDoc an XML {@code Document} that contains the results of the detection
	 * @param detectedSmells a {@code Map} from smellType to a {@code Set} of detected smells
	 * @return a set which contains all the detected smells of the given smell type
	 * @throws Exception
	 */
	private void extractSmells(SmellType smellType, Document xmlDoc, Map<SmellType, Set<Smell>> detectedSmells) throws Exception {
		NodeList fileNodes = xmlDoc.getDocumentElement().getElementsByTagName("file");
		for(int i = 0; i < fileNodes.getLength(); i++) {
			Node fileNode = fileNodes.item(i);
			
			String fileName = fileNode.getAttributes().getNamedItem("name").getNodeValue();
			String filePath = fileName.substring(fileName.indexOf("\\", fileName.indexOf(projectDirectory + 1)));
			File targetFile = new File(filePath);
			String className = fileName.substring(fileName.lastIndexOf('\\') + 1).replace(".java", "");
			
			NodeList errorNodes = fileNode.getChildNodes();
			for(int j = 0; j < errorNodes.getLength(); j++) {
				Node errorNode = errorNodes.item(j);
				//This is needed because there are unexpected children
				if(!errorNode.getNodeName().equals("error"))
					continue;
				
				String source = errorNode.getAttributes().getNamedItem("source").getNodeValue();
				SmellType detectedSmellType = MAP_FROM_DECTECTED_SMELLS_TO_SMELLTYPE.get(source.substring(source.lastIndexOf('.') + 1));
								
				int startLine = Integer.parseInt(errorNode.getAttributes().getNamedItem("line").getNodeValue());
				
				if(detectedSmellType == SmellType.GOD_CLASS) {
					//CheckStyle returns line 1 in case a GodClass is found, instead of the line in which the class is declared
					Utils.addSmell(detectedSmellType, detectedSmells, getDetectorName(),
							Utils.createSmellObject(detectedSmellType, className, targetFile, startLine));
				} else {
					String methodName = "";
					if(detectedSmellType == SmellType.LONG_PARAMETER_LIST) {
						methodName = (String) Utils.extractMethodNameAndCorrectLineFromFile(targetFile, startLine)[0];
					} else {
						String message = errorNode.getAttributes().getNamedItem("message").getNodeValue().replace("Method ", "");
						methodName = message.substring(0, message.indexOf(" "));
					}
					
					Utils.addSmell(detectedSmellType, detectedSmells, getDetectorName(),
							Utils.createSmellObject(detectedSmellType, className, methodName, targetFile, startLine));
				}
			}
		}
	}

	@Override
	public Map<SmellType, Set<Smell>> getDetectedSmells() {
		// TODO Auto-generated method stub
		return this.detectedSmells;
	}
	
}