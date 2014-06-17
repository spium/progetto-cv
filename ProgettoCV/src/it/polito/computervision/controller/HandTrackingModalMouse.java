package it.polito.computervision.controller;

import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.List;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;

import it.polito.computervision.actions.ActionManager;
import it.polito.computervision.gestures.GestureData;
import it.polito.computervision.gestures.GestureListener;
import it.polito.computervision.virtualscreen.HandData;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;

public class HandTrackingModalMouse extends DefaultModalGraphMouse<RDFNode, Statement> {

	private ScalingControl scaler;
	private Float initialDistance;
	java.awt.geom.Point2D center;

	protected HandTrackingModalMouse(VisualizationViewer<RDFNode, Statement> vv) {
		this(vv, 1.f,1.f);
	}

	protected HandTrackingModalMouse(VisualizationViewer<RDFNode, Statement> vv, float in, float out) {
		super(in, out);
		scaler = new CrossoverScalingControl();
		initialDistance = null;
		center = null;

		final VisualizationViewer<RDFNode, Statement> viewer = vv;
		final HandTrackingModalMouse self = this;

		ActionManager.getInstance().bind("zoom", new GestureListener() {
			@Override
			public void onGestureStarted(GestureData gesture) {

			}

			@Override
			public void onGestureInProgress(GestureData gesture) {
				List<HandData> hands = gesture.getHands();
				if(hands.size() == 2) {
					if(initialDistance == null) {
						initialDistance = gesture.getData("initialDistance");
						center = new java.awt.geom.Point2D.Float((hands.get(0).getProjectedPosition().getX() + hands.get(1).getProjectedPosition().getX())/2,(hands.get(0).getProjectedPosition().getY() + hands.get(1).getProjectedPosition().getY())/2);
					}
					float currDist = gesture.getData("currentDistance");
					scaler.scale(viewer, currDist/initialDistance, center);
					initialDistance = currDist;
				}
			}

			@Override
			public void onGestureCompleted(GestureData gesture) {
				initialDistance = null;
				center = null;
			}
		});

		ActionManager.getInstance().bind("pan", new GestureListener() {

			@Override
			public void onGestureStarted(GestureData gesture) {
				System.out.println("started pan");
				HandData hand = gesture.getHands().get(0);
				int x = hand.getProjectedPosition().getX().intValue();
				int y = hand.getProjectedPosition().getY().intValue();
//				self.mouseEntered(new MouseEvent(viewer, MouseEvent.MOUSE_ENTERED, new Date().getTime(), 0, x, y, 1, false, MouseEvent.BUTTON1));
				self.mousePressed(new MouseEvent(viewer, MouseEvent.MOUSE_PRESSED, new Date().getTime(), 0, x, y, 1, false, MouseEvent.BUTTON1));
			}

			@Override
			public void onGestureInProgress(GestureData gesture) {
				System.out.println("pan in progress");
				HandData hand = gesture.getHands().get(0);
				int x = hand.getProjectedPosition().getX().intValue();
				int y = hand.getProjectedPosition().getY().intValue();
				self.mouseDragged(new MouseEvent(viewer, MouseEvent.MOUSE_DRAGGED, new Date().getTime(), MouseEvent.BUTTON1_DOWN_MASK, x, y, 1, false, MouseEvent.BUTTON1));
//				self.mouseMoved(new MouseEvent(viewer, MouseEvent.MOUSE_MOVED, new Date().getTime(), 0, x - this.x, y - this.y, 1, false, MouseEvent.BUTTON1));
			}

			@Override
			public void onGestureCompleted(GestureData gesture) {
				System.out.println("completed pan");
				HandData hand = gesture.getHands().get(0);
				int x = hand.getProjectedPosition().getX().intValue();
				int y = hand.getProjectedPosition().getY().intValue();
				
				self.mouseReleased(new MouseEvent(viewer, MouseEvent.MOUSE_RELEASED, new Date().getTime(), 0, x, y, 1, false, MouseEvent.BUTTON1));
//				self.mouseExited(new MouseEvent(viewer, MouseEvent.MOUSE_EXITED, new Date().getTime(), 0, x, y, 1, false, MouseEvent.BUTTON1));
			}

		});
	}


}
