package it.polito.computervision.controller;

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
import java.util.Collection;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

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
//		VirtualScreenManager.getInstance().destroy();
		NiTE.shutdown();
		OpenNI.shutdown();
	}

	public synchronized void paint(Graphics g) {
//		if (frame == null) {
//			return;
//		}

		int framePosX = 0;
		int framePosY = 0;

		if (frame != null) {
			int width = frame.getWidth();
			int height = frame.getHeight();
			BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

			frame.getData();
			img.setRGB(0, 0, width, height, frame.getData().asIntBuffer().array(), 0, width);

			framePosX = (getWidth() - width) / 2;
			framePosY = (getHeight() - height) / 2;

			System.out.println("drawing");
			g.drawImage(img, framePosX, framePosY, null);
		}

		// draw hands
		if(hands != null) {
			for (HandData hand : hands) {

				Point2D<Float> pos = hand.getPosition();
				if(hand.isTouching()) {
					g.setColor(Color.GREEN);
				}
				else {
					g.setColor(Color.RED);
				}
				g.fillRect(framePosX + pos.getX().intValue() - 3, framePosY + pos.getY().intValue() - 3, 15, 15);
				
				System.out.println(hand);
			}
		}
	}

	@Override
	public synchronized void onNewFrame(Collection<HandData> hands) {

		this.hands = hands;

		repaint();
	}
	
	@Override
	public void onFrameReady(VideoStream stream) {
		if(frame != null) {
			frame.release();
			frame = null;
		}

		frame = stream.readFrame();
		
		repaint();
	}

	public static void main(String s[]) {
		// initialize OpenNI and NiTE
		OpenNI.initialize();
		NiTE.initialize();

		List<DeviceInfo> devicesInfo = OpenNI.enumerateDevices();
		if (devicesInfo.size() == 0) {
			JOptionPane.showMessageDialog(null, "No device is connected", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Device device = Device.open(devicesInfo.get(0).getUri());
		VideoStream stream = VideoStream.create(device, SensorType.COLOR);
		

//		VirtualScreenManager.getInstance().start(2);
//		VirtualScreenManager.getInstance().initialize(new FlatVirtualScreen(), new StaticVirtualScreenInitializer(new Size(1,1), 700));
		
		final Main app = new Main(stream);
//		VirtualScreenManager.getInstance().addVirtualScreenListener(app);
		app.run();
	}

	


}