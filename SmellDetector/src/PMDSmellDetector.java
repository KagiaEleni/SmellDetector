
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class PMDSmellDetector extends SmellDetector {
	
	private String projectDirectory;
	private Map<SmellType, Set<Smell>> detectedSmells;

	    public PMDSmellDetector(String projectDirectory) {
	        this.projectDirectory = projectDirectory;
	        this.detectedSmells = new HashMap<>();
	    }
	    
	private static final Set<SmellType> SUPPORTED_SMELL_TYPES = Collections.unmodifiableSet(
			new HashSet<SmellType>(Arrays.asList(SmellType.DUPLICATE_CODE,
												SmellType.GOD_CLASS,
												SmellType.LONG_METHOD,
												SmellType.LONG_PARAMETER_LIST)));
	
	@Override
	public Set<SmellType> getSupportedSmellTypes() {
		return SUPPORTED_SMELL_TYPES;
	}

	@Override
	public String getDetectorName() {
		return "PMD";
	}
	
	@Override
	public void findSmells(SmellType smellType, Map<SmellType, Set<Smell>> detectedSmells) throws Exception {
	  
		detectPMDSmells(smellType, detectedSmells);
        detectCPDDuplicates(detectedSmells);
                
        this.detectedSmells = detectedSmells;
        System.out.println("End PMD");

    }
	
	/**
	 * A method responsible to find and extract the duplicate code smells for the selected
	 * project using CPD.
	 * 
	 * @param detectedSmells a {@code Map} from smellType to a {@code Set} of detected smells
	 * @throws Exception
	 */
	private void detectCPDDuplicates(Map<SmellType, Set<Smell>> detectedSmells) throws Exception {
		File cpdBatFile = new File(getClass().getClassLoader().getResource("pmd-bin-6.37.0/bin/cpd.bat").toURI());
		
		String cpdOutput = Utils.runCommand(buildDuplicateCodeToolCommand(cpdBatFile), null, true);
		Document cpdXmlDoc = Utils.getXmlDocument(cpdOutput);
		
		extractDuplicates(cpdXmlDoc, detectedSmells, projectDirectory);
	}
	
	/**
	 * Builds a list that includes (in parts) the needed command to trigger the duplicate
	 * code tool via the command line and produce the detection results.
	 * 
	 * @param duplicateCodeToolBatFile the file of the duplicate code tool
	 * @return a list with the needed command
	 */
	private List<String> buildDuplicateCodeToolCommand(File duplicateCodeToolBatFile) {
	    List<String> duplicateCodeToolCmdList = new ArrayList<String>();
	    
	    try {
	        duplicateCodeToolCmdList.add("cmd");
	        duplicateCodeToolCmdList.add("/c");
	        duplicateCodeToolCmdList.add(duplicateCodeToolBatFile.getPath());
	        duplicateCodeToolCmdList.add("--minimum-tokens");
	        duplicateCodeToolCmdList.add("100");
	        duplicateCodeToolCmdList.add("--files");
	        duplicateCodeToolCmdList.add(projectDirectory); 
	        duplicateCodeToolCmdList.add("--format");
	        duplicateCodeToolCmdList.add("xml");
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    
	    return duplicateCodeToolCmdList;
	}

	
	/**
	 * Extracts Duplicate Code smells and returns a set that contains all of them.
	 * 
	 * @param xmlDoc an XML {@code Document} that contains the results of the detection
	 * @param detectedSmells a {@code Map} from smellType to a {@code Set} of detected smells
	 * @throws Exception 
	 */
	private void extractDuplicates(Document xmlDoc, Map<SmellType, Set<Smell>> detectedSmells, 
			String projectDirectory) throws Exception {
	    int duplicationGroupId = Utils.getGreatestDuplicationGroupId(detectedSmells);
	    
	    NodeList duplicationNodes = xmlDoc.getDocumentElement().getElementsByTagName("duplication");
	    for (int i = 0; i < duplicationNodes.getLength(); i++) {
	        
	        NodeList fileEntries = duplicationNodes.item(i).getChildNodes();
	        for (int j = 0; j < fileEntries.getLength(); j++) {
	            Node fileEntry = fileEntries.item(j);
	            // Each duplication has a <codefragment> element which only contains the duplicate code
	            // and doesn't have any attributes. So this is recognized this way and is ignored
	            if (!fileEntry.hasAttributes())
	                continue;
	            
	            NamedNodeMap fileEntryAttributes = fileEntry.getAttributes();
	            int startLine = Integer.parseInt(fileEntryAttributes.getNamedItem("line").getNodeValue());
	            int endLine = Integer.parseInt(fileEntryAttributes.getNamedItem("endline").getNodeValue());
	            String path = fileEntryAttributes.getNamedItem("path").getNodeValue();
	            String className = path.substring(path.lastIndexOf("\\") + 1);
	            String filePath = path.substring(path.indexOf("\\", path.indexOf(projectDirectory)));
	            File targetFile = new File(filePath);
	            
	            Utils.addSmell(SmellType.DUPLICATE_CODE, detectedSmells, getDetectorName(),
	                    Utils.createSmellObject(SmellType.DUPLICATE_CODE, duplicationGroupId, className,
	                    		targetFile, startLine, endLine));
	        }
	        
	        duplicationGroupId++;
	    }
	}

	
	/**
	 * A method responsible to find and extract the rest of the smells that are supported
	 * by PMD.
	 * 
	 * @param smellType the type of smells to be detected
	 * @param detectedSmells a {@code Map} from smellType to a {@code Set} of detected smells
	 * @throws Exception
	 */
	private void detectPMDSmells(SmellType smellType, Map<SmellType, Set<Smell>> detectedSmells) throws Exception {
		File pmdBatFile = new File(getClass().getClassLoader().getResource("pmd-bin-6.37.0/bin/pmd.bat").toURI());
		File pmdConfigFile = new File(getClass().getClassLoader().getResource("pmd-bin-6.37.0/bin/pmd-config.xml").toURI());
		File pmdCacheFile = new File(getClass().getClassLoader().getResource("pmd-bin-6.37.0/bin/pmd-cache.txt").toURI());
		
		String pmdOutput = Utils.runCommand(buildMainToolCommand(pmdBatFile, pmdConfigFile, pmdCacheFile), null, true);
		Document pmdXmlDoc = Utils.getXmlDocument(pmdOutput);
		
		extractSmells(smellType, pmdXmlDoc, detectedSmells);
	}
	
	/**
	 * A map that contains the code smells detected from the tool as the key, and their
	 * corresponding {@code SmellType} as the value.
	 */
	private static final Map<String, SmellType> MAP_FROM_DECTECTED_SMELLS_TO_SMELLTYPE;
	static {
		Map<String, SmellType> tempMap = new HashMap<String, SmellType>(3);
		tempMap.put("GodClass", SmellType.GOD_CLASS);
		tempMap.put("ExcessiveMethodLength", SmellType.LONG_METHOD);
		tempMap.put("ExcessiveParameterList", SmellType.LONG_PARAMETER_LIST);
		MAP_FROM_DECTECTED_SMELLS_TO_SMELLTYPE = Collections.unmodifiableMap(tempMap);
	}

	/**
	 * Extracts God Class, Long Method and Long Parameter List code smells and adds them
	 * to the {@code Map} of detected smells.
	 * 
	 * @param smellType the type of smell to be detected
	 * @param xmlDoc an XML {@code Document} that contains the results of the detection
	 * @param detectedSmells a {@code Map} from smellType to a {@code Set} of detected smells
	 * @throws Exception
	 */
	private void extractSmells(SmellType smellType, Document xmlDoc, Map<SmellType, Set<Smell>> detectedSmells) throws Exception {
	    NodeList fileNodes = xmlDoc.getDocumentElement().getElementsByTagName("file");
	    for (int fileIndex = 0; fileIndex < fileNodes.getLength(); fileIndex++) {
	        Node fileNode = fileNodes.item(fileIndex);
	        String fileName = fileNode.getAttributes().getNamedItem("name").getNodeValue();
	        String filePath = fileName.substring(fileName.indexOf("\\", fileName.indexOf("\\", projectDirectory.length())));
	        File targetFile = new File(filePath); // Create a java.io.File object here
	        
	        NodeList violationNodes = fileNode.getChildNodes();
	        for (int i = 0; i < violationNodes.getLength(); i++) {
	            Node violationNode = violationNodes.item(i);
	            // This is needed because there are unexpected children, e.g. #text
	            if (!violationNode.getNodeName().equals("violation"))
	                continue;
	            
	            SmellType detectedSmellType = MAP_FROM_DECTECTED_SMELLS_TO_SMELLTYPE.get(violationNode.getAttributes().getNamedItem("rule").getNodeValue());
	            if (smellType != SmellType.ALL_SMELLS && smellType != detectedSmellType)
	                continue;
	            
	            int startLine = Integer.parseInt(violationNode.getAttributes().getNamedItem("beginline").getNodeValue());
	            String className = violationNode.getAttributes().getNamedItem("class").getNodeValue();
	            
	            if (detectedSmellType == SmellType.GOD_CLASS) {
	                Utils.addSmell(detectedSmellType, detectedSmells, getDetectorName(),
	                        Utils.createSmellObject(SmellType.GOD_CLASS, className, targetFile, startLine));
	            } else {
	                String methodName = violationNode.getAttributes().getNamedItem("method").getNodeValue();
	                Utils.addSmell(detectedSmellType, detectedSmells, getDetectorName(),
	                        Utils.createSmellObject(detectedSmellType, className, methodName, targetFile, startLine));
	            }
	        }
	    }
	}

	
	/**
	 * Builds a list that includes (in parts) the needed command to execute the main tool via
	 * the command line and produce the smell detection results.
	 * 
	 * @param mainToolBatFile the file of the main tool
	 * @param configFile the configuration file of the main tool
	 * @param cacheFile the file which contains cache details for the tool
	 * @return a list with the needed command
	 */
	private List<String> buildMainToolCommand(File mainToolBatFile, File configFile, File cacheFile) {
		List<String> mainToolCmdList = new ArrayList<String>();
		
		try {
			mainToolCmdList.add("cmd");
			mainToolCmdList.add("/c");
			mainToolCmdList.add(mainToolBatFile.getPath());
			mainToolCmdList.add("-d");
			mainToolCmdList.add(projectDirectory);
			mainToolCmdList.add("-cache");
			mainToolCmdList.add(cacheFile.getPath());
			mainToolCmdList.add("-f");
			mainToolCmdList.add("xml");
			mainToolCmdList.add("-R");
			mainToolCmdList.add(configFile.getPath());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		return mainToolCmdList;
	}

	@Override
	public Map<SmellType, Set<Smell>> getDetectedSmells() {
		// TODO Auto-generated method stub
		return this.detectedSmells;
	}
		
}
