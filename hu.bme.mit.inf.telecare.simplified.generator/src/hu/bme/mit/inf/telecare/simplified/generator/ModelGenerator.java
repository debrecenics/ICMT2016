package hu.bme.mit.inf.telecare.simplified.generator;

import telecare.EventFinishedTrigger;
import telecare.Host;
import telecare.Measure;
import telecare.MeasurementType;
import telecare.PeriodicTrigger;
import telecare.Report;
import telecare.Sensor;
import telecare.TelecareFactory;
import telecare.TelecarePackage;
import telecare.TelecareSystem;

public class ModelGenerator {

	@SuppressWarnings("unused")
	private TelecarePackage ePackage;
	private TelecareFactory eFactory;
	private TelecareSystem system;
	
	public ModelGenerator(int modelSize) {
		
		ePackage = TelecarePackage.eINSTANCE;
		eFactory = TelecareFactory.eINSTANCE;
		
		generate(modelSize);
	}
	
	
	public TelecareSystem getModel() {
		return system;
	}
	
//	private void createResource(String genFolder, String level) {
//		ResourceSet rSet = new ResourceSetImpl();
//		resource = rSet.createResource(URI.createFileURI(genFolder+"instance-"+level+".xmi"));
//	}
	
//	private void saveResource() throws IOException {
//		
//		Model2Yed yed = new Model2Yed();
//		CharSequence sequence = yed.transform(Lists.newArrayList(resource.getAllContents()));
//		String file = resource.getURI().toFileString();
//		file += ".gml";
//		
//		PrintWriter writer = new PrintWriter(file, "UTF-8");
//		writer.print(sequence.toString());
//		writer.close();
//		
//		resource.save(Collections.emptyMap());
//	}
	
	private void generate(int n) {
		//Root object
		system = eFactory.createTelecareSystem();
		//Sensor
		Sensor sensor = eFactory.createSensor();
		sensor.setName("sensor"+n);
		//Periodic Trigger
		PeriodicTrigger periodicTrigger = eFactory.createPeriodicTrigger();
		periodicTrigger.setName("periodicTrigger_"+n);
		for(int k = 0; k < n; k++) {
			//Measurement Types
			MeasurementType typeA = eFactory.createMeasurementType();
			typeA.setName("typeA_"+k+"_"+n);
			MeasurementType typeB = eFactory.createMeasurementType();
			typeB.setName("typeB_"+k+"_"+n);
			//Hosts
			Host hostA = eFactory.createHost();
			hostA.setName("hostA_"+k+"_"+n);
			Host hostB = eFactory.createHost();
			hostB.setName("hostB_"+k+"_"+n);
			//Generate Dynamic parts
			for(int i = 0; i < 2; i++) {
				//Report
				Report report = eFactory.createReport();
				report.setName("report_"+k+"_"+(i+1)+"_"+n);
				if(i % 2 == 0) {
					report.setWhere(hostA);
				} else {
					report.setWhere(hostB);
				}
				//All measurements finished trigger
				EventFinishedTrigger allMeasurementFinishedTrigger = eFactory.createEventFinishedTrigger();
				allMeasurementFinishedTrigger.setName("measurementsFinished_"+k+"_"+(i+1)+"_"+n);
				for(int j = 0; j < 2; j++) {
					//Measurements
					Measure measure = eFactory.createMeasure();
					measure.setName("measure"+mapIntToChar[j]+"_"+k+"_"+(i+1)+"_"+n);
					measure.setWhen(periodicTrigger);
					if(j % 2 == 0) {
						measure.setType(typeA);
					} else {
						measure.setType(typeB);
					}
					report.getWhat().add(measure);
					allMeasurementFinishedTrigger.getTriggeredBy().add(measure);
					sensor.getActions().add(measure);
					//EventFinishedTriggers
				}
				report.setWhen(allMeasurementFinishedTrigger);
				sensor.getActions().add(report);
				sensor.getTriggers().add(allMeasurementFinishedTrigger);
			}
			
			system.getHosts().add(hostA);
			system.getHosts().add(hostB);
			system.getMeasurementTypes().add(typeA);
			system.getMeasurementTypes().add(typeB);
		}
		sensor.getTriggers().add(periodicTrigger);
		system.getSensors().add(sensor);
		
	}

	
	private static final char[] mapIntToChar = {'A','B','C','D'};
	
}
