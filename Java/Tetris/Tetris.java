package com.chain.game.tetris;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * 俄罗斯方块：与贪吃蛇不同，变换图形是立即响应按键的
 * 
 * BUG：按键过快的话会出现问题
 * 
 * @author Chain
 *
 */
public class Tetris extends JFrame implements Runnable {

	private static final long serialVersionUID = 7261054718032677058L;

	// 网格状态
	enum State {
		// 正在下落的方块
		ACTIVE,
		// 已经落到底部的方块
		STOPPED,
		// 空白区域
		BLANK,
		// 旋转中心
		ACTIVE_MID,
	}

	// 游戏速度
	private final int SLOW = 500;
	// 快速方块落下
	private final int FAST = 50;
	// 缓冲
	private final int WAIT = 200;

	// 每个方块的边长
	private final int BLOCK_SIZE = 20;
	// 游戏空间行数(4和3的倍数)
	private int rows = 24;
	// 游戏空间列数(4和3的公倍数)
	private int columns = 16;
	// 游戏地图格子，每个格子保存一个方块，数组纪录方块的状态
	private State map[][] = new State[rows][columns];
	// 标记是否正在游戏
	private boolean status = true;
	// 标记是否暂停继续
	private boolean pause = false;
	// 标记生成的图形是否正在下落
	private boolean fall = true;
	// 生成的图形最下一行在地图中所在行数的索引
	private int xbottom = 0;
	// 生成的图形行数
	private int xrows = 0;
	// 标记生成的图形是否快速下降
	private boolean fast = false;
	// 当前生成的图形形状
	private int shape;
	// 得分
	private int score;

	// 用于绘制网格
	private JPanel p;

	// 几种方块的颜色
	private final Color COLOR_ACTIVE = Color.BLUE;
	private final Color COLOR_STOPPED = Color.GRAY;
	private final Color COLOR_SCORE = Color.GRAY;
	private final Color COLOR_BLANK = Color.WHITE;

	public Tetris() {
		// 初始化窗体信息
		this.setTitle("俄罗斯方块");
		this.setSize(columns * BLOCK_SIZE + 10, rows * BLOCK_SIZE + 60);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setResizable(false);
		this.setLocationRelativeTo(null);

		// 初始化所有的方块为空
		for (int i = 0; i < map.length; i++)
			for (int j = 0; j < map[i].length; j++)
				map[i][j] = State.BLANK;

		// 监听按键
		this.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				int keyCode = e.getKeyCode();
				if (keyCode == KeyEvent.VK_SPACE) // 继续或暂停
					space();
				// 游戏未暂停时响应
				else if (!pause) {
					if (keyCode == KeyEvent.VK_LEFT) // ←键 左移
						left();
					else if (keyCode == KeyEvent.VK_RIGHT) // →键 右移
						right();
					else if (keyCode == KeyEvent.VK_DOWN) // ↓键 加速
						accelerate();
					else if (keyCode == KeyEvent.VK_UP) // ↑ 键 旋转
						rotate();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				int keyCode = e.getKeyCode();
				if (keyCode == KeyEvent.VK_DOWN) // 释放向下键，取消快速下降
					fast = false;
			}
		});

		p = new JPanel() {

			private static final long serialVersionUID = -4394303302152969801L;

			// 重写JPanel的paint方法
			@Override
			public void paint(Graphics g) {
				super.paint(g);
				for (int i = 0; i < rows; i++)
					for (int j = 0; j < columns; j++) {
						if (map[i][j] == State.ACTIVE || map[i][j] == State.ACTIVE_MID) // 绘制活动块
							g.setColor(COLOR_ACTIVE);
						else if (map[i][j] == State.STOPPED) // 绘制静止块
							g.setColor(COLOR_STOPPED);
						else
							g.setColor(COLOR_BLANK);
						g.fillRoundRect(j * BLOCK_SIZE, i * BLOCK_SIZE + 25, BLOCK_SIZE - 1, BLOCK_SIZE - 1,
								BLOCK_SIZE / 5, BLOCK_SIZE / 5);
					}

				// 打印得分
				g.setColor(COLOR_SCORE);
				g.setFont(new Font("Times New Roman", Font.BOLD, 20));
				g.drawString("SCORE: " + score, 5, 20);

				// 打印继续/暂停
				g.setColor(COLOR_SCORE);
				g.setFont(new Font("Times New Roman", Font.BOLD, 20));
				g.drawString("STATUS: " + (pause ? "PAUSE" : "RUN"), this.getWidth() - 160, 20);

				// 游戏结束
				if (!status) {
					g.setColor(Color.RED);
					g.setFont(new Font("Times New Roman", Font.BOLD, 30));
					g.drawString("GAME OVER", this.getWidth() / 2 - 90, this.getHeight() / 2);
				}

			}

		};

		this.add(p);

		this.setVisible(true);
	}

	// 暂停或继续
	private void space() {
		pause = !pause;
		this.repaint();
	}

	// 生成随机的方块图形
	private void generate() {
		Random rand = new Random();
		// 随机方块形状
		shape = rand.nextInt(28);
		int colmid = columns >> 1;
		// 根据随机数等概的产生不同的图形，从顶部开始
		// System.out.println(shape);
		switch (shape) {
		case 0: // 一型
		case 1:
			map[0][colmid] = map[0][colmid - 1] = map[0][colmid + 1] = map[0][colmid + 2] = State.ACTIVE;
			xrows = 1;
			break;
		case 2: // |型
		case 3:
			map[0][colmid] = map[1][colmid] = map[2][colmid] = map[3][colmid] = State.ACTIVE;
			xrows = 4;
			break;
		case 4: // L型1
			map[0][colmid] = map[1][colmid] = map[2][colmid] = map[2][colmid + 1] = State.ACTIVE;
			xrows = 3;
			break;
		case 5: // L型2
			map[0][colmid] = map[0][colmid - 1] = map[1][colmid - 1] = map[0][colmid + 1] = State.ACTIVE;
			xrows = 2;
			break;
		case 6: // L型3
			map[0][colmid] = map[1][colmid] = map[2][colmid] = map[0][colmid - 1] = State.ACTIVE;
			xrows = 3;
			break;
		case 7: // L型4
			map[0][colmid + 1] = map[1][colmid - 1] = map[1][colmid] = map[1][colmid + 1] = State.ACTIVE;
			xrows = 2;
			break;
		case 8: // 反L型1
			map[0][colmid] = map[1][colmid] = map[2][colmid] = map[2][colmid - 1] = State.ACTIVE;
			xrows = 3;
			break;
		case 9: // 反L型2
			map[0][colmid - 1] = map[1][colmid] = map[1][colmid - 1] = map[1][colmid + 1] = State.ACTIVE;
			xrows = 2;
			break;
		case 10: // 反L型3
			map[0][colmid] = map[1][colmid] = map[2][colmid] = map[0][colmid + 1] = State.ACTIVE;
			xrows = 3;
			break;
		case 11: // 反L型4
			map[0][colmid] = map[0][colmid - 1] = map[0][colmid + 1] = map[1][colmid + 1] = State.ACTIVE;
			xrows = 2;
			break;
		case 12: // T型1
			map[1][colmid] = map[1][colmid - 1] = map[1][colmid + 1] = map[0][colmid] = State.ACTIVE;
			xrows = 2;
			break;
		case 13: // T型2
			map[0][colmid] = map[1][colmid + 1] = map[2][colmid] = map[1][colmid] = State.ACTIVE;
			xrows = 3;
			break;
		case 14: // T型3
			map[0][colmid] = map[0][colmid - 1] = map[0][colmid + 1] = map[1][colmid] = State.ACTIVE;
			xrows = 2;
			break;
		case 15: // T型4
			map[1][colmid] = map[2][colmid] = map[1][colmid - 1] = map[0][colmid] = State.ACTIVE;
			xrows = 3;
			break;
		case 16: // 田型
		case 17: // 田型
		case 18: // 田型
		case 19: // 田型
			map[0][colmid] = map[0][colmid + 1] = map[1][colmid] = map[1][colmid + 1] = State.ACTIVE;
			xrows = 2;
			break;
		case 20: // Z型1
			map[0][colmid - 1] = map[1][colmid] = map[1][colmid + 1] = State.ACTIVE;
			map[0][colmid] = State.ACTIVE_MID;
			xrows = 2;
			break;
		case 21: // Z型2
			map[1][colmid - 1] = map[0][colmid] = map[2][colmid - 1] = State.ACTIVE;
			map[1][colmid] = State.ACTIVE_MID;
			xrows = 3;
			break;
		case 22: // Z型3
			map[0][colmid - 1] = map[0][colmid] = map[1][colmid + 1] = State.ACTIVE;
			map[1][colmid] = State.ACTIVE_MID;
			xrows = 2;
			break;
		case 23: // Z型4
			map[1][colmid] = map[0][colmid] = map[2][colmid - 1] = State.ACTIVE;
			map[1][colmid - 1] = State.ACTIVE_MID;
			xrows = 3;
			break;
		case 24: // 反Z型1
			map[0][colmid + 1] = map[1][colmid] = map[1][colmid - 1] = State.ACTIVE;
			map[0][colmid] = State.ACTIVE_MID;
			xrows = 2;
			break;
		case 25: // 反Z型2
			map[1][colmid - 1] = map[0][colmid - 1] = map[2][colmid] = State.ACTIVE;
			map[1][colmid] = State.ACTIVE_MID;
			xrows = 3;
			break;
		case 26: // 反Z型3
			map[0][colmid + 1] = map[0][colmid] = map[1][colmid - 1] = State.ACTIVE;
			map[1][colmid] = State.ACTIVE_MID;
			xrows = 2;
			break;
		case 27: // 反Z型4
			map[1][colmid] = map[0][colmid - 1] = map[2][colmid] = State.ACTIVE;
			map[1][colmid - 1] = State.ACTIVE_MID;
			xrows = 3;
			break;
		}
		// 获得底部所在行数
		xbottom = xrows - 1;
	}

	// 下落
	private void fall() {
		// 是否能够下落
		fall = true;
		// 从当前行检查，如果遇到阻碍，则停止下落
		for (int i = 0; i < xrows; i++) {
			for (int j = 0; j < columns; j++)
				// 遍历到行中块为活动块，而下一行块为静止块，则遇到阻碍
				if ((map[xbottom - i][j] == State.ACTIVE || map[xbottom - i][j] == State.ACTIVE_MID)
						&& map[xbottom - i + 1][j] == State.STOPPED) {
					// 稍微停顿下，留给玩家做紧急调整
					if (!fast) {
						try {
							Thread.sleep(WAIT);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						// 再次判断
						if ((map[xbottom - i][j] == State.ACTIVE || map[xbottom - i][j] == State.ACTIVE_MID)
								&& map[xbottom - i + 1][j] == State.STOPPED) {
							fall = false; // 停止下落
							break;
						}
					} else {
						fall = false; // 停止下落
						break;
					}
				}
			if (!fall)
				break;
		}

		// 可以下落
		if (fall) {
			// 图形下落一行
			for (int i = 0; i < xrows; i++)
				for (int j = 0; j < columns; j++) {
					if (map[xbottom - i][j] == State.ACTIVE) { // 活动块向下移动一行
						map[xbottom - i][j] = State.BLANK; // 原活动块变成空块
						map[xbottom - i + 1][j] = State.ACTIVE; // 下一行块变成活动块
					}
					if (map[xbottom - i][j] == State.ACTIVE_MID) { // 活动块向下移动一行
						map[xbottom - i][j] = State.BLANK; // 原活动块变成空块
						map[xbottom - i + 1][j] = State.ACTIVE_MID; // 下一行块变成活动块
					}
				}

			// 重绘
			this.repaint();
		} else if (xbottom < xrows)
			// 行索引小于生成的图形行数，说明图形刚出现就遇到阻碍，即已经顶到地图最上方了，游戏结束
			status = false;
	}

	// 向左走
	private void left() {
		// 标记左边是否有阻碍
		boolean hasBlock = false;

		// 判断是否左边有阻碍
		for (int i = 0; i < xrows; i++)
			if ((map[xbottom - i][0] == State.ACTIVE) || (map[xbottom - i][0] == State.ACTIVE_MID)) { // 判断左边是否为墙
				hasBlock = true;
				break; // 有阻碍，不用再循环判断行
			} else {
				for (int j = 1; j < columns; j++) // 判断左边是否有其它块
					if ((map[xbottom - i][j] == State.ACTIVE || map[xbottom - i][j] == State.ACTIVE_MID)
							&& map[xbottom - i][j - 1] == State.STOPPED) {
						hasBlock = true;
						break; // 有阻碍，不用再循环判断列
					}
				if (hasBlock)
					break; // 有阻碍，不用再循环判断行
			}

		// 左边没有阻碍，则将图形向左移动一个块的距离
		if (!hasBlock) {
			for (int i = 0; i < xrows; i++)
				for (int j = 1; j < columns; j++) {
					if (map[xbottom - i][j] == State.ACTIVE) {
						map[xbottom - i][j] = State.BLANK;
						map[xbottom - i][j - 1] = State.ACTIVE;
					}
					if (map[xbottom - i][j] == State.ACTIVE_MID) {
						map[xbottom - i][j] = State.BLANK;
						map[xbottom - i][j - 1] = State.ACTIVE_MID;
					}
				}

			// 重绘
			this.repaint();
		}
	}

	// 向右走，和向左走差不多
	private void right() {
		// 标记右边是否有阻碍
		boolean hasBlock = false;

		// 判断是否右边有阻碍
		for (int i = 0; i < xrows; i++)
			if ((map[xbottom - i][columns - 1] == State.ACTIVE) || map[xbottom - i][columns - 1] == State.ACTIVE_MID) { // 判断右边是否为墙
				hasBlock = true;
				break; // 有阻碍，不用再循环判断行
			} else {
				for (int j = 0; j < columns - 1; j++) // 判断右边是否有其它块
					if ((map[xbottom - i][j] == State.ACTIVE || map[xbottom - i][j] == State.ACTIVE_MID)
							&& map[xbottom - i][j + 1] == State.STOPPED) {
						hasBlock = true;
						break; // 有阻碍，不用再循环判断列
					}
				if (hasBlock)
					break; // 有阻碍，不用再循环判断行
			}

		// 右边没有阻碍，则将图形向右移动一个块的距离
		if (!hasBlock) {
			for (int i = 0; i < xrows; i++)
				for (int j = columns - 2; j >= 0; j--) {
					if (map[xbottom - i][j] == State.ACTIVE) {
						map[xbottom - i][j] = State.BLANK;
						map[xbottom - i][j + 1] = State.ACTIVE;
					}
					if (map[xbottom - i][j] == State.ACTIVE_MID) {
						map[xbottom - i][j] = State.BLANK;
						map[xbottom - i][j + 1] = State.ACTIVE_MID;
					}
				}

			// 重绘
			this.repaint();
		}
	}

	// 加速向下
	private void accelerate() {
		// 标记可以加速下落
		fast = true;
	}

	// 旋转：旋转规则采用SRS(一型和Z型特殊)
	private void rotate() {
		try {
			// 旋转中心
			int centerx = -1;
			int centery = -1;

			// 用于临时存储变换后的图形容器，直接操作map存在问题
			int tmpx = -1;
			int tmpy = -1;
			State[][] tmp = null;
			// 容器左上角
			int startx = -1;
			int starty = -1;

			if (shape > 15 && shape < 20) { // 田型
				return;
			} else if (shape < 4) { // 一型
				tmpx = 4;
				tmpy = 4;
				tmp = new State[4][4];

				// 判断是横着的还是竖着的
				boolean kind = false;
				for (int i = 0; i < columns; i++)
					// 找到活动块，并进一步判断
					if (map[xbottom][i] == State.ACTIVE) {
						if (map[xbottom - 1][i] == State.ACTIVE)
							// 竖着的
							kind = true;
						// 竖着的话则第二个是中心，逆时针旋转
						// 横着的话则第三个是中心，顺时针旋转
						if (kind) {
							centerx = xbottom - 2;
							centery = i;
						} else {
							centerx = xbottom;
							centery = i + 2;
						}
						break;
					}

				// 将图形放置在容器中
				startx = centerx - 1;
				starty = centery - 2;
				for (int i = 0; i < tmpx; i++)
					for (int j = 0; j < tmpy; j++)
						if (map[startx + i][starty + j] != State.STOPPED)
							tmp[i][j] = map[startx + i][starty + j];

				// 旋转
				if (kind) {
					for (int i = 0; i < tmpx; i++)
						tmp[i][2] = State.BLANK;

					for (int i = 0; i < tmpy; i++)
						tmp[1][i] = State.ACTIVE;
				} else {
					for (int i = 0; i < tmpy; i++)
						tmp[1][i] = State.BLANK;

					for (int i = 0; i < tmpx; i++)
						tmp[i][2] = State.ACTIVE;
				}

				// 检查是否冲突,即旋转后是否遇到STOPPED
				for (int i = 0; i < tmpx; i++)
					for (int j = 0; j < tmpy; j++) {
						State fact = map[i + startx][j + starty];
						State now = tmp[i][j];
						if ((now == State.ACTIVE || now == State.ACTIVE_MID) && fact == State.STOPPED)
							return;
					}

				if (kind) {
					xrows = 1;
					xbottom -= 2;
				} else {
					xrows = 4;
					xbottom += 2;
				}

			} else if (shape > 3 && shape < 16) { // (反)L型、T型
				tmpx = 3;
				tmpy = 3;
				tmp = new State[tmpx][tmpy];

				// 找中心
				// 判断是否是横着的
				int many = 0;
				boolean kind = false;
				for (int i = 0; i < xrows; i++) {
					many = 0;
					for (int j = 0; j < columns; j++) {
						if (map[xbottom - i][j] == State.ACTIVE)
							many++;
						// 找到横着的三个，即横着的
						if (many == 3) {
							kind = true;
							centerx = xbottom - i;
							centery = j - 1;
							break;
						}
					}
					if (many == 3)
						break;
				}

				// 不是横着的，再判断是否是竖着的
				if (many != 3)
					for (int i = 0; i < columns; i++) {
						many = 0;
						for (int j = 0; j < xrows; j++) {
							if (map[xbottom - j][i] == State.ACTIVE)
								many++;
							// 找到竖着的三个，即竖着的
							if (many == 3) {
								kind = false;
								centerx = xbottom - 1;
								centery = i;
								break;
							}
						}
						if (many == 3)
							break;
					}

				// 以中心顺时针旋转90度
				startx = centerx - 1;
				starty = centery - 1;
				for (int i = 0; i < tmpx; i++)
					for (int j = 0; j < tmpy; j++)
						if (map[startx + i][starty + j] != State.STOPPED)
							tmp[j][tmpy - 1 - i] = map[startx + i][starty + j];

				// 检查是否冲突,即旋转后是否遇到STOPPED
				for (int i = 0; i < tmpx; i++)
					for (int j = 0; j < tmpy; j++) {
						State fact = map[i + startx][j + starty];
						State now = tmp[i][j];
						if ((now == State.ACTIVE || now == State.ACTIVE_MID) && fact == State.STOPPED)
							return;
					}

				// 有两种情况需要xbottem+1和xbottem-1
				for (int i = 0, j = 0; i < tmpy; i++) {
					if (map[centerx + 1][i + starty] == State.BLANK)
						j++;
					if (j == 3)
						xbottom++;
				}

				for (int i = 0, j = 0; i < tmpy; i++) {
					if (tmp[tmpx - 1][i] == State.BLANK)
						j++;
					if (j == 3)
						xbottom--;
				}

				if (kind)
					xrows = 3;
				else
					xrows = 2;

			} else if (shape > 19 && shape < 28) { // (反)Z型
				tmpx = 3;
				tmpy = 3;
				tmp = new State[tmpx][tmpy];

				// 找中心
				// 判断是否是横着的,3行中非空行的个数
				boolean kind = false;
				boolean[] have = new boolean[tmpx];
				for (int i = 0; i < columns; i++)
					for (int j = 0; j < tmpx; j++)
						if (map[xbottom - j][i] == State.ACTIVE || map[xbottom - j][i] == State.ACTIVE_MID) {
							// 确定中心
							if (map[xbottom - j][i] == State.ACTIVE_MID) {
								centerx = xbottom - j;
								centery = i;
							}
							if (!have[tmpx - 1 - j])
								have[tmpx - 1 - j] = true;
						}

				for (int i = 0; i < tmpx; i++)
					if (!have[i]) {
						// 横着的
						kind = true;
						break;
					}

				// 以中心顺时针旋转90度
				startx = centerx - 1;
				starty = centery - 1;
				for (int i = 0; i < tmpx; i++)
					for (int j = 0; j < tmpy; j++)
						if (map[startx + i][starty + j] != State.STOPPED)
							tmp[j][tmpy - 1 - i] = map[startx + i][starty + j];

				// 检查是否冲突,即旋转后是否遇到STOPPED
				for (int i = 0; i < tmpx; i++)
					for (int j = 0; j < tmpy; j++) {
						State fact = map[i + startx][j + starty];
						State now = tmp[i][j];
						if ((now == State.ACTIVE || now == State.ACTIVE_MID) && fact == State.STOPPED)
							return;
					}

				// 有两种情况需要xbottem+1和xbottem-1
				for (int i = 0, j = 0; i < tmpy; i++) {
					if (map[centerx + 1][i + starty] == State.BLANK)
						j++;
					if (j == 3)
						xbottom++;
				}

				for (int i = 0, j = 0; i < tmpy; i++) {
					if (tmp[tmpx - 1][i] == State.BLANK)
						j++;
					if (j == 3)
						xbottom--;
				}

				if (kind)
					xrows = 3;
				else
					xrows = 2;

			}

			// 将容器写回map
			// 清除上一个图形状态
			for (int i = 0; i < tmpx; i++)
				for (int j = 0; j < tmpy; j++) {
					State fact = map[i + startx][j + starty];
					if (fact == State.ACTIVE || fact == State.ACTIVE_MID)
						map[i + startx][j + starty] = State.BLANK;
				}

			// 写入新的图形状态
			for (int i = 0; i < tmpx; i++)
				for (int j = 0; j < tmpy; j++) {
					State fact = map[i + startx][j + starty];
					if (fact == State.STOPPED)
						continue;
					map[i + startx][j + starty] = tmp[i][j];
				}

			this.repaint();
		} catch (Exception e) {
			// 不考虑变换后越界问题，直接异常抛出且不处理，但效率会降低
		}
	}

	// 判断是否能消除行
	private void judge() {
		int[] blocksCount = new int[rows]; // 记录每行有方块的列数
		int eliminateRows = 0; // 消除的行数
		// 计算每行方块数量
		for (int i = 0; i < rows; i++) {
			blocksCount[i] = 0;
			for (int j = 0; j < columns; j++)
				if (map[i][j] == State.STOPPED)
					blocksCount[i]++;
		}

		// 实现有满行的方块消除操作
		for (int i = 0; i < rows; i++)
			if (blocksCount[i] == columns) {
				// 清除一行
				for (int m = i; m >= 0; m--)
					for (int n = 0; n < columns; n++)
						map[m][n] = (m == 0) ? State.BLANK : map[m - 1][n];
				eliminateRows++; // 记录消除行数
			}

		// 计算积分
		score += eliminateRows;
		// 重绘方块
		this.repaint();
	}

	// 落地
	private void land() {
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < columns; j++)
				if (map[i][j] == State.ACTIVE || map[i][j] == State.ACTIVE_MID) // 将活动状态改为静止状态
					map[i][j] = State.STOPPED;
		this.repaint();
	}

	@Override
	public void run() {
		while (true) { // 正在游戏
			// 游戏结束或暂停
			if (!status || pause) {
				try {
					Thread.sleep(SLOW);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}
			// 生成方块图形
			generate();
			// 图形循环下落
			while (xbottom < rows - 1) {
				// 游戏暂停
				if (pause) {
					try {
						Thread.sleep(SLOW);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					continue;
				}
				fall(); // 下降
				if (!fall)
					break;
				xbottom++; // 每下降一行，指针向下移动一行
				// 休眠，加速休眠一毫秒，未加速休眠500毫秒
				try {
					Thread.sleep(fast ? FAST : SLOW);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			fast = false; // 按向下键加速，默认不加速
			// 下落到遇到阻碍为止，修改图形方块状态
			land();
			// 判断是否可消除行
			judge();
		}
	}

	public static void main(String[] args) {
		new Thread(new Tetris()).start(); // 启动游戏
	}
}
