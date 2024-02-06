package com.github.terasscriptnest;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import com.github.terasscriptnest.JComponentOutputStream.JComponentHandler;

public class GUI {

	/* Constants */
	public static final Color COLOR_METALLIC_BLUE = new Color(0x73728D), COLOR_METALLIC_LIGHTBLUE = new Color(0xD7D6F1),
			COLOR_BLANCHED_ALMOND = new Color(0xFFEBCD), COLOR_WHITEPURPLE = new Color(0xF0EFFF),
			COLOR_LIGHTBLUE = new Color(0xBFDFDF), COLOR_PASTEL_GREEN = new Color(0xD8FFCF),
			COLOR_PASTEL_BLUE = new Color(0xCFFFFF), COLOR_PASTEL_PURPLE = new Color(0xECCFFF),
			COLOR_PASTEL_RED = new Color(0xFFC0C0), COLOR_TANGERINE = new Color(0xFF9900);

	/* States */
	public boolean isProcessing = false; // Used to prevent button spam.

	/* Graphical Elements */
	// Layers
	JFrame frame = new JFrame();
	JPanel panelLayer1 = new JPanel(), panelLayer2 = new JPanel(), panelLayer3 = new JPanel(),
			// Functions
			panelConsole = new JPanel(), panelController = new JPanel(), panelDebugger = new JPanel(),
			panelMemory = new JPanel();
	// Headers
	JLabel headerCNSL = new JLabel(html("Console"), SwingConstants.CENTER),
			headerCTRL = new JLabel(html("Speech Controller - Fight Club"), SwingConstants.CENTER),
			headerDBG = new JLabel(html("Debugger"), SwingConstants.CENTER),
			headerMEM = new JLabel(html("Memory Management"), SwingConstants.CENTER),
			// Components
			status1 = new JLabel("..."), status2 = new JLabel("..."), status3 = new JLabel("..."),
			status4 = new JLabel("..."), status5 = new JLabel("..."), status6 = new JLabel("..."),
			status7 = new JLabel("..."), status8 = new JLabel("..."), status9 = new JLabel("...");
	JButton button1 = new JButton(html("Bot test<br/> message")),
			button2 = new JButton(html("Console test<br/> message")), button3 = new JButton(html("Check queue")),
			button4 = new JButton(html("Load queue")), button5 = new JButton(html("Save queue")),
			button6 = new JButton(html("Refresh queue")), button7 = new JButton(html("Respawn bot")),
			button8 = new JButton(html("Clear console")), button9 = new JButton(html("Send")),
			button10 = new JButton(html("Send Angee")), button11 = new JButton(html("Send ANGEE")),
			button12 = new JButton(html("Send Repeated")), button13 = new JButton(html("Shutdown"));
	JScrollPane console = new JScrollPane();
	JTextArea textArea = new JTextArea("Add your message here");
	JComponentOutputStream outputStream;
	//ImageIcon icon = new ImageIcon(Main.pathSmallIcon); // RESOURCE DEBUG// = loadImage(Main.pathSmallIcon);

	/* Sizes */
	private static final int SIZE_X = 960, SIZE_Y = 600;
	Dimension sizeFrame = new Dimension(SIZE_X, SIZE_Y),
			sizeConsole = new Dimension(SIZE_X, sizeFrame.height - (sizeFrame.height / 3)),
			sizeLayer1 = new Dimension(SIZE_X / 6, sizeFrame.height - sizeConsole.height),
			sizeDebugger = new Dimension(SIZE_X / 6, sizeLayer1.height - (sizeLayer1.height / 2)),
			sizeLayer2 = new Dimension(SIZE_X / 6, sizeLayer1.height - sizeDebugger.height),
			sizeController = new Dimension(SIZE_X / 6, sizeLayer2.height - (sizeLayer2.height / 8)),
			sizeLayer3 = new Dimension(SIZE_X / 6, sizeLayer2.height - sizeController.height),
			sizeMemory = new Dimension(SIZE_X / 6, sizeLayer2.height - (sizeLayer3.height / 16)),
			sizeTextField = new Dimension(400, 30);

	/* Constructor */
	public GUI() {
		/* Element Preferences */
		// Frame
		frame.setTitle("AngryTeraBot GrrGrr");
		frame.setSize(SIZE_X, SIZE_Y);
		if (SystemTray.isSupported()) {
			frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			trayInitialize(Main.pathTrayIcon);
		} else {
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}
		frame.setIconImage( (new ImageIcon(Toolkit.getDefaultToolkit().getImage(Main.pathSmallIcon)).getImage()) );
		frame.setResizable(true);
		frame.setPreferredSize(sizeFrame);
		// Layers
		panelLayer1.setLayout(new GridLayout(0, 1));
		panelLayer1.setPreferredSize(sizeLayer1);
		panelLayer2.setLayout(new GridLayout(0, 1));
		panelLayer2.setPreferredSize(sizeLayer2);
		panelLayer3.setLayout(new GridLayout(0, 1));
		panelLayer3.setPreferredSize(sizeLayer3);
		// Console Component
		panelConsole.setLayout(new GridLayout(0, 1));
		panelConsole.setPreferredSize(sizeConsole);
		console = consoleBuild();
		// Debugger Component
		panelDebugger.setLayout(new GridLayout(2, 8));
		panelDebugger.setPreferredSize(sizeDebugger);
		button1.addActionListener(new ActionListener() { // Bot test message
			public void actionPerformed(ActionEvent e) {
				if (!isProcessing) {
					status1.setText(html("Sent"));
					Main.bot.teraReply("HELO AM AWAKE");
					pause(status1, 4);
				}
			}
		});
		button2.addActionListener(new ActionListener() { // Console test message
			public void actionPerformed(ActionEvent e) {
				if (!isProcessing) {
					status2.setText(html("Toggled"));
					System.out.println("-- Console status is normal --");
					pause(status2, 1);
				}
			}
		});
		button3.addActionListener(new ActionListener() { // Check queue
			public void actionPerformed(ActionEvent e) {
				if (!isProcessing) {
					status3.setText(html("Peeked"));
					Main.bot.peekTera();
					pause(status3, 2);
				}
			}
		});
		button4.addActionListener(new ActionListener() { // Load queue
			public void actionPerformed(ActionEvent e) {
				if (!isProcessing) {
					status4.setText(html("Loading<br/> please wait..."));
					Main.bot.loadTera();
					pause(status4, 7);
				}
			}
		});
		button5.addActionListener(new ActionListener() { // Save queue
			public void actionPerformed(ActionEvent e) {
				if (!isProcessing) {
					status5.setText(html("Saving<br/> please wait..."));
					Main.bot.saveTera();
					pause(status5, 7);
				}
			}
		});
		button6.addActionListener(new ActionListener() { // Refresh queue
			public void actionPerformed(ActionEvent e) {
				if (!isProcessing) {
					status6.setText(html("Clearing<br/> please wait..."));
					Main.bot.refreshTera();
					pause(status6, 5);
				}
			}
		});
		button7.addActionListener(new ActionListener() { // Respawns the bot
			public void actionPerformed(ActionEvent e) {
				if (!isProcessing) {
					status7.setText(html("Respawned<br/> bot"));
					System.out.println("** Respawning...");
					pause(status7, 7);
					if (null != textArea.getText() && !textArea.getText().contentEquals("Add your message here")) {
						Main.respawnTera(textArea.getText());
					} else {
						Main.respawnTera();
					}
				}
			}
		});
		button8.addActionListener(new ActionListener() { // Clears the console
			public void actionPerformed(ActionEvent e) {
				if (!isProcessing) {
					status8.setText(html("Cleared<br/> console"));
					outputStream.clear();
					pause(status8, 2);
				}
			}
		});
		// Controller Component
		panelController.setLayout(new FlowLayout());
		panelController.setPreferredSize(sizeController);
		textArea.setPreferredSize(sizeTextField);
		button9.addActionListener(new ActionListener() { // Send message to fight club
			public void actionPerformed(ActionEvent e) {
				if (!isProcessing && !textArea.getText().contentEquals("Add your message here")) {
					status9.setText(html("Sent"));
					System.out.println(">> " + textArea.getText());
					Main.bot.teraReply(textArea.getText());
					pause(status9, 2);
				}
			}
		});
		button10.addActionListener(new ActionListener() { // Send AngeeText to fight club
			public void actionPerformed(ActionEvent e) {
				if (!isProcessing && !textArea.getText().contentEquals("Add your message here")) {
					status9.setText(html("Sent"));
					System.out.println(">> " + textArea.getText());
					Main.bot.teraReply(Main.bot.teraAngeeText(textArea.getText()));
					pause(status9, 2);
				}
			}
		});
		button11.addActionListener(new ActionListener() { // Send ANGEEtext to fight club
			public void actionPerformed(ActionEvent e) {
				if (!isProcessing && !textArea.getText().contentEquals("Add your message here")) {
					status9.setText(html("Sent"));
					System.out.println(">> " + textArea.getText());
					Main.bot.teraReply(Main.bot.teraANGEEText(textArea.getText()));
					pause(status9, 2);
				}
			}
		});
		button12.addActionListener(new ActionListener() { // Send RepeatText to fight club
			public void actionPerformed(ActionEvent e) {
				if (!isProcessing && !textArea.getText().contentEquals("Add your message here")) {
					status9.setText(html("Sent"));
					System.out.println(">> " + textArea.getText());
					Main.bot.teraReply(Main.bot.teraRepeatText(textArea.getText()));
					pause(status9, 2);
				}
			}
		});
		button13.addActionListener(new ActionListener() { // Shuts down the program
			public void actionPerformed(ActionEvent e) {
				if (!isProcessing) {
					status9.setText(html("Respawned<br/> bot"));
					System.out.println("** Saving...");
					Main.bot.saveTera();
					status9.setText(html("Shutting down..."));
					shutdown(status9, 2);
				}
			}
		});

		/* Element Order */
		// Console
		panelConsole.add(console);
		// Debugger
		panelDebugger.add(button1);
		panelDebugger.add(button2);
		panelDebugger.add(button3);
		panelDebugger.add(button4);
		panelDebugger.add(button5);
		panelDebugger.add(button6);
		panelDebugger.add(button7);
		panelDebugger.add(button8);
		panelDebugger.add(status1);
		panelDebugger.add(status2);
		panelDebugger.add(status3);
		panelDebugger.add(status4);
		panelDebugger.add(status5);
		panelDebugger.add(status6);
		panelDebugger.add(status7);
		panelDebugger.add(status8);
		// Controller
		panelController.add(status9);
		panelController.add(textArea);
		panelController.add(button9);
		panelController.add(button10);
		panelController.add(button11);
		panelController.add(button12);
		panelController.add(button13);

		/*
		 * Panel Order * Each panel layer from 1..n has a North, Center, & South; (Frame
		 * is layer0). North for label headers, Center for element components, South for
		 * panel layers.
		 */
		frame.add(headerCNSL, BorderLayout.NORTH);
		frame.add(panelConsole, BorderLayout.CENTER);
		frame.add(panelLayer1, BorderLayout.SOUTH);

		panelLayer1.add(headerDBG, BorderLayout.NORTH);
		panelLayer1.add(panelDebugger, BorderLayout.CENTER);
		panelLayer1.add(panelLayer2, BorderLayout.SOUTH);

		panelLayer2.add(headerCTRL, BorderLayout.NORTH);
		panelLayer2.add(panelController, BorderLayout.CENTER);

		/*
		 * Scheduled Console Clear *** repeats every 6 hours
		 */
		Timer cleanUpTimer = new Timer();
		TimerTask cleanUp = new TimerTask() {
			public void run() {
				outputStream.clear();
			}
		};
		cleanUpTimer.schedule(cleanUp, AngryTeraBot.HOUR * 6);

		/* GUI Initialization */
		frame.pack();
		frame.setVisible(true);
	}

	/* Component JScrollPane Console */
	public JScrollPane consoleBuild() {

		JLabel console = new JLabel();
		JComponentOutputStream consoleOutputStream = new JComponentOutputStream(console, new JComponentHandler() {
			private StringBuilder sb = new StringBuilder();

			@Override
			public void setText(JComponent swingComponent, String text) {
				sb.delete(0, sb.length());
				append(swingComponent, text);
			}

			@Override
			public void replaceRange(JComponent swingComponent, String text, int start, int end) {
				sb.replace(start, end, text);
				redrawTextOf(swingComponent);
			}

			@Override
			public void append(JComponent swingComponent, String text) {
				sb.append(text);
				redrawTextOf(swingComponent);
			}

			private void redrawTextOf(JComponent swingComponent) {
				((JLabel) swingComponent).setText("<html><pre>" + sb.toString() + "</pre></html>");
			}
		});
		this.outputStream = consoleOutputStream;
		PrintStream con = new PrintStream(consoleOutputStream);
		System.setOut(con);
		System.setErr(con);
		JScrollPane sp = new JScrollPane(console, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		sp.getVerticalScrollBar().setUnitIncrement(8);
		sp.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
		return sp;
	}

	/* Pause Logic */
	public void pause(JLabel status, int seconds) {
		this.isProcessing = true;
		Timer waitTime = new Timer();
		TimerTask reset = new TimerTask() {
			public void run() {
				isProcessing = false;
				status.setText("...");
			}
		};
		waitTime.schedule(reset, AngryTeraBot.SECOND * seconds);
	}

	public void shutdown(JLabel status, int seconds) {
		this.isProcessing = true;
		Timer waitTime = new Timer();
		TimerTask kill = new TimerTask() {
			public void run() {
				isProcessing = false;
				System.exit(0);
			}
		};
		waitTime.schedule(kill, AngryTeraBot.SECOND * seconds);
	}

	private String html(String text) {
		return "<html>" + text + "</html>";
	}

	/* Initializes the system tray */
	private void trayInitialize(String iconPath) {
		SystemTray systemTray = SystemTray.getSystemTray();
		final TrayIcon trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().getImage(iconPath));
		// RESOURCE DEBUG// = new TrayIcon(loadImage(iconPath).getImage());
		PopupMenu popMenu = new PopupMenu();
		MenuItem show = new MenuItem("Show");
		show.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(true);
			}
		});
		MenuItem exit = new MenuItem("Exit");
		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		popMenu.add(show);
		popMenu.add(exit);
		trayIcon.setPopupMenu(popMenu);
		try {
			systemTray.add(trayIcon);
		} catch (AWTException e1) {
			e1.printStackTrace();
		}
	}
	/*
	 * private ImageIcon loadImage(String path) { ImageIcon icon = null; try {
	 * java.net.URL iconURL = getClass().getResource(path); icon = new
	 * ImageIcon(iconURL); } catch (Exception e) { e.printStackTrace(); } return
	 * icon; }
	 */
}
