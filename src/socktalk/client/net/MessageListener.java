package socktalk.client.net;

import socktalk.common.Message;

public interface MessageListener {
	void onMessage(Message message);
	void onDisconnected();
}
