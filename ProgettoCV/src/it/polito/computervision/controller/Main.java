package it.polito.computervision.controller;

import it.polito.computervision.actions.ActionManager;
import it.polito.computervision.gestures.GestureData;
import it.polito.computervision.gestures.GestureListener;
import it.polito.computervision.gestures.GestureManager;
import it.polito.computervision.gestures.impl.ClickGesture;
import it.polito.computervision.gestures.impl.PanGesture;
import it.polito.computervision.gestures.impl.ZoomGesture;
import it.polito.computervision.virtualscreen.HandData;
import it.polito.computervision.virtualscreen.VirtualScreenListener;
import it.polito.computervision.virtualscreen.VirtualScreenManager;
import it.polito.computervision.virtualscreen.impl.FlatVirtualScreen;
import it.polito.computervision.virtualscreen.impl.StaticVirtualScreenInitializer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import net.rootdev.jenajung.JenaJungGraph;
import net.rootdev.jenajung.Transformers;

import org.opencv.core.Core;
import org.opencv.core.Size;
import org.openni.Device;
import org.openni.DeviceInfo;
import org.openni.OpenNI;
import org.openni.SensorType;
import org.openni.VideoFrameRef;
import org.openni.VideoStream;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.FileManager;
import com.primesense.nite.NiTE;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;

import org.openni.Point2D;

public class Main implements VirtualScreenListener, VisualizationServer.Paintable {

	public static final int BORDER = 20;
	
	private JFrame mFrame;
	private boolean mShouldRun = true;
//		private VideoStream stream;
//		private VideoFrameRef frame;
	private List<HandData> hands;
	private VisualizationViewer<RDFNode, Statement> viz;
	int width, height;

	public Main(VisualizationViewer<RDFNode, Statement> viz, int frameWidth, int frameHeight) {
		hands = null;
//		this.stream = stream;
		this.viz = viz;
		width = frameWidth;
		height = frameHeight;
		this.viz.addPostRenderPaintable(this);

//				stream.setMirroringEnabled(true);
//				
//				stream.addNewFrameListener(this);
//				stream.start();
		mFrame = new JFrame("NiTE Hand Tracker Viewer");

		// register to key events
		mFrame.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent arg0) {}

			@Override
			public void keyReleased(KeyEvent arg0) {}

			@Override
			public void keyPressed(KeyEvent arg0) {
				if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
					mShouldRun = false;
				}
			}
		});

		// register to closing event
		mFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				mShouldRun = false;
			}
		});

		mFrame.getContentPane().add(viz);
		mFrame.pack();
		mFrame.setSize(viz.getSize());
		mFrame.setVisible(true);
	}

	void run() {
		while (mShouldRun) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		mFrame.dispose();
		//		if(frame != null) {
		//			frame.release();
		//			frame = null;
		//		}
		//		stream.destroy();
		GestureManager.getInstance().stop();
		VirtualScreenManager.getInstance().destroy();
		NiTE.shutdown();
		OpenNI.shutdown();
	}

	public synchronized void paint(Graphics graphics) {

		int framePosX = 0;
		int framePosY = 0;

//				if (frame == null) {
//					return;
//				}
//		
//				if (frame != null && frame.getData() != null && frame.getData().hasRemaining()) {
//					int width = frame.getWidth();
//					int height = frame.getHeight();
//					BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//					ByteBuffer frameData = frame.getData().order(ByteOrder.LITTLE_ENDIAN);
//					int[] fdArray = new int[frameData.remaining()/3];
//					int pos = 0;
//					byte[] rgb = new byte[3];
//					while(frameData.remaining() >= 3) {
//						frameData.get(rgb);
//						fdArray[pos++] = 0xFF000000 | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
//					}
//					
//					img.setRGB(framePosX, framePosY, width, height, fdArray, 0, width);
					//Size frameSize = VirtualScreenManager.getInstance().getFrameSize();
					framePosX = (mFrame.getWidth() - (int) width) / 2;
					framePosY = (mFrame.getHeight() - (int) height) / 2;
		
//					graphics.drawImage(img, framePosX, framePosY, null);
//				}

		// draw hands
		if(hands != null) {
			for (HandData hand : hands) {

				Point2D<Float> pos = hand.getProjectedPosition();
				if(hand.isTouching()) {
					graphics.setColor(Color.GREEN);
				}
				else {
					graphics.setColor(Color.RED);
				}
				graphics.fillRect(framePosX + pos.getX().intValue() + 7, framePosY + pos.getY().intValue() + 7, 15, 15);

				//				System.out.println(hand);
			}
		}
	}
	
	@Override
	public boolean useTransform() {
		return false;
	}

	@Override
	public synchronized void onNewFrame(List<HandData> hands) {

		this.hands = hands;
		mFrame.repaint();
	}

//		@Override
//		public synchronized void onFrameReady(VideoStream stream) {
//			
//			if(frame != null) {
//				frame.release();
//				frame = null;
//			}
//	
//			frame = stream.readFrame();
//			repaint();
//		}

	public static void main(String s[]) {
		// initialize OpenNI and NiTE
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		OpenNI.initialize();
		NiTE.initialize();

		List<DeviceInfo> devicesInfo = OpenNI.enumerateDevices();
		if (devicesInfo.size() == 0) {
			JOptionPane.showMessageDialog(null, "No device is connected", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Device device = Device.open(devicesInfo.get(0).getUri());
//				VideoStream stream = VideoStream.create(device, SensorType.COLOR);
//				device.setDepthColorSyncEnabled(true);

		VirtualScreenManager.getInstance().start(2);
		VirtualScreenManager.getInstance().initialize(new FlatVirtualScreen(), new StaticVirtualScreenInitializer(new Size(1,1), 1300));

//		GestureManager.getInstance().registerGesture(new ClickGesture("click"));
//		GestureManager.getInstance().registerGesture(new PanGesture("swipe-left", EnumSet.<PanGesture.Direction>of(PanGesture.Direction.LEFT), false));
//		GestureManager.getInstance().registerGesture(new PanGesture("swipe-right", EnumSet.<PanGesture.Direction>of(PanGesture.Direction.RIGHT), false));
		GestureManager.getInstance().registerGesture(new PanGesture("pan"));
		GestureManager.getInstance().registerGesture(new ZoomGesture("zoom"));

//		GestureManager.getInstance().addGestureListener(new GestureListener() {
//
//			@Override
//			public void onGestureStarted(GestureData gesture) {
//				System.out.println("Gesture started: " + gesture.getName());
//				for(HandData gd : gesture.getHands()) {
//					System.out.println("Position: (" + gd.getPosition().getX() + "," + gd.getPosition().getY() + ")");
//				}
//			}
//
//			@Override
//			public void onGestureInProgress(GestureData gesture) {
//				System.out.println("Gesture in progress: " + gesture.getName());
//				for(HandData gd : gesture.getHands()) {
//					System.out.println("Position: (" + gd.getPosition().getX() + "," + gd.getPosition().getY() + ")");
//				}
//			}
//
//			@Override
//			public void onGestureCompleted(GestureData gesture) {
//				System.out.println("Gesture completed: " + gesture.getName());
//				if(gesture.hasData("startPoint")) {
//					Point2D<Float> startPoint = gesture.<Point2D<Float>>getData("startPoint");
//					System.out.println("Initial position: (" + startPoint.getX() + ", " + startPoint.getY() + ")");
//				}
//				if(gesture.hasData("initialDistance")) {
//					Float dist = gesture.<Float>getData("initialDistance");
//					System.out.println("Initial distance: " + dist.floatValue());
//				}
//				for(HandData gd : gesture.getHands()) {
//					System.out.println("Position: (" + gd.getPosition().getX() + "," + gd.getPosition().getY() + ")");
//				}
//			}
//
//		});

		GestureManager.getInstance().start();

		String resource = "res/example.owl";

		System.out.print("Loading model...");
		Model model = FileManager.get().loadModel(resource);
		System.out.println("done");
				
		JenaJungGraph g = new JenaJungGraph(model);

		Layout<RDFNode, Statement> layout = new FRLayout<RDFNode, Statement>(g);

		int width, height;
		do {
			Size frameSize = VirtualScreenManager.getInstance().getFrameSize();
			width = (int) frameSize.width;
			height = (int) frameSize.height;
		}
		while(width == 0 || height == 0);
		
		width -= 2*Main.BORDER;
		height -= 2*Main.BORDER;
		
		layout.setSize(new Dimension(width, height));
		VisualizationViewer<RDFNode, Statement> viz = new VisualizationViewer<RDFNode, Statement>(layout, new Dimension(width, height));
		RenderContext<RDFNode, Statement> context = viz.getRenderContext();
		context.setEdgeLabelTransformer(Transformers.EDGE);
		context.setVertexLabelTransformer(Transformers.NODE);
		HandTrackingModalMouse mouse = new HandTrackingModalMouse(viz);
		//GestureManager.getInstance().addGestureListener(mouse);
		viz.setGraphMouse(mouse);

		viz.setSize(new Dimension(width, height));

		final Main app = new Main(viz, width, height);
		VirtualScreenManager.getInstance().addVirtualScreenListener(app);
		
		ActionManager.getInstance().start();
		
		System.out.println("About to run");
		app.run();
	}
}