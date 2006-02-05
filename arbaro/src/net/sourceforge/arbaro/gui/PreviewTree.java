/*
 * Created on 09.09.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.sourceforge.arbaro.gui;

import net.sourceforge.arbaro.params.IntParam;
import net.sourceforge.arbaro.params.Params;
import net.sourceforge.arbaro.tree.Tree;
import net.sourceforge.arbaro.mesh.Mesh;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;


/**
 * @author wdiestel
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public final class PreviewTree extends Tree {
	// preview always shows this levels and 
	// the previous levels stems 
	int showLevel=1;
	Params originalParams;
	Mesh mesh;
	
	protected ChangeEvent changeEvent = null;
	protected EventListenerList listenerList = new EventListenerList();

	/**
	 * @param other
	 */
	public PreviewTree(Tree other) {
		super(other);
		originalParams=other.params;
	}
	
	public void setShowLevel(int l) {
		int Levels = ((IntParam)(originalParams.getParam("Levels"))).intValue(); 
		if (l>Levels) showLevel=Levels;
		else showLevel=l;
	}
	
	public int getShowLevel() {
		return showLevel;
	}

	public void remake() throws Exception {
			clear();
			params = new Params(originalParams);
			params.preview=true;
//			previewTree = new Tree(originalTree);
			
			// manipulate params to avoid making the whole tree
			// FIXME: previewTree.Levels <= tree.Levels
			int Levels = ((IntParam)(originalParams.getParam("Levels"))).intValue(); 
			if (Levels>showLevel+1) {
				setParam("Levels",""+(showLevel+1));
				setParam("Leaves","0");
			} 
			for (int i=0; i<showLevel; i++) {
				setParam(""+i+"Branches","1");
				// if (((FloatParam)previewTree.getParam(""+i+"DownAngleV")).doubleValue()>0)
				setParam(""+i+"DownAngleV","0");
			}
			
		    make();	
			mesh = createStemMesh(true);
			
			fireStateChanged();
	}
	
	public Mesh getMesh() {
		return mesh;
	}

	public void addChangeListener(ChangeListener l) {
		listenerList.add(ChangeListener.class, l);
	}
	
	public void removeChangeListener(ChangeListener l) {
		listenerList.remove(ChangeListener.class, l);
	}
	
	protected void fireStateChanged() {
		Object [] listeners = listenerList.getListenerList();
		for (int i = listeners.length -2; i>=0; i-=2) {
			if (listeners[i] == ChangeListener.class) {
				if (changeEvent == null) {
					changeEvent = new ChangeEvent(this);
				}
				((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
			}
		}
	}

	
}
