package hu.bme.mit.inf.telecare.simplified.generator;

import java.util.Collection;

import org.eclipse.incquery.runtime.api.IPatternMatch;

import com.google.common.collect.ImmutableSet;

import hu.bme.mit.inf.telecare.simplified.generator.ChangeGenerator.ChangeTypes;

public class Experiment {

	private static final int MODEL_SIZE = 200;
	private static final int CHANGE_SIZE = 30;
	public CategorizedModel result;
	private ChangeGenerator changeGenerator;
	private ModelGenerator modelGenerator;

	public static final int[] MODEL_SIZES = {1,2,4,8,16,32,64,128,256};
	public static final int[] CHANGE_SIZES = {1,2,4,8,16,32};
	
	public static void main(String[] args) throws Exception {
		for (int modelSize : MODEL_SIZES) {
			for (int changeSize : CHANGE_SIZES) {
				if(changeSize < modelSize)					
					execute(modelSize, changeSize);
			}
		}
	}

	private static void execute(int modelSize, int changeSize) throws Exception {
		Experiment experiment = new Experiment(modelSize,changeSize);
		CategorizedModel.saveYed("/home/vialpando/Eclipse/AlloyGen/git/ICMT2016/hu.bme.mit.inf.telecare.simplified.generator/instances/experiment-original-"+MODEL_SIZE+"-"+CHANGE_SIZE+".gml", 
				experiment.getResult().yedOriginal);
		CategorizedModel.saveYed("/home/vialpando/Eclipse/AlloyGen/git/ICMT2016/hu.bme.mit.inf.telecare.simplified.generator/instances/experiment-events-original-"+MODEL_SIZE+"-"+CHANGE_SIZE+".gml", 
				experiment.getResult().yedEventsOriginal);
		CategorizedModel.saveYed("/home/vialpando/Eclipse/AlloyGen/git/ICMT2016/hu.bme.mit.inf.telecare.simplified.generator/instances/experiment-dataflow-original-"+MODEL_SIZE+"-"+CHANGE_SIZE+".gml", 
				experiment.getResult().yedDataflowOriginal);
		CategorizedModel.saveYed("/home/vialpando/Eclipse/AlloyGen/git/ICMT2016/hu.bme.mit.inf.telecare.simplified.generator/instances/experiment-events-modified-"+MODEL_SIZE+"-"+CHANGE_SIZE+".gml", 
				experiment.getResult().yedEventsModified);
		CategorizedModel.saveYed("/home/vialpando/Eclipse/AlloyGen/git/ICMT2016/hu.bme.mit.inf.telecare.simplified.generator/instances/experiment-dataflow-modified-"+MODEL_SIZE+"-"+CHANGE_SIZE+".gml", 
				experiment.getResult().yedDataflowModified);
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
