/*
 * Copyright (c) 2015 Raytheon BBN Technologies Corp. All rights reserved.
 */

package org.apache.poi.xdgf.geom;

import com.graphbuilder.curve.ControlPath;
import com.graphbuilder.curve.GroupIterator;
import com.graphbuilder.curve.NURBSpline;
import com.graphbuilder.curve.ShapeMultiPath;
import com.graphbuilder.curve.ValueVector;

public class SplineRenderer {

	public static ShapeMultiPath createNurbsSpline(ControlPath controlPoints,
			 									   ValueVector knots,
			 									   ValueVector weights,
			 									   int degree) {
		
		double firstKnot = knots.get(0);
		double lastKnot = knots.get(knots.size()-1);
		
		// scale knots to [0, 1] based on first/last knots
		for (int i = 0; i < knots.size(); i++) {
			knots.set((knots.get(i) - firstKnot)/lastKnot, i);
		}
		
		// if we don't have enough knots, duplicate the last knot until we do
		for (int i = knots.size(); i < controlPoints.numPoints() + degree + 1; i++) {
			knots.add(1);
		}
		
		GroupIterator gi = new GroupIterator("0:n-1", controlPoints.numPoints());
		
		NURBSpline spline = new NURBSpline(controlPoints, gi);
		
		spline.setDegree(degree);
		spline.setKnotVectorType(NURBSpline.NON_UNIFORM);
		spline.setKnotVector(knots);
		
		if (weights == null) {
			spline.setUseWeightVector(false);
		} else {
			spline.setWeightVector(weights);
		}
		
		// now that this is done, add it to the path
		ShapeMultiPath shape = new ShapeMultiPath();
		shape.setFlatness(0.01);
		
		spline.appendTo(shape);
		return shape;
	}
	
}
