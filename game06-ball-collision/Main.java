package com.chain.test.day11;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * 模拟小球撞击（无阻力）
 * 
 * @author chain
 *
 */
public class Main extends JFrame {

	private static final long serialVersionUID = 1L;

	protected static final int CIRCLE_MAX_AMOUNT = 10;
	protected static final int CIRCLE_RADIUS = 30;
	protected static final int CIRCLE_DIAMETER = CIRCLE_RADIUS << 1;
	protected static final int CIRCLE_WEIGHT = 1;
	protected static final int CIRCLE_SCALE = 3;
	// 球运行速度不要设置的过大，否则会出现球“陷”在墙里
	protected static final int CIRCLE_MAX_SPEED = 3;
	protected static final int CIRCLE_MIN_SPEED = 1;
	protected static final int ERROR_APART = 1;
	protected static final int ERROR_CROSS = 1;
	// 移动速度要大于绘图频率
	// 移动速度要大于小球设置的速度
	// 否则会出现问题
	protected static final int MOVE_SPEED = 20;
	protected static final int PAINT_SPEED = 5;
	protected static final int CIRCLE_SPEED = 1000;

	private static final int WIDTH = 600;
	private static final int HEIGHT = 500;

	// 构造函数内不要放置页面相关的代码
	private Main() {
	}

	public static void main(String[] args) {
		Main m = new Main();
		// 先显示界面
		m.init();
		// 再启动相关线程
		m.start();

		try {
			m.join();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private CircleCanvasThread canvasThread;
	private CirclesThread circlesThread;

	private CircleCanvas canvas;

	private void init() {
		// 注意设置的先后顺序
		this.setTitle("模拟小球撞击");
		this.setResizable(false);
		this.setSize(WIDTH, HEIGHT);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);

		// panel自带双缓冲
		JPanel panel = new JPanel();
		panel.setBounds(0, 0, WIDTH, HEIGHT);
		// canvas也有双缓冲
		canvas = new CircleCanvas(WIDTH, HEIGHT);
		canvas.setBounds(0, 0, WIDTH, HEIGHT);
		panel.add(canvas);
		this.add(panel);

		this.pack();
		this.setVisible(true);
	}

	private void start() {
		circlesThread = new CirclesThread(WIDTH, HEIGHT);
		canvas.init();
		canvas.setCircles(circlesThread.getCircles());
		canvasThread = new CircleCanvasThread(canvas);

		circlesThread.start();
		canvasThread.start();
	}

	private void join() throws InterruptedException {
		circlesThread.join();
		canvasThread.join();
		System.out.println("exit");
	}
}
