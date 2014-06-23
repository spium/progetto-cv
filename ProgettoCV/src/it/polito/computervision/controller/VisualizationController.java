package it.polito.computervision.controller;

import it.polito.computervision.actions.ActionManager;
import it.polito.computervision.gestures.Gesture;
import it.polito.computervision.gestures.GestureData;
import it.polito.computervision.gestures.GestureListener;
import it.polito.computervision.gestures.GestureListenerAdapter;
import it.polito.computervision.gestures.GestureManager;
import it.polito.computervision.gestures.impl.ClickGesture;
import it.polito.computervision.gestures.impl.PanGesture;
import it.polito.computervision.gestures.impl.ZoomGesture;
import it.polito.computervision.virtualscreen.HandData;
import it.polito.computervision.virtualscreen.VirtualScreenManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;

import org.apache.commons.collections15.Transformer;
import org.opencv.core.Size;
import org.openni.Point2D;

import net.rootdev.jenajung.JenaJungGraph;
import net.rootdev.jenajung.Transformers;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.FileManager;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.algorithms.layout.util.Relaxer;
import edu.uci.ics.jung.algorithms.layout.util.VisRunner;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Graphs;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.layout.LayoutTransition;
import edu.uci.ics.jung.visualization.util.Animator;

public class VisualizationController {

	public static final int LAYOUT_BORDER = 150;
	public static final int FRAME_BORDER = 50;
	public static final int MAX_NODES_IN_VIEWPORT = 10;
	public static final int LAYOUT_SIZE_MULTIPLIER = 2;

	private VisualizationViewer<RDFNode, Statement> viewer;
	private Model model;
	private Graph<RDFNode, Statement> graph;
	private Layout<RDFNode, Statement> layout;
	private JenaJungGraph ontology;
	private int width, height;
	private DefaultModalGraphMouse<RDFNode, Statement> mouse;
	private JFrame parent;

	private int layoutSizeThreshold = MAX_NODES_IN_VIEWPORT,
			lastLayoutSizeThreshold = 0;

	private Map<String, GestureListener> gestureActions;
	private Map<String, Gesture> gestures;


	public VisualizationController(String rdfResource, JFrame parent) {
		if(rdfResource == null || rdfResource.trim().isEmpty() || parent == null)
			throw new IllegalArgumentException("Args null or empty");

		this.parent = parent;
		System.out.print("Loading model...");
		model = FileManager.get().loadModel(rdfResource);
		if(model != null) {
			System.out.println("done");
			ontology = new JenaJungGraph(model);
			//new empty graph
			graph = Graphs.synchronizedDirectedGraph(new DirectedSparseMultigraph<RDFNode, Statement>());

			gestureActions = new HashMap<String, GestureListener>();
			gestures = new HashMap<String, Gesture>();

			gestures.put("click", new ClickGesture("click"));
			gestures.put("pan", new PanGesture("pan"));
			gestures.put("zoom", new ZoomGesture("zoom"));
			gestures.put("swipe-down", new PanGesture("swipe-down", EnumSet.of(PanGesture.Direction.DOWN), false));
			gestures.put("swipe-up", new PanGesture("swipe-up", EnumSet.of(PanGesture.Direction.UP), false));
			gestures.put("swipe-left", new PanGesture("swipe-left", EnumSet.of(PanGesture.Direction.LEFT), false));
			gestures.put("swipe-right", new PanGesture("swipe-right", EnumSet.of(PanGesture.Direction.RIGHT), false));

			GestureManager.getInstance().registerGesture(gestures.get("click"));
			GestureManager.getInstance().registerGesture(gestures.get("pan"));
			GestureManager.getInstance().registerGesture(gestures.get("zoom"));


			RDFNode root = null;
			for(RDFNode n : ontology.getVertices()) {
				root = n;
				break;
			}

			if(root != null) {
				graph.addVertex(root);


				do {
					Size frameSize = VirtualScreenManager.getInstance().getFrameSize();
					width = (int) (frameSize.width * VirtualScreenManager.PROJECTED_POSITION_MULTIPLIER);
					height = (int) (frameSize.height * VirtualScreenManager.PROJECTED_POSITION_MULTIPLIER);
				}
				while(height == 0 || width == 0);

				layout = new FRLayout<RDFNode, Statement>(graph);
				layout.setSize(new Dimension(width - LAYOUT_BORDER, height - LAYOUT_BORDER));
				Relaxer relaxer = new VisRunner((IterativeContext)layout);
				relaxer.stop();
				relaxer.prerelax();

				Layout<RDFNode, Statement> staticLayout = new StaticLayout<RDFNode, Statement>(graph, layout);
				viewer = new VisualizationViewer<RDFNode, Statement>(staticLayout, new Dimension(width - FRAME_BORDER, height - FRAME_BORDER));
				staticLayout.setLocation(root, viewer.getCenter());
			}

			RenderContext<RDFNode, Statement> context = viewer.getRenderContext();
			context.setEdgeLabelTransformer(Transformers.EDGE);
			context.setVertexLabelTransformer(Transformers.NODE);

			context.setVertexShapeTransformer(new Transformer<RDFNode, Shape>() {

				private Shape shape = new Ellipse2D.Float(-35, -35, 70, 70);

				@Override
				public Shape transform(RDFNode node) {
					return shape;
				}

			});

			context.setVertexFillPaintTransformer(new Transformer<RDFNode, Paint>() {

				@Override
				public Paint transform(RDFNode node) {
					boolean picked = viewer.getPickedVertexState().isPicked(node);

					if(graph.getVertices().containsAll(ontology.getNeighbors(node))) {
						return picked ? Color.ORANGE : Color.RED;
					}
					else {
						return picked ? Color.YELLOW : Color.CYAN;
					}
				}
			});

			mouse = new DefaultModalGraphMouse<RDFNode, Statement>();
			viewer.setGraphMouse(mouse);

			this.parent.getContentPane().add(mouse.getModeComboBox(), BorderLayout.SOUTH);

			viewer.addPostRenderPaintable(new HandPointRenderer(this.parent, width, height));

			this.parent.getContentPane().add(viewer, BorderLayout.CENTER);
			this.parent.pack();
			this.parent.setPreferredSize(viewer.getPreferredSize());

			//setup bindings for gesture events

			gestureActions.put("pan", new GestureListenerAdapter() {

				@Override
				public void onGestureStarted(GestureData gesture) {
					Point2D<Float> hand = gesture.getData("initialPosition");
					int x = hand.getX().intValue();
					int y = hand.getY().intValue();
					mouse.mousePressed(new MouseEvent(viewer, MouseEvent.MOUSE_PRESSED, new Date().getTime(), 0, x, y, 1, false, MouseEvent.BUTTON1));
				}

				@Override
				public void onGestureInProgress(GestureData gesture) {
					HandData hand = gesture.getHands().get(0);
					int x = hand.getProjectedPosition().getX().intValue();
					int y = hand.getProjectedPosition().getY().intValue();
					mouse.mouseDragged(new MouseEvent(viewer, MouseEvent.MOUSE_DRAGGED, new Date().getTime(), MouseEvent.BUTTON1_DOWN_MASK, x, y, 1, false, MouseEvent.BUTTON1));
				}

				@Override
				public void onGestureCompleted(GestureData gesture) {
					HandData hand = gesture.getHands().get(0);
					int x = hand.getProjectedPosition().getX().intValue();
					int y = hand.getProjectedPosition().getY().intValue();
					mouse.mouseReleased(new MouseEvent(viewer, MouseEvent.MOUSE_RELEASED, new Date().getTime(), 0, x, y, 1, false, MouseEvent.BUTTON1));
				}

			});

			gestureActions.put("swipe-down", new GestureListenerAdapter() {

				@Override
				public void onGestureCompleted(GestureData gesture) {
					//expand/collapse all neighbors

					boolean changed = false;
					RDFNode central = null;
					Set<RDFNode> pickedNodes = viewer.getPickedVertexState().getPicked();
					for(RDFNode picked : pickedNodes) {
						central = picked;
						changed = expandNodes(ontology.getNeighbors(picked));
					}

					if(changed)
						updateLayout(central);
				}
			});

			gestureActions.put("swipe-up", new GestureListenerAdapter() {

				@Override
				public void onGestureCompleted(GestureData gesture) {
					//expand/collapse all neighbors

					boolean changed = false;
					RDFNode central = null;
					Set<RDFNode> pickedNodes = viewer.getPickedVertexState().getPicked();
					for(RDFNode picked : pickedNodes) {
						central = picked;
						changed = collapseNodes(ontology.getNeighbors(picked), pickedNodes);
					}

					if(changed)
						updateLayout(central);
				}
			});

			gestureActions.put("swipe-left", new GestureListenerAdapter() {
				@Override
				public void onGestureCompleted(GestureData gesture) {
					//expand/collapse only neighbors with incoming edges

					boolean changed = false;
					RDFNode central = null;
					Set<RDFNode> pickedNodes = viewer.getPickedVertexState().getPicked();
					for(RDFNode picked : pickedNodes) {
						central = picked;
						changed = toggleNodes(ontology.getPredecessors(picked), pickedNodes);
					}

					if(changed)
						updateLayout(central);
				}
			});

			gestureActions.put("swipe-right", new GestureListenerAdapter() {
				@Override
				public void onGestureCompleted(GestureData gesture) {
					//expand/collapse only neighbors with incoming edges

					boolean changed = false;
					RDFNode central = null;
					Set<RDFNode> pickedNodes = viewer.getPickedVertexState().getPicked();
					for(RDFNode picked : pickedNodes) {
						central = picked;
						changed = toggleNodes(ontology.getSuccessors(picked), pickedNodes);
					}

					if(changed)
						updateLayout(central);
				}
			});

			ActionManager.getInstance().bind("zoom", new GestureListenerAdapter() {
				private Float initialDistance;
				private java.awt.geom.Point2D center;
				private ScalingControl scaler = new CrossoverScalingControl();

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

			ActionManager.getInstance().bind("pan", gestureActions.get("pan"));

			ActionManager.getInstance().bind("click", new GestureListenerAdapter() {

				@Override
				public void onGestureCompleted(GestureData gesture) {
					Point2D<Float> pos = gesture.getData("initialPosition");
					mouse.mouseClicked(new MouseEvent(viewer, MouseEvent.MOUSE_CLICKED, new Date().getTime(), 0, pos.getX().intValue(), pos.getY().intValue(), 1, false, MouseEvent.BUTTON1));

					RDFNode clicked = viewer.getPickSupport().getVertex(viewer.getGraphLayout(), pos.getX().intValue(), pos.getY().intValue());
					//clear picked vertices
					viewer.getPickedVertexState().clear();
					if(clicked != null) {

						//pick the clicked vertex
						viewer.getPickedVertexState().pick(clicked, true);

						updateLayout(clicked);
					}

					setPickedMode(!viewer.getPickedVertexState().getPicked().isEmpty());
				}
			});

			ActionManager.getInstance().bind("swipe-down", gestureActions.get("swipe-down"));

			GestureManager.getInstance().start();
			ActionManager.getInstance().start();
		}
		else
			throw new IllegalArgumentException("Could not load model: " + rdfResource);
	}

	public VisualizationViewer<RDFNode, Statement> getViewer() {
		return viewer;
	}

	private void setPickedMode(boolean how) {
		ActionManager am = ActionManager.getInstance();
		GestureManager gm = GestureManager.getInstance();
		if(how) {
			//unbind pan
			if(am.isBound("pan")) {
				am.unbind("pan");
				gm.unregisterGesture(gestures.get("pan"));

				//bind swipes
				gm.registerGesture(gestures.get("swipe-down"));
				gm.registerGesture(gestures.get("swipe-left"));
				gm.registerGesture(gestures.get("swipe-up"));
				gm.registerGesture(gestures.get("swipe-right"));

				am.bind("swipe-down", gestureActions.get("swipe-down"));
				am.bind("swipe-left", gestureActions.get("swipe-left"));
				am.bind("swipe-up", gestureActions.get("swipe-up"));
				am.bind("swipe-right", gestureActions.get("swipe-right"));

			}
		}
		else {
			if(am.isBound("swipe-down")) {
				//unbind swipes
				am.unbind("swipe-down");
				am.unbind("swipe-left");
				am.unbind("swipe-up");
				am.unbind("swipe-right");
				gm.unregisterGesture(gestures.get("swipe-down"));
				gm.unregisterGesture(gestures.get("swipe-left"));
				gm.unregisterGesture(gestures.get("swipe-up"));
				gm.unregisterGesture(gestures.get("swipe-right"));

				//bind pan
				am.bind("pan", gestureActions.get("pan"));
				gm.registerGesture(gestures.get("pan"));
			}
		}
	}

	private void updateLayout(RDFNode central) {
		int nodes = graph.getVertexCount();
		//decrease layout size
		while(nodes < lastLayoutSizeThreshold) {
			width /= LAYOUT_SIZE_MULTIPLIER;
			height /= LAYOUT_SIZE_MULTIPLIER;
			layoutSizeThreshold /= (LAYOUT_SIZE_MULTIPLIER * LAYOUT_SIZE_MULTIPLIER);
			lastLayoutSizeThreshold /= (LAYOUT_SIZE_MULTIPLIER * LAYOUT_SIZE_MULTIPLIER);
			if(lastLayoutSizeThreshold < MAX_NODES_IN_VIEWPORT)
				lastLayoutSizeThreshold = 0;
		}
		
		//increase layout size
		while(nodes > layoutSizeThreshold) {
			width *= LAYOUT_SIZE_MULTIPLIER;
			height *= LAYOUT_SIZE_MULTIPLIER;
			lastLayoutSizeThreshold = layoutSizeThreshold;
			layoutSizeThreshold *= LAYOUT_SIZE_MULTIPLIER * LAYOUT_SIZE_MULTIPLIER;
		}
		
		layout.setSize(new Dimension(width - LAYOUT_BORDER, height - LAYOUT_BORDER));
		
		layout.initialize();

		Relaxer relaxer = new VisRunner((IterativeContext)layout);
		relaxer.stop();
		relaxer.prerelax();

		if(central != null) {
			java.awt.geom.Point2D q = layout.transform(central);
			java.awt.geom.Point2D lvc = viewer.getRenderContext().getMultiLayerTransformer().inverseTransform(viewer.getCenter());
			final double dx = (lvc.getX() - q.getX());
			final double dy = (lvc.getY() - q.getY());
			viewer.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT).translate(dx, dy);
		}

		StaticLayout<RDFNode, Statement> staticLayout =	new StaticLayout<RDFNode, Statement>(graph, layout);
		LayoutTransition<RDFNode, Statement> lt = new LayoutTransition<RDFNode, Statement>(viewer, viewer.getGraphLayout(), staticLayout);
		Animator animator = new Animator(lt, 50);
		animator.start();
	}

	private boolean expandNodes(Collection<RDFNode> nodes) {
		boolean changed = false;
		if(nodes.size() > 0) {
			for(RDFNode n : nodes) {
				if(!graph.containsVertex(n)) {
					graph.addVertex(n);
					changed = true;
				}
			}

			if(changed) {
				for(RDFNode n1 : graph.getVertices()) {
					for(RDFNode n2 : graph.getVertices()) {
						if(n1 != n2) {
							Collection<Statement> edges = ontology.findEdgeSet(n1, n2);
							for(Statement s : edges) {
								if(!graph.containsEdge(s))
									graph.addEdge(s, s.getSubject(), s.getObject());
							}
						}
					}
				}
			}
		}

		return changed;

	}

	private boolean collapseNodes(Collection<RDFNode> nodes, Collection<RDFNode> pickedNodes) {
		boolean changed = false;
		HashSet<Statement> edgesToRemove = new HashSet<Statement>();
		if(nodes.size() > 0) {
			for(RDFNode n : nodes) {
				if(!pickedNodes.contains(n) && graph.containsVertex(n)) {
					graph.removeVertex(n);
					edgesToRemove.addAll(ontology.getIncidentEdges(n));
					changed = true;
				}
			}

			if(changed) {
				for(Statement s : edgesToRemove) {
					if(graph.containsEdge(s))
						graph.removeEdge(s);
				}
			}
		}

		return changed;
	}

	private boolean toggleNodes(Collection<RDFNode> nodes, Collection<RDFNode> pickedNodes) {
		return graph.getVertices().containsAll(nodes) ? collapseNodes(nodes, pickedNodes) : expandNodes(nodes);
	}

}
