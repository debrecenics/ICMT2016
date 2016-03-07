package hu.bme.mit.inf.telecare.simplified.generator;

import java.util.Collection;

import org.eclipse.incquery.runtime.api.IPatternMatch;

import com.google.common.collect.ImmutableSet;

import hu.bme.mit.inf.telecare.simplified.generator.ChangeGenerator.ChangeTypes;

public class Experiment {

	private static final int MODEL_SIZE = 4;
	private static final int CHANGE_SIZE = 2;
	private CategorizedModel result;
	private ChangeGenerator changeGenerator;
	private ModelGenerator modelGenerator;

	public static void main(String[] args) throws Exception {
		Experiment experiment = new Experiment(MODEL_SIZE, CHANGE_SIZE);
		CategorizedModel.saveYed("/home/vialpando/Eclipse/AlloyGen/git/ICMT2016/hu.bme.mit.inf.telecare.simplified.generator/instances/experiment-modified-"+MODEL_SIZE+"-"+CHANGE_SIZE+".gml", 
				experiment.getResult().yedModified);
		
		System.out.println("DeletedObjects");
		System.out.println(experiment.result.objRemoval);
		System.out.println("DeletedReferences");
		System.out.println(experiment.result.refRemoval);
		System.out.println("AddedObjects");
		System.out.println(experiment.result.objAddition);
		System.out.println("AddedReferences");
		System.out.println(experiment.result.refAddition);
		
		System.out.println("Changeable");
		System.out.println(experiment.result.changeblePart);
		System.out.println("Removable");
		System.out.println(experiment.result.removablePart);
		System.out.println("New objects");
		System.out.println(experiment.result.newObjects);
	}
	
	public Experiment(int modelSize, int changeSize) throws Exception {
		modelGenerator = new ModelGenerator(modelSize);
		changeGenerator = new ChangeGenerator(changeSize,modelGenerator.getModel(),
				ImmutableSet.of(
						ChangeTypes.AddNewActivationWithNewEdgeToReportAndPeriodicTrigger,
						ChangeTypes.RemoveHostWithEdgesAddNewEdgesToOtherHost, 
						ChangeTypes.RemoveInformationTypeWithEdges));
		result = changeGenerator.getModel();
	}
	
	public Collection<IPatternMatch> getTraceObject() {
		return changeGenerator.getTraceO().values();
	}
	
	public Collection<IPatternMatch> getTraceFeature() {
		return changeGenerator.getTraceF().values();
	}
	
	public CategorizedModel getResult() {
		return result;
	}	
}
