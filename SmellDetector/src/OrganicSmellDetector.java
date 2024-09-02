
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.pucrio.opus.smells.Organic;
import br.pucrio.opus.smells.collector.SmellName;
import br.pucrio.opus.smells.resources.Method;
import br.pucrio.opus.smells.resources.Type;

public class OrganicSmellDetector extends SmellDetector {

	private String projectDirectory;
	private Map<SmellType, Set<Smell>> detectedSmells;

	public OrganicSmellDetector(String projectDirectory) {
	    this.projectDirectory = projectDirectory;
	    this.detectedSmells = new HashMap<>();
	}
	
	private static final Set<SmellType> SUPPORTED_SMELL_TYPES = Collections.unmodifiableSet(
			new HashSet<SmellType>(Arrays.asList(SmellType.CLASS_DATA_SHOULD_BE_PRIVATE,
												 SmellType.COMPLEX_CLASS,
												 SmellType.FEATURE_ENVY,
												 SmellType.GOD_CLASS,
												 SmellType.LAZY_CLASS,
												 SmellType.LONG_METHOD,
												 SmellType.LONG_PARAMETER_LIST,
												 SmellType.MESSAGE_CHAIN,
												 SmellType.REFUSED_PARENT_BEQUEST,
												 SmellType.SPECULATIVE_GENERALITY,
												 SmellType.SPAGHETTI_CODE,
												 SmellType.DISPERSED_COUPLING,
												 SmellType.INTENSIVE_COUPLING,
												 SmellType.BRAIN_CLASS,
												 SmellType.SHOTGUN_SURGERY,
												 SmellType.BRAIN_METHOD,
												 SmellType.DATA_CLASS)));
	@Override
	public Set<SmellType> getSupportedSmellTypes() {
		return SUPPORTED_SMELL_TYPES;
	}

	@Override
	public String getDetectorName() {
		return "Organic";
	}
	
	private static final Map<SmellName, SmellType> MAP_FROM_SMELLNAME_TO_SMELLTYPE;
	static {
		MAP_FROM_SMELLNAME_TO_SMELLTYPE = new HashMap<>(17);
		MAP_FROM_SMELLNAME_TO_SMELLTYPE.put(SmellName.ClassDataShouldBePrivate, SmellType.CLASS_DATA_SHOULD_BE_PRIVATE);
		MAP_FROM_SMELLNAME_TO_SMELLTYPE.put(SmellName.ComplexClass, SmellType.COMPLEX_CLASS);
		MAP_FROM_SMELLNAME_TO_SMELLTYPE.put(SmellName.FeatureEnvy, SmellType.FEATURE_ENVY);
		MAP_FROM_SMELLNAME_TO_SMELLTYPE.put(SmellName.GodClass, SmellType.GOD_CLASS);
		MAP_FROM_SMELLNAME_TO_SMELLTYPE.put(SmellName.LazyClass, SmellType.LAZY_CLASS);
		MAP_FROM_SMELLNAME_TO_SMELLTYPE.put(SmellName.LongMethod, SmellType.LONG_METHOD);
		MAP_FROM_SMELLNAME_TO_SMELLTYPE.put(SmellName.LongParameterList, SmellType.LONG_PARAMETER_LIST);
		MAP_FROM_SMELLNAME_TO_SMELLTYPE.put(SmellName.MessageChain, SmellType.MESSAGE_CHAIN);
		MAP_FROM_SMELLNAME_TO_SMELLTYPE.put(SmellName.RefusedBequest, SmellType.REFUSED_PARENT_BEQUEST);
		MAP_FROM_SMELLNAME_TO_SMELLTYPE.put(SmellName.SpeculativeGenerality, SmellType.SPECULATIVE_GENERALITY);
		MAP_FROM_SMELLNAME_TO_SMELLTYPE.put(SmellName.SpaghettiCode, SmellType.SPAGHETTI_CODE);
		MAP_FROM_SMELLNAME_TO_SMELLTYPE.put(SmellName.DispersedCoupling, SmellType.DISPERSED_COUPLING);
		MAP_FROM_SMELLNAME_TO_SMELLTYPE.put(SmellName.IntensiveCoupling, SmellType.INTENSIVE_COUPLING);
		MAP_FROM_SMELLNAME_TO_SMELLTYPE.put(SmellName.BrainClass, SmellType.BRAIN_CLASS);
		MAP_FROM_SMELLNAME_TO_SMELLTYPE.put(SmellName.ShotgunSurgery, SmellType.SHOTGUN_SURGERY);
		MAP_FROM_SMELLNAME_TO_SMELLTYPE.put(SmellName.BrainMethod, SmellType.BRAIN_METHOD);
		MAP_FROM_SMELLNAME_TO_SMELLTYPE.put(SmellName.DataClass, SmellType.DATA_CLASS);
	}

	@Override
	// Method to find smells in the specified Java project
	public void findSmells(SmellType smellType, Map<SmellType, Set<Smell>> detectedSmells) throws Exception {

	    List<String> sourcePath = new ArrayList<>();
	    sourcePath.add(projectDirectory);
	    Organic organicPlugin = new Organic();

	    List<Type> classTypeDeclarations = organicPlugin.loadAllTypes(sourcePath);
	    organicPlugin.collectTypeMetrics(classTypeDeclarations);
	    organicPlugin.detectSmells(classTypeDeclarations);
	    classTypeDeclarations = organicPlugin.onlySmelly(classTypeDeclarations);

	    for (Type classTypeDeclaration : classTypeDeclarations) {
	        String fullClassPath = classTypeDeclaration.getFullyQualifiedName();
	        String className = fullClassPath.substring(fullClassPath.lastIndexOf('.') + 1);

	        File sourceFile = classTypeDeclaration.getSourceFile().getFile();

	        // Adjust the path accordingly, assuming getFilePath() returns the path as a String
	        String FilePath = sourceFile.getAbsolutePath();

	        // Convert to File object
	        File targetFile = new File(FilePath);

	        // Extract smells using the file path
	        extractSmells(smellType, detectedSmells, classTypeDeclaration.getSmells(), className, targetFile);

	        for (Method methodTypeDeclaration : classTypeDeclaration.getMethods()) {
	            extractSmells(smellType, detectedSmells, methodTypeDeclaration.getSmells(), className, targetFile);
	        }
	    }
	}

	
	private void extractSmells(SmellType smellType, Map<SmellType, Set<Smell>> detectedSmells, 
			   List<br.pucrio.opus.smells.collector.Smell> toolSmells,String className, File targetFile) throws Exception {
		for(br.pucrio.opus.smells.collector.Smell smell: toolSmells) {
			SmellType detectedSmellType = MAP_FROM_SMELLNAME_TO_SMELLTYPE.get(smell.getName());

			int startingLine = smell.getStartingLine();

			if(Utils.isClassSmell(detectedSmellType)) {
				Utils.addSmell(detectedSmellType, detectedSmells, getDetectorName(),
						Utils.createSmellObject(detectedSmellType, className, targetFile, startingLine));
			} else {
				String methodName = (String) Utils.extractMethodNameAndCorrectLineFromFile(targetFile, startingLine)[0];
				Utils.addSmell(detectedSmellType, detectedSmells, getDetectorName(),
						Utils.createSmellObject(detectedSmellType, className, methodName, targetFile, startingLine));
			}
		}
	}
	
	public void printDetectedSmells() {
	    if (detectedSmells == null || detectedSmells.isEmpty()) {
	        System.out.println("No smells detected.");
	        return;
	    }

	    for (Map.Entry<SmellType, Set<Smell>> entry : detectedSmells.entrySet()) {
	        SmellType smellType = entry.getKey();
	        Set<Smell> smells = entry.getValue();
	        
	        System.out.println("Smell Type: " + smellType);
	        for (Smell smell : smells) {
	            smell.print(); // Calling the existing print method in the Smell class
	            System.out.println(); // Adding a new line for better readability
	        }
	    }
	}
	
	public Map<SmellType, Set<Smell>> getDetectedSmells() {
        return this.detectedSmells;
    }
	
}
