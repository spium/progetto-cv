package it.polito.computervision.controller;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;

import it.polito.computervision.gestures.GestureData;
import it.polito.computervision.gestures.GestureListener;
import it.polito.computervision.virtualscreen.HandData;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;

public class HandTrackingModalMouse extends DefaultModalGraphMouse<RDFNode, Statement> implements GestureListener {

	private ScalingControl scaler;
	private VisualizationServer<RDFNode, Statement> vv;
	private Float initialDistance;
	java.awt.geom.Point2D center;

	protected HandTrackingModalMouse(VisualizationServer<RDFNode, Statement> vv) {
		this(vv, 1.f,1.f);
	}

	protected HandTrackingModalMouse(VisualizationServer<RDFNode, Statement> vv, float in, float out) {
		super(in, out);
		scaler = new CrossoverScalingControl();
		this.vv = vv;
		initialDistance = null;
		center = null;
	}

	@Override
	public void onGestureStarted(GestureData gesture) {

	}

	@Override
	public void onGestureInProgress(GestureData gesture) {
		if(gesture.getName() == "zoom") {
			HandData[] hands = gesture.getHands().toArray(new HandData[2]);
			if(hands.length == 2) {
				if(initialDistance == null || initialDistance <= 0.f) {
					initialDistance = gesture.getData("initialDistance");
					center = new java.awt.geom.Point2D.Float((hands[0].getProjectedPosition().getX() + hands[1].getProjectedPosition().getX())/2,(hands[0].getProjectedPosition().getY() + hands[1].getProjectedPosition().getY())/2);
				}
				Float currDist = gesture.getData("currentDistance");
				if(initialDistance != null && initialDistance > 0 && currDist != null && currDist > 0) {
					System.out.println("ratio: " + currDist/initialDistance);
					scaler.scale(vv, currDist/initialDistance, center);
					initialDistance = currDist;
				}
				else {
					System.out.println("initial: " + initialDistance + ", curr: " + currDist);
				}
			}

		}
	}

	@Override
	public void onGestureCompleted(GestureData gesture) {
		if(gesture.getName() == "zoom") {
			initialDistance = null;
			center = null;
		}
	}





}
