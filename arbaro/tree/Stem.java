//  #**************************************************************************
//  #
//  #    $Id$  
//  #             - Stem class - here is most of the logic of 
//  #               the tree generating algorithm
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

import net.sourceforge.arbaro.transformation.*;
import net.sourceforge.arbaro.params.*;
import net.sourceforge.arbaro.mesh.*;

class ArbaroError extends Exception {
    public ArbaroError(String errmsg) {
	super(errmsg);
    }
};

class ErrorNotYetImplemented extends ArbaroError{
    public ErrorNotYetImplemented(String errmsg) {
	super(errmsg);
    } 
};


public class Stem {
    // A helper class for making 3d trees, this class makes a stem
    // (trunk or branch)
    Tree tree;
    Params par;
    LevelParams lpar;
    Stem parent; // the parent stem

    Transformation transf;
    //FIXME: trees with Levels>4 not yet tested!!!
    int stemlevel; // the branch level, coudl be > 4
    double offset; // how far from the parent's base
  
    java.util.Vector segments; // the segments forming the stem
    java.util.Vector clones;      // the stem clones (for splitting)
    java.util.Vector substems;    // the substems
    java.util.Vector leaves;     // the leaves

    double length;
    double segment_len;
    int segment_cnt;
    double base_radius;

    double length_child_max;
    double substems_per_segment;
    double substem_rotangle;

    double leaves_per_segment;
    double split_corr;

    boolean prunetest; // flag for pruning cycles

    int index; // substem number
    java.util.Vector clone_index; // clone number (Integers)

    public Stem(Tree tr, Params params, LevelParams lparams, Stem parnt, int stlev,
		Transformation trf, double offs) /* offs=0 */ {
	tree = tr;
	par = params;
	lpar = lparams;
	parent = parnt;
	stemlevel = stlev;
	transf = trf; 
	offset = offs;

	// initialize lists
	segments = new java.util.Vector(lpar.nCurveRes);

	if (lpar.nSegSplits > 0 || par._0BaseSplits>0) {
	    clones = new java.util.Vector(); // lpar.nSegSplits*lpar.nCurveRes+1);
	}

	if (stemlevel < par.Levels-1) {
	    LevelParams lpar_1 = params.levelParams[Math.min(lpar.level+1,3)];
	    substems = new java.util.Vector(lpar_1.nBranches);
	}

	if (stemlevel == par.Levels-1 && par.Leaves != 0) {
	    leaves = new java.util.Vector(Math.abs(par.Leaves));
	}

	// inialize other variables
	leaves_per_segment = 0;
	split_corr = 0;
	index=0; // substem number
	clone_index = new java.util.Vector();
	prunetest = false; // flag used for pruning
      
	//...
    }

    void TRF(String where, Transformation trf) {
	// print out the transformation to stderr nicely if debugging is enabled
	DBG(where + ": " + trf.toString());
    }

    public void DBG(String dbgstr) {
	// print debug string to stderr if debugging is enabled
	if (par.debug) System.err.println(tree_position() + ":" + dbgstr);
    }

    String tree_position() {
	// returns the position of the stem in the tree as a string, e.g. 0c0.1
	// for the second substem of the first clone of the trunk
	Stem stem = this;
	int lev = stemlevel;
	String pos = "";
	while (lev>=0) {
	    if (stem.clone_index.size()>0) { 
		String clonestr = "";
		for (int i=0; i<stem.clone_index.size(); i++) {
		    clonestr += "c"+((Integer)stem.clone_index.elementAt(i)).toString();
		}
		pos = "" + stem.index+clonestr+"."+pos;
	    } else {
		pos = "" + stem.index+"."+pos;
	    }
	    if (lev>0) stem = stem.parent;
	    lev--;
	} 	
	if (pos.charAt(pos.length()-1) == '.') pos = pos.substring(0,pos.length()-1);
	return pos;
    }
	 
    Stem clone(Transformation trf, int start_segm) {
	// creates a clone stem with same atributes as this stem
	Stem clone = new Stem(tree,par,lpar,parent,stemlevel,trf,offset);
	clone.segment_len = segment_len;
	clone.segment_cnt = segment_cnt;
	clone.length = length;
	clone.base_radius = base_radius;
	clone.split_corr = split_corr; 
	clone.prunetest = prunetest;
	clone.index = index;

	//DBG("Stem.clone(): clone_index "+clone_index);
	clone.clone_index.addAll(clone_index);

	//DBG("Stem.clone(): level: "+stemlevel+" clones "+clones);
	clone.clone_index.addElement(new Integer(clones.size()));
	if (! prunetest) {
	    clone.length_child_max = length_child_max;
	    //clone.substem_cnt = substem_cnt;
	    clone.substems_per_segment = substems_per_segment;
	    //clone.substemdist = substemdist;
	    //clone.substemdistv = substemdistv;
	    //clone.seg_splits = self.seg_splits
	    // FIXME: for more then one clone this angle should somehow
	    // correspond to the rotation angle of the clone
	    clone.substem_rotangle=substem_rotangle+180;
	    clone.leaves_per_segment=leaves_per_segment;
	}
	return clone;
    }
    
    public void make() {
	// makes the stem with all its segments, substems, clones and leaves
	segment_cnt = lpar.nCurveRes;
	length = stem_length();
	segment_len = length/lpar.nCurveRes;
	base_radius = stem_base_radius();

	DBG("Stem.make(): len: "+length+" sgm_cnt: "+ segment_cnt+" base_rad: "+base_radius);

	// FIXME: should pruning occur for the trunk too?
	if (stemlevel>0 && par.PruneRatio > 0) {
	    pruning();
	}
	// FIXME: if length=0 the stem object persists here but without any segments
	// alternativly make could return an error value, the invoking function
	// then had to delete this stem
	if (length > 0.001 * par.scale_tree) {
	    prepare_substem_params();
	    make_segments(0,segment_cnt);
	} else {
	    DBG("length "+length+" (after pruning?) to small - stem not created");
	}
    }
		
    void pruning() {
	//if (par.verbose) System.err.print("?");
	// save random state, split and len values
	lpar.saveState();
	double splitcorr = split_corr;
	double origlen = length;
	double seglen = segment_len;
	
	// start pruning
	prunetest = true;
	//DBG("PRUNE-test: level: %d, offs: %f, len: %f, segmts: %d\n"%
	//  		(self.level,self.offset,self.length,self.segment_cnt))
	  	
	// test length
	int segm = make_segments(0,segment_cnt);
	//self.DBG("PRUNE-test returned %d\n"%(segm))
	while (segm >= 0 && length > 0.001*par.scale_tree) {
	    //self.DBG("PRUNE: level: %d, offs: %f, len: %f, segm: %d/%d\n"%\
	    //			(self.level,self.offset,self.length,segm,self.segment_cnt))
	    
	    // restore random state and split values
	    lpar.restoreState();
	    split_corr = splitcorr;
	    // delete segments and clones
	    if (clones != null) clones.clear();
	    segments.clear();
	    //FIXME: get somehow a good value how much length should be shortened
	    // calc new length and related values
	    
	    // length = length-seglen
	    
	    // get new length
	    double minlen = length/2; // shorten max. half of length
	    double maxlen = length-origlen/15; // shorten min of 1/15 of orig. len
	    length = Math.min(Math.max(segment_len*segm,minlen),maxlen);
	    
	    // calc new values dependent from length
	    segment_len = length/lpar.nCurveRes;
	    base_radius = stem_base_radius();
	    // test once more
	    if (length > 0) {
		//self.DBG("PRUNE-retry: level: %d, offs: %f, len: %f, segmts: %d\n"%\
		//		(self.level,self.offset,self.length,self.segment_cnt))
	  	
		segm = make_segments(0,segment_cnt);
	    }
	}
	// this length fits the envelope, 
	// diminish the effect corresp. to PruneRatio
	length = origlen - (origlen-length)*par.PruneRatio;
	  	
	// restore random state and split values
	lpar.restoreState();
	split_corr = splitcorr;
	// delete segments and clones
	if (clones != null) clones.clear();
	segments.clear();
	prunetest = false;
	//self.DBG("PRUNE-ok: len: %f, segm: %d/%d\n"%(self.length,segm,self.segment_cnt))
    }
   
    double stem_length() {
	if (stemlevel == 0) { // trunk
	    return (lpar.nLength + lpar.var(lpar.nLengthV)) * par.scale_tree;
	} else if (stemlevel == 1) {
	    double parlen = parent.length;
	    double baselen = par.BaseSize*par.scale_tree;
	    double ratio  = (parlen-offset)/(parlen-baselen);
	    DBG("Stem.stem_length(): parlen: "+parlen+" offset: "+offset+" baselen: "+baselen+" ratio: "+ratio);
	    return parlen * parent.length_child_max * par.shape_ratio(ratio);
	} else { // higher levels
	    return parent.length_child_max*(parent.length-0.6*offset);
	}
    }

    // makes the segments of the stem
    int make_segments(int start_seg,int end_seg) {
	if (stemlevel==1) tree.setMakeProgress();
	
	if (par.verbose) {
	    if (! prunetest) {
		if (stemlevel==0) System.err.print("=");
		else if (stemlevel==1 && start_seg==0) System.err.print("/");
		else if (stemlevel==2 && start_seg==0) System.err.print(".");
	    }
	}
	
	Transformation trf = transf;
	  	
	for (int s=start_seg; s<end_seg; s++) {
	    if (stemlevel==0) tree.setMakeProgress();
	
	    if (! prunetest && par.verbose) {
		if (stemlevel==0) System.err.print("|");
	    }
	  
	    // curving
	    trf=new_direction(trf,s);
	    TRF("Stem.make_segments(): after new_direction ",trf);
	    
	    // segment radius
	    double rad1 = stem_radius(s*segment_len);
	    double rad2 = stem_radius((s+1)*segment_len);
	    
	    // create new segment
	    Segment segment = new Segment(par,lpar,this,s,trf,rad1,rad2,segment_len);
	    segment.make();
	    segments.addElement(segment);
	      
	    // create substems
	    // self.DBG("SS-makingsubst? pt: %d, lev: %d\n"%(self.prunetest,self.level))
	    if (! prunetest && lpar.level<par.Levels-1) {
		// self.DBG("SS-making substems\n")
		make_substems(segment);
	    }
	    
	    if (! prunetest && lpar.level==par.Levels-1 && par.Leaves!=0) {
		make_leaves(segment);
	    }

	    // shift to next position
	    trf = trf.translate(trf.getZ().mul(segment_len));
	    //self.DBG("transf: %s\n"%(transf))
	    //self.DBG("pos: %s\n"%(transf.vector))
	      
	    // test if too long
	    if (prunetest && ! inside_envelope(trf.getT())) {
		// DBG("PRUNE: not inside - return %d\n"%(s))
		return s;
	    }
    
	    // splitting (create clones)
	    if (s<end_seg-1) {
		int segm = make_clones(trf,s);
		// trf is changed by make_clones
		// prune test - clone not inside envelope 
		if (segm>=0) {
		    //DBG("PRUNE: clone not inside - return %d\n"%(segm))
		    return segm;
		}
	    }
	}

	return -1;
    }
    
    boolean inside_envelope(Vector vector) {
	// FIXME: why vector is of type Matrix here?
	// self.DBG("TRANSFVECTOR: %s, class: %s\n"%(vector,vector.__class__))
	// vector = Vector(data=vector.col(0))
	//  self.DBG("TRANSFVECTOR: %s, class: %s\n"%(vector,vector.__class__))
	double r = Math.sqrt(vector.getX()*vector.getX() + vector.getY()*vector.getY());
	double ratio = (par.scale_tree - vector.getZ())/(par.scale_tree*(1-par.BaseSize));
	return (r/par.scale_tree) < (par.PruneWidth * par.shape_ratio(ratio,8));
    }

  
    Transformation new_direction(Transformation trf, int nsegm) {
	    // next segments  direction
    
	    // I think the first segment shouldn't get another
	    // direction because there is yet a variation from down and rotation angle
	    // otherwise the first trunk segment should get a random rotation(?)
	    if (nsegm == 0 && stemlevel>0) return trf;
	  
	    TRF("Stem.new_direction() before curving",trf);
	  
	    // get curving angle
	    double delta;
	    if (lpar.nCurveBack==0) {
		delta = lpar.nCurve / lpar.nCurveRes;

	    } else {
		if (nsegm < (lpar.nCurveRes+1)/2) {
		    delta = lpar.nCurve*2 / lpar.nCurveRes;
		} else {
		    delta = lpar.nCurveBack*2 / lpar.nCurveRes;
		}
	    }
	    delta += split_corr;
	    DBG("Stem.new_direction(): delta: "+delta);
	    trf = trf.rotx(delta);
	  
	  
	    // With Weber/Penn the orientation of the x- and y-axis 
	    // shouldn't be disturbed (maybe, because proper curving relies on this)
	    // so may be such random rotations shouldn't be used, instead nCurveV should
	    // add random rotation to rotx, and rotate nCurveV about the tree's z-axis too?
	   
	    // add random rotation about z-axis
	    if (lpar.nCurveV > 0) {
		if (nsegm==0 && stemlevel==0) { // first_trunk_segment
		    // random rotation more moderate
		    delta = (Math.abs(lpar.var(lpar.nCurveV)) - 
			     Math.abs(lpar.var(lpar.nCurveV)))
			/ lpar.nCurveRes;
		}	else {
		    // full random rotation
		    delta = lpar.var(lpar.nCurveV)/lpar.nCurveRes;
		}
		// self.DBG("curvV (delta): %s\n" % str(delta))
		double rho = 180+lpar.var(180);
		trf = trf.rotaxisz(delta,rho);
	    }  
	    TRF("Stem.new_direction() after curving",trf);
	    
	    // attraction up/down
	    if (par.AttractionUp != 0 && stemlevel>=2) {
		//self.TRF("new_direction ATTRAC:",transf)
		double decl = Math.acos(trf.getZ().getZ());
		// FIXME: what's wrong here??????
		// orient = acos(trf[Y][Z])
		// self.DBG("ATTRAC decl: %f, orient %f, cos(orient): %f\n" % (decl*180/pi,orient*180/pi,cos(orient)))
		//curve_up = self.tree.AttractionUp * abs(decl * cos(orient)) 
		//	/ self.tree.nCurveRes[self.level]

		double curve_up = par.AttractionUp * 
		    Math.abs(decl * Math.sin(decl)) / lpar.nCurveRes;
		//	self.DBG("ATTRAC curve_up: %f\n" % (curve_up*180/pi))
		Vector z = trf.getZ();
		trf = trf.rotaxis(-curve_up*180/Math.PI,new Vector(-z.getY(),z.getX(),0));
		//transf = transf.rotx(curve_up*180/pi)
		//self.TRF("new_direction ATTRAC:",transf)
	    }
	    return trf;
    }
	  
    double stem_base_radius() {
	if (stemlevel == 0) { // trunk
	    // radius at the base of the stem
	    // FIXME: I think nScale+-nScaleV should applied to the stem radius but not to base radius(?)
	    return length * par.Ratio * par._0Scale; 
	    //+ self.var(self.tree.nScaleV[0]))
	} else {
	    // max radius is the radius of the parent at offset
	    double max_radius = parent.stem_radius(offset);
	    double radius = parent.base_radius * Math.pow(length/parent.length,par.RatioPower);
	    return Math.min(radius,max_radius);
	}
    }

    public double stem_radius(double h) {
	DBG("Stem.stem_radius("+h+") base_rad:"+base_radius);

	double angle = 0; //FIXME: add some arg angle for Lobes, but in the moment
	// Lobes are calculated later in mesh creation

	// gets the stem width at a given position within the stem
	double Z = h/length;
	double taper = lpar.nTaper;
	  
	double unit_taper=0;
	if (taper <= 1) {
	    unit_taper = taper;
	} else if (taper <=2) {
	    unit_taper = 2 - taper;
	}
	
	double radius = base_radius * (1 - unit_taper * Z);

	// spherical end or periodic tapering
	double depth;
	if (taper>1) {
	    double Z2 = (1-Z)*length;
	    if (taper<2 || Z2<radius) {
		depth = 1;
	    } else {
		depth=taper-2;
	    }
	    double Z3;
	    if (taper<2) {
		Z3=Z2;
	    } else {
		Z3=Math.abs(Z2-2*radius*(int)(Z2/2/radius+0.5));
	    }
	    if (taper>2 || Z3<radius) {
		radius=(1-depth)*radius+depth*Math.sqrt(radius*radius-(Z3-radius)*(Z3-radius));
		//  self.DBG("TAPER>2: Z2: %f, Z3: %f, depth: %f, radius %f\n"%(Z2,Z3,depth,radius))
	    }	  
	}	    
	//   DBG("Stem.stem_radius(): stemlevel:"+stemlevel);
	if (stemlevel==0) { 
	    // add flaring (thicker stem base)
	    //DBG("Stem.stem_radius(): Flare: "+par.Flare);
	    if (par.Flare != 0) {
		double flare = par.Flare * (Math.pow(100,(1-8*Z)) - 1) / 100.0 + 1;
		DBG("Stem.stem_radius(): Flare: "+flare+" h: "+h+" Z: "+Z);
		radius = radius*flare;
	    }
	    // add lobes - this is done in mesh creation not here at the moment
	    if (par.Lobes>0 && angle!=0) {
		// FIXME: use the formular from Segment.create_mesh_section() instead
		radius = radius*(1.0+par.LobeDepth*Math.sin(par.Lobes*angle*Math.PI/180));
	    }
	     	
	
	    // FIXME:? this is always part of the formular for base_radius, isn't it?
	    //if self.level==0:
	    // FIXME: don't no if 0ScaleV scales arbitrary for every calculation
	    // here, otherwise this scaling factor should be calced once in trunk.__init__
	    // and only applied here
	    //	radius = radius*(self.tree.nScale[0]+self.var(self.tree.nScaleV[0]))
	    
	}

	DBG("Stem.stem_radius("+h+") = "+radius);
	
	return radius;
    }
	

    void prepare_substem_params() {
	//int level = min(stemlevel+1,3);
	LevelParams lpar_1 = par.levelParams[Math.min(stemlevel+1,3)];
	  
	// splitting ratio
	// self.seg_splits = self.tree.nSegSplits[self.level]
	
	// maximum length of a substem
	length_child_max = lpar_1.nLength+lpar_1.var(lpar_1.nLengthV);
				
	// maximum number of substems
	double stems_max = lpar_1.nBranches;
	// actual number of substems and substems per segment
	double substem_cnt;
	if (stemlevel==0) {
	    substem_cnt = stems_max;
	    substems_per_segment = substem_cnt / (float)segment_cnt / (1-par.BaseSize);
	    DBG("Stem.prepare_substem_params(): stems_max: "+ substem_cnt 
		+ " substems_per_segment: " + substems_per_segment);
	} else if (stemlevel==1) {
	    substem_cnt = (int)(stems_max * 
				(0.2 + 0.8*length/parent.length/parent.length_child_max));
	    substems_per_segment = substem_cnt / (float)segment_cnt;
	    //	    DBG("Stem.prepare_substem_params(): stems_max: "+stems_max+ "length/parlen"+
	    //		(length/parent.length) + "len_ch_max: "+parent.length_child_max);
	    DBG("Stem.prepare_substem_params(): substem_cnt: "+ substem_cnt 
		+ " substems_per_segment: " + substems_per_segment);
	} else {
	    substem_cnt = (int)(stems_max * (1.0 - 0.5 * offset/parent.length));
	    substems_per_segment = substem_cnt / (float)segment_cnt;
	}
	substem_rotangle = 0;
	  
	// how much leaves for this stem - not really a substem parameter
	if (lpar.level == par.Levels-1) {
	    leaves_per_segment = leaves_per_branch() / segment_cnt;
	}
    }
   
    int leaves_per_branch() {
	// calcs the number of leaves for a stem
	if (par.Leaves==0) return 0;
	if (stemlevel == 0) {
	    // FIXME: maybe set Leaves=0 when Levels=1 in Params.prepare()
	    System.err.println("WARNING: trunk cannot have leaves, no leaves are created");
	    return 0;
	}
	return (int)(Math.abs(par.Leaves) 
		     * par.shape_ratio(offset/parent.length,par.LeafDistrib) 
		     * par.LeafQuality);
    }

    void make_substems(Segment segment) {
	// creates substems for the current segment
	LevelParams lpar_1 = par.levelParams[Math.min(stemlevel+1,3)];

	DBG("Stem.make_substems(): substems_per_segment "+substems_per_segment);

	double subst_per_segm;
	double offs;

	if (stemlevel>0) {
	    // full length of stem can have substems
	    subst_per_segm = substems_per_segment;

	    if (segment.index==0) {
		offs = parent.stem_radius(offset)/segment_len;
	    } else { offs = 0; }

	} else if (segment.index*segment_len > par.BaseSize*length) {
	    // segment is fully out of the bare trunk region => normal nb of substems
	    subst_per_segm = substems_per_segment;
	    offs = 0;
	} else if ((segment.index+1)*segment_len <= par.BaseSize*length) {
	    // segment is fully part of the bare trunk region => no substems
	    return;
	} else {
	    // segment has substems in the upper part only
	    offs = (par.BaseSize*length - segment.index*segment_len)/segment_len;
	    // DBG("base: %f seg: %d seglen: %f offs: %f\n" %
	    // (self.tree.BaseSize*self.length,segment.index*self.segment_len,
	    //	  			self.segment_len,offs))
	    subst_per_segm = substems_per_segment*(1-offs);
	}	
	  
	// how many substems in this segment
	int substems_eff = (int)(subst_per_segm + lpar.substemErrorValue+0.5);
	  		 
	// adapt error value
	lpar.substemErrorValue -= (substems_eff - subst_per_segm);
	  
	//DBG("SS-%d/%d: subst/segm: %f, subst_eff: %f, err: %f\n" % (self.level,segment.index,
	//	  	subst_per_segm,substems_eff,self.tree.substemErrorValue[self.level]))
	
	//	  self.DBG("level: %d, segm: %d, substems: %d\n" % 
	//	  	(self.level,segment.index,substems_eff))

	if (substems_eff <= 0) return;
	  
	DBG("Stem.make_substems(): substems_eff: "+substems_eff);

	// what distance between the segements substems
	double dist = (1.0-offs)/substems_eff*lpar_1.nBranchDist;
	double distv = dist*lpar_1.nBranchDistV/2;

	DBG("Stem.make_substems(): offs: "+offs+" dist: "+dist+" distv: "+distv);

	for (int s=0; s<substems_eff; s++) {
	    // where on the segment add the substem
	    double where = offs+dist/2+s*dist+lpar_1.var(distv);

	    //	      self.DBG("SS-DIST: where: %f, dist: %f, distv: %f\n" % 
	    //	      	(where,dist,distv))
	      
	    //offset from stembase
	    double offset = (segment.index + where) * segment_len;

	    DBG("Stem.make_substems(): offset: "+ offset+" segminx: "+segment.index
		+" where: "+where+ " seglen: "+segment_len);
	      
	    //self.TRF("make_substems segment trf",segment.transf)
	    //      # get the direction for the substem
	    //      # FIXME: this changes the own random state, thus incrementing
	    //      # one level the substems get a new distribution, instead
	    //      # the random generator of the next level should be used!
	    Transformation trf = substem_direction(segment.transf,offset);
	      
	    //	      # translate it to its position on the stem
	    //	      #transf = transf.translate(segment.transf.z()*where*self.segment_len)
	    trf = segment.substem_position(trf,where);
	      
	    //self.TRF("make_substems subst trf",transf)
	    
	    // create new substem
	    Stem substem = new Stem(tree,par,lpar_1,this,stemlevel+1,trf,offset);
	    substem.index=substems.size();
	    DBG("Stem.make_substems(): make new substem");
	    substem.make();
	    substems.addElement(substem);
	}
    }
      
    Transformation substem_direction(Transformation trf, double offset) {
	LevelParams lpar_1 = par.levelParams[Math.min(stemlevel+1,3)];
	//lev = min(level+1,3);

	// get rotation angle
	double rotangle;
	if (lpar_1.nRotate>=0) { // rotating substems
	    substem_rotangle = (substem_rotangle + lpar_1.nRotate+lpar_1.var(lpar_1.nRotateV)+360) % 360;
	    rotangle = substem_rotangle;
	} else { // alternating substems
	    if (Math.abs(substem_rotangle) != 1) substem_rotangle = 1;
	    substem_rotangle = -substem_rotangle;
	    rotangle = substem_rotangle * (180+lpar_1.nRotate+lpar_1.var(lpar_1.nRotateV));
	}

	// get downangle
	double downangle;
	if (lpar_1.nDownAngleV>=0) {
	    downangle = lpar_1.nDownAngle+lpar_1.var(lpar_1.nDownAngleV);
	} else {
	    double len = (stemlevel==0)? length*(1-par.BaseSize) : length;
	    downangle = lpar_1.nDownAngle +
		lpar_1.nDownAngleV*(1 - 2 * par.shape_ratio((length-offset)/len,0));
	}  
	//self.DBG("substem_direction %d: down %s, rot %s, ssrot: %s\n" % (level,str(downangle),str(rotangle),
	//	  	str(self.substem_rotangle)))
	DBG("Stem.substem_direction(): down: "+downangle+" rot: "+rotangle);
	  
	return trf.rotxz(downangle,rotangle);
    }
	      
    void make_leaves(Segment segment) {
	// creates leaves for the current segment
 
	if (par.Leaves > 0) { // # normal mode
	    // how many leaves in this segment
	    // FIXME: leavesErrorValue isn't needed for several levels, move it to par
	    double leaves_eff = (int)(leaves_per_segment + lpar.leavesErrorValue+0.5);
	  		 
	    //	  	self.DBG("n-o of leaves: %d\n" % leaves_eff)
	  
	    // adapt error value
	    lpar.leavesErrorValue -= (leaves_eff - leaves_per_segment);
	  
	    if (leaves_eff <= 0) return;
	  
	    double offs;
	    if (segment.index==0) {
		offs = parent.stem_radius(offset)/segment_len;
	    } else {
		offs = 0;
	    }
	  
	    // what distance between the leaves
	    double dist = (1.0-offs)/leaves_eff;
	  
	    for (int s=0; s<leaves_eff; s++) {
		// where on the segment add the leaf
	
		// FIXME: may be use the same distribution method (BranchDist) as for substems?
		double where = offs+dist/2+s*dist+lpar.var(dist/2);
	      
		// offset from stembase
		double loffs = (segment.index+where)*segment_len;
		// get a new direction for the leaf
		Transformation trf = substem_direction(segment.transf,loffs);
		// translate it to its position on the stem
		trf = trf.translate(segment.transf.getZ().mul(where*segment_len));
	
		// create new leaf
		// FIXME: realy stemlevel+1 here?
		Leaf leaf = new Leaf(par,trf,loffs);
		leaf.make();
		leaves.addElement(leaf);
		
	    }
	}

	// leaves placed in a fan at stem end
	else if (par.Leaves<0 && segment.index == segment_cnt-1) {

	    LevelParams lpar_1 = par.levelParams[Math.min(stemlevel+1,3)];
	    int cnt = (int)(leaves_per_branch()+0.5);
	  	
	    Transformation trf = segment.transf.translate(segment.transf.getZ().mul(segment_len));
	    double distangle = lpar_1.nRotate/cnt;
	    double varangle = lpar_1.nRotateV/cnt;
	    double downangle = lpar_1.nDownAngle;
	    double vardown = lpar_1.nDownAngleV;
	    double offsetangle = 0;
	    //self.DBG("LEAVES FAN MODE: %d leaves\n" % cnt)
	    // use different method for odd and even number
	    if (cnt%2 == 1) {
		// create one leaf in the middle
		// FIXME: realy stemlevel+1 here?
		Leaf leaf = new Leaf(par,trf,segment_cnt*segment_len);
		leaf.make();
		leaves.addElement(leaf);
		offsetangle = distangle;
	    } else {
		offsetangle = distangle/2;
	    }
	    // create leaves left and right of the middle
	    for (int s=0; s<cnt/2; s++) {
		for (int rot=1; rot !=-1; rot=-rot) {
		    Transformation transf1 = trf.roty(rot*(offsetangle+s*distangle
							   +lpar_1.var(varangle)));
		    transf1 = transf1.rotx(downangle+lpar_1.var(vardown));
		    Leaf leaf = new Leaf(par,transf1,segment_cnt*segment_len);
		    leaf.make();
		    leaves.addElement(leaf);
		}
	    }
	}
    }
      
 
    int make_clones(Transformation trf,int nseg) {
	// splitting
	// FIXME: maybe move this calculation to LevelParams
	// but pay attention to saving errorValues and restoring when making prune tests
	int seg_splits_eff;
	if (stemlevel==0 && nseg==0 && par._0BaseSplits>0) {
	    seg_splits_eff = par._0BaseSplits;
	}
	else {
	    // how many clones?
	    double seg_splits = lpar.nSegSplits;
	    seg_splits_eff = (int)(seg_splits+lpar.splitErrorValue+0.5);
      
	    // adapt error value
	    lpar.splitErrorValue -= (seg_splits_eff - seg_splits);
	}
    
	if (seg_splits_eff<1) return -1;
    
	// print "cloning... height: "+str(nseg)+" split_eff: "+str(seg_splits_eff)   
	// print "  error_val: "+ str(self.tree.splitErrorValue[self.level])
	//	  self.DBG("CLONING: seg_splits_eff: %f\n" % (seg_splits_eff))
	
	double s_angle = 360/(seg_splits_eff+1);
    
	// make clones
	// if seg_splits_eff > 0:
	for (int i=0; i<seg_splits_eff; i++) { 
      
	    // copy params
	    Stem clone = clone(trf,nseg+1);
      
	    // NOTE: its a little bit problematic here
	    // when the clone is given as a parent to
	    // the substems, it should have the same
	    // params for length and segment_cnt like
	    // the original stem, but this could be
	    // somewhat confusing(?)
	    // clone.segment_cnt = remaining_segs;
	    // clone.length = remaining_segs * self.segment_len
      
	    // change the direction for the clone
	    //if self.debug: sys.stderr.write("-SPLIT_CORE_BEFOR: %s, dir: %s\n" % \
				//	(str(clone.split_corr),str(clone.direction)))
		
		clone.transf = clone.split(trf,s_angle*(1+i),nseg,seg_splits_eff);
	
				//if self.debug: sys.stderr.write("-SPLIT_CORE_AFTER: %s, dir: %s\n" % 
		//	(str(clone.split_corr),str(clone.direction)))
	  
		// make segments etc. for the clone
		int segm = clone.make_segments(nseg+1,clone.segment_cnt);
		if (segm>=0) { // prune test - clone not inside envelope
		    return segm;
		}
		// add clone to the list
		clones.addElement(clone);
	}
	// get another direction for the original stem too   
	trf = split(trf,0,nseg,seg_splits_eff);
	return -1;
    }
  
      
    Transformation split(Transformation trf,
			 double s_angle, int nseg, int nsplits) {
	// applies a split angle to the stem - the Weber/Penn method
	int remaining_seg = segment_cnt-nseg-1;
    
	// self.DBG("split before: sangle: %.1f, z: %s\n"%(s_angle,str(transf.z())))
	    	    
	// the splitangle
	// FIXME: don't know if it should be nSplitAngle or nSplitAngle/2
	double declination = Math.acos(trf.getZ().getZ())*180/Math.PI;
	double split_angle = Math.max(0,(lpar.nSplitAngle
				    + lpar.var(lpar.nSplitAngleV) - declination));
	    
	// FIXME: first works better for level 0, second for further levels
	// transf = transf.rotxz(split_angle,s_angle)
	trf = trf.rotx(split_angle);
	    	
	// adapt split correction
	split_corr -=  split_angle/remaining_seg;
	//t_corr = Transformation().rotx(-split_angle/remaining_seg)
	double split_diverge;
	if (s_angle>0) { // original stem has s_angle==0    
	    if (par._0BaseSplits>0 && stemlevel==0 && nseg==0) {
		split_diverge = s_angle + lpar.var(lpar.nSplitAngleV);
	    }	else {
		split_diverge = 20 + 0.75 * (30 + Math.abs(declination-90)) 
		    * Math.pow((lpar.var(1)+1)/2.0,2);
		if (lpar.var(1) >= 0) split_diverge = - split_diverge;
	    }
	
	    trf = trf.rotaxis(split_diverge,new Vector(0,0,1));

	} else split_diverge = 0; // for debugging only
	    	    
	// FIXME: split_correction: calc the inverse transformation matrix from Tcorr = Trand*Tsplit
	// and multiply this to split_correction: Tsplitcorr = (Tcorr/remaining_seg) * Tsplitcorr

	/*	
		self.DBG("split after: splitangle: %.1f, split_diverge: %.1f, z: %s, decl: %f\n"%\
	    	(split_angle,split_diverge,str(transf.z()),declination))
		self.TRF("split1: after split",transf)
	*/    
	
	// adjust some parameters	
	//split_cnt = split_cnt+1;

	// lower substem prospensity
	if (! prunetest) {
	    substems_per_segment /= (float)(nsplits+1);
	    //      self.DBG("SS-half: %f\n"%self.substems_per_segment)
	    // FIXME same reduction for leaves_per_segment?
	}
	return trf;
    }
	  
    /*
      # The Weber/Penn splitting method is problematic for big splitting angles, or
      # may be i misunderstood it, but it seems, that evenly splitting like for
      # an umbrella formed acacia (don't know the english name of that trees) isn't
    */
    
    void povray(PrintWriter w, int level) throws Exception {
	// output povray code for one stem level or leaves (last level+1)
	  
	if (par.verbose) {
	    if (stemlevel<=1 && clone_index.size()==0) System.err.print(".");
	}
	
	String indent = "    ";
	  
	// output self and clones of same level
	if (level==stemlevel) {
	    if (par.output==Params.CONES) {

		boolean union=false;
		if (segments.size()>1 || (substems != null && substems.size()>0)) union = true;

		if (union) w.print(indent + "union { ");
		w.println("/* " + tree_position() + " */");

		for (int i=0; i<segments.size(); i++) {
		    ((Segment)segments.elementAt(i)).povray(w);
		}

		if (clones != null) {
		    for (int i=0; i<clones.size(); i++) {
			((Stem)clones.elementAt(i)).povray(w,level);
		    }
		}

		if (union) w.println(indent + "}");

	    } else if (par.output==Params.MESH) {

		// create mesh
		Mesh mesh = new Mesh();
		for (int i=0; i<segments.size(); i++) {
		    ((Segment)segments.elementAt(i)).add_to_mesh(mesh);
		}

		// output mesh
		w.println("/* " + tree_position() + " */");
		mesh.povray(w,level<=par.smooth_mesh_level,indent);

		if (clones != null) {
		    for (int i=0; i<clones.size(); i++) {
			((Stem)clones.elementAt(i)).povray(w,level);
		    }
		}

	    }	else {
		throw new ErrorNotYetImplemented("output method "+par.output
						 +" not (yet) implemented.");
	    }
	
	// output leaves
	} else if (level==par.Levels && stemlevel==par.Levels-1) {
	    boolean union=false;
	    if (leaves.size()>0) union=true;

	    if (union) w.print(indent + "union { ");
	    w.println( "/* " + tree_position() + " */");

	    for (int i=0; i<leaves.size(); i++) {
		((Leaf)leaves.elementAt(i)).povray(w);
	    }

	    if (clones != null) {
		for (int i=0; i<clones.size(); i++) {
		    ((Stem)clones.elementAt(i)).povray(w,level);
		}
	    }

	    if (union) w.println(indent + "}");
	}
	  		  
	// recursive call to substems
	else if (level > stemlevel) {
	    // FIXME? more correct it would be inc by 1 in the for block,
	    // but this would need more calls to synchronized incProgress
	    // if this work ok, don't change this
	    tree.incPovrayProgress(substems.size());
	    for (int i=0; i<substems.size(); i++) {
		((Stem)substems.elementAt(i)).povray(w,level);
	    }
	    if (clones != null) {
		for (int i=0; i<clones.size(); i++) {
		    ((Stem)clones.elementAt(i)).povray(w,level);
		}	  	
	    }
	}
    }

    /* return the number of all substems and substems of substems a.s.o. */
    long substemTotal() {
	if (substems == null) return 0;

	long sum = substems.size();
	for (int i=0; i<substems.size(); i++) {
	    sum += ((Stem)substems.elementAt(i)).substemTotal();
	}
	return sum;
    }
	  
  /*
      def count_real_substems(self,follow_clones=0):
          "returns number of real (with level+1) substems of th stem and all its clones"
	  sum = 0
	  for ss in self.substems:
	  	if ss.level > self.level: sum = sum+1
		elif follow_clones: sum = sum+ss.count_real_substems()
	  return sum
	  	  
      def dump(self):
	  indent = " "*(self.level*2+2)
	  print indent+"STEM:"
	  print indent+"debug:",self.debug
	  print indent+"level:",self.level
	  print indent+"position:",self.position
	  print indent+"direction:",self.direction
	  print indent+"offset:",self.offset
	  print indent+"length:",self.length
	  print indent+"base_radius:",self.base_radius
	  if self.level>0:
	  	print indent+"parent_radius:",self.parent.stem_radius(self.offset)
	  #...
	  print indent+"substems (own/clones/all): %d/%d/%d" %\
	  	(self.count_real_substems(),len(self.substems)-self.count_real_substems(),\
	  	self.count_real_substems(1))
	  print indent+"leaves: ",len(self.leaves)
	  for ss in self.substems:
	  	ss.dump()
	  for l in self.leaves:
	  	l.dump()
  */
  
};
	      














