package com.chain.game.retrosnaker;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * 经典游戏：贪吃蛇
 *
 * 内容： 一条蛇，一次一个苹果，蛇撞到墙或者吃到自己游戏结束
 * 
 * BUG：按键过快的话会出现BUG
 * 
 * @author Chain
 *
 */
public class RetroSnaker extends JFrame implements Runnable {

	private static final long serialVersionUID = 6565734678034031233L;

	// 蛇移动的速度
	private static final int SPEED = 200;
	// 每个方块的边长
	private static final int BLOCK_SIZE = 15;
	// 游戏空间行数
	private final int rows = 30;
	// 游戏空间列数
	private final int columns = 30;
	// 游戏地图格子，每个格子保存一个方块，数组纪录方块的状态
	private State sat[][] = new State[rows][columns];
	// 标记游戏继续/暂停
	private boolean pause = true;
	// 标记游戏是否结束
	private boolean over = false;
	// 玩家总积分
	private int score;
	// 蛇
	private Snake snake;

	private static final Color COLOR_APPLE = Color.RED;
	private static final Color COLOR_SNAKE = Color.BLACK;
	private static final Color COLOR_BLANK = Color.WHITE;
	private static final Color COLOR_SCORE = Color.GRAY;

	// 用于绘制
	private JPanel p;

	public RetroSnaker() {
		// 初始化所有的方块为空
		for (int i = 0; i < sat.length; i++)
			for (int j = 0; j < sat[i].length; j++)
				sat[i][j] = State.BLANK;

		snake = Snake.getSnake(sat);

		init();
	}

	// 初始化窗体
	private void init() {
		this.setTitle("贪吃蛇");
		this.setSize(columns * BLOCK_SIZE + 10, rows * BLOCK_SIZE + 60);
		this.setLocation(200, 100);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setResizable(false);

		// 注册方向键事件监听器
		this.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				int keyCode = e.getKeyCode();
				if (keyCode == KeyEvent.VK_LEFT) // ←键
					snake.change(Snake.TURN_LEFT);
				else if (keyCode == KeyEvent.VK_RIGHT) // →键
					snake.change(Snake.TURN_RIGHT);
				else if (keyCode == KeyEvent.VK_DOWN) // ↓键
					snake.change(Snake.TURN_DOWN);
				else if (keyCode == KeyEvent.VK_UP) // ↑键
					snake.change(Snake.TURN_UP);
				else if (keyCode == KeyEvent.VK_SPACE) // 空格键控制继续/暂停
					pause = !pause;
				else if (keyCode == KeyEvent.VK_R) { // R键重置
					snake.reset();
					score = 0;
					over = false;
				}
			}
		});

		p = new JPanel() {

			private static final long serialVersionUID = -6588545341260894414L;

			@Override
			public void paint(Graphics g) {
				super.paint(g);

				// 绘制网格
				for (int i = 0; i < rows; i++)
					for (int j = 0; j < columns; j++) {
						State s = sat[i][j];
						if (s == State.SNAKE) {
							g.setColor(COLOR_SNAKE);
						} else if (s == State.APPLE) {
							g.setColor(COLOR_APPLE);
						} else {
							g.setColor(COLOR_BLANK);
						}
						g.fillRoundRect(j * BLOCK_SIZE, i * BLOCK_SIZE + 25, BLOCK_SIZE - 1, BLOCK_SIZE - 1,
								BLOCK_SIZE / 5, BLOCK_SIZE / 5);
					}

				// 打印得分
				g.setColor(COLOR_SCORE);
				g.setFont(new Font("Times New Roman", Font.BOLD, 20));
				g.drawString("STATUS : " + (over ? "OVER" : (pause ? "WAIT" : "RUN")), 5, 20);
				g.drawString("SCORE : " + score, 300, 20);

				// 游戏结束
				if (over) {
					g.setColor(Color.RED);
					g.setFont(new Font("Times New Roman", Font.BOLD, 20));
					g.drawString("GAME OVER", this.getWidth() / 2 - 60, this.getHeight() / 2);
				}
			}
		};

		this.add(p);

		this.setVisible(true);
	}

	// 多线程处理的作用是：显示和逻辑分开,这样显示sleep时不影响按键
	@Override
	public void run() {
		while (true) {
			// 显示上一个状态
			this.repaint();
			// 等待SPEED时间,留给玩家调整蛇的状态
			snake.key(true);
			try {
				Thread.sleep(SPEED);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			snake.key(false);
			// 移动蛇并获得移动的结果
			if (!pause) {
				int ans = snake.move();
				if (ans == -1) {
					over = true;
					pause = true;
				} else if (ans == 1)
					score++;
			}
		}
	}

	public static void main(String[] args) {
		new Thread(new RetroSnaker()).start();
	}

}
