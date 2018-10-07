package application;
import java.net.InetAddress;

public class Message {
	private int messageType;
	private int requestID;
	private InetAddress address;
	private OpID operationID;
	private String[] args;
//	private int protocolID;
	
	public Message() {
		
	}
	
	public Message(int messageType, int requestID, OpID opid, String[] args, InetAddress address) {
		this.messageType = messageType;
		this.requestID = requestID;
		this.operationID = opid;
		this.args = args;
//		this.protocolID = protocolID;
		this.address = address;
	}
	
	public int getMessageType() {
		return messageType;
	}
	
	public int getRequestID() {
		return requestID;
	}
	
	
	public OpID getOperationID() {
		return operationID;
	}
	
	public String[] getArgs() {
		return args;
	}

//	public int getProtocolID() {
//		return protocolID;
//	}

	public InetAddress getAddress() {
		return address;
	}
	
	
	
	
}