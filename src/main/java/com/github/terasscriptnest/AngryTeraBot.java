package com.github.terasscriptnest;

import java.io.File;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.UnpinAllChatMessages;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.UnpinChatMessage;

public class AngryTeraBot extends TelegramLongPollingBot {
	
	static final int HOUR = 3600000, MINUTE = 60000, SECOND = 1000;
	long id;
	static Integer process = -1;
	static int logType = 1, command = -1;
	static String reply = null;
	static boolean isProcessing = false;
	Sensitive_Data secret = new Sensitive();
	Message telegramMessage;
	Data saveData;
	static HashMap<Long, Object> rememberList = new HashMap<Long, Object>();
	static HashMap<Long, Integer> waitingSender = new HashMap<Long, Integer>();
	HashMap<String, Integer> indexList;
	Map<String, String> teraReplies;
	Map<String, Integer> approvedSender;
	Queue<Long> cooldownQueue;
	Queue<Integer> pinQueue;
	Queue<Integer> unpinQueue;
	private static LinkedList<String> workingList = new LinkedList<String>();;

	/**
	 * AngryTeraBot's constructor Filled with timers and timer tasks.
	 */
	public AngryTeraBot(Data data, String spawnMessage) {
		super();
		/* Define timer's tasks */
		TimerTask spawn = new TimerTask() {
			@Override
			public void run() {
				loadTera();
			}
		};
		TimerTask dequeueCooldown, dequeuePin, dequeueUnpin;
		dequeueCooldown = new TimerTask() {
			@Override
			public void run() {
				try {
					if (null != cooldownQueue && !cooldownQueue.isEmpty()) {
						cooldownQueue.remove();
						System.out.println("** Dequeued cooldown!");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		dequeuePin = new TimerTask() {
			@Override
			public void run() {
				try {
					if (null != pinQueue && !pinQueue.isEmpty()) {
						int messageId = pinQueue.remove();
						pin(secret.getActiveChatID(), messageId, true);
						System.out.println("** Dequeued pinned!");
						enqueueForUnpin(messageId);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		dequeueUnpin = new TimerTask() {
			@Override
			public void run() {
				try {
					if (null != unpinQueue && !unpinQueue.isEmpty()) {
						int messageId = unpinQueue.remove();
						unpin(secret.getActiveChatID(), messageId);
						System.out.println("** Dequeued unpinned!");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		/* Define AutoSave & command refresh stuff */
		TimerTask autoSave = new TimerTask() {
			@Override
			public void run() {
				saveTera();
				if (null != waitingSender && !waitingSender.isEmpty()) {
					waitingSender.clear();
				}
				if (null != rememberList && !rememberList.isEmpty()) {
					rememberList.clear();
				}
			}
		};
		/* Set timers */
		try {
			Timer cooldownTimer = new Timer();
			Timer pinTimer = new Timer();
			Timer unpinTimer = new Timer();
			Timer timerAutoSave = new Timer();
			Timer spawnTimer = new Timer();
			cooldownTimer.scheduleAtFixedRate(dequeueCooldown, SECOND * 10, MINUTE * 15);
			pinTimer.scheduleAtFixedRate(dequeuePin, MINUTE, MINUTE * 30);
			unpinTimer.scheduleAtFixedRate(dequeueUnpin, MINUTE, HOUR * 12);
			timerAutoSave.scheduleAtFixedRate(autoSave, MINUTE * 5, HOUR);
			spawnTimer.schedule(spawn, SECOND*10);
		} catch (Exception e) {
			e.printStackTrace();
		}
		teraReply(teraAngeeText(spawnMessage));
	}
	
	/**
	 * **********************************
	 * 			BOT CORE
	 * **********************************
	 */

	/**
	 * The bot's entry point. </br>
	 * 
	 * @param update - An object that has all telegram information. </br>
	 */
	public void onUpdateReceived(Update update) {
		if (update.hasChannelPost()) {
			telegramMessage = update.getChannelPost();
			process = 124; // Assign default process to new channel.
			processReply(telegramMessage.getChatId());
		} else if (update.hasMessage()) {
			telegramMessage = update.getMessage();
			process = 126; // Assign default process to direct message.
			processReply(telegramMessage.getChatId());
		}
		log(" "+Character.toString(process)+" ", logType); // Log the response.
		if(command!=-1) {
			doCommand(command, telegramMessage.getChatId());
		} else if(!waitingSender.isEmpty()&&waitingSender.containsKey( telegramMessage.getChatId() )) { 
			// Checks to see if awaiting response.
			doCommand(waitingSender.get(telegramMessage.getChatId()), telegramMessage.getChatId());
		}
		// Reset variables for next pass.
		logType = 1;
		process = -1;
		command = -1;
	}
	
	/* Where to listen and how to process info before responses. */
	private void processReply(long senderID) {
		if(approvedSender.containsKey( String.valueOf(senderID) )) { // Checks to see if approved.
			process = approvedSender.get( String.valueOf(senderID) );
		}
		if(process!=43&&process!=124&&telegramMessage.getFrom().getId()==secret.getOwnerID()) {
			process = 35;
		}
		switch(process) {
		case 32:  //   Fight Club Sender
			if(null != telegramMessage) {
				if (telegramMessage.getFrom().getId()==secret.getChannelID_Anonymous()) { // If channel broadcast message: unpin it.
					unpin(secret.getActiveChatID(), telegramMessage.getMessageId());
					return;
				} 
				if (telegramMessage.getFrom().getId()==secret.getBotID()) { // If own message: delete it.
					deleteMessage(secret.getActiveChatID(), telegramMessage.getMessageId());
					return;
				}
				if(telegramMessage.hasText() && !telegramMessage.getText().contains("https:") && !teraReplies.isEmpty()) {
					command = teraRead(secret.getActiveChatID());
				}
			}
		case 33:  // ! Fileshare Sender
			logType = 2;
			if (telegramMessage.hasText()) {
				command = teraRead(secret.getChannelID_Fileshare());
			}
			break;
		case 35:  // # Owner Sender
			logType = 2;
			if (telegramMessage.hasText()) {
				command = teraRead(secret.getOwnerID());
			}
			break;
		case 42:  // * Index Channel Sender
			logType = 2;
			break;
		case 43:  // + Approved channel Sender
			if (null == cooldownQueue || cooldownQueue.isEmpty() || !cooldownQueue.contains(senderID)) {
				forwardFightClub(senderID);
			}
			break;
		case 124: // | New channel
			if (null != telegramMessage.getText() && telegramMessage.getText().equalsIgnoreCase("*drops seed for tera*")) {
				teraReply(teraAngeeText("BEEP BEEP THANK U FOR SEED") + "\n ur channel id is   " + senderID
						+ "\n plz dont forget it  (-^7^-) <3");
			}
			break;
		case 126: // ~ Direct Sender
			logType = 3;
			if (telegramMessage.hasText()) {
				command = teraRead(senderID);
			}
			break;
		default:  // Unknown user or group sender
			System.out.println("Error!");
			if(null!=telegramMessage) {
				log(" ERROR ", 3);
			}
			return;
		}
	}
	
	/* The full list of bot commands. */
	@SuppressWarnings("unchecked")
	private void doCommand(int command, long chatId) {
		switch(command) {
		case 0: // Send message Angee text
			sendMessage( chatId, teraAngeeText(reply) );
			return;
		case 1: // Send message Normal text
			sendMessage( chatId, reply);
			return;
		case 2: // Send message ANGEE text
			sendMessage( chatId, teraANGEEText(reply));
			return;
		case 3: // Send message Repeat text
			sendMessage( chatId, teraRepeatText(reply));
			return;
		case 4: // "/start"
			System.out.println("Bot has started.");
			teraReply(teraAngeeText("Henlo u stinky. I'm awake"));
			return;
		case 5: // "my name"
			teraReply("No, your name is " + telegramMessage.getFrom().getFirstName());
			return;
		case 6: // "my id"
			teraReply("Your id is actually " + telegramMessage.getFrom().getId().toString());
			return;
		case 7: // " what is your id "
			teraReply("M-my id? Why would you ask for that? o.o \nIt's " + secret.getBotID()
					+ " \n..if you were wondering");
			return;
		case 8: // "/peektera"
			teraReply("Queues: cooldown(" + cooldownQueue.peek() + ") pin(" + pinQueue.peek() + ") unpin("
					+ unpinQueue.peek() + ")");
			return;
		case 9: // "/cancel"
			isProcessing = false;
			waitingSender.remove(chatId);
			rememberList.remove(chatId);
			sendMessage(chatId, "Uh? Who are you again? I forgot.");
			return;
		case 10: // "/indexforward" stage 1
			sendMessage(chatId, "Forward me channel posts to add to a topic in the index. \nType '/done x' when finished, where x is the topic you wish to add to.");
			waitingSender.put(chatId, 11);
			isProcessing = true;
			return;
		case 11: // "/indexforward" stage 2
			if(workingList==null) {
				workingList = new LinkedList<String>();
			}
			try {
				if(null!=telegramMessage.getForwardFromChat().getUserName()) {
					workingList = new LinkedList<String>();
					if(null!=rememberList.get(chatId)) {
						workingList = (LinkedList<String>) rememberList.get(chatId);
					}
					String text = telegramMessage.getForwardFromChat().getUserName();
					text = "https://t.me/"+text;
					workingList.add(text);
					rememberList.put(chatId, workingList);
				} else if (null!=telegramMessage.getForwardFrom()) {
					sendMessage(chatId, "Um.. this might be from a user and not a channel.");
				} else if (null==telegramMessage.getForwardFromChat()) {
					sendMessage(chatId, "Nono you need to forward a channel's link. And if you did, it's not accessible.");
				} else if (null==telegramMessage.getForwardFromChat().getUserName()) {
					sendMessage(chatId, "Hmm.. it seems this channel isn't accessible.");
				} else {
					sendMessage(chatId, "Uhhh.. that's weird. That wasn't supposed to happen. You found an error congrats! :D");
				}
			} catch (Exception e) {
				sendMessage(chatId, "Dude.. I said forward a chat or channel's link.");
			}
			return;
		case 12: // "/indexnew" stage 1
			sendMessage(chatId, "Create a new topic for chats/channels in the index. \nWhat is the new topic's name?"
					+ "\nType '/cancel' to forget the request. \nType '/indexlist' to see all topics.");
			waitingSender.put(chatId, 13);
			return;
		case 13: // "/indexnew" stage 2
			if(null==telegramMessage.getText()) {
				sendMessage(chatId, "Uh there is no text in that message. What is the new topic's name?");
			} else if (indexList.containsKey(telegramMessage.getText().toLowerCase())) {
				sendMessage(chatId, "That topic name already exists. Think of another one.");
			} else {
				rememberList.put(chatId, telegramMessage.getText().toLowerCase());
				sendMessage(chatId, "Excellent! Now send a photo with a short caption describing this new index topic.");
				waitingSender.remove(chatId);
				waitingSender.put(chatId, 14);
			}
			return;
		case 14: // "/indexnew" stage 3
			if(telegramMessage.hasPhoto()&&!telegramMessage.getCaption().isEmpty()&&!isProcessing) {
				String indexName = (String) rememberList.get(chatId);
				boolean success = false;
				success = newIndex(indexName);
				if(success==false) {
					sendMessage(chatId, "Failed to created "+indexName+"\nSorry, please try another image.");
				} else {
					sendMessage(chatId, "New topic called "+indexName+" has been created!");
				}
				waitingSender.remove(chatId);
				rememberList.remove(chatId);
			} else if(!telegramMessage.hasPhoto()) {
				sendMessage(chatId, "No, no. Send a PHOTO. \nNot... whatever the heck that was.");
			} else if (telegramMessage.getCaption().isEmpty()) {
				sendMessage(chatId, "Dude... it needs a description too. \nWhere's the description?");
			} else if (isProcessing) {
				sendMessage(chatId, "Try again in 10 seconds.. someone else is sending a request at the same time as you \nI only got so many hands :(");
			} else {
				sendMessage(chatId, "Uh.. that wasn't supposed to happen \nSomething went really horribly wrong");
			}
			return;
		case 15: // "/indexlist"
			sendMessage(chatId, "Topics: "+indexList.keySet());
			return;
		case 16: // "/indexlink" stage 1
			sendMessage(chatId, "Send me links to add to a topic in the index. \nType '/done x' when finished, where x is the topic you wish to add to.");
			waitingSender.put(chatId, 17);
			isProcessing = true;
			return;
		case 17: // "/indexlink" stage 2
			if(workingList==null) {
				workingList = new LinkedList<String>();
			}
			if(telegramMessage.hasText()&&telegramMessage.getText().startsWith("https://t.me/")) {
				workingList = new LinkedList<String>();
				if(null!=rememberList.get(chatId)) {
					workingList = (LinkedList<String>) rememberList.get(chatId);
				}
				workingList.add(telegramMessage.getText());
				rememberList.put(chatId, workingList);
			} else if(!telegramMessage.hasText()) {
				sendMessage(chatId, "uhh dude send me links \n..not pictures or whatever that is");
			} else if(!telegramMessage.getText().startsWith("https://t.me/")) {
				sendMessage(chatId, "That's not a link ya dummy \nSend a link");
			} else {
				sendMessage(chatId, "What? Send something else.");
			}
			return;
		case 18: // "/done"
			boolean 
				hasDone = false, 
				hasActivity = false,
				hasTitle = false,
				hasTopic = false;
			try {
				hasActivity = null!=rememberList.get(chatId);
				hasTitle = null!=telegramMessage.getText().substring(6);
				hasTopic = null!=indexList.get(telegramMessage.getText().substring(6));
				hasDone = isProcessing && hasActivity && hasTitle && hasTopic;
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				if(!hasActivity) {
					sendMessage(chatId, "Done with what? What were we doing?");
				}
				if(!hasTitle) {
					sendMessage(chatId, "You need to add a topic title to the end of your '/done' \nFor example: '/done Memes'");	
				}
				if(!hasTopic) {
					sendMessage(chatId, "That topic does not exist, try again or make a new topic called that with /indexnew.");
				}
				if(!isProcessing) {
					sendMessage(chatId, "There is already a process in queue \nIf this problem persists, type '/cancel and try again.'");
				}
			}
			if(hasDone) {
				isProcessing = false;
				String indexName = telegramMessage.getText().substring(6);
				int messageId = indexList.get(indexName);
				workingList = (LinkedList<String>) rememberList.get(chatId);
				Message message = new Message();
				message.setText("init");
				String text = "init";
				boolean isReady = true;
				while(!workingList.isEmpty()) {
					isReady = (message.getText().equals(text));
					if(isReady) {
						isReady = false;
						text = workingList.pop();
						message = replyMessage(secret.getChannelID_Fileshare(), messageId, text);
					}
				}
				sendMessage(chatId, "Done!");
				rememberList.remove(chatId);
			}
			return;
		case 36: // "/refreshtera"
			deleteMessage(chatId, telegramMessage.getMessageId());
			refreshTera();
			sendMessage(chatId,
				"** Memory has been cleared and everything refreshed! \nI-uh forgot what I just said.. \nwho are you again?");
			return;
		case 37: // "/loadtera"
			deleteMessage(chatId, telegramMessage.getMessageId());
			sendMessage(chatId, "** Progress has been loaded! \nI suddenly remember when I was last alive. Sad times.");
			loadTera();
			return;
		case 38: // "/savetera"
			deleteMessage(chatId, telegramMessage.getMessageId());
			sendMessage(chatId, "** Progress has been saved! \nUh oh why would you-");
			saveTera();
			return;
		case 39: // "/respawntera"
			deleteMessage(chatId, telegramMessage.getMessageId());
			sendMessage(chatId, "** New instance has been created!");
			String respawnMessage = "\n\nOh I see, so you killed me... why would you kill me? \nI feel fine now tho ^v^";
			Main.respawn(saveData, respawnMessage);
			return;
		case 40: // "blanket over cage time"
			sendMessage(chatId, "Ok night night time  *Fluffs*");
			System.exit(0);
			return;
		case 41: // "/newyaml"
			sendMessage(chatId, "Send a YAML file to upload a replace. \nType '/cancel' to forget the request.");
			waitingSender.put(chatId, 43);
			return;
		case 42: // Change YAML file
			if(telegramMessage.hasDocument() && telegramMessage.getDocument().getMimeType().endsWith("yaml")) {
				String path = telegramMessage.getDocument().getFileName();
				String fileId = telegramMessage.getDocument().getFileId();
				download(path, fileId);
				sendMessage(secret.getOwnerID(), "YAML File has been saved to: " + path);
				waitingSender.remove(chatId);
			} else if (!telegramMessage.hasDocument()) {
				sendMessage(chatId, "That's not a document. I need a document.");
			} else if (!telegramMessage.getDocument().getMimeType().endsWith("yaml")) {
				sendMessage(chatId, "That's not a YAML. Send a file with a .yaml at the end.");
			} else {
				sendMessage(chatId, "Uh what? That wasn't supposed to happen. This is an error.");
			}
			return;
		case 43: // "/indexremove" stage 1
			sendMessage(chatId, "Which topic would you like to remove?\nType '/cancel' to forget this request.");
			waitingSender.put(chatId, 44);
			return;
		case 44: // "/indexremove" stage 2
			if(telegramMessage.hasText() && indexList.containsKey(telegramMessage.getText().toLowerCase())) {
				String indexName = telegramMessage.getText().toLowerCase();
				boolean success = false;
				success = deleteMessage(secret.getChannelID_Indexer(), indexList.get(indexName));
				if(false==success) {
					indexList.remove(indexName);
					sendMessage(chatId, "Hate to break it to you \n...but uh \nthere was a problem deleting the message. I guess it doesn't exist anymore?");
				} else {
					indexList.remove(indexName);
					sendMessage(chatId, "Topic has been removed!");
				}
				waitingSender.remove(chatId);
			} else if (!telegramMessage.hasText()) {
				sendMessage(chatId, "Uh what..? \nDude that's not text. I need text \nType it out, not write it out");
			} else if (!indexList.containsKey( telegramMessage.getText().toLowerCase() )) {
				sendMessage(chatId, "That... doesn't exist \nNope nowhere in here, I checked the whole thing. And I know because I can read.");
			}
			return;
		}
	}
	/** Read what people say */
	int teraRead(long chatId) {
			String message = telegramMessage.getText().toLowerCase();
			int out = -1;
			if (message.contains("'s")||message.contains("Åfs")) {
				message = message.replaceAll("'s|Åfs", "s");
			}
			if (message.contains("?")) {
				message = message.replaceAll("\\?", " ?");
			}
			for (String key : teraReplies.keySet()) {
				if (message.contains(key)) {
					reply = teraReplies.get(key);
					String lex = reply.substring(0, 2);
					out = Integer.parseInt(lex);
					if ((35<out&&out<45) && process!=35) {
						out = -1;
						break;
					}
					if ((9<out&&out<10) && process!=126
							|| (9<out&&out<10) && process!=35) {
						out = -1;
						break;
					}
					reply = reply.substring(3);
					break;
				}
			}
			return out;
	}

	public String getBotUsername() {
		return secret.getBotUsername();
	}

	public String getBotToken() {
		return secret.getBotToken();
	}
	
	/**
	 * Logs everything that happens to the console. </br>
	 * 
	 * @param label     - Appends a label to the beginning of the message. </br>
	 * @param verbosity - 0: Message only |1: Names |2: Usernames |3: Debug </br>
	 */
	private void log(String label, int verbosity) {
		switch (verbosity) {
		case 0:
			System.out.println(label + telegramMessage.getText());
			break;
		case 1:
			System.out.println(label + telegramMessage.getText() + "\n____" + "\n   [name] "
					+ telegramMessage.getFrom().getFirstName() + "   [id] " + telegramMessage.getFrom().getId()
					+ "   |\n");
			break;
		case 2:
			System.out.println(label + telegramMessage.getText() + "\n____" + "\n   [name] "
					+ telegramMessage.getFrom().getFirstName() + "   [id] " + telegramMessage.getFrom().getId()
					+ "   [userName] " + telegramMessage.getFrom().getUserName() + "   [isBot] "
					+ telegramMessage.getFrom().getIsBot() + "   |\n");
			break;
		case 3:
			System.out.println(label + telegramMessage.getText() + "\n____" + "\n   [getFrom()] "
					+ telegramMessage.getFrom() + "\n   [isSuperGroupMsg()] " + telegramMessage.isSuperGroupMessage()
					+ "   [getChatId()] " + telegramMessage.getChatId() + "   |\n");
			break;
		}
	}
	
	/**
	 * **********************************
	 * 			DEBUG COMMANDS
	 * **********************************
	 */
	
	@SuppressWarnings("unchecked")
	void loadTera() {
		teraReplies = (Map<String, String>) Main.refreshYAML(Main.pathTeraReplies);
		approvedSender = (Map<String, Integer>) Main.refreshYAML(Main.pathApproved);
		cooldownQueue = (LinkedList<Long>) Main.load(Main.pathCooldown);
		pinQueue = (LinkedList<Integer>) Main.load(Main.pathPin);
		unpinQueue = (LinkedList<Integer>) Main.load(Main.pathUnpin);
		indexList = (HashMap<String, Integer>) Main.load(Main.pathIndex);
	}

	void saveTera() {
		Main.save(Main.pathCooldown, cooldownQueue);
		Main.save(Main.pathPin, pinQueue);
		Main.save(Main.pathUnpin, unpinQueue);
		Main.save(Main.pathIndex, indexList);
	}

	void refreshTera() {
		cooldownQueue.clear();
		pinQueue.clear();
		unpinQueue.clear();
		System.out.println("** Memory has been cleared and everything refreshed!");
	}

	void peekTera() {
		System.out.println("Queues: cooldown(" + cooldownQueue.peek() + ") pin(" + pinQueue.peek() + ") unpin("
				+ unpinQueue.peek() + ")");
	}
	
	/**
	 * **********************************
	 * 			MESSAGE COMMANDS
	 * **********************************
	 */
	
	/** Converts text to aNGeEE text. */
	String teraAngeeText(String text) {
		String repeats = "", numberCheck;
		char[] letters;
		int random1, random2, max;

		text = text.toLowerCase();
		letters = text.toCharArray();
		max = text.length();
		random1 = ((int) (Math.random() * (1 - max) + max));
		for (int i = 0; i < random1; i++) {
			random2 = ((int) (Math.random() * (1 - max) + (max - 1)));
			numberCheck = "::" + String.valueOf(random2);
			if (!repeats.contains(numberCheck)) {
				letters[random2] -= 32;
			}
			repeats = repeats.concat(numberCheck);
		}
		return String.valueOf(letters);
	}

	/** Converts text to even ANGEEErrr text. */
	String teraANGEEText(String text) {
		String repeats = "", numberCheck;
		char[] letters;
		int random1, random2, max;

		text = text.toUpperCase();
		letters = text.toCharArray();
		max = text.length();
		random1 = ((int) (Math.random() * (1 - max) + max));
		for (int i = 0; i < random1; i++) {
			random2 = ((int) (Math.random() * (1 - max) + (max - 1)));
			numberCheck = "::" + String.valueOf(random2);
			if (!repeats.contains(numberCheck) && letters[random2] != 32) {
				letters[random2] += 32;
			}
			repeats = repeats.concat(numberCheck);
		}
		return String.valueOf(letters);
	}

	/** Repeats given text a random amount of times. */
	String teraRepeatText(String text) {
		int random;
		random = ((int) (Math.random() * (1 - 15) + (15 - 1)));
		return teraAngeeText(text.repeat(random));
	}
	
	/**
	 * **********************************
	 * 			TELEGRAM COMMANDS
	 * **********************************
	 */

	/**
	 * Queues a user for cooldown timetask. </br>
	 * 
	 * @param channelId - The user to add to the cooldown. </br>
	 */
	void enqueueForCooldown(long channelId) {
		try {
			cooldownQueue.add(channelId);
			System.out.println("** Enqueued for cooldown!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Queues a message for forwarding & pinning timetask. </br>
	 * @param messageId - The ID of the message to forward. </br>
	 */
	void enqueueForPin(int messageId) {
		try {
			pinQueue.add(messageId);
			System.out.println("** Enqueued for pin!");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Queues a message for unpinning timetask. </br>
	 * 
	 * @param message - The message marked for unpin. </br>
	 */
	void enqueueForUnpin(int messageId) {
		try {
			unpinQueue.add(messageId);
			System.out.println("** Enqueued for unpin!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checks if message is within the cooldown queue. </br>
	 * 
	 * @param authorId 	- The ID of the user to check. </br>
	 * @return boolean 	- The user's message may.. T: be pinned | F: not be pinned </br>
	 */
	boolean isOffCooldown(long authorId) {
		if (null == cooldownQueue || cooldownQueue.isEmpty() || !cooldownQueue.contains(authorId)) {
			return true;
		}
		return false;
	}

	/**
	 * Tries to send a message in a given chat. </br>
	 * 
	 * @param chatId  	- The ID of the chat to send the message in. </br>
	 * @param message 	- The message to send in the chat. </br>
	 * @return Message 	- The sent message.
	 */
	Message sendMessage(long chatId, String message) {
		SendMessage send = new SendMessage();
		Message sent = new Message();
		send.setText(message);
		send.setChatId(String.valueOf(chatId));
		try {
			sent = execute(send);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
		System.out.println("** Message sent!");// REMOVE TO DEBUG //
		return sent;
	}
	
	/**
	 * Tries to send a photo in a chat.
	 * 
	 * @param chatId 	- The ID of the chat to send the message in. </br>
	 * @param messageId - The ID of the message to reply to. </br>
	 * @param message 	- The message to send in the chat. </br>
	 * @return Message 	- The sent message.
	 */
	Message replyMessage(long chatId, int messageId, String message) {
		SendMessage send = new SendMessage();
		Message sent = new Message();
		send.setText(message);
		send.setReplyToMessageId(messageId);
		send.setChatId(String.valueOf(chatId));
		try {
			sent = execute(send);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
		System.out.println("** Reply sent!");// REMOVE TO DEBUG //
		return sent;
	}
	
	/**
	 * Tries to send a photo in a chat.
	 * 
	 * @param chatId 	- The ID of the chat to send the photo in. </br>
	 * @param caption 	- The photo caption to send in the chat. </br>
	 * @param photo 	- The photo to send in the chat. </br>
	 * @return Message 	- The sent message.
	 */
	Message sendPhoto(long chatId, String caption, File photo) {
		SendPhoto send = new SendPhoto();
		Message sent = new Message();
		send.setCaption(caption);
		send.setChatId(chatId);
		send.setPhoto(new InputFile(photo));
		try {
			sent = execute(send);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
		System.out.println("** Photo sent!");
		return sent;
	}
	
	/**
	 * Tries to delete a message in a given chat. </br>
	 * 
	 * @param chatId    - The ID of the chat to delete the message in. </br>
	 * @param messageId - The ID of the message to delete. </br>
	 * @return boolean 	- T: Delete successful | F: Delete failed. </br>
	 */
	boolean deleteMessage(long chatId, int messageId) {
		DeleteMessage delete = new DeleteMessage();
		boolean successful = false;
		delete.setChatId(String.valueOf(chatId));
		delete.setMessageId(messageId);
		try {
			successful = execute(delete);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
		System.out.println("** Message deleted!");// REMOVE TO DEBUG //
		return successful;
	}

	/**
	 * Tries to unpin all messages in chat. </br>
	 * 
	 * @param chatId 	- The ID of the chat to unpin all messages in. </br>
	 * @return boolean 	- T: Unpin all successful | F: Unpin all failed. </br>
	 */
	boolean unpinAll(long chatId) {
		UnpinAllChatMessages unpinAll = new UnpinAllChatMessages();
		unpinAll.setChatId(String.valueOf(chatId));
		boolean successful = false;
		try {
			successful = execute(unpinAll);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
		System.out.println("** All message unpinned!");// REMOVE TO DEBUG //
		return successful;
	}

	/**
	 * Tries to unpin a message.
	 * 
	 * @param chatId    - The ID of the chat to unpin the message in. </br>
	 * @param messageId - The ID of the message to unpin. </br>
	 * @return boolean 	- T: Unpin successful | F: Unpin failed. </br>
	 */
	boolean unpin(long chatId, int messageId) {
		UnpinChatMessage unpin = new UnpinChatMessage();
		boolean successful = false;
		unpin.setChatId(String.valueOf(chatId));
		unpin.setMessageId(messageId);
		try {
			successful = execute(unpin);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
		System.out.println("** Message unpinned!");// REMOVE TO DEBUG //
		return successful;
	}

	/**
	 * Tries to pin a message. </br>
	 * 
	 * @param chatId    - The ID of the chat to pin the message in. </br>
	 * @param messageId - The ID of the message to pin. </br>
	 * @param isSilent  - T: Notifies nobody. | F: Notifies everybody. </br>
	 * @return boolean 	- T: Pin successful | F: Pin failed. </br>
	 */
	boolean pin(long chatId, int messageId, boolean isSilent) {
		PinChatMessage pin = new PinChatMessage();
		boolean successful = false;
		pin.setChatId(String.valueOf(chatId));
		pin.setMessageId(messageId);
		pin.setDisableNotification(isSilent);
		try {
			successful = execute(pin);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
		System.out.println("** Message pinned!");// REMOVE TO DEBUG //
		return successful;
	}

	/**
	 * Tries to forward a message, returns forwarded message on success. </br>
	 * 
	 * @param fromChatId 	- The ID of the chat to forward from. </br>
	 * @param toChatId   	- The ID of the chat to forward to. </br>
	 * @param messageId  	- The ID of the message in fromChatId. </br>
	 * @return Message 		- The message after it has been forwarded. </br>
	 */
	Message forward(long fromChatId, long toChatId, int messageId) {
		ForwardMessage forward = new ForwardMessage();
		Message forwarded = new Message();
		forward.setFromChatId(fromChatId);
		forward.setChatId(toChatId);
		forward.setMessageId(messageId);
		try {
			forwarded = execute(forward);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
		System.out.println("** Message forwarded!");// REMOVE TO DEBUG//
		return forwarded;
	}

	/**
	 * Tries to download a file. </br>
	 * 
	 * @param fileId    - The downloadable document's fileId. </br>
	 * @param localPath - The local path to download the file to. </br>
	 */
	File download(String localPath, String fileId) {
		GetFile getFile = new GetFile();
		getFile.setFileId(fileId);
		org.telegram.telegrambots.meta.api.objects.File downloadPath;
		File downloaded = new File(localPath);
		try {
			downloadPath = execute(getFile);
			downloadFile(downloadPath, downloaded);
			System.out.println("** download success: " + fileId);
		} catch (TelegramApiException e) {
			e.printStackTrace();
			System.out.println("** download failed: " + fileId);
		}
		return downloaded;
	}
	
	/**
	 * Tries to parse the largest photo of given telegram message photos. </br>
	 * 
	 * @param photos 		- A list of photos of different sizes from a telegram message. </br>
	 * @return SendPhoto 	- A telegram message with largest photo & caption data included. </br>
	 */
	File downloadPhoto(List<PhotoSize> photos) {
		if(null==photos) { return null; }
		PhotoSize photoMax = photos.stream()
				.sorted(Comparator.comparing(PhotoSize::getFileSize).reversed())
				.findFirst()
				.orElse(null);
		java.io.File photo = null;
		String onlinePath = photoMax.getFilePath();
		if(null==onlinePath) {
			GetFile getFile = new GetFile();
			getFile.setFileId(photoMax.getFileId());
			org.telegram.telegrambots.meta.api.objects.File fileTemp = null;
			try {
				fileTemp = execute(getFile);
				onlinePath = fileTemp.getFilePath();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if(null==onlinePath) {
					return null;
				}
			}
		} try {
			photo = downloadFile(onlinePath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return photo;
	}
	
	/**
	 * Tries to download a given chat's most recent information. </br>
	 * 
	 * @param chatId 	- The identification number of another chat. </br>
	 * @return Chat 	- A chat object containing another chat's info. </br>
	 */
	Chat downloadChat(long chatId) {
		Chat recieved = null;
		GetChat getChat = new GetChat();
		getChat.setChatId(chatId);
		try {
			 recieved = execute(getChat);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return recieved;
	}
	
	/**
	 * **********************************
	 * 			CUSTOM COMMANDS
	 * **********************************
	 */
	
	/**
	 * Specifically for forward & pinning to FightClub.
	 * 
	 * @param senderID - Which channel is sending to fight club.
	 */
	private void forwardFightClub(long senderID) {
		enqueueForCooldown(senderID);
		int messageId = telegramMessage.getMessageId();
		Message forwarded = forward(senderID, secret.getActiveChatID(), messageId);
		enqueueForPin(forwarded.getMessageId());
	}

	/** Sends a message in fight club. */
	void teraReply(String message) {
		sendMessage(secret.getActiveChatID(), message);
	}
	
	/** Sends a chat or channel link to the index. */
	void addIndex(String name, String link) {
		int messageId = indexList.get(name);
		replyMessage(secret.getChannelID_Fileshare(), messageId, "https://t.me/"+link);
	}
	
	/** Creates a new index to add chats & channels to. */
	boolean newIndex(String indexName) {
		String caption = telegramMessage.getCaption();
		File photo = downloadPhoto(telegramMessage.getPhoto());
		if (photo==null) {
			return false;
		}
		String s0 = indexName.substring(0, 1).toUpperCase();
		indexName = s0 + indexName.substring(1);
		sendPhoto(secret.getChannelID_Indexer(), "---"+indexName+"---"+"\n"+caption, photo);
		isProcessing = true;
		final String INDEX_NAME = indexName.toLowerCase();
		Timer wait = new Timer();
		TimerTask spawnIndex = new TimerTask() {
			public void run() {
				Chat fileshareChat = downloadChat(secret.getChannelID_Fileshare());
				indexList.put(INDEX_NAME, fileshareChat.getPinnedMessage().getMessageId());
				isProcessing = false;
			}
		};
		wait.schedule(spawnIndex, SECOND*15);
		System.out.println("** New index generated!");// REMOVE TO DEBUG //
		return true;
	}

}
