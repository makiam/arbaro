//#**************************************************************************
//#
//#    Copyright (C) 2003-2006  Wolfram Diestel
//#
//#    This program is free software; you can redistribute it and/or modify
//#    it under the terms of the GNU General Public License as published by
//#    the Free Software Foundation; either version 2 of the License, or
//#    (at your option) any later version.
//#
//#    This program is distributed in the hope that it will be useful,
//#    but WITHOUT ANY WARRANTY; without even the implied warranty of
//#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//#    GNU General Public License for more details.
//#
//#    You should have received a copy of the GNU General Public License
//#    along with this program; if not, write to the Free Software
//#    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//#
//#    Send comments and bug fixes to diestel@steloj.de
//#
//#**************************************************************************/


package net.sourceforge.arbaro.tree;

import net.sourceforge.arbaro.transformation.*;
import net.sourceforge.arbaro.params.*;

/**
 * Segments with helical curving or nonlinearly changing radius
 * can be broken into subsegments. Normal segments consist of only
 * one subsegment.
 * 
 * @author Wolfram Diestel
 */
class SubsegmentImpl implements StemSection { 
	// a Segment can have one or more Subsegments
	public Vector pos; 
	public double dist; // distance from segment's base 
	public double rad;
	SegmentImpl segment;
	public SubsegmentImpl prev=null;
	public SubsegmentImpl next=null;
	
	public Vector getPosition() {
		return pos;
	}
	
	public double getRadius() {
		return rad;
	}
	
	public Transformation getTransformation() { 
		return segment.transf.translate(pos.sub(segment.getLowerPosition())); 
	}
	
	public Vector getZ() {
		return segment.transf.getZ();
	}
	
	/*
	public double getHeight() {
		return dist;
	}
	*/
	
	public double getDistance() {
		return segment.index * segment.length + dist;
	}
	
	/*public Subsegment getNext() {
		return next;
	}
	*/
	
	/*public StemSection getNext() {
		if (next != null)
			return next;
		else
			return segment.
	}
	*/
	/*public boolean isLastSubsegment() {
		return (next==null);
	}
	*/
	
	/* Last section of the stem? */
//	public boolean isLast() {
//		return (segment.isLastStemSegment() && next==null);
//	}
//	
//	/* Last section of the stem? */
//	public boolean isFirst() {
//		return false; // first StemSection always is the first segment itself
//		// return (segment.isFirstStemSegment() && prev==null);
//	}
	
	
	public SubsegmentImpl(Vector p, double r, double h, SegmentImpl segment) {
		pos = p;
		rad = r;
		dist = h;
		this.segment = segment;
	}
	
	/* (non-Javadoc)
	 * @see net.sourceforge.arbaro.tree.TraversableSubsegment#traverseStem(net.sourceforge.arbaro.tree.StemTraversal)
	 */
//	public boolean traverseStem(StemTraversal traversal) {
//	    return traversal.visitSubsegment(this);
//	}
	
	
	public Vector[] getSectionPoints() {
		Params par = segment.par;
		LevelParams lpar = segment.lpar;

		int pt_cnt = lpar.mesh_points;
		Vector[] points;
//	private void createSectionMeshpoints(StemSection sec,double rad, 
//			boolean donttrf, double vMap) {
		//h = (self.index+where)*self.stem.segment_len
		//rad = self.stem.stem_radius(h)
		// self.stem.DBG("MESH: pos: %s, rad: %f\n"%(str(pos),rad))
		
		// System.err.println("Segment-create meshpts, pos: "+pos+" rad: "+rad);
		
		//LevelParams lpar = params.levelParams[stem.getLevel()]; //segment.lpar;
//		Vector pos = sec.getPosition();
		Transformation trf = getTransformation(); //segment.getTransformation().translate(pos.sub(segment.getLowerPosition()));
		//self.stem.TRF("MESH:",trf)
		
		// if radius = 0 create only one point
		if (rad<0.000001) {
			points = new Vector[1];
			points[0] = trf.apply(new Vector(0,0,0));
		} else { //create pt_cnt points
			points = new Vector[pt_cnt];
			//stem.DBG("MESH+LOBES: lobes: %d, depth: %f\n"%(self.tree.Lobes, self.tree.LobeDepth))
			
			for (int i=0; i<pt_cnt; i++) {
				double angle = i*360.0/pt_cnt;
				// for Lobes ensure that points are near lobes extrema, but not exactly there
				// otherwise there are to sharp corners at the extrema
				if (lpar.level==0 && par.Lobes != 0) {
					angle -= 10.0/par.Lobes;
				}
				
				// create some point on the unit circle
				Vector pt = new Vector(Math.cos(angle*Math.PI/180),Math.sin(angle*Math.PI/180),0);
				// scale it to stem radius
				if (lpar.level==0 && (par.Lobes != 0 || par._0ScaleV !=0)) {
					// self.stem.DBG("MESH+LOBES: angle: %f, sinarg: %f, rad: %f\n"%(angle, \
					//self.tree.Lobes*angle*pi/180.0, \
					//	rad*(1.0+self.tree.LobeDepth*cos(self.tree.Lobes*angle*pi/180.0))))
					double rad1 = rad * (1 + 
							par.random.uniform(-par._0ScaleV,par._0ScaleV)/
							segment.getSubsegmentCount());
					pt = pt.mul(rad1*(1.0+par.LobeDepth*Math.cos(par.Lobes*angle*Math.PI/180.0))); 
				} else {
					pt = pt.mul(rad); // faster - no radius calculations
				}
				// apply transformation to it
				// (for the first trunk segment transformation shouldn't be applied to
				// the lower meshpoints, otherwise there would be a gap between 
				// ground and trunk)
				// FIXME: for helical stems may be/may be not a random rotation 
				// should applied additionally?
				
				pt = trf.apply(pt);
				points[i] = pt;
			}
		}
		
		return points;
	}

	


}
