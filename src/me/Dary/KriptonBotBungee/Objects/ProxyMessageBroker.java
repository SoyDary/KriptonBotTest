package me.Dary.KriptonBotBungee.Objects;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.BiConsumer;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import net.md_5.bungee.api.config.ServerInfo;

public class ProxyMessageBroker {
	
	private final Map<String, Deque<BiConsumer<Long, String>>> callbacks = new HashMap<>();
	
	public void request(ServerInfo server, String channel ,BiConsumer<Long, String> callback, String... args) {
		callbacks.computeIfAbsent(channel, key -> new ArrayDeque<>()).add(callback);
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		for (String arg : args) {
			out.writeUTF(arg);
		}
		long id = new Random().nextLong();
		out.writeLong(id);
		server.sendData(channel, out.toByteArray());;
	}

	public void consume(String channel, Long id, String message) {
		Deque<BiConsumer<Long, String>> callbackQueue = callbacks.computeIfAbsent(channel, key -> new ArrayDeque<>());
		if (!callbackQueue.isEmpty()) {
			callbackQueue.poll().accept(id, message);
		}
	}

}
