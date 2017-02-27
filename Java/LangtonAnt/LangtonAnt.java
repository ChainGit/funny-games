package com.chain.c003;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * 兰顿蚂蚁
 * 
 * 由来： 兰顿蚂蚁是由克里斯托夫·兰顿提出的细胞自动机的例子。
 * 
 * 释义： 在平面上的正方形格被填上黑色或白色。在其中一格正方形有一只“蚂蚁”。它的头部朝向上下左右其中一方。
 * 若蚂蚁在黑格，右转90度，将该格改为白格，向前移一步； 若蚂蚁在白格，左转90度，将该格改为黑格，向前移一步。
 * 很多时，蚂蚁刚刚开始时留下的路线都会有接近对称、像是会重复。 <br>
 * 但不论起始状态如何，蚂蚁的路线必然是无限长的，形成一个"高速公路"。
 * 
 * @author Chain
 *
 */
public class LangtonAnt extends JFrame implements Runnable {

	private static final long serialVersionUID = -869692582958968663L;

	// 用于绘制网格
	private JPanel p;
	// 绘制速度
	private final int SPEED = 10;
	// 方块的大小
	private final int BLOCK_SIZE = 6;
	// 游戏空间行数
	private final int rows = 100;
	// 游戏空间列数
	private final int columns = 100;
	// 游戏地图格子，每个格子保存一个方块，数组纪录方块的状态
	private boolean map[][] = new boolean[rows][columns];
	// 标记蚂蚁是否继续爬
	private boolean status = true;
	// 本程序只有一只蚂蚁
	private int antx = rows >> 1;
	private int anty = columns >> 1;
	// 0 上 1下 2左 3右
	private int direction;

	// 两种颜色
	private final Color colorBlack = Color.BLACK;
	private final Color colorWhite = Color.WHITE;

	public LangtonAnt() {
		// 随机蚂蚁所在的位置
		direction = (int) (Math.random() * 4);
		antx += (int) (Math.random() * 20 - 10);
		anty += (int) (Math.random() * 20 - 10);

		// 初始化窗体
		init();
	}

	// 初始化窗体
	private void init() {
		this.setTitle("兰顿蚂蚁");
		this.setSize(columns * BLOCK_SIZE + 10, rows * BLOCK_SIZE + 60);
		this.setLayout(new BorderLayout());
		this.setLocation(300, 50);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);

		this.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_SPACE)
					status = !status;
			}
		});

		p = new JPanel() {

			private static final long serialVersionUID = 6546531907131303600L;

			// 重写paint方法,用于绘制格子
			// JPanel有双缓冲,直接绘制JFrame有卡顿
			@Override
			public void paint(Graphics g) {
				super.paint(g);
				for (int i = 0; i < rows; i++)
					for (int j = 0; j < columns; j++) {
						boolean now = map[i][j];
						if (now)
							g.setColor(colorBlack);
						else
							g.setColor(colorWhite);
						g.fillRoundRect(j * BLOCK_SIZE, i * BLOCK_SIZE + 25, BLOCK_SIZE - 1, BLOCK_SIZE - 1,
								BLOCK_SIZE / 5, BLOCK_SIZE / 5);
					}
			}
		};

		this.add(p, BorderLayout.CENTER);

		this.setVisible(true);

	}

	// LangtonAnt实现runnable接口,与main线程分开,即逻辑和显示分离
	@Override
	public void run() {
		while (true) {
			if (status)
				change(direction);
			this.repaint();
			try {
				Thread.sleep(SPEED);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	// 改变格子颜色和蚂蚁的方向
	public void change(int dir) {
		// 防止越界
		if (antx > rows - 1 || anty > columns - 1 || antx < 0 || anty < 0) {
			status = false;
			return;
		}

		boolean current = map[antx][anty];
		map[antx][anty] = !current;
		if (current)
			// 黑格子,右转
			switch (dir) {
			case 0:
				direction = 3;
				antx++;
				break;
			case 1:
				direction = 2;
				antx--;
				break;
			case 2:
				direction = 0;
				anty--;
				break;
			case 3:
				direction = 1;
				anty++;
				break;
			}
		else
			// 白格子,左转
			switch (dir) {
			case 0:
				direction = 2;
				antx--;
				break;
			case 1:
				direction = 3;
				antx++;
				break;
			case 2:
				direction = 1;
				anty++;
				break;
			case 3:
				direction = 0;
				anty--;
				break;
			}
	}

	public static void main(String[] args) {
		new Thread(new LangtonAnt()).start();
	}

}
