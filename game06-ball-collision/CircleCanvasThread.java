package com.chain.test.day11;

public class CircleCanvasThread extends Thread {

	private CircleCanvas canvas;

	public CircleCanvasThread(CircleCanvas canvas) {
		this.canvas = canvas;
	}

	@Override
	public void run() {
		while (true) {
			canvas.repaint();
			try {
				Thread.sleep(Main.PAINT_SPEED);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

}
