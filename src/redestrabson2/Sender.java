package redestrabson2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Sender
{	
	public static String protocolName;
	public static int noOfBitsInSeq;
	public static int windowSize;
	public static int timeOutValue;
	public static int maxSegmentSize;
	public static int duplicatePack;
	public static int totalNoOfPackets;
	public static double bitErrorProbability;
	public static double lossAckProbability = 0.1;
	
	public static void main(String args[]) throws Exception
	{
		
		// Initialization and retrieving algorithm parameters from file
		String filePath = (System.getProperty("user.dir")).toString() + args[0];
		//List<String> algorithmParameters = new ArrayList<String>();
		File f = new File(filePath);
		BufferedReader br = new BufferedReader(new FileReader(f));
		//String line = null;
		
		
		/**
		while((line = br.readLine()) != null) {
			algorithmParameters.add(line);
		}*/
		InetAddress IPAddress = InetAddress.getByName(args[1]); // pega endere�o
		int portNumber = Integer.parseInt(args[2]);// porta
		//int tamanhoJanela = Integer.parseInt(args[3]);
		
		br.close();
		
		int highestAckRcvd = 1;
		List<Integer> srAcks = new ArrayList<Integer>();
		
		//protocolName = algorithmParameters.get(0).trim();
		//noOfBitsInSeq = Integer.parseInt(algorithmParameters.get(1).trim());
		windowSize = Integer.parseInt(args[3]);
		timeOutValue = Integer.parseInt(args[4]);
		maxSegmentSize = Integer.parseInt(args[5]);
		duplicatePack = Integer.parseInt(args[6]);
		bitErrorProbability = Integer.parseInt(args[7]);
		
		totalNoOfPackets = -1; // tem que concertar isso aq
		
		Data.setWindowSize(windowSize);
		
		DatagramSocket clientSocket = new DatagramSocket();
		
		
		byte[] sendData = new byte[1024];
		byte[] receiveData = new byte[1024];
		
		int base = 1;
		int nextSequenceNumber = 1;
		int i = 1;
		ArrayList<Packet> sentPackets = new ArrayList<Packet>();
		
		while (i <= totalNoOfPackets) {
			try {
				if (nextSequenceNumber - base < windowSize) {
					
					Packet pkt = Data.makePacket(nextSequenceNumber);
					
					/**
					 * Sending few packets with some bit error with probability causing bit error is 0.1.
					 * One in every 10 packets has bit error
					 */
					if(Math.random() < bitErrorProbability) {
						String errorData = new String(pkt.getData());
						errorData = Data.changeBit(errorData);
						pkt.setData(errorData.getBytes());
					}
					
					// Converting the packet object to bytes to send it to receiver
					sendData = Data.toBytes(pkt);
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, portNumber);
					
					// Printing packet information
					int seqNo = -1;
					if (protocolName.equalsIgnoreCase("GBN")) {
						if (i > windowSize + 1) {
							seqNo = pkt.getSequenceNumber() % (windowSize + 1);
						} else {
							seqNo = pkt.getSequenceNumber();
						}
					} else {
						if (i > windowSize * 2) {
							seqNo = pkt.getSequenceNumber() % (windowSize * 2);
						} else {
							seqNo = pkt.getSequenceNumber();
						}
					}
					
					System.out.println("SENDING PACKET NUMBER: " + i + " SEQ NO: " + seqNo);
					
					// Send it to the receiver socket
					clientSocket.send(sendPacket);
					
					if (base == nextSequenceNumber) {
						clientSocket.setSoTimeout(timeOutValue);
					}
					
					// Add the packet to the sent list
					sentPackets.add(pkt);
					
					// Increment sequence number
					nextSequenceNumber++;
					i++;
				}
				
				
				// Receiving acknowledgement for the packet
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				clientSocket.receive(receivePacket);
				
				String response = new String(receivePacket.getData());
				if (!(response.contains("Packet Loss"))) {
		
					// Simulating ACK loss with probability 0.05
					if (Math.random() < lossAckProbability) {
						System.out.println("***ACK LOSS***");
					} else {
						
						// Converting it and displaying
						Acknowledgement ack = (Acknowledgement) Data.toObject(receivePacket.getData());
						int ackNo;
						if (protocolName.equalsIgnoreCase("GBN")) {
							if (ack.getAckNumber() > (windowSize + 1)) {
								ackNo = ack.getAckNumber() % (windowSize + 1);
							} else {
								ackNo = ack.getAckNumber();
							}
							System.out.println("RECEIVED ACK: " + ackNo);
							System.out.println("\n");
							
							if (ack.getAckNumber() == base) {
								base = ack.getAckNumber() + 1;
							}
						} else {
							if (ack.getAckNumber() > (windowSize * 2)) {
								ackNo = ack.getAckNumber() % (windowSize * 2);
							} else {
								ackNo = ack.getAckNumber();
							}
							System.out.println("RECEIVED ACK: " + ackNo);
							System.out.println("\n");
							
							srAcks.add(ack.getAckNumber());
							if (ack.getAckNumber() > highestAckRcvd) {
								highestAckRcvd = ack.getAckNumber();
							}
							Collections.sort(srAcks);
							int flag = 0;
							for (int b = base - 1; b < srAcks.size(); b++) {
								if (srAcks.get(b) == b + 1) {
									flag = 1;
								}
							}
							
							// Increment base if the Acknowledgement are in order
							if (flag == 1) {
								base = highestAckRcvd + 1;
							}
						}
					}
				}
			} catch (Exception e) {
				
				if (protocolName.equalsIgnoreCase("GBN")) {
					System.out.println("Timeout. Resending unacknowledged packets from base to (nextseqnum -1)...");
					int b = base;
					int ns = nextSequenceNumber - 1;
					for(int j = b-1 ; j < ns; j++) {
						sendData = Data.toBytes(sentPackets.get(j));
						DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, portNumber);
						
						int seqNo;
						if (sentPackets.get(j).getSequenceNumber() > windowSize + 1) {
							seqNo = sentPackets.get(j).getSequenceNumber() % (windowSize + 1);
						} else {
							seqNo = sentPackets.get(j).getSequenceNumber();
						}
						System.out.println("RESENDING PACKET NUMBER: " + sentPackets.get(j).getSequenceNumber()  + " SEQ NO: " + seqNo);
						
						// Send it to the receiver socket
						clientSocket.send(sendPacket);
						
						// Receiving acknowledgement for the packet
						DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
						clientSocket.receive(receivePacket);
						
						String response = new String(receivePacket.getData());
						if (!(response.contains("Packet Loss"))) {
				
							// Simulating ACK loss with probability 0.05
							if (Math.random() < lossAckProbability) {
								System.out.println("***ACK LOSS***");
							} else {
								
								// Converting it and displaying
								Acknowledgement ack = (Acknowledgement) Data.toObject(receivePacket.getData());
								System.out.println("RECEIVED ACK: " + ack.getAckNumber());
								System.out.println("\n");
								
								if (ack.getAckNumber() == base) {
									base = ack.getAckNumber() + 1;
								}
							}
						}
					}
				} else {
					// do for SR
					System.out.println("Timeout. Resending oldest unacked packet...");
					sendData = Data.toBytes(sentPackets.get(base - 1));
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, portNumber);
					
					int seqNo;
					if (sentPackets.get(base - 1).getSequenceNumber() > windowSize * 2) {
						seqNo = sentPackets.get(base - 1).getSequenceNumber() % (windowSize * 2);
					} else {
						seqNo = sentPackets.get(base - 1).getSequenceNumber();
					}
					System.out.println("RESENDING PACKET NUMBER: " + sentPackets.get(base - 1).getSequenceNumber()  + " SEQ NO: " + seqNo);
					
					// Send it to the receiver socket
					clientSocket.send(sendPacket);
				}
			}
		} 
		clientSocket.close();
	}
}