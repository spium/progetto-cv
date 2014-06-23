package it.polito.computervision.controller;

import it.polito.computervision.actions.ActionManager;
import it.polito.computervision.gestures.GestureManager;
import it.polito.computervision.gestures.impl.ClickGesture;
import it.polito.computervision.gestures.impl.PanGesture;
import it.polito.computervision.gestures.impl.ZoomGesture;
import it.polito.computervision.virtualscreen.VirtualScreenManager;
import it.polito.computervision.virtualscreen.impl.FlatVirtualScreen;
import it.polito.computervision.virtualscreen.impl.StaticVirtualScreenInitializer;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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

	private JFrame mFrame;
	private boolean mShouldRun = true;
	private VisualizationController controller;

	public Main(String resource) {

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
		
		controller = new VisualizationController(resource, mFrame);
		
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
		
		GestureManager.getInstance().stop();
		VirtualScreenManager.getInstance().destroy();
		NiTE.shutdown();
		OpenNI.shutdown();
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

		Device.open(devicesInfo.get(0).getUri());

		VirtualScreenManager.getInstance().start(2);
		VirtualScreenManager.getInstance().initialize(new FlatVirtualScreen(), new StaticVirtualScreenInitializer(new Size(1,1), 1000));

//		String resource = "res/example.owl";
		String resource = "http://www.w3.org/TR/owl-guide/wine.rdf";

		final Main app = new Main(resource);
		
		
				
		System.out.println("About to run");
		app.run();
	}
}