package it.polito.computervision.controller;

import it.polito.computervision.gestures.GestureData;
import it.polito.computervision.gestures.GestureListener;
import it.polito.computervision.gestures.GestureManager;
import it.polito.computervision.gestures.impl.ClickGesture;
import it.polito.computervision.gestures.impl.PanGesture;
import it.polito.computervision.virtualscreen.HandData;
import it.polito.computervision.virtualscreen.VirtualScreenListener;
import it.polito.computervision.virtualscreen.VirtualScreenManager;
import it.polito.computervision.virtualscreen.impl.FlatVirtualScreen;
import it.polito.computervision.virtualscreen.impl.StaticVirtualScreenInitializer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.opencv.core.Core;
import org.opencv.core.Size;
import org.openni.Device;
import org.openni.DeviceInfo;
import org.openni.OpenNI;
import org.openni.SensorType;
import org.openni.VideoFrameRef;
import org.openni.VideoStream;

import com.primesense.nite.NiTE;

import org.openni.Point2D;

public class Main extends Component implements VirtualScreenListener, VideoStream.NewFrameListener {

	private static final long serialVersionUID = -7120492427686133224L;

	private JFrame mFrame;
	private boolean mShouldRun = true;
	private VideoStream stream;
	private VideoFrameRef frame;
	Collection<HandData> hands;

	public Main(VideoStream stream) {
		hands = null;
		frame = null;
		this.stream = stream;
		
		
		
		stream.setMirroringEnabled(true);
		
		stream.addNewFrameListener(this);
		stream.start();
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

		this.setSize(800, 600);
		mFrame.add("Center", this);
		mFrame.setSize(this.getWidth(), this.getHeight());
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
		if(frame != null) {
			frame.release();
			frame = null;
		}
		stream.destroy();
		GestureManager.getInstance().stop();
		VirtualScreenManager.getInstance().destroy();
		NiTE.shutdown();
		OpenNI.shutdown();
	}

	public synchronized void paint(Graphics graphics) {
		if (frame == null) {
			return;
		}

		int framePosX = 0;
		int framePosY = 0;

		if (frame != null && frame.getData() != null && frame.getData().hasRemaining()) {
			int width = frame.getWidth();
			int height = frame.getHeight();
			BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			ByteBuffer frameData = frame.getData().order(ByteOrder.LITTLE_ENDIAN);
			int[] fdArray = new int[frameData.remaining()/3];
			int pos = 0;
			byte[] rgb = new byte[3];
			while(frameData.remaining() >= 3) {
				frameData.get(rgb);
				fdArray[pos++] = 0xFF000000 | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
			}
			
			img.setRGB(framePosX, framePosY, width, height, fdArray, 0, width);
			framePosX = (getWidth() - width) / 2;
			framePosY = (getHeight() - height) / 2;

			graphics.drawImage(img, framePosX, framePosY, null);
		}

		// draw hands
		if(hands != null) {
			for (HandData hand : hands) {

				Point2D<Float> pos = hand.getPosition();
				if(hand.isTouching()) {
					graphics.setColor(Color.GREEN);
				}
				else {
					graphics.setColor(Color.RED);
				}
				graphics.fillRect(framePosX + pos.getX().intValue(), framePosY + pos.getY().intValue(), 15, 15);
				
//				System.out.println(hand);
			}
		}
	}

	@Override
	public synchronized void onNewFrame(Collection<HandData> hands) {

		this.hands = hands;

		repaint();
	}
	
	@Override
	public synchronized void onFrameReady(VideoStream stream) {
		if(frame != null) {
			frame.release();
			frame = null;
		}

		frame = stream.readFrame();
		
//		repaint();
	}

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
		VideoStream stream = VideoStream.create(device, SensorType.COLOR);
		device.setDepthColorSyncEnabled(true);

		VirtualScreenManager.getInstance().start(2);
		VirtualScreenManager.getInstance().initialize(new FlatVirtualScreen(), new StaticVirtualScreenInitializer(new Size(1,1), 800));
		
		GestureManager.getInstance().registerGesture(new ClickGesture());
		GestureManager.getInstance().registerGesture(new PanGesture());
		GestureManager.getInstance().addGestureListener(new GestureListener() {

			@Override
			public void onGestureStarted(GestureData gesture) {
				System.out.println("Gesture started: " + gesture.getName());
				for(HandData gd : gesture.getHands()) {
					System.out.println("Position: (" + gd.getPosition().getX() + "," + gd.getPosition().getY() + ")");
				}
			}

			@Override
			public void onGestureInProgress(GestureData gesture) {
				System.out.println("Gesture in progress: " + gesture.getName());
				for(HandData gd : gesture.getHands()) {
					System.out.println("Position: (" + gd.getPosition().getX() + "," + gd.getPosition().getY() + ")");
				}
			}

			@Override
			public void onGestureCompleted(GestureData gesture) {
				System.out.println("Gesture completed: " + gesture.getName());
				for(HandData gd : gesture.getHands()) {
					System.out.println("Position: (" + gd.getPosition().getX() + "," + gd.getPosition().getY() + ")");
				}
			}
			
		});
		
		GestureManager.getInstance().start();
		
		final Main app = new Main(stream);
		VirtualScreenManager.getInstance().addVirtualScreenListener(app);
		app.run();
	}

	


}