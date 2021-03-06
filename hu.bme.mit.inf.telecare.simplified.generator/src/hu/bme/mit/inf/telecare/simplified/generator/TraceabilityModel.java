package hu.bme.mit.inf.telecare.simplified.generator;

import java.util.Collection;

import org.eclipse.incquery.runtime.api.IPatternMatch;
import org.eclipse.incquery.runtime.api.IQuerySpecification;
import org.eclipse.incquery.runtime.api.IncQueryEngine;
import org.eclipse.incquery.runtime.api.IncQueryMatcher;
import org.eclipse.incquery.runtime.exception.IncQueryException;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import telecare.TelecareSystem;

public class TraceabilityModel {

	@SuppressWarnings("deprecation")
	public static Multimap<Object, IPatternMatch> calculateTraceList(TelecareSystem system, 
			Collection<IQuerySpecification<?>> specifications) throws IncQueryException {
		IncQueryEngine engine = IncQueryEngine.on(system);
		Multimap<Object, IPatternMatch> traceMap = ArrayListMultimap.create();
		for (IQuerySpecification<?> spec : specifications) {
			IncQueryMatcher<? extends IPatternMatch> matcher = engine.getMatcher(spec);
			for (IPatternMatch match : matcher.getAllMatches()) {
				for (String param : match.parameterNames()) {
					traceMap.put(match.get(param), match);
				}
			}
		}

		return traceMap;
	}
	
}
