package com;

import com.pojo.Item;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.time.ZonedDateTime;
import java.util.List;

public class Telegram {

    private static TelegramBot bot;

    public static void init() {
        try {
            bot = new TelegramBot();
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private static String chatId = "-1001651509125";

    public static void sendMessages(List<Item> items) {
        for (Item item : items) {
            String message = item.title + " \n" + item.link;
            sendMessage(message);
        }
    }

    public static void sendMessage(String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);
        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private static class TelegramBot extends TelegramLongPollingBot {

        @Override
        public void onUpdateReceived(Update update) {

//            SendMessage message = new SendMessage();
//            message.setChatId(update.getMessage().getChatId().toString());
//            message.setText("Чакаракафон.");
//            try {
//                execute(message);
//            } catch (TelegramApiException e) {
//                e.printStackTrace();
//            }
        }


        @Override
        public String getBotUsername() {
            return "HAL";
        }

        @Override
        public String getBotToken() {
            return "5321370449:AAEjOhmwlRFijwkf28w_zqqbALapQB0PlxE";
        }
    }
}
