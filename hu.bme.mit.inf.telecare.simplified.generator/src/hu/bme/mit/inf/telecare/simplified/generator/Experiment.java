package hu.bme.mit.inf.telecare.simplified.generator;

public class Experiment {

	private CategorizedModel result;

	public Experiment(int modelSize, int changeSize) throws Exception {
		ModelGenerator modelGenerator = new ModelGenerator(modelSize);
		ChangeGenerator changeGenerator = new ChangeGenerator(changeSize,modelGenerator.getModel());
		result = changeGenerator.getModel();
	}
	
	public CategorizedModel getResult() {
		return result;
	}	
}
