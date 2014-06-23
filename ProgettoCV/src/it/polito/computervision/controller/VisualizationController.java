package it.polito.computervision.controller;

import it.polito.computervision.actions.ActionManager;
import it.polito.computervision.gestures.GestureData;
import it.polito.computervision.gestures.GestureListener;
import it.polito.computervision.virtualscreen.HandData;
import it.polito.computervision.virtualscreen.VirtualScreenManager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.util.Collection;
import java.util.Date;
import java.util.List;

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

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.algorithms.layout.util.Relaxer;
import edu.uci.ics.jung.algorithms.layout.util.VisRunner;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.ObservableGraph;
import edu.uci.ics.jung.graph.util.Graphs;
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

	private VisualizationViewer<RDFNode, Statement> viewer;
	private Model model;
	private Graph<RDFNode, Statement> graph;
	private Layout<RDFNode, Statement> layout;
	private JenaJungGraph ontology;
	private int width, height;
	private DefaultModalGraphMouse<RDFNode, Statement> mouse;
	private JFrame parent;


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
			graph = new ObservableGraph<RDFNode, Statement>(Graphs.synchronizedDirectedGraph(new DirectedSparseMultigraph<RDFNode, Statement>()));

			RDFNode root = null;
			for(RDFNode n : ontology.getVertices()) {
				root = n;
				break;
			}

			if(root != null) {
				graph.addVertex(root);
			}

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

			mouse = new DefaultModalGraphMouse<RDFNode, Statement>();
			viewer.setGraphMouse(mouse);

			viewer.addPostRenderPaintable(new HandPointRenderer(this.parent, width, height));

			this.parent.getContentPane().add(viewer, BorderLayout.CENTER);
			this.parent.pack();
			this.parent.setPreferredSize(viewer.getPreferredSize());


			//setup bindings for gesture events
			ActionManager.getInstance().bind("zoom", new GestureListener() {
				private Float initialDistance;
				private java.awt.geom.Point2D center;
				private ScalingControl scaler = new CrossoverScalingControl();

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
					HandData hand = gesture.getHands().get(0);
					int x = hand.getProjectedPosition().getX().intValue();
					int y = hand.getProjectedPosition().getY().intValue();
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

			ActionManager.getInstance().bind("click", new GestureListener() {

				private RDFNode picked = null;
				
				@Override
				public void onGestureStarted(GestureData gesture) {

				}

				@Override
				public void onGestureInProgress(GestureData gesture) {

				}

				@Override
				public void onGestureCompleted(GestureData gesture) {
					Point2D<Float> pos = gesture.getData("initialPosition");
					mouse.mouseClicked(new MouseEvent(viewer, MouseEvent.MOUSE_CLICKED, new Date().getTime(), 0, pos.getX().intValue(), pos.getY().intValue(), 1, false, MouseEvent.BUTTON1));
										
					RDFNode clicked = viewer.getPickSupport().getVertex(viewer.getGraphLayout(), pos.getX().intValue(), pos.getY().intValue());
					if(clicked != null) {
						if(picked == null || clicked != picked) {
							//pick the clicked vertex
							if(picked != null)
								viewer.getPickedVertexState().pick(picked, false);
							System.out.println("clicked");
							picked = clicked;
							viewer.getPickedVertexState().pick(clicked, true);
						}
						else {
							//picked == clicked -> unpick
							viewer.getPickedVertexState().pick(picked, false);
							picked = null;
						}
						
						

						
						Collection<RDFNode> neighbors = ontology.getNeighbors(clicked);
						Graph<RDFNode, Statement> graph = viewer.getGraphLayout().getGraph();
						for(RDFNode n : neighbors) {
							graph.addVertex(n);
						}

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

						
						layout.initialize();

		        		Relaxer relaxer = new VisRunner((IterativeContext)layout);
		        		relaxer.stop();
		        		relaxer.prerelax();
		        		StaticLayout<RDFNode, Statement> staticLayout =	new StaticLayout<RDFNode, Statement>(graph, layout);
		        		staticLayout.setLocation(clicked, viewer.getCenter());
						LayoutTransition<RDFNode, Statement> lt = new LayoutTransition<RDFNode, Statement>(viewer, viewer.getGraphLayout(), staticLayout);
						Animator animator = new Animator(lt, 50);
						animator.start();
						
//						Layout<RDFNode, Statement> layout = new CircleLayout<RDFNode, Statement>(graph);
//						layout.setSize(viewer.getGraphLayout().getSize());
//						layout.setLocation(clicked, viewer.getCenter());
//						viewer.setGraphLayout(layout);
					}
					else if(picked != null) {
						viewer.getPickedVertexState().pick(picked, false);
						picked = null;
					}
				}
			});

		}
		else
			throw new IllegalArgumentException("Could not load model: " + rdfResource);
	}

	public VisualizationViewer<RDFNode, Statement> getViewer() {
		return viewer;
	}

}
