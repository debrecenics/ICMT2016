package hu.bme.mit.inf.telecare.simplified.generator;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.incquery.runtime.api.IPatternMatch;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import events.AbstractActivity;
import telecare.TelecareSystem;

public class CategorizedModel {

	public final Set<Object> changeblePart = Sets.newHashSet();
	public final Set<Object> removablePart = Sets.newHashSet();
	
	public final Set<IPatternMatch> objRemoval = Sets.newHashSet();
	public final Set<IPatternMatch> refRemoval = Sets.newHashSet();
	public final Set<IPatternMatch> dontCareRemoval = Sets.newHashSet();

	public final Set<IPatternMatch> objAddition = Sets.newHashSet();
	public final Set<IPatternMatch> refAddition= Sets.newHashSet();

	public final Set<EObject> newObjects = Sets.newHashSet();
	
	public CharSequence yedOriginal;
	public CharSequence yedModified;
	public CharSequence yedEventsOriginal;
	public CharSequence yedDataflowOriginal;
	public final TelecareSystem system;
	
	public final List<EObject> viewObjectsDataflow = Lists.newArrayList();
	public final Map<EObject,EObject> mapDataflow = Maps.newHashMap();
	public final List<EObject> viewObjectsEvents = Lists.newArrayList();
	public final Multimap<EObject,AbstractActivity> mapEvents = ArrayListMultimap.create();
	
	
	public CategorizedModel(TelecareSystem system) {
		this.system = system;
	}
	
	public static void saveYed(String path, CharSequence sequence) throws Exception {
		try(  PrintWriter out = new PrintWriter( path )  ){
		    out.println( sequence.toString() );
		}
	}
}
