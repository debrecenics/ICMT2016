package hu.bme.mit.inf.telecare.simplified.model;

import java.io.PrintWriter;
import java.rmi.UnexpectedException;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.incquery.runtime.api.IPatternMatch;
import org.eclipse.incquery.runtime.api.IQuerySpecification;
import org.eclipse.incquery.runtime.exception.IncQueryException;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import hu.bme.mit.inf.dslreasoner.visualisation.emf2yed.Model2Yed;
import hu.bme.mit.inf.telecare.simplified.model.query.ActionMatch;
import hu.bme.mit.inf.telecare.simplified.model.query.AfterExtendedAMatch;
import hu.bme.mit.inf.telecare.simplified.model.query.AfterExtendedBMatch;
import hu.bme.mit.inf.telecare.simplified.model.query.AfterMatch;
import hu.bme.mit.inf.telecare.simplified.model.query.DataflowExtendedMatch;
import hu.bme.mit.inf.telecare.simplified.model.query.DataflowMatch;
import hu.bme.mit.inf.telecare.simplified.model.query.FinishMatch;
import hu.bme.mit.inf.telecare.simplified.model.query.InitMatch;
import hu.bme.mit.inf.telecare.simplified.model.query.SrcMatch;
import hu.bme.mit.inf.telecare.simplified.model.query.TrgMatch;
import hu.bme.mit.inf.telecare.simplified.model.query.util.ActionQuerySpecification;
import hu.bme.mit.inf.telecare.simplified.model.query.util.AfterExtendedAQuerySpecification;
import hu.bme.mit.inf.telecare.simplified.model.query.util.AfterExtendedBQuerySpecification;
import hu.bme.mit.inf.telecare.simplified.model.query.util.AfterQuerySpecification;
import hu.bme.mit.inf.telecare.simplified.model.query.util.DataflowExtendedQuerySpecification;
import hu.bme.mit.inf.telecare.simplified.model.query.util.DataflowQuerySpecification;
import hu.bme.mit.inf.telecare.simplified.model.query.util.FinishQuerySpecification;
import hu.bme.mit.inf.telecare.simplified.model.query.util.InitQuerySpecification;
import hu.bme.mit.inf.telecare.simplified.model.query.util.SrcQuerySpecification;
import hu.bme.mit.inf.telecare.simplified.model.query.util.TrgQuerySpecification;
import telecare.TelecareFactory;
import telecare.TelecarePackage;

public class ChangeGenerator {

	private static final Random RANDOM = new Random();

	private static Resource resource;
	
	private static Set<IQuerySpecification<?>> objRules;
	private static Set<IQuerySpecification<?>> refRules;
	private static Set<IQuerySpecification<?>> extendedRules;

	private static Multimap<Object, IPatternMatch> traceO;
	private static Multimap<Object, IPatternMatch> traceF;
	private static Multimap<Object, IPatternMatch> traceE;

	private static CategorizedModel model;
	
	@SuppressWarnings("unused")
	private static TelecarePackage ePackage;
	private static TelecareFactory eFactory;
	
	public static void main(String[] args) throws Exception {
		ePackage = TelecarePackage.eINSTANCE;
		eFactory = TelecareFactory.eINSTANCE;
		
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		loadResource(args[0]);
		
		initialize();
		buildTraceability();		
		introduceChanges(args[1]);
		
		System.out.println("Delete object match");
		System.out.println(model.objRemoval);
		System.out.println("Delete don't care matches");
		System.out.println(model.dontCareRemoval);
		System.out.println("Delete edge match");
		System.out.println(model.refRemoval);
		System.out.println("Changeable Part");
		System.out.println(model.changeblePart);
		System.out.println("Removable Part");
		System.out.println(model.removablePart);
		
		Model2Yed yed = new Model2Yed();
		CharSequence sequence = yed.transform(Lists.newArrayList(resource.getAllContents()), model.changeblePart, model.removablePart);
		String file = resource.getURI().toFileString();
		file += ".changed-"+args[1]+".gml";
		
		PrintWriter writer = new PrintWriter(file, "UTF-8");
		writer.print(sequence.toString());
		writer.close();
 	}

	private static void initialize() throws IncQueryException {
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

	private static void buildTraceability() throws IncQueryException {
		traceO = TraceabilityModel.calculateTraceList(resource, objRules);
		traceF = TraceabilityModel.calculateTraceList(resource, refRules);
		traceE = TraceabilityModel.calculateTraceList(resource, extendedRules);
	}

	private static void introduceChanges(String arg) throws UnexpectedException {
		Integer numberOfChanges = Integer.valueOf(arg);
		model = new CategorizedModel(resource);
		for(int i = 0; i < numberOfChanges; i++) {
			IPatternMatch objRemoval = getArbitraryFromSet(traceO.values().stream().filter(x -> !(x instanceof InitMatch) && !(x instanceof FinishMatch)).collect(Collectors.toList()));
			model.objRemoval.add(objRemoval);
			removeObjectMatch(model, objRemoval);
			
			IPatternMatch refRemoval = getArbitraryFromSet(traceF.values());
			model.refRemoval.add(refRemoval);
			removeReferenceMatch(model, refRemoval);
		}
	}

	private static void removeObjectMatch(CategorizedModel model, IPatternMatch removal) {
		traceO.values().remove(removal);
		removeDontCareMatches(model, removal);
		for (String param : removal.parameterNames()) {
			Object object = removal.get(param);
			if(traceO.containsKey(object) || traceF.containsKey(object)) {
				model.changeblePart.add(object);
			} else {
				model.removablePart.add(object);
			}
				
		}
	}

	private static void removeReferenceMatch(CategorizedModel model, IPatternMatch removal) {
		traceF.values().remove(removal);
		Object first = removal.get(0);
		Object second = removal.get(1);
		List<IPatternMatch> extendedRemovals = traceE.values().stream().filter(x -> x.get(0) == first && x.get(1) == second).collect(Collectors.toList());
		traceE.values().removeAll(extendedRemovals);
		
		for (IPatternMatch match : extendedRemovals) {
			for (String param : match.parameterNames()) {
				Object object = match.get(param);
				if(traceO.containsKey(object) || traceF.containsKey(object)) {
					model.changeblePart.add(object);
				} else {
					model.removablePart.add(object);
				}
			}
		}
	}
	
	private static IPatternMatch getArbitraryFromSet(Collection<IPatternMatch> matchSet) throws UnexpectedException {
		int size = matchSet.size();
		int item = RANDOM.nextInt(size); // In real life, the Random object should be rather more shared than this
		int i = 0;
		for(IPatternMatch obj : matchSet)
		{
		    if (i == item)
		        return obj;
		    i = i + 1;
		}
		throw new UnexpectedException("There no more matches in the set");
	}
	
	private static void removeDontCareMatches(CategorizedModel model, IPatternMatch match) {
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
	
	private static void loadResource(String path) {
		ResourceSet rSet = new ResourceSetImpl();
		resource = rSet.getResource(URI.createFileURI(path), true);
	}
	
}
