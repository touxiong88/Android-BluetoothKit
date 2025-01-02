package com.faytech.bluetooth.library.channel;

public interface IChannel {

	void write(final byte[] bytes, ChannelCallback callback);
	void onRead(final byte[] bytes);
	void onRecv(byte[] bytes);
	void send(byte[] value, ChannelCallback callback);
}
