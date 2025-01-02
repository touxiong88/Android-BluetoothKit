package com.faytech.bluetooth.library.channel;

public enum ChannelState {

	IDLE,
	READY,
	WAIT_START_ACK,
	WRITING,
	SYNC,
	SYNC_ACK,
	SYNC_WAIT_PACKET,
	READING,
}
