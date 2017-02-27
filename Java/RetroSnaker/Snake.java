package com.chain.game.retrosnaker;

import java.util.LinkedList;

/**
 * 
 * 蛇类：只处理蛇头和蛇尾，并记录变换方向的历史
 * 
 * reset和change是键盘事件 <br>
 * key和move是线程事件<br>
 * 注意两者之间的关系处理
 * 
 * @author Chain
 *
 */
public class Snake {

	// 蛇的运动方向
	public static final int TURN_LEFT = 3;
	public static final int TURN_RIGHT = 1;
	public static final int TURN_UP = 0;
	public static final int TURN_DOWN = 2;

	// 网格参数
	private State[][] sat;
	private int rows;
	private int columns;

	// 苹果
	private Apple apple;
	// 蛇
	private static Snake snake;

	// 蛇头的位置
	private int headx;
	private int heady;
	// 蛇头的移动方向 0up 1right 2down 3left
	// 初始为右
	private int dirhead = 1;

	// 蛇尾的位置
	private int tailx;
	private int taily;
	// 蛇尾的移动方向 0up 1right 2down 3left
	// 初始为右
	private int dirtail = 1;

	// 队列，用于记忆历史变换的方向时的数据
	private LinkedList<int[]> lst = new LinkedList<>();

	// 用于处理如果按键过于频繁，远超过游戏速度时的数据和显示跟不上的问题
	private boolean flag;
	// 每次游戏时间周期只记录最后一次按键
	private boolean pkey;

	// 新建一个蛇部分对象
	private Snake(State[][] s) {
		if (sat == null) {
			sat = s;
			rows = sat[0].length;
			columns = sat.length;
		}
		if (apple == null)
			apple = Apple.getApple(s);
	}

	// 获得蛇，单例设计模式
	public static Snake getSnake(State[][] s) {
		if (snake == null)
			snake = new Snake(s);
		snake.init();
		return snake;
	}

	// 是否允许按键处理
	public void key(boolean f) {
		this.flag = f;
		if (!flag)
			pkey = false;
	}

	// 初始化蛇，初始长度为3，方向为1
	private void init() {
		headx = 2;
		heady = 0;
		dirhead = 1;
		tailx = 0;
		taily = 0;
		dirtail = 1;

		flag = false;
		pkey = false;

		lst.clear();

		for (int i = 0; i < sat.length; i++)
			for (int j = 0; j < sat[i].length; j++)
				if (sat[i][j] == State.SNAKE)
					sat[i][j] = State.BLANK;

		sat[0][0] = State.SNAKE;
		sat[0][1] = State.SNAKE;
		sat[0][2] = State.SNAKE;

	}

	// 改变蛇运动的方向
	public void change(int turn) {
		if (!flag)
			return;

		int lastdir = dirhead;
		switch (turn) {
		case TURN_UP:
			switch (dirhead) {
			case 3:
			case 1:
				dirhead = 0;
			}
			break;
		case TURN_DOWN:
			switch (dirhead) {
			case 1:
			case 3:
				dirhead = 2;
			}
			break;
		case TURN_LEFT:
			switch (dirhead) {
			case 0:
			case 2:
				dirhead = 3;
			}
			break;
		case TURN_RIGHT:
			switch (dirhead) {
			case 0:
			case 2:
				dirhead = 1;
			}
			break;
		}
		// 记录变换方向的历史
		if (lastdir != dirhead)
			if (!pkey) {
				lst.addFirst(new int[] { headx, heady, dirhead });
				pkey = true;
			} else
				lst.set(0, new int[] { headx, heady, dirhead });
	}

	// 移动蛇(先增加头部后再判断，根据判断结果决定是否需要删除尾巴)
	public int move() {
		// 移动头部
		switch (dirhead) {
		case 0:
			heady--;
			break;
		case 1:
			headx++;
			break;
		case 2:
			heady++;
			break;
		case 3:
			headx--;
			break;
		}
		// 判断是否撞到墙
		if (headx < 0 || heady < 0 || headx > columns - 1 || heady > rows - 1)
			return -1;

		State sp = sat[heady][headx];
		// 增长头部
		sat[heady][headx] = State.SNAKE;

		int[] carr = null;
		if (!lst.isEmpty())
			carr = lst.getLast();
		int cx = -1;
		int cy = -1;
		int cd = -1;
		if (carr != null) {
			cx = carr[0];
			cy = carr[1];
			cd = carr[2];
		}

		// 根据网格状态判断是否吃到苹果或者自己
		if (sp == State.SNAKE) {
			return -1;
		} else if (sp == State.APPLE) {
			apple.make();
			return 1;
		} else {
			// 删除尾巴
			sat[taily][tailx] = State.BLANK;

			if (cd != -1)
				if (cx == tailx && cy == taily) {
					dirtail = cd;
					lst.removeLast();
				}

			switch (dirtail) {
			case 0:
				taily--;
				break;
			case 1:
				tailx++;
				break;
			case 2:
				taily++;
				break;
			case 3:
				tailx--;
				break;
			}
		}
		return 0;
	}

	// 重置蛇
	public void reset() {
		apple.make();
		this.init();
	}

}
