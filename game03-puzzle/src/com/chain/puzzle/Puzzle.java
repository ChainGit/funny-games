package com.chain.puzzle;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * 移动方块拼图(3x3)
 * 
 * 核心：移动方块其实是对数据的操作，而数据的变动会造成方块的刷新。
 * 
 * @author Chain
 *
 */
public class Puzzle extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int EMPTY = -1;

	private int[][] dat;
	private int empty;
	private int emptyIndex;
	private ImageIcon[] images;
	private ImageIcon blankImage;
	private JLabel blankJLabel;
	private JLabel[] jLabels;
	private JPanel jPanel;

	private JLabel pressedJLabel;
	private int pressedX;
	private int pressedY;
	private JLabel releasedJLabel;
	private int releasedX;
	private int releasedY;

	private boolean isPressed;

	{
		dat = new int[3][3];
		images = new ImageIcon[9];
		jLabels = new JLabel[9];
		URL url = this.getClass().getClassLoader().getResource("f-b.jpg");
		blankImage = new ImageIcon(url);
		blankImage.setImage(blankImage.getImage().getScaledInstance(200, 200, Image.SCALE_DEFAULT));
		blankJLabel = new JLabel(blankImage);
		for (int i = 0; i < 9; i++) {
			ImageIcon image = new ImageIcon(this.getClass().getClassLoader().getResource("f-" + i + ".jpg"));
			image.setImage(image.getImage().getScaledInstance(200, 200, Image.SCALE_DEFAULT));
			images[i] = image;
			jLabels[i] = new JLabel(image);
		}
	}

	private void initData() {
		int[] rnds = randomByList(0, 8, 9);
		for (int i = 0; i < 9; i++)
			dat[i / 3][i % 3] = rnds[i];
		int blank = (int) (Math.random() * 9);
		emptyIndex = blank;
		empty = dat[blank / 3][blank % 3];
		dat[blank / 3][blank % 3] = EMPTY;
		showData();
	}

	private void showData() {
		System.out.println("data:");
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++)
				System.out.printf("%2d ", dat[i][j]);
			System.out.println();
		}
		System.out.println("empty: " + empty);
		System.out.println("emptyIndex: " + emptyIndex);
		System.out.println();
	}

	private void initUI() {
		this.setTitle("移动方块拼图");
		this.setLayout(new BorderLayout());
		this.setSize(620, 620);
		this.setLocationRelativeTo(getOwner());
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setResizable(false);

		this.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				int keyCode = e.getKeyCode();
				if (keyCode == KeyEvent.VK_R) { // R键(复位)
					initData();
					loadImages();
				} else if (keyCode == KeyEvent.VK_H && e.isControlDown()) { // 作弊模式
					cheatData();
					loadImages();
				}
			}
		});

		jPanel = new JPanel();
		jPanel.setLayout(new GridLayout(3, 3, 5, 5));
		jPanel.setBackground(Color.GRAY);
		jPanel.setSize(610, 610);
		this.add(jPanel, BorderLayout.CENTER);

		loadImages();

		blankJLabel.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseReleased(MouseEvent e) {
				releasedJLabel = (JLabel) e.getComponent();
				if (isPressed)
					moveSquare();
			}
		});

		for (int i = 0; i < 9; i++) {
			jLabels[i].addMouseListener(new MouseAdapter() {

				@Override
				public void mousePressed(MouseEvent e) {
					pressedJLabel = (JLabel) e.getComponent();
					pressedX = e.getX();
					pressedY = e.getY();
					isPressed = true;
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					releasedJLabel = (JLabel) e.getComponent();
					releasedX = e.getX();
					releasedY = e.getY();
					if (isPressed)
						moveSquare();
				}

				@Override
				public void mouseEntered(MouseEvent e) {
					((JLabel) e.getComponent()).setBorder(BorderFactory.createLineBorder(Color.BLACK));
				}

				@Override
				public void mouseExited(MouseEvent e) {
					((JLabel) e.getComponent()).setBorder(BorderFactory.createLineBorder(Color.GRAY));
				}
			});
		}

		this.setVisible(true);
	}

	protected void cheatData() {
		for (int i = 0; i < 9; i++)
			dat[i / 3][i % 3] = i;
	}

	private void moveSquare() {
		if (releasedJLabel == null || pressedJLabel == null)
			return;
		if (releasedJLabel != pressedJLabel)
			return;

		String releasedText = releasedJLabel.getText();
		int index = new Integer(releasedText);

		if (pressedX - releasedX > 30) {
			System.out.println("向左");
			if (index % 3 != 0 && index - 1 == emptyIndex)
				swapAndRepaint(dat, index, emptyIndex);
		}
		if (releasedX - pressedX > 30) {
			System.out.println("向右");
			if (index % 3 != 2 && index + 1 == emptyIndex)
				swapAndRepaint(dat, index, emptyIndex);
		}
		if (pressedY - releasedY > 30) {
			System.out.println("向上");
			if (index / 3 - 1 == emptyIndex / 3 && index % 3 == emptyIndex % 3)
				swapAndRepaint(dat, index, emptyIndex);
		}
		if (releasedY - pressedY > 30) {
			System.out.println("向下");
			if (index / 3 + 1 == emptyIndex / 3 && index % 3 == emptyIndex % 3)
				swapAndRepaint(dat, index, emptyIndex);
		}

		checkIfOk();

		isPressed = false;
		releasedJLabel = null;
		pressedJLabel = null;

	}

	private void checkIfOk() {
		int i = 0;
		for (; i < 9; i++) {
			if (dat[i / 3][i % 3] == -1)
				continue;
			if (dat[i / 3][i % 3] != i)
				break;
		}
		if (i == 9)
			dat[emptyIndex / 3][emptyIndex % 3] = empty;
	}

	private void swapAndRepaint(int[][] dat, int index, int emptyIndex) {
		int tmp = dat[emptyIndex / 3][emptyIndex % 3];
		dat[emptyIndex / 3][emptyIndex % 3] = dat[index / 3][index % 3];
		dat[index / 3][index % 3] = tmp;
		this.emptyIndex = index;
		showData();
		loadImages();
	}

	private void loadImages() {
		jPanel.removeAll();
		for (int i = 0; i < 9; i++) {
			if (dat[i / 3][i % 3] == -1) {
				jPanel.add(blankJLabel);
				blankJLabel.setText(String.valueOf(i));
				continue;
			}
			jPanel.add(jLabels[dat[i / 3][i % 3]]);
			jLabels[dat[i / 3][i % 3]].setText(String.valueOf(i));
		}
		jPanel.validate();
		jPanel.repaint();
	}

	public Puzzle() {
		initData();
		initUI();
	}

	public static void main(String[] args) {
		new Puzzle();
	}

	private int[] randomByList(int min, int max, int n) {
		int[] result = new int[n];
		List<Integer> lst = new ArrayList<>();
		Random rand = new Random();
		while (lst.size() < n) {
			int num = rand.nextInt(max + 1) + min;
			if (!lst.contains(num))
				lst.add(num);
		}
		for (int i = 0; i < n; i++)
			result[i] = lst.get(i);
		return result;
	}
}
