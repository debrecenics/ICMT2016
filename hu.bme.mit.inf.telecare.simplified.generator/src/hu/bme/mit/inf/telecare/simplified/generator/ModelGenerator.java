package hu.bme.mit.inf.telecare.simplified.generator;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import com.google.common.collect.Lists;

import hu.bme.mit.inf.dslreasoner.visualisation.emf2yed.Model2Yed;
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
	private static TelecarePackage ePackage;
	private static TelecareFactory eFactory;
	private static Resource resource;
	
	
	public static void main(String[] args) throws IOException {
		ePackage = TelecarePackage.eINSTANCE;
		eFactory = TelecareFactory.eINSTANCE;
		
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		
		createResource(args[0], args[1]);
		generate(Integer.valueOf(args[1]));
		saveResource();
	}

	private static void createResource(String genFolder, String level) {
		ResourceSet rSet = new ResourceSetImpl();
		resource = rSet.createResource(URI.createFileURI(genFolder+"instance-"+level+".xmi"));
	}
	
	private static void saveResource() throws IOException {
		
		Model2Yed yed = new Model2Yed();
		CharSequence sequence = yed.transform(Lists.newArrayList(resource.getAllContents()));
		String file = resource.getURI().toFileString();
		file += ".gml";
		
		PrintWriter writer = new PrintWriter(file, "UTF-8");
		writer.print(sequence.toString());
		writer.close();
		
		resource.save(Collections.emptyMap());
	}
	
	private static void generate(int n) {
		//Root object
		TelecareSystem system = eFactory.createTelecareSystem();
		//Sensor
		Sensor sensor = eFactory.createSensor();
		sensor.setName("sensor"+n);
		//Measurement Types
		MeasurementType typeA = eFactory.createMeasurementType();
		typeA.setName("typeA."+n);
		MeasurementType typeB = eFactory.createMeasurementType();
		typeB.setName("typeB."+n);
		//Hosts
		Host hostA = eFactory.createHost();
		hostA.setName("hostA."+n);
		Host hostB = eFactory.createHost();
		hostB.setName("hostB."+n);
		//Periodic Trigger
		PeriodicTrigger periodicTrigger = eFactory.createPeriodicTrigger();
		periodicTrigger.setName("periodicTrigger"+n);
		//Generate Dynamic parts
		for(int i = 0; i < n; i++) {
			//Report
			Report report = eFactory.createReport();
			report.setName("report-"+(i+1)+"."+n);
			if(i % 2 == 0) {
				report.setWhere(hostA);
			} else {
				report.setWhere(hostB);
			}
			//All measurements finished trigger
			EventFinishedTrigger allMeasurementFinishedTrigger = eFactory.createEventFinishedTrigger();
			allMeasurementFinishedTrigger.setName("measurementsFinished-"+(i+1)+"."+n);
			for(int j = 0; j < 4; j++) {
				//Measurements
				Measure measure = eFactory.createMeasure();
				measure.setName("measure"+mapIntToChar[j]+"-"+(i+1)+"."+n);
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
		
		sensor.getTriggers().add(periodicTrigger);
		system.getHosts().add(hostA);
		system.getHosts().add(hostB);
		system.getMeasurementTypes().add(typeA);
		system.getMeasurementTypes().add(typeB);
		system.getSensors().add(sensor);
		resource.getContents().clear();
		resource.getContents().add(system);
	}

	
	private static char[] mapIntToChar = {'A','B','C','D'};
	
}
