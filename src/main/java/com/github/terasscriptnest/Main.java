package com.github.terasscriptnest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

//import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.yaml.snakeyaml.Yaml;

import it.sauronsoftware.junique.AlreadyLockedException;
import it.sauronsoftware.junique.JUnique;

public class Main {

	static String pathCooldown = "cooldown.ser", pathPin = "pin.ser", pathUnpin = "unpin.ser", pathIndex = "index.ser",
			pathTeraReplies = "teraReplies.yaml", pathApproved = "approved.yaml",
			pathSmallIcon = "atlas_small_icon.png", pathTrayIcon = "atlas_tray_icon.png";
	static final String APPID = "com.github.terasscriptnest.angryterabot";

	static AngryTeraBot bot;
	static Data saveFile;

	public static void main(String[] args) {
		boolean alreadyRunning;
		try { // Unique instance verification using JUnique mutex
			JUnique.acquireLock(APPID);
			alreadyRunning = false;
		} catch (AlreadyLockedException e) {
			alreadyRunning = true;
		}
		if (!alreadyRunning) { // Program Start

			/* Graphic interface instantiation */
			new GUI();

			/* Initial resource loading */
			Data data = new Data();

			/* Telegram Bot registration. */
			// respawn(data, "AH AHH ME AWAKE ME AWAKE!! O.O WHAT HAPPENED!??");
			saveFile = data;
		}
	}

	static void respawnTera() {
		respawn(saveFile, "*Smacks into window*");
	}

	static void respawnTera(String spawnMessage) {
		respawn(saveFile, spawnMessage);
	}

	static void respawn(Data data, String spawnMessage) {
		bot = new AngryTeraBot(data, spawnMessage);
		try {
			// ApiContextInitializer.init();
			TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
			telegramBotsApi.registerBot(bot);
			System.out.println("** respawn successful: \t" + data);
		} catch (TelegramApiException e) {
			e.printStackTrace();
			System.out.println("** respawn failed: \t" + data);
		}
	}

	static Object refreshYAML(String path) {
		Object data = null;
		Yaml yaml = null;
		InputStream input = null;
		try {
			yaml = new Yaml();
			input = new FileInputStream(new File(path));
			// RESOURCE DEBUG// input =
			// Main.class.getClassLoader().getResourceAsStream(path);
			data = yaml.load(input);
			input.close();
			System.out.println("** loading successful: \t" + path);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("** loading failed: \t" + path);
		}
		return data;
	}

	static Object load(String path) {
		Object data = null;
		FileInputStream fileIn = null;
		ObjectInputStream objectIn = null;
		try {
			fileIn = new FileInputStream(path);
			// RESOURCE DEBUG// fileIn =
			// Main.class.getClassLoader().getResourceAsStream(path);
			objectIn = new ObjectInputStream(fileIn);
			data = (Object) objectIn.readObject();
			objectIn.close();
			fileIn.close();
			System.out.println("** loading successful: \t" + path);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("** loading failed: \t" + path);
		}
		return data;
	}

	static void save(String path, Object data) {
		FileOutputStream fileOut = null;
		ObjectOutputStream objectOut = null;
		try {
			fileOut = new FileOutputStream(path);
			objectOut = new ObjectOutputStream(fileOut);
			objectOut.writeObject(data);
			objectOut.close();
			fileOut.close();
			System.out.println("** save successful: \t" + path);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("** save failed: \t" + path);
		}
	}
}
