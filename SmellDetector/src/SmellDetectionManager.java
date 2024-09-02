
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class SmellDetectionManager {
	
	private SmellType smellTypeToBeDetected;
	private List<SmellDetector> smellDetectors;
	private String projectDirectory;
	private Map<SmellType, Set<Smell>> detectedSmells;


	
	public SmellDetectionManager(SmellType smellType, String projectDirectory) {
		this.smellTypeToBeDetected = smellType;
        this.projectDirectory = projectDirectory;
        try {
			initialiseNecessaryClassFields();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void initialiseNecessaryClassFields() throws URISyntaxException {

		detectedSmells = new HashMap<>();
        
        smellDetectors = new ArrayList<>(6);
        boolean useAllDetectors = (smellTypeToBeDetected == SmellType.ALL_SMELLS);
        
        if (useAllDetectors || smellTypeToBeDetected == SmellType.PMD) {
        	 PMDSmellDetector pmdDetector = new PMDSmellDetector(projectDirectory);
        	 try {
				pmdDetector.findSmells(smellTypeToBeDetected, detectedSmells);
				updateDetectedSmells(pmdDetector);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
             smellDetectors.add(pmdDetector);
        }
        
        if (useAllDetectors || smellTypeToBeDetected == SmellType.CHECKSTYLE) {
            CheckStyleSmellDetector checkStyleDetector = new CheckStyleSmellDetector(projectDirectory);
            checkStyleDetector.findSmells(smellTypeToBeDetected, detectedSmells);
            updateDetectedSmells(checkStyleDetector);
            smellDetectors.add(checkStyleDetector);
        }
        
        if (useAllDetectors || smellTypeToBeDetected == SmellType.DUDE) {
            DuDeSmellDetector dudeDetector = new DuDeSmellDetector(projectDirectory);
            try {
				dudeDetector.findSmells(smellTypeToBeDetected, detectedSmells);
				updateDetectedSmells(dudeDetector);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            smellDetectors.add(dudeDetector);
        }
               
        if (useAllDetectors || smellTypeToBeDetected == SmellType.ORGANIC) {
            OrganicSmellDetector organicDetector = new OrganicSmellDetector(projectDirectory);
            try {
				organicDetector.findSmells(smellTypeToBeDetected, detectedSmells);
				updateDetectedSmells(organicDetector);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            smellDetectors.add(organicDetector);
        }
	}
	
	
	private void updateDetectedSmells(SmellDetector detector) {
	    if (detector != null) {
	        Map<SmellType, Set<Smell>> detectedSmells = detector.getDetectedSmells();
	        
	        // Check if the map is empty
	        if (detectedSmells == null || detectedSmells.isEmpty()) {
	            return; // Exit early if there are no smells to process
	        }

	        for (Map.Entry<SmellType, Set<Smell>> entry : detectedSmells.entrySet()) {
	            SmellType smellType = entry.getKey();
	            Set<Smell> smells = entry.getValue();
	            detectedSmells.merge(smellType, smells, (existingSmells, newSmells) -> {
	                existingSmells.addAll(newSmells);
	                return existingSmells;
	            });
	        }
	    }
	}
	
	public Map<SmellType, Set<Smell>> getDetectedSmells() {
        return detectedSmells;
    }
}

