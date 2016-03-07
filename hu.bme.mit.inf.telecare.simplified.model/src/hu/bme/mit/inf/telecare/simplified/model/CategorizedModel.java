package hu.bme.mit.inf.telecare.simplified.model;

import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.incquery.runtime.api.IPatternMatch;

import com.google.common.collect.Sets;

public class CategorizedModel {

	public final Set<Object> fixPart = Sets.newHashSet();
	public final Set<Object> changeblePart = Sets.newHashSet();
	public final Set<Object> removablePart = Sets.newHashSet();
	
	public final Set<IPatternMatch> objRemoval = Sets.newHashSet();
	public final Set<IPatternMatch> dontCareRemoval = Sets.newHashSet();
	public final Set<IPatternMatch> refRemoval = Sets.newHashSet();
	
	public final Resource resourceModel;
	
	public CategorizedModel(Resource resourceModel) {
		this.resourceModel = resourceModel;
	}
}
