//  #**************************************************************************
//  #
//  #    Copyright (C) 2003-2006  Wolfram Diestel
//  #
//  #    This program is free software; you can redistribute it and/or modify
//  #    it under the terms of the GNU General Public License as published by
//  #    the Free Software Foundation; either version 2 of the License, or
//  #    (at your option) any later version.
//  #
//  #    This program is distributed in the hope that it will be useful,
//  #    but WITHOUT ANY WARRANTY; without even the implied warranty of
//  #    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  #    GNU General Public License for more details.
//  #
//  #    You should have received a copy of the GNU General Public License
//  #    along with this program; if not, write to the Free Software
//  #    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//  #
//  #    Send comments and bug fixes to diestel@steloj.de
//  #
//  #**************************************************************************/

package net.sourceforge.arbaro.tree;

import net.sourceforge.arbaro.transformation.*;
import net.sourceforge.arbaro.params.*;

/**
 * @author wolfram
 *
 */
class SegmentGenerator extends GeneratorBase {
	private StemImpl stem;
	private SegmentImpl segm;
	private StemGenerator stemFactory;
	private Params par;
	private LevelParams lpar;
	
	
	public SegmentGenerator(StemImpl stem, StemGenerator stemFactory) {
		super(stemFactory);
		this.stem = stem;
		this.stemFactory = stemFactory;
		this.par = stemFactory.par;
		this.lpar = par.getLevelParams(stem.getLevel());
	}
	
	/**
	 * Makes the segments from subsegments 
	 */
	public SegmentImpl makeSegment(int index, Transformation trf, 
			double rad1, double rad2) {
			
		segm = new SegmentImpl(stem,index,trf,rad1,rad2);
		
		// FIXME: numbers for cnt should correspond to Smooth value
		// helical stem
		if (lpar.nCurveV<0) { 
			makeHelix(10);
		}
		
		// spherical end
		else if (lpar.nTaper > 1 && lpar.nTaper <=2 
				&& segm.index == lpar.nCurveRes-1) {
			makeSphericalEnd(10);
		}
		
		// periodic tapering
		else if (lpar.nTaper>2) {
			makeSubsegments(20);
		}
		
		// trunk flare
		// FIXME: if nCurveRes[0] > 10 this division into several
		// subsegs should be extended over more then one segments?
		else if (lpar.level==0 && par.Flare!=0 && segm.index==0) {
			DBG(stem,"Segment.make() - flare");
			makeFlare(10);
			
		} else {
			makeSubsegments(1);
		}
		
		// FIXME: for helical stems maybe this test
		// should be made for all subsegments
		segm.minMaxTest();
		
		return segm;
	}
	
	/**
	 * Creates susbsegments for the segment
	 * 
	 * @param cnt the number of subsegments
	 */
	private void makeSubsegments(int cnt) {
		Vector dir = segm.getUpperPosition().sub(segm.getLowerPosition());
		for (int i=0; i<cnt+1; i++) {
			double pos = i*segm.length/cnt;
			// System.err.println("SUBSEG:stem_radius");
			double rad = stemFactory.stemRadius(stem,segm.index*segm.length + pos);
			// System.err.println("SUBSEG: pos: "+ pos+" rad: "+rad+" inx: "+index+" len: "+length);
			
			segm.subsegments.addElement(new SubsegmentImpl(segm.getLowerPosition().add(dir.mul(pos/segm.length)),rad, pos));
		}
	}
	
	/**
	 * Make a subsegments for a segment with spherical end
	 * (last stem segment), subsegment lengths decrements near
	 * the end to get a smooth surface
	 * 
	 * @param cnt the number of subsegments
	 */
	private void makeSphericalEnd(int cnt) {
		Vector dir = segm.getUpperPosition().sub(segm.getLowerPosition());
		for (int i=0; i<cnt; i++) {
			double pos = segm.length-segm.length/Math.pow(2,i);
			double rad = stemFactory.stemRadius(stem,segm.index*segm.length + pos);
			//stem.DBG("FLARE: pos: %f, rad: %f\n"%(pos,rad))
			segm.subsegments.addElement(new SubsegmentImpl(segm.getLowerPosition().add(dir.mul(pos/segm.length)),rad, pos));
		}
		segm.subsegments.addElement(new SubsegmentImpl(segm.getUpperPosition(),segm.rad2,segm.length));
	}
	
	/**
	 * Make subsegments for a segment with flare
	 * (first trunk segment). Subsegment lengths are decrementing
	 * near the base of teh segment to get a smooth surface
	 * 
	 * @param cnt the number of subsegments
	 */
	private void makeFlare(int cnt) {
		Vector dir = segm.getUpperPosition().sub(segm.getLowerPosition());
		segm.subsegments.addElement(new SubsegmentImpl(segm.getLowerPosition(),segm.rad1,0));
		for (int i=cnt-1; i>=0; i--) {
			double pos = segm.length/Math.pow(2,i);
			double rad = stemFactory.stemRadius(stem,segm.index*segm.length+pos);
			//self.stem.DBG("FLARE: pos: %f, rad: %f\n"%(pos,rad))
			segm.subsegments.addElement(new SubsegmentImpl(segm.getLowerPosition().add(dir.mul(pos/segm.length)),rad, pos));
		}
	}
	
	/**
	 * Make subsegments for a segment with helical curving.
	 * They curve around with 360° from base to top of the
	 * segment
	 * 
	 * @param cnt the number of subsegments, should be higher
	 *        when a smooth curve is needed.
	 */
	private void makeHelix(int cnt) {
		double angle = Math.abs(lpar.nCurveV)/180*Math.PI;
		// this is the radius of the helix
		double rad = Math.sqrt(1.0/(Math.cos(angle)*Math.cos(angle)) - 1)*segm.length/Math.PI/2.0;
		DBG(stem,"Segment.make_helix angle: "+angle+" len: "+segm.length+" rad: "+rad);
		
		//self.stem.DBG("HELIX: rad: %f, len: %f\n" % (rad,len))
		for (int i=0; i<cnt+1; i++) {
			Vector pos = new Vector(rad*Math.cos(2*Math.PI*i/cnt)-rad,
					rad*Math.sin(2*Math.PI*i/cnt),
					i*segm.length/cnt);
			//self.stem.DBG("HELIX: pos: %s\n" % (str(pos)))
			// this is the stem radius
			double srad = stemFactory.stemRadius(stem,segm.index*segm.length + i*segm.length/cnt);
			segm.subsegments.addElement(new SubsegmentImpl(segm.transf.apply(pos), srad, i*segm.length/cnt));
		}
	}
	
	


}
