//  #**************************************************************************
//  #
//  #    $Id$  
//  #            - Segment class - a segment is a part of a stem
//  #
//  #    Copyright (C) 2003  Wolfram Diestel
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

import java.io.PrintWriter;
import java.text.NumberFormat;

import net.sourceforge.arbaro.transformation.*;
import net.sourceforge.arbaro.mesh.*;
import net.sourceforge.arbaro.params.*;

/**
 * Segments with helical curving or nonlinearly changing radius
 * can be broken into subsegments. Normal segments consist of only
 * one subsegment.
 * 
 * @author Wolfram Diestel
 */
class Subsegment { 
    // a Segment can have one or more Subsegments
    Vector pos; 
    double rad; 

    public Subsegment(Vector p, double r) {
	pos = p;
	rad = r; 
    }
};

/**
 * A segment class, multiple segments form a stem.
 * 
 * @author Wolfram Diestel
 */
class Segment {
  
    Params par;
    LevelParams lpar;
    int index;
    Transformation transf;
    double rad1;
    double rad2;
    double length;

    Stem stem;

    java.util.Vector subsegs;

    public Segment(Params params, LevelParams lparams, Stem stm, int inx, 
		   Transformation trf, double r1, double r2, double len) {
	par = params;
	lpar = lparams;
	index = inx;
	transf = trf; 
	rad1 = r1;
	rad2 = r2;
	length = len;
	stem = stm;
	// FIXME: rad1 and rad2 could be calculated only when output occurs (?)
	// or here in the constructor ?
	// FIXME: inialize subsegs with a better estimation of size
	subsegs = new java.util.Vector(10);
    }
	
    /**
     * Makes the segments from subsegments 
     */
    public void make() {
	// FIXME: numbers for cnt should correspond to Smooth value
	// helical stem
	if (lpar.nCurveV<0) { 
	    make_helix(10);
	}
    
	// spherical end
	else if (lpar.nTaper > 1 && lpar.nTaper <=2 && is_last_stem_segment()) {
	    make_spherical_end(10);
	}

	// periodic tapering
	else if (lpar.nTaper>2) {
	    make_subsegs(20);
	}
	
	// trunk flare
	// FIXME: if nCurveRes[0] > 10 this division into several
	// subsegs should be extended over more then one segments?
	else if (lpar.level==0 && par.Flare!=0 && index==0) {
	    stem.DBG("Segment.make() - flare");
	    make_flare(10);

	} else {
	    make_subsegs(1);
	}
    }

    /**
     * Creates susbsegments for the segment
     * 
     * @param cnt the number of subsegments
     */
    private void make_subsegs(int cnt) {
	Vector dir = pos_to().sub(pos_from());
	for (int i=0; i<cnt+1; i++) {
	    double pos = i*length/cnt;
	    // System.err.println("SUBSEG:stem_radius");
	    double rad = stem.stem_radius(index*length + pos);
	    // System.err.println("SUBSEG: pos: "+ pos+" rad: "+rad+" inx: "+index+" len: "+length);

	    subsegs.addElement(new Subsegment(pos_from().add(dir.mul(pos/length)),rad));
	}
    }

    /**
     * Make a subsegments for a segment with spherical end
     * (last stem segment), subsegment lengths decrements near
     * the end to get a smooth surface
     * 
     * @param cnt the number of subsegments
     */
    private void make_spherical_end(int cnt) {
	Vector dir = pos_to().sub(pos_from());
	for (int i=0; i<cnt; i++) {
	    double pos = length-length/Math.pow(2,i);
	    double rad = stem.stem_radius(index*length + pos);
      //stem.DBG("FLARE: pos: %f, rad: %f\n"%(pos,rad))
	    subsegs.addElement(new Subsegment(pos_from().add(dir.mul(pos/length)),rad));
	}
	subsegs.addElement(new Subsegment(pos_to(),rad2));
    }

    /**
     * Make subsegments for a segment with flare
     * (first trunk segment). Subsegment lengths are decrementing
     * near the base of teh segment to get a smooth surface
     * 
     * @param cnt the number of subsegments
     */
    private void make_flare(int cnt) {
	Vector dir = pos_to().sub(pos_from());
	subsegs.addElement(new Subsegment(pos_from(),rad1));
	for (int i=cnt-1; i>=0; i--) {
	    double pos = length/Math.pow(2,i);
	    double rad = stem.stem_radius(index*length+pos);
	    //self.stem.DBG("FLARE: pos: %f, rad: %f\n"%(pos,rad))
	    subsegs.addElement(new Subsegment(pos_from().add(dir.mul(pos/length)),rad));
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
    private void make_helix(int cnt) {
	double angle = Math.abs(lpar.nCurveV)/180*Math.PI;
	// this is the radius of the helix
	double rad = Math.sqrt(1.0/(Math.cos(angle)*Math.cos(angle)) - 1)*length/Math.PI/2.0;
	stem.DBG("Segment.make_helix angle: "+angle+" len: "+length+" rad: "+rad);

	//self.stem.DBG("HELIX: rad: %f, len: %f\n" % (rad,len))
	for (int i=0; i<cnt+1; i++) {
	    Vector pos = new Vector(rad*Math.cos(2*Math.PI*i/cnt)-rad,
				rad*Math.sin(2*Math.PI*i/cnt),
				i*length/cnt);
	    //self.stem.DBG("HELIX: pos: %s\n" % (str(pos)))
	    // this is the stem radius
	    double srad = stem.stem_radius(index*length + i*length/cnt);
	    subsegs.addElement(new Subsegment(transf.apply(pos),srad));
	}
    }
	
    /**
     * Calcs the position of a substem in the segment given 
	 * a relativ position where in 0..1 - needed esp. for helical stems,
	 * because the substems doesn't grow from the axis of the segement
     *
     * @param trf the transformation of the substem
     * @param where the offset, where the substem spreads out
     * @return the new transformation of the substem (shifted from
     *        the axis of the segment to the axis of the subsegment)
     */
    public Transformation substem_position(Transformation trf,double where) {
	if (lpar.nCurveV>=0) { // normal segment 
	    return trf.translate(transf.getZ().mul(where*length));
	} else { // helix
	    // get index of the subsegment
	    int i = (int)where*(subsegs.size()-1);
	    // interpolate position
	    Vector p1 = ((Subsegment)subsegs.elementAt(i)).pos;
	    Vector p2 = ((Subsegment)subsegs.elementAt(i+1)).pos;
	    Vector pos = p1.add(p2.sub(p1).mul(where - i/(subsegs.size()-1)));
	    return trf.translate(pos.sub(pos_from()));
	}
    }
  
    /**
     * Position at the beginning of the segment
     * 
     * @return beginning point of the segment
     */
    Vector pos_from() {
	// self.stem.DBG("segmenttr0: %s, t: %s\n"%(self.transf_pred,self.transf_pred.t()))
	return transf.getT();
    }
  
    /**
     * Position of the end of the segment
     * 
     * @return end point of the segment
     */
    Vector pos_to() {
	//self.stem.DBG("segmenttr1: %s, t: %s\n"%(self.transf,self.transf.t()))
	return transf.getT().add(transf.getZ().mul(length));
    }

    /**
     * Tests, if the segment is the first stem segment
     * 
     * @return true, if it's the first stem segment, false otherwise
     */
    boolean is_first_stem_segment() {
	return index == 0;
    }
  
    /**
     * Tests, if the segment ist the last stem segment
     * 
     * @return true, if it's the last stem segment, false otherwise
     */
    boolean is_last_stem_segment() {
	return index == lpar.nCurveRes-1;
    }

    /**
     * Returns a number of spaces
     * 
     * @param len number of spaces
     * @return string made from spaces
     */
    private String whitespace(int len) {
	char[] ws = new char[len];
	for (int i=0; i<len; i++) {
	    ws[i] = ' ';
	}
	return new String(ws);
    }
  
    /**
     * Outputs Povray code for the segment, when output type is
     * primitives
     * 
     * @param w the output stream
     */
    public void povray(PrintWriter w) {
	String indent = whitespace(lpar.level*2+4);
	NumberFormat fmt = FloatFormat.getInstance();
    
	// FIXME: for cone output - if starting direction is not 1*y, there is a gap 
	// between earth and tree base
	// best would be to add roots to the trunk(?)	  
    
	for (int i=0; i<subsegs.size()-1; i++) {
	    Subsegment ss1 = (Subsegment)subsegs.elementAt(i);
	    Subsegment ss2 = (Subsegment)subsegs.elementAt(i+1);
	    w.println(indent + "cone   { " + ss1.pos.povray() + ", "
		      + fmt.format(ss1.rad) + ", " 
		      + ss2.pos.povray() + ", " 
		      + fmt.format(ss2.rad) + " }"); 
	    // for helix subsegs put spheres between
	    if (lpar.nCurveV<0 && i<subsegs.size()-2) {
		w.println(indent + "sphere { " 
			  + ss1.pos.povray() + ", "
			  + fmt.format(ss1.rad-0.0001) + " }");
	    }
	}
    
	// put sphere at segment end
	if ((rad2 > 0) && (! is_last_stem_segment() || 
			   (lpar.nTaper>1 && lpar.nTaper<=2))) 
	    {  
		w.println(indent + "sphere { " + pos_to().povray() + ", "
			  + fmt.format(rad2-0.0001) + " }");
	    }
    }
  
    /**
	 * Creates the mesh points for a cross section somewhere in the segment
     * 
     * @param pos the position of the section
     * @param rad the radius of the cross section
     * @param meshpart the mesh part where the points should be added
     * @param donttrf if true, the transformation is not applied to the section points
     */
    void create_section_meshpoints(Vector pos, double rad, MeshPart meshpart,
				   boolean donttrf) {
	//h = (self.index+where)*self.stem.segment_len
	//rad = self.stem.stem_radius(h)
	// self.stem.DBG("MESH: pos: %s, rad: %f\n"%(str(pos),rad))

	// System.err.println("Segment-create meshpts, pos: "+pos+" rad: "+rad);

    
	Transformation trf = transf.translate(pos.sub(pos_from()));
	//self.stem.TRF("MESH:",trf)
    
	// if radius = 0 create only one point
	if (rad<0.000001) {
	    MeshSection section = new MeshSection(1);
	    section.add_point(trf.apply(new Vector(0,0,0)));
	    meshpart.add_section(section);
	} else { //create pt_cnt points
	    int pt_cnt = lpar.mesh_points;
	    MeshSection section = new MeshSection(pt_cnt);
	    //stem.DBG("MESH+LOBES: lobes: %d, depth: %f\n"%(self.tree.Lobes, self.tree.LobeDepth))
      
	    for (int i=0; i<pt_cnt; i++) {
		double angle = i*360/pt_cnt;
		// for Lobes ensure that points are near lobes extrema, but not exactly there
		// otherwise there are to sharp corners at the extrema
		if (lpar.level==0 && par.Lobes != 0) {
		    angle -= 10/par.Lobes;
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
					 subsegs.size());
		    pt = pt.mul(rad1*(1.0+par.LobeDepth*Math.cos(par.Lobes*angle*Math.PI/180.0))); 
		} else {
		    pt = pt.mul(rad); // faster - no radius calculations
		}
		// apply transformation to it
		// (for the first trunk segment transformation shouldn't be applied to
		// the lower meshpoints, otherwise there would be a gap between 
		// ground and trunk)
		// FIXME: for helical stems may be/may be not a rotation 
		// should applied additionally?
	
		if (! donttrf) {  // not (stem.level==0 && index==0):
		    pt = trf.apply(pt);
		} else {
		    // FIXME: should apply z-rotation and translation only here
		    pt = trf.apply(pt); //FIXME: trf.getT(); // tranlate only
		}
		//self.stem.DBG("MESHPT: %s\n"%(pt))
		section.add_point(pt);
	    }
	    //add section to the mesh part
	    meshpart.add_section(section);
	}
    }
  
    /**
     * Adds the segments to a mesh part. For every subsegment one ore
     * two mesh sections are added.
     * 
     * @param meshpart the mesh part, to wich the segment should be added
     */
    public void add_to_meshpart(MeshPart meshpart) {
	// creates the part of the mesh for this segment
	//pt_cnt = self.tree.meshpoints[self.stem.level]
	//smooth = self.stem.level<=self.tree.smooth_mesh_level

	if (meshpart.size() == 0) { // first segment, create lower meshpoints
	    Subsegment ss = (Subsegment)subsegs.elementAt(0);
	    // one point at the stem origin, with normal in reverse z-direction
	    create_section_meshpoints(ss.pos,0,meshpart,
				      is_first_stem_segment() && lpar.level==0);
	    ((MeshSection)meshpart.firstElement()).set_normals_vector(transf.getZ().mul(-1));
      
	    // more points around the stem origin
	    create_section_meshpoints(ss.pos,ss.rad,meshpart,
				      is_first_stem_segment() && lpar.level==0);
	}
    
	// create meshpoints on top of each subsegment
	for (int i=1; i<subsegs.size(); i++) {
	    Subsegment ss = (Subsegment)subsegs.elementAt(i);
	    create_section_meshpoints(ss.pos,ss.rad,meshpart,false);
	}

	// System.err.println("MESHCREATION, segmindex: "+index);
    
	// close mesh with normal in z-direction
	if (is_last_stem_segment()) {
	    if (rad2>0.000001) {
		create_section_meshpoints(pos_to(),0,meshpart,false);
	    }
	    //DBG System.err.println("LAST StemSegm, setting normals to Z-dir");
	    ((MeshSection)meshpart.lastElement()).set_normals_vector(transf.getZ());
	}
    }
};
























