package com.github.terasscriptnest;

public interface Sensitive_Data
{
  /**
   * Pin a message in a chat permanently.
   * Will always repin when unpinned.
   * 
   * @return An integer type number as a message ID.
   */
  Integer getPermaPinnedMessageID();
  
  /**
   * The exact instance that this bot is running as.
   * 
   * @return A Long type number as the user ID of this bot.
   */
  Long getBotID();
  
  /**
   * The telegram chatroom ID that you want this bot to operate in.
   * 
   * @return A Long type number as a chatroom ID.
   */
  Long getActiveChatID();
  
  /**
   * The user ID of the owner who is running this bot.
   * 
   * @return A Long type number as a user ID of the owner.
   */
  Long getOwnerID();
  
  /**
   * The telegram channel ID you want this bot to regulate for anonymous chatting.
   * 
   * @return A Long type number as a channel ID.
   */
  Long getChannelID_Anonymous();
  
  /**
   * The telegram channel ID you want this bot to regulate for group link storage (i.e., indexing).
   * 
   * @return A Long type number as a channel ID.
   */
  Long getChannelID_Indexer();
  
  /**
   * The telegram channel ID you want this bot to regulate for file sharing.
   * 
   * @return A Long type number as a channel ID.
   */
  Long getChannelID_Fileshare();
  
  /**
   * KEEP SECRET!!
   * The special token of this exact bot.
   * 
   * @return A String of this bot's exact control ID for the bot father.
   */
  String getBotToken();
  
  /**
   * Should be whatever exact '@username' you set the bot to in the bot father.
   * 
   * @return A String of this bot's exact username assigned to it.
   */
  String getBotUsername();
}
