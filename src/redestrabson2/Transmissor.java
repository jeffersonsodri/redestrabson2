package redestrabson2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Transmissor
{	
	public static String nomeProtocolo;
	public static int numBitsEmSeq;
	public static int tamanhoJanela;
	public static int valorDoTimeout;
	public static int tamanhoMaximoSeguimento;
	public static int pacoteDuplicado;
	public static int totalNumPacotes;
	public static double probabilidadeErroBit;
	public static double probabilidadeAckPerdido = 0.1;
	
	public static void main(String args[]) throws Exception
	{
		
		//Inicializacao e recebimento dos parametros do arquivo para o programa
		String arquivo = (System.getProperty("user.dir")).toString() + args[0];
		//List<String> parametrosAlgoritmo = new ArrayList<String>();
		File f = new File(arquivo);
		BufferedReader br = new BufferedReader(new FileReader(f));
		//String line = null;
		
		
		/**
		while((line = br.readLine()) != null) {
			parametrosAlgoritmo.add(line);
		}*/
		InetAddress enderecoIP = InetAddress.getByName(args[1]); // pega endere�o
		int portNumber = Integer.parseInt(args[2]);// porta
		//int tamanhoJanela = Integer.parseInt(args[3]);
		
		br.close();
		
		int highestAckRcvd = 1;
		List<Integer> srAcks = new ArrayList<Integer>();
		
		//nomeProtocolo = parametrosAlgoritmo.get(0).trim();
		//numBitsEmSeq = Integer.parseInt(parametrosAlgoritmo.get(1).trim());
		tamanhoJanela = Integer.parseInt(args[3]);
		valorDoTimeout = Integer.parseInt(args[4]);
		tamanhoMaximoSeguimento = Integer.parseInt(args[5]);
		pacoteDuplicado = Integer.parseInt(args[6]);
		probabilidadeErroBit = Integer.parseInt(args[7]);
		
		totalNumPacotes = -1; // tem que concertar isso aq
		
		Dado.settamanhoJanela(tamanhoJanela);
		
		DatagramSocket clientSocket = new DatagramSocket();
		
		
		byte[] dadoEnviado = new byte[1024];
		byte[] dadoRecebido = new byte[1024];
		
		int base = 1;
		int proximoNumSequencia = 1;
		int i = 1;
		ArrayList<Pacote> pacotesEnviados = new ArrayList<Pacote>();
		
		while (i <= totalNumPacotes) {
			try {
				if (proximoNumSequencia - base < tamanhoJanela) {
					
					Pacote pacote = Dado.makePacket(proximoNumSequencia);
					
					/**
					 * Enviando alguns pacotes como alguma probabilidade de erro no bit causando erro no bit 0.1.
					 * Uma vez a cada 10 pacotes tem um bit error.
					 */
					if(Math.random() < probabilidadeErroBit) {
						String errorData = new String(pacote.getDado());
						errorData = Dado.changeBit(errorData);
						pacote.setDado(errorData.getBytes());
					}
					
					//Convertendo o objeto Pacote para Bytes para enviar ao receptor 
					dadoEnviado = Dado.toBytes(pacote);
					DatagramPacket pacoteDatagramEnviado = new DatagramPacket(dadoEnviado, dadoEnviado.length, enderecoIP, portNumber);
					
					// Imprimindo informacao do pacote
					int seqNumero = -1;
					if (nomeProtocolo.equalsIgnoreCase("GBN")) {
						if (i > tamanhoJanela + 1) {
							seqNumero = pacote.getNumeroDeSequencia() % (tamanhoJanela + 1);
						} else {
							seqNumero = pacote.getNumeroDeSequencia();
						}
					} else {
						if (i > tamanhoJanela * 2) {
							seqNumero = pacote.getNumeroDeSequencia() % (tamanhoJanela * 2);
						} else {
							seqNumero = pacote.getNumeroDeSequencia();
						}
					}
					
					System.out.println("PACOTE ENVIADO NUMERO: " + i + " SEQUENCIA NUMERO: " + seqNumero);
					
					// Envio para socket do receptor
					clientSocket.send(pacoteDatagramEnviado);
					
					if (base == proximoNumSequencia) {
						clientSocket.setSoTimeout(valorDoTimeout);
					}
					
					// Adicao do pacote para a lista de envio
					pacotesEnviados.add(pacote);
					
					// Incremento do numero de sequencia
					proximoNumSequencia++;
					i++;
				}
				
				
				// Recebendo recomenhecimento do pacote
				DatagramPacket receivePacket = new DatagramPacket(dadoRecebido, dadoRecebido.length);
				clientSocket.receive(receivePacket);
				
				String resposta = new String(receivePacket.getData());
				if (!(resposta.contains("PACOTE PERDIDO"))) {
		
					// Simulando ACK perdido com probabilidade 0.05
					if (Math.random() < probabilidadeAckPerdido) {
						System.out.println("***ACK PERDIDO***");
					} else {
						
						// Convertendo para o display
						Reconhecimento ack = (Reconhecimento) Dado.toObject(receivePacket.getData());
						int ackNo;
						if (nomeProtocolo.equalsIgnoreCase("GBN")) {
							if (ack.getnumeroAck() > (tamanhoJanela + 1)) {
								ackNo = ack.getnumeroAck() % (tamanhoJanela + 1);
							} else {
								ackNo = ack.getnumeroAck();
							}
							System.out.println("ACK RECEBIDO: " + ackNo);
							System.out.println("\n");
							
							if (ack.getnumeroAck() == base) {
								base = ack.getnumeroAck() + 1;
							}
						} else {
							if (ack.getnumeroAck() > (tamanhoJanela * 2)) {
								ackNo = ack.getnumeroAck() % (tamanhoJanela * 2);
							} else {
								ackNo = ack.getnumeroAck();
							}
							System.out.println("ACK RECEBIDO: " + ackNo);
							System.out.println("\n");
							
							srAcks.add(ack.getnumeroAck());
							if (ack.getnumeroAck() > highestAckRcvd) {
								highestAckRcvd = ack.getnumeroAck();
							}
							Collections.sort(srAcks);
							int flag = 0;
							for (int b = base - 1; b < srAcks.size(); b++) {
								if (srAcks.get(b) == b + 1) {
									flag = 1;
								}
							}
							
							// Incremento basico se o reconhecimento est� em ordem
							if (flag == 1) {
								base = highestAckRcvd + 1;
							}
						}
					}
				}
			} catch (Exception e) {
				
				if (nomeProtocolo.equalsIgnoreCase("GBN")) {
					System.out.println("Timeout. \n" + "Reenviando pacotes n�o reconhecidos da base para(proximoNumeroSequencia -1)...");
					int b = base;
					int ns = proximoNumSequencia - 1;
					for(int j = b-1 ; j < ns; j++) {
						dadoEnviado = Dado.toBytes(pacotesEnviados.get(j));
						DatagramPacket pacoteDatagramEnviado = new DatagramPacket(dadoEnviado, dadoEnviado.length, enderecoIP, portNumber);
						
						int seqNumero;
						if (pacotesEnviados.get(j).getNumeroDeSequencia() > tamanhoJanela + 1) {
							seqNumero = pacotesEnviados.get(j).getNumeroDeSequencia() % (tamanhoJanela + 1);
						} else {
							seqNumero = pacotesEnviados.get(j).getNumeroDeSequencia();
						}
						System.out.println("RESENDING PACKET NUMBER: " + pacotesEnviados.get(j).getNumeroDeSequencia()  + " SEQ NO: " + seqNumero);
						
						// Envido para o socket do receptor
						clientSocket.send(pacoteDatagramEnviado);
						
						// Recebendo reconhecimento para o pacote
						DatagramPacket receivePacket = new DatagramPacket(dadoRecebido, dadoRecebido.length);
						clientSocket.receive(receivePacket);
						
						String resposta = new String(receivePacket.getData());
						if (!(resposta.contains("PACOTE PERDIDO"))) {
				
							// Simulando ACK perdido com probabilidade 0.05
							if (Math.random() < probabilidadeAckPerdido) {
								System.out.println("***ACK PERDIDO***");
							} else {
								
								// Convertendo para o display
								Reconhecimento ack = (Reconhecimento) Dado.toObject(receivePacket.getData());
								System.out.println("ACK RECEBIDO: " + ack.getnumeroAck());
								System.out.println("\n");
								
								if (ack.getnumeroAck() == base) {
									base = ack.getnumeroAck() + 1;
								}
							}
						}
					}
				} else {
					System.out.println("Timeout. Reenviar pacote nao negociado mais antigo...");
					dadoEnviado = Dado.toBytes(pacotesEnviados.get(base - 1));
					DatagramPacket pacoteDatagramEnviado = new DatagramPacket(dadoEnviado, dadoEnviado.length, enderecoIP, portNumber);
					
					int seqNumero;
					if (pacotesEnviados.get(base - 1).getNumeroDeSequencia() > tamanhoJanela * 2) {
						seqNumero = pacotesEnviados.get(base - 1).getNumeroDeSequencia() % (tamanhoJanela * 2);
					} else {
						seqNumero = pacotesEnviados.get(base - 1).getNumeroDeSequencia();
					}
					System.out.println("RESENDING PACKET NUMBER: " + pacotesEnviados.get(base - 1).getNumeroDeSequencia()  + " SEQ NO: " + seqNumero);
					
					// Envido para o socket do receptor
					clientSocket.send(pacoteDatagramEnviado);
				}
			}
		} 
		clientSocket.close();
	}
}