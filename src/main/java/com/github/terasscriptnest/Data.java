package com.github.terasscriptnest;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class Data implements Serializable {
	/**
	 * This is the bot's save data.
	 */
	private static final long serialVersionUID = 4501894818863266218L;
	Map<String, String> teraReplies; // Holds response data
	Map<String, Integer> approvedSender; // List of approved users
	HashMap<String, Integer> indexList; // List of currently saved indices.
	Queue<Long> cooldownQueue; // Queue for next pinQueue entry.
	Queue<Integer> pinQueue; // Queue for next message pin.
	Queue<Integer> unpinQueue; // Queue for next message unpin.
}
