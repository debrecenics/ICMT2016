package hu.bme.mit.inf.dslreasoner.visualisation.emf2yed

import com.google.common.collect.Table
import java.util.Collections
import java.util.HashMap
import java.util.List
import java.util.Map
import java.util.Set
import org.eclipse.emf.ecore.EAttribute
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EReference

class Model2Yed{
	
	def public transform(List<EObject> model) {
		transform(model, Collections.emptySet, Collections.emptySet, Collections.emptySet, null, null)
	}
	
	def public transform(List<EObject> model, Set<Object> yellow, Set<Object> red, Set<EObject> green, Table<EObject,EObject,EReference> addEdges, Table<EObject,EObject,EReference> delEdges) {
		val Map<EObject, Integer> objectToID = new HashMap
		
		'''
		Creator	"dslreasoner"
		graph
		[
			«FOR object:model SEPARATOR '\n'»
				«this.transformObject(object,objectToID.size+1,objectToID, yellow, red, green)»
			«ENDFOR»
			«FOR from:model»
				«FOR reference:from.eClass.EAllReferences»
					«IF reference.isMany»
					«FOR target : from.eGet(reference) as List<EObject>»
						«reference.transformRelation(from,target,objectToID, addEdges, delEdges)»
					«ENDFOR»
					«ELSE»
						«reference.transformRelation(from,from.eGet(reference) as EObject,objectToID, addEdges, delEdges)»
					«ENDIF»
				«ENDFOR»
			«ENDFOR»
			«IF addEdges != null && !addEdges.empty»
			«FOR cell:addEdges.cellSet»
				«cell.value.transformRelation(cell.rowKey,cell.columnKey,objectToID,addEdges,delEdges)»
			«ENDFOR»
			«ENDIF»
		]
		'''
	}
	
	val protected titleSize = 16
	val protected attributeSize = 14
	val protected borderDistance = 6
	val protected attributeBorderDistance = 8
	val protected ratio = 11.0/20.0
	
	def protected color(EObject object, Set<Object> yellow, Set<Object> red, Set<EObject> green) {
		if(yellow.contains(object))
			return "#FFFF00" 
		if(red.contains(object))
			return "#FF0000"
		if(green.contains(object))
			return "#00FF00"
		return "#FFFFFF"
	}
	
	def protected transformObject(EObject object,int id,Map<EObject, Integer> objectToID, Set<Object> yellow, Set<Object> red, Set<EObject> green){
		val title = object.transormTitle(id)
		val attributes = object.eClass.
			getEAllAttributes.map[transformAttribute(object.eGet(it,true))]
		
		val color = color(object,yellow,red,green)
		
		var double width = title.length*titleSize + borderDistance*2;
		for(x:attributes.map[length*attributeSize + borderDistance*2 + attributeBorderDistance*2])
			width = Math::max(width,x)
		width = width*ratio
		
		val height = Math::max(
			titleSize+4,
			(attributes.size+1)*attributeSize + borderDistance*2)
			
		objectToID.put(object,id)
		
		'''
		node
			[
				id	«id»
				graphics
				[
					w	«width»
					h	«height»
					type	"rectangle"
					fill	"«color»"
					fill2	"«color»"
					outline	"#000000"
				]
				LabelGraphics
				[
					text	"«title»"
					outline	"#000000"
					fill	"«color»"
					fontSize	«titleSize»
					fontName	"Consolas"
					autoSizePolicy	"node_width"
					anchor	"t"
					borderDistance	0.0
				]
				LabelGraphics
				[
					text	"
		«FOR attribute : attributes»
		«attribute»
		«ENDFOR»"
					fontSize	«attributeSize»
					fontName	"Consolas"
					alignment	"left"
					anchor	"tl"
					borderDistance	«borderDistance»
				]
			]
		'''
	}
	
	def protected transormTitle(EObject object,int id)
		 '''o«id»: «object.eClass.name»'''
	
	def protected dispatch transformAttribute(EAttribute attribute, Object value) '''«attribute.name» = «value»'''
	def protected dispatch transformAttribute(EAttribute attribute, List<?> values) '''«attribute.name» = [«values.map[transformAttributeValue(attribute.EType.instanceClass)].join(",")»]'''
	
	def dispatch transformAttributeValue(String value, Class<?> type) '''"«value»"'''
	def dispatch transformAttributeValue(double value,Class<?> type) '''«value»'''
	def dispatch transformAttributeValue(int value,Class<?> type) '''«value»'''
	def dispatch transformAttributeValue(Object value,Class<?> type) {
		if(type.isEnum) '''::«value.toString»'''
		else return '''«value»'''
	}
	
	def protected color(EReference ref, EObject row, EObject column, Table<EObject,EObject,EReference> addEdges, Table<EObject,EObject,EReference> delEdges) {
		if(addEdges != null && addEdges.get(row,column) == ref)
			return "#00FF00"
		if(delEdges != null && delEdges.get(row,column) == ref)
			return "#FF0000"
		return "#000000"
	}
	
	def protected transformRelation(EReference reference, EObject source, EObject target,Map<EObject, Integer> objectToID, Table<EObject,EObject,EReference> addEdges, Table<EObject,EObject,EReference> delEdges){
		if(source!=null && target!=null) {
			'''
			edge
			[
				source	«objectToID.get(source)»
				target	«objectToID.get(target)»
				graphics
				[
					« IF (addEdges != null && addEdges.get(source,target) == reference) || (delEdges != null && delEdges.get(source,target) == reference)»
					style	"dashed"
					«ENDIF»
					fill	"«color(reference, source, target, addEdges, delEdges)»"
					«IF reference.containment»
						width	3
					«ENDIF»
					targetArrow	"standard"
				]
				LabelGraphics
				[
					text	"«IF(delEdges != null && delEdges.get(source,target) == reference)»«'«'»del«'»'»«ENDIF»«IF(addEdges != null && addEdges.get(source,target) == reference)»«'«'»new«'»'»«ENDIF»«reference.name»"
					color	"«color(reference, source, target, addEdges, delEdges)»"
					fontSize	14
					fontName	"Consolas"
					configuration	"AutoFlippingLabel"
					model	"six_pos"
					position	"thead"
				]
			]
			'''}
		else return ''''''
	}
}