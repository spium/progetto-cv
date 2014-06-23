package it.polito.computervision.controller;

import it.polito.computervision.virtualscreen.HandData;
import it.polito.computervision.virtualscreen.VirtualScreenListener;
import it.polito.computervision.virtualscreen.VirtualScreenManager;

import java.awt.Color;
import java.awt.Graphics;
import java.util.List;

import javax.swing.JFrame;

import org.openni.Point2D;

import edu.uci.ics.jung.visualization.VisualizationServer.Paintable;

public class HandPointRenderer implements Paintable, VirtualScreenListener {

	private JFrame mFrame;
	private int width, height;
	private List<HandData> hands;
	
	public HandPointRenderer(JFrame frame, int width, int height) {
		this.mFrame = frame;
		this.width = width;
		this.height = height;
		hands = null;
		
		VirtualScreenManager.getInstance().addVirtualScreenListener(this);
	}
	
	@Override
	public synchronized void paint(Graphics graphics) {
		int framePosX = (mFrame.getWidth() - width) / 2;
		int framePosY = (mFrame.getHeight() - height) / 2;

		graphics.setColor(Color.BLACK);
		graphics.drawRect(framePosX, framePosY, width, height);

		// draw hands
		if(hands != null) {
			for (HandData hand : hands) {
				Point2D<Float> pos = hand.getProjectedPosition();
				if(hand.isTouching()) {
					graphics.setColor(Color.GREEN);
				}
				else {
					graphics.setColor(Color.BLUE);
				}
				
				graphics.fillRect(framePosX + pos.getX().intValue() + 7, framePosY + pos.getY().intValue() + 7, 15, 15);
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

}
