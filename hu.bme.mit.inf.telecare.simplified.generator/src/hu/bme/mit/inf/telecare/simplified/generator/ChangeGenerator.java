package hu.bme.mit.inf.telecare.simplified.generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.incquery.runtime.api.IPatternMatch;
import org.eclipse.incquery.runtime.api.IQuerySpecification;
import org.eclipse.incquery.runtime.exception.IncQueryException;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import dataflow.DataflowFactory;
import dataflow.DataflowPackage;
import dataflow.InformationType;
import events.AbstractActivity;
import events.Activity;
import events.EventsFactory;
import events.EventsPackage;
import events.Finish;
import events.Init;
import hu.bme.mit.inf.dslreasoner.visualisation.emf2yed.Model2Yed;
import hu.bme.mit.inf.telecare.simplified.generator.query.ActionMatch;
import hu.bme.mit.inf.telecare.simplified.generator.query.AfterExtendedAMatch;
import hu.bme.mit.inf.telecare.simplified.generator.query.AfterExtendedBMatch;
import hu.bme.mit.inf.telecare.simplified.generator.query.AfterMatch;
import hu.bme.mit.inf.telecare.simplified.generator.query.DataflowExtendedMatch;
import hu.bme.mit.inf.telecare.simplified.generator.query.DataflowMatch;
import hu.bme.mit.inf.telecare.simplified.generator.query.FinishMatch;
import hu.bme.mit.inf.telecare.simplified.generator.query.InitMatch;
import hu.bme.mit.inf.telecare.simplified.generator.query.SrcMatch;
import hu.bme.mit.inf.telecare.simplified.generator.query.TrgMatch;
import hu.bme.mit.inf.telecare.simplified.generator.query.util.ActionQuerySpecification;
import hu.bme.mit.inf.telecare.simplified.generator.query.util.AfterExtendedAQuerySpecification;
import hu.bme.mit.inf.telecare.simplified.generator.query.util.AfterExtendedBQuerySpecification;
import hu.bme.mit.inf.telecare.simplified.generator.query.util.AfterQuerySpecification;
import hu.bme.mit.inf.telecare.simplified.generator.query.util.DataflowExtendedQuerySpecification;
import hu.bme.mit.inf.telecare.simplified.generator.query.util.DataflowQuerySpecification;
import hu.bme.mit.inf.telecare.simplified.generator.query.util.FinishQuerySpecification;
import hu.bme.mit.inf.telecare.simplified.generator.query.util.InitQuerySpecification;
import hu.bme.mit.inf.telecare.simplified.generator.query.util.SrcQuerySpecification;
import hu.bme.mit.inf.telecare.simplified.generator.query.util.TrgQuerySpecification;
import telecare.Host;
import telecare.Measure;
import telecare.MeasurementType;
import telecare.NamedElement;
import telecare.PeriodicTrigger;
import telecare.Report;
import telecare.TelecareFactory;
import telecare.TelecarePackage;
import telecare.TelecareSystem;

public class ChangeGenerator {

//	private static final Random RANDOM = new Random();

	private Set<IQuerySpecification<?>> objRules;
	private Set<IQuerySpecification<?>> refRules;
	private Set<IQuerySpecification<?>> extendedRules;
	
	private Multimap<Object, IPatternMatch> traceO;
	private Multimap<Object, IPatternMatch> traceF;
	private Multimap<Object, IPatternMatch> traceE;

	private CategorizedModel model;
	
	
	
	@SuppressWarnings("unused")
	private TelecarePackage eTelecarePackage;
	private TelecareFactory eTelecareFactory;
	@SuppressWarnings("unused")
	private DataflowPackage eDataflowPackage;
	private DataflowFactory eDataflowFactory;
	@SuppressWarnings("unused")
	private EventsPackage eEventsPackage;
	private EventsFactory eEventsFactory;
	
	public ChangeGenerator(int changeSize, TelecareSystem system, Collection<ChangeTypes> changeTypes) throws Exception {
		
		eTelecarePackage = TelecarePackage.eINSTANCE;
		eTelecareFactory = TelecareFactory.eINSTANCE;
		eDataflowPackage = DataflowPackage.eINSTANCE;
		eDataflowFactory = DataflowFactory.eINSTANCE;
		eEventsPackage = EventsPackage.eINSTANCE;
		eEventsFactory = EventsFactory.eINSTANCE;
		
		model = new CategorizedModel(system);
		
		initialize();
		buildTraceability();
		buildViewModelObjects();
		buildViewModelReferences();
		if(changeTypes.contains(ChangeTypes.AddNewActivationWithNewEdgeToReportAndPeriodicTrigger))
			introduceChanges(changeSize, ChangeTypes.AddNewActivationWithNewEdgeToReportAndPeriodicTrigger);
		if(changeTypes.contains(ChangeTypes.RemoveHostWithEdgesAddNewEdgesToOtherHost))
			introduceChanges(changeSize, ChangeTypes.RemoveHostWithEdgesAddNewEdgesToOtherHost);
		if(changeTypes.contains(ChangeTypes.RemoveInformationTypeWithEdges))
			introduceChanges(changeSize, ChangeTypes.RemoveInformationTypeWithEdges);
		
		model.yedOriginal = calculateYed();
		model.yedEventsOriginal = calculateEventsYed();
		model.yedDataflowOriginal = calculateDataflowYed();
		model.yedModified = calculateYedColored();
	}
	
	private void buildViewModelReferences() {
		Collection<IPatternMatch> referenceMatches = traceF.values();
		for (IPatternMatch match : referenceMatches) {
			if(match instanceof AfterMatch) {
				AfterMatch afterMatch = (AfterMatch) match;
				AbstractActivity source = model.mapEvents.get(afterMatch.getA());
				AbstractActivity target = model.mapEvents.get(afterMatch.getB());
				source.getAfter().add(target);
			}
			if(match instanceof DataflowMatch) {
				DataflowMatch dataflowMatch = (DataflowMatch) match;
				InformationType type = (InformationType) model.mapDataflow.get(dataflowMatch.getType());
				dataflow.Host host = (dataflow.Host) model.mapDataflow.get(dataflowMatch.getHost());
				type.getDataflow().add(host);
			}
		}
	}
	
	private void buildViewModelObjects() {
		Collection<IPatternMatch> objectMatches = traceO.values();
		for (IPatternMatch match : objectMatches) {
			if(match instanceof ActionMatch) {
				Activity activity = eEventsFactory.createActivity();
				long i = model.viewObjectsEvents.stream().filter(x -> x instanceof Activity).count()+1;
				activity.setName("activity_"+i);
				model.viewObjectsEvents.add(activity);
				model.mapEvents.put(((ActionMatch) match).getT(), activity);
			}
			if(match instanceof InitMatch) {
				Init init = eEventsFactory.createInit();
				model.viewObjectsEvents.add(init);
				model.mapEvents.put(((InitMatch) match).getT(), init);
			}
			if(match instanceof FinishMatch) {
				Finish finish = eEventsFactory.createFinish();
				model.viewObjectsEvents.add(finish);
				model.mapEvents.put(((FinishMatch) match).getT(), finish);
			}
			if(match instanceof TrgMatch) {
				dataflow.Host host = eDataflowFactory.createHost();
				long i = model.viewObjectsEvents.stream().filter(x -> x instanceof dataflow.Host).count()+1;
				host.setName("host_"+i);
				model.viewObjectsDataflow.add(host);
				model.mapDataflow.put(((TrgMatch) match).getHost(), host);
			}
			if(match instanceof SrcMatch) {
				InformationType type = eDataflowFactory.createInformationType();
				long i = model.viewObjectsEvents.stream().filter(x -> x instanceof InformationType).count()+1;
				type.setName("type_"+i);
				model.viewObjectsDataflow.add(type);
				model.mapDataflow.put(((SrcMatch) match).getType(), type);
			}
		}
	}

	public CategorizedModel getModel() {
		return model;
	}

	private CharSequence calculateDataflowYed() {
		Model2Yed yed = new Model2Yed();
		CharSequence sequence = yed.transform(model.viewObjectsDataflow);
		return sequence;
	}
	
	private CharSequence calculateEventsYed() {
		Model2Yed yed = new Model2Yed();
		CharSequence sequence = yed.transform(model.viewObjectsEvents);
		return sequence;
	}
	
	private CharSequence calculateYed() {
		Model2Yed yed = new Model2Yed();
		ArrayList<EObject> objects = Lists.newArrayList(model.system.eAllContents());
		objects.add(model.system);
		CharSequence sequence = yed.transform(objects);
		return sequence;
	}
	
	private CharSequence calculateYedColored() {
		Model2Yed yed = new Model2Yed();
		ArrayList<EObject> objects = Lists.newArrayList(model.system.eAllContents());
		objects.add(model.system);
		objects.addAll(model.newObjects);
		CharSequence sequence = yed.transform(objects, model.changeblePart, model.removablePart, model.newObjects);
		return sequence;
	}

	private void initialize() throws IncQueryException {
		objRules = ImmutableSet.<IQuerySpecification<?>>of(
				ActionQuerySpecification.instance(),
				FinishQuerySpecification.instance(),
				InitQuerySpecification.instance(),
				SrcQuerySpecification.instance(),
				TrgQuerySpecification.instance());
		
		refRules = ImmutableSet.<IQuerySpecification<?>>of(
				AfterQuerySpecification.instance(),
				DataflowQuerySpecification.instance());
		
		extendedRules = ImmutableSet.<IQuerySpecification<?>>of(
				AfterExtendedAQuerySpecification.instance(),
				AfterExtendedBQuerySpecification.instance(),
				DataflowExtendedQuerySpecification.instance());
	}

	private void buildTraceability() throws IncQueryException {
		traceO = TraceabilityModel.calculateTraceList(model.system, objRules);
		traceF = TraceabilityModel.calculateTraceList(model.system, refRules);
		traceE = TraceabilityModel.calculateTraceList(model.system, extendedRules);
	}

	private void introduceChanges(int numberOfChanges, ChangeTypes type) throws Exception {
		for(int i = 0; i < numberOfChanges; i++) {
			switch (type) {			
			case AddNewActivationWithNewEdgeToReportAndPeriodicTrigger:
				introduceAddNewActivationWithNewEdgeToReportAndPeriodicTrigger(i);
				break;
			case RemoveHostWithEdgesAddNewEdgesToOtherHost:
				introduceRemoveHostWithEdgesAddNewEdgesToOtherHost(i);
				break;
			case RemoveInformationTypeWithEdges:
				introduceRemoveInformationTypeWithEdges(i);
				break;
			default:
				break;
			}
		}
	}
	
	private void introduceRemoveInformationTypeWithEdges(int i) {
		IPatternMatch objRemoval = traceO.values().stream().filter(x -> x instanceof SrcMatch).findFirst().get();
		model.objRemoval.add(objRemoval);
		removeObjectMatch(model, objRemoval);
	}
	
	private void introduceRemoveHostWithEdgesAddNewEdgesToOtherHost(int i) throws Exception {
		IPatternMatch hostMatch = traceO.values().stream().filter(x -> x instanceof TrgMatch).findFirst().get();
		
		Collection<IPatternMatch> dataflows = traceF.values().stream().filter(x -> x instanceof DataflowMatch && x.get(1) == hostMatch.get(0)).collect(Collectors.toList());
		for (IPatternMatch removal : dataflows) {
			removeReferenceMatch(model, removal);
		}
		
		Collection<MeasurementType> measurementTypes = dataflows.stream().map(y -> (MeasurementType)y.get(0)).collect(Collectors.toList());
		for (MeasurementType measurementType : measurementTypes) {
			Host host = (Host)traceO.keySet().stream().filter(x -> x instanceof Host).findFirst().get();
			DataflowMatch newReferenceMatchFromMeasurementToHost = DataflowQuerySpecification.instance().newEmptyMatch();
			newReferenceMatchFromMeasurementToHost.setHost(host);
			newReferenceMatchFromMeasurementToHost.setType(measurementType);
		}
		model.objRemoval.add(hostMatch);
		removeObjectMatch(model, hostMatch);
	}
	
	private void introduceAddNewActivationWithNewEdgeToReportAndPeriodicTrigger(int i) throws Exception {
		 Measure measure = eTelecareFactory.createMeasure();
		 measure.setName("newAction_"+i);
		 
		 model.newObjects.add(measure);
		 
		 ActionMatch newObjectMatch = ActionQuerySpecification.instance().newEmptyMatch();
		 newObjectMatch.setT(measure);
		 
		 AfterMatch newReferenceMatchToPeriodicTrigger = AfterQuerySpecification.instance().newEmptyMatch();
		 newReferenceMatchToPeriodicTrigger.setA((NamedElement)traceO.keySet().stream().filter(x -> x instanceof PeriodicTrigger).findFirst().get());
		 newReferenceMatchToPeriodicTrigger.setB(measure);
	
		 AfterMatch newReferenceMatchToReport = AfterQuerySpecification.instance().newEmptyMatch();
		 newReferenceMatchToReport.setA(measure);
		 newReferenceMatchToReport.setB((NamedElement)traceO.keySet().stream().filter(x -> x instanceof Report).findFirst().get());
	
		 model.objAddition.add(newObjectMatch);
		 model.refAddition.add(newReferenceMatchToPeriodicTrigger);
		 model.refAddition.add(newReferenceMatchToReport);
		 
	}
	
//	private void introduceChanges(int numberOfChanges) throws UnexpectedException {
//		for(int i = 0; i < numberOfChanges; i++) {
//			IPatternMatch objRemoval = getArbitraryFromSet(traceO.values().stream().filter(x -> !(x instanceof InitMatch) && !(x instanceof FinishMatch)).collect(Collectors.toList()));
//			model.objRemoval.add(objRemoval);
//			removeObjectMatch(model, objRemoval);
//			
//			IPatternMatch refRemoval = getArbitraryFromSet(traceF.values());
//			model.refRemoval.add(refRemoval);
//			removeReferenceMatch(model, refRemoval);
//		}
//	}

	private void removeObjectMatch(CategorizedModel model, IPatternMatch removal) {
		traceO.values().remove(removal);
		removeDontCareMatches(model, removal);
		for (String param : removal.parameterNames()) {
			Object object = removal.get(param);
			if(traceO.containsKey(object) || traceF.containsKey(object)) {
				if(!model.removablePart.contains(object))
					model.changeblePart.add(object);
			} else {
				model.changeblePart.remove(object);
				model.removablePart.add(object);
			}	
		}
	}

	private void removeReferenceMatch(CategorizedModel model, IPatternMatch removal) {
		traceF.values().remove(removal);
		Object first = removal.get(0);
		Object second = removal.get(1);
		List<IPatternMatch> extendedRemovals = traceE.values().stream().filter(x -> x.get(0) == first && x.get(1) == second).collect(Collectors.toList());
		traceE.values().removeAll(extendedRemovals);
		
		for (IPatternMatch match : extendedRemovals) {
			for (String param : match.parameterNames()) {
				Object object = match.get(param);
				if(traceO.containsKey(object) || traceF.containsKey(object)) {
					if(!model.removablePart.contains(object))
						model.changeblePart.add(object);
				} else {
					model.changeblePart.remove(object);
					model.removablePart.add(object);
				}
			}
		}
	}
	
//	private IPatternMatch getArbitraryFromSet(Collection<IPatternMatch> matchSet) throws UnexpectedException {
//		int size = matchSet.size();
//		int item = RANDOM.nextInt(size); // In real life, the Random object should be rather more shared than this
//		int i = 0;
//		for(IPatternMatch obj : matchSet)
//		{
//		    if (i == item)
//		        return obj;
//		    i = i + 1;
//		}
//		throw new UnexpectedException("There no more matches in the set");
//	}
	
	private void removeDontCareMatches(CategorizedModel model, IPatternMatch match) {
		if(match instanceof InitMatch || match instanceof FinishMatch || match instanceof ActionMatch) {
			Object object = match.get(0);
			List<IPatternMatch> dontCare = traceF.get(object).stream().filter(x -> x instanceof AfterMatch)
									   .filter(y -> y.get(0) == object || y.get(1) == object).collect(Collectors.toList());
			traceF.values().removeAll(dontCare);
			model.dontCareRemoval.addAll(dontCare);
			List<IPatternMatch> dontCareExtended = traceE.get(object).stream().filter(x -> 
																	x instanceof AfterExtendedAMatch ||
																	x instanceof AfterExtendedBMatch)
									  .filter(y -> y.get(0) == object || y.get(1) == object).collect(Collectors.toList());
			traceE.values().removeAll(dontCareExtended);
			model.dontCareRemoval.addAll(dontCareExtended);
		}
		if(match instanceof TrgMatch) {
			Object object = match.get(0);
			List<IPatternMatch> dontCare = traceF.get(object).stream().filter(x -> x instanceof DataflowMatch)
									   .filter(y -> y.get(1) == object).collect(Collectors.toList());
			traceF.values().removeAll(dontCare);
			model.dontCareRemoval.addAll(dontCare);
			List<IPatternMatch> dontCareExtended = traceE.get(object).stream().filter(x -> x instanceof DataflowExtendedMatch)
									  .filter(y -> y.get(1) == object).collect(Collectors.toList());
			traceE.values().removeAll(dontCareExtended);
			model.dontCareRemoval.addAll(dontCareExtended);
		}
		if(match instanceof SrcMatch) {
			Object object = match.get(0);
			List<IPatternMatch> dontCare = traceF.get(object).stream().filter(x -> x instanceof DataflowMatch)
									   .filter(y -> y.get(0) == object).collect(Collectors.toList());
			traceF.values().removeAll(dontCare);
			model.dontCareRemoval.addAll(dontCare);
			List<IPatternMatch> dontCareExtended = traceE.get(object).stream().filter(x -> x instanceof DataflowExtendedMatch)
									  .filter(y -> y.get(0) == object).collect(Collectors.toList());
			traceE.values().removeAll(dontCareExtended);
			model.dontCareRemoval.addAll(dontCareExtended);
		}
	}
	
//	private void loadResource(String path) {
//		ResourceSet rSet = new ResourceSetImpl();
//		resource = rSet.getResource(URI.createFileURI(path), true);
//	}
	
	public Multimap<Object, IPatternMatch> getTraceO() {
		return traceO;
	}
	
	public Multimap<Object, IPatternMatch> getTraceF() {
		return traceF;
	}
	
	enum ChangeTypes {
		RemoveInformationTypeWithEdges,
		RemoveHostWithEdgesAddNewEdgesToOtherHost,
		AddNewActivationWithNewEdgeToReportAndPeriodicTrigger
	}
	
}
