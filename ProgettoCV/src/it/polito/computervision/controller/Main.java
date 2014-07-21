package it.polito.computervision.controller;

import it.polito.computervision.actions.ActionManager;
import it.polito.computervision.gestures.GestureManager;
import it.polito.computervision.virtualscreen.VirtualScreenManager;
import it.polito.computervision.virtualscreen.impl.FlatVirtualScreen;
import it.polito.computervision.virtualscreen.impl.StaticVirtualScreenInitializer;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.opencv.core.Core;
import org.opencv.core.Size;
import org.openni.Device;
import org.openni.DeviceInfo;
import org.openni.OpenNI;

import com.primesense.nite.NiTE;

public class Main {

	private static final int DEFAULT_VSCREEN_DISTANCE = 1500;
	private JFrame mFrame;
	private boolean mShouldRun = true;
	@SuppressWarnings("unused")
	private VisualizationController controller;

	public Main(String resource, String[] roots) {

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

		controller = new VisualizationController(resource, roots, mFrame);

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
	}

	public static void main(String argv[]) {
		if(argv.length < 1) {
			System.err.println("Syntax: <ontology> [rootNodes] [virtualScreenDistance]");
			System.err.println("ontology: path to the ontology");
			System.err.println("rootNodes: names of the root nodes to start navigation from (default: hierarchy root)");
			System.err.println("virtualScreenDistance: distance of the virtual screen from the sensor (default: 1.5 meters)");

			return;
		}

		String[] roots = null;
		int dist = DEFAULT_VSCREEN_DISTANCE;
		if(argv.length > 1) {
			ArrayList<String> rootList = new ArrayList<String>();
			for(int i = 1; i < argv.length; ++i) {
				try {
					dist = (int) (Float.parseFloat(argv[i])*1000); //convert to mm
					if(dist <= 500) {
						System.err.println("Virtual screen distance must be > 0.5 meters");
						return;
					}
					//distance should be the last parameter
					break;
				}
				catch(NumberFormatException e) {
					//if it's not a number then assume it's the name of a root
					rootList.add(argv[i]);
				}
			}

			if(rootList.size() > 0)
				roots = rootList.toArray(new String[rootList.size()]);
		}

		// initialize OpenNI and NiTE
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		OpenNI.initialize();
		NiTE.initialize();

		List<DeviceInfo> devicesInfo = OpenNI.enumerateDevices();
		if (devicesInfo.size() == 0) {
			JOptionPane.showMessageDialog(null, "No device is connected", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Device.open(devicesInfo.get(0).getUri());

		VirtualScreenManager.getInstance().start(2);
		//size doesn't matter in this implementation
		VirtualScreenManager.getInstance().initialize(new FlatVirtualScreen(), new StaticVirtualScreenInitializer(new Size(1,1), dist));		

		final Main app = new Main(argv[0], roots);

		System.out.println("About to run");
		app.run();

		ActionManager.getInstance().stop();
		GestureManager.getInstance().stop();
		VirtualScreenManager.getInstance().destroy();
		NiTE.shutdown();
		OpenNI.shutdown();
	}
}