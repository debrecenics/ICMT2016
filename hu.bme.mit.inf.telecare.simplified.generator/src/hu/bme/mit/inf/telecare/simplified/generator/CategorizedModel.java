package hu.bme.mit.inf.telecare.simplified.generator;

import java.util.Set;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.incquery.runtime.api.IPatternMatch;

import com.google.common.collect.Sets;

import telecare.TelecareSystem;

public class CategorizedModel {

	public final Set<Object> changeblePart = Sets.newHashSet();
	public final Set<Object> removablePart = Sets.newHashSet();
	
	public final Set<IPatternMatch> objRemoval = Sets.newHashSet();
	public final Set<IPatternMatch> dontCareRemoval = Sets.newHashSet();
	public final Set<IPatternMatch> refRemoval = Sets.newHashSet();

	public CharSequence yedOriginal;
	public CharSequence yedModified;
	public final Resource resourceModel;
	public final TelecareSystem system;
	
	public CategorizedModel(Resource resourceModel, TelecareSystem system) {
		this.resourceModel = resourceModel;
		this.system = system;
	}
}
