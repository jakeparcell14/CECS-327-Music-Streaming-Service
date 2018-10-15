package application;
import java.net.InetAddress;

public class Message {
	/**
	 * Message type, 0 = request, 1 = reply
	 */
	private int messageType;
	
	/**
	 * specific ID for this request.
	 */
	private int requestID;
	
	/**
	 * Address of the sender, to be used to return data to client.
	 */
	private InetAddress address;
	
	/**
	 * Enumerated ID that indicates which operation/method to execute.
	 */
	private OpID operationID;
	
	/**
	 * String of arguments
	 */
	private String[] args;
	
	/**
	 * Type of protocol to be used.
	 */
	private int protocolID;
	
	public Message() {
		
	}
	
	public Message(int messageType, int requestID, OpID opid, String[] args, InetAddress address, int protocolID) {
		this.messageType = messageType;
		this.requestID = requestID;
		this.operationID = opid;
		this.args = args;
		this.protocolID = protocolID;
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

	public int getProtocolID() {
		return protocolID;
	}

	public InetAddress getAddress() {
		return address;
	}
	public String toString() {
		String temp= messageType+" "+requestID+" "+operationID+" ";
		for(int i=0;i<args.length;i++) {
			temp=temp+" "+args[i];
		}
		temp=temp+" "+protocolID+" "+address;
		return temp;
	}
	
	
	
	
}