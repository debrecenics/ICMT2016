package hu.bme.mit.inf.dslreasoner.visualisation.emf2yed;

import java.io.ByteArrayInputStream;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

public class ActionHandler extends AbstractHandler{
	
	Model2Yed visualiser = new Model2Yed();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
	    ISelection sel = HandlerUtil.getActiveMenuSelection(event);
	    IStructuredSelection selection = (IStructuredSelection) sel;

	    Object firstElement = selection.getFirstElement();
	    
	    if(firstElement instanceof IFile) {
	    	IFile file = (IFile) firstElement;
	    	ResourceSet resSet = new ResourceSetImpl();
	    	URI uri = URI.createPlatformResourceURI(
					file.getFullPath().toString(),true);
	    	
	    	try {
				Resource resource = resSet.getResource(uri, true);
				List<EObject> objects = new LinkedList<EObject>();
				
				for (EObject root : resource.getContents()) {
					objects.add(root);
					TreeIterator<EObject> iterator = root.eAllContents();
					while(iterator.hasNext()) objects.add(iterator.next());
				}
				
				
				final String newFileName = uri.segment(uri.segmentCount()-1) +	".gml";
				IFile file2=null;
				IContainer containter = file.getParent();
				if (containter instanceof IFolder) {
					IFolder folder = (IFolder) containter;
					file2 = folder.getFile(newFileName);
				}
				else if (containter instanceof IProject) {
					IProject project = (IProject) containter;
					file2 = project.getFile(newFileName);
				}
				if(file2!=null) {
					//file2.setContents(source, updateFlags, new NullProgressMonitor());
					CharSequence content = visualiser.transform(objects); 
					try {
						file2.create(new ByteArrayInputStream(content.toString().getBytes()),true, new NullProgressMonitor());
					} catch (CoreException e) {
						showErrorMessage("Can not save the model");
					}
				}
				
	    	}catch(RuntimeException e) {
				showErrorMessage("The selected element is not an eResource!");
				e.printStackTrace();
			}
	    }
	    else {
	    	showErrorMessage("Can not visualise the selected element!");
	    }
		return null;
	}
	
	public static void showErrorMessage(String errorMessage) {
		MessageDialog messageDialog = new MessageDialog(
			null,
			"Error during visualisation",
			null,
			errorMessage,
			MessageDialog.ERROR,
			new String[]{"Ok"}, 0);
		messageDialog.open();
	}
}
