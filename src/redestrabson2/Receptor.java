package redestrabson2;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.*;
import java.util.ArrayList;

class Receptor
{
	public static double probabilidadePacotePerdido;
	public static String nomeProtocolo;
	public static int tamanhoJanela;
	
	public static void main(String args[]) throws Exception
	{		
		String arquivo = (System.getProperty("user.dir")).toString() + args[0];
		File f = new File(arquivo);
		BufferedReader br = new BufferedReader(new FileReader(f));
		nomeProtocolo = br.readLine();
		br.close();
		
		int numeroPorta = Integer.parseInt(args[1]);
		DatagramSocket serverSocket = new DatagramSocket(numeroPorta);
		tamanhoJanela = Integer.parseInt(args[2]);
		probabilidadePacotePerdido = Integer.parseInt(args[3]);
		byte[] dadoRecebido = new byte[1024];
		byte[] dadoEnviado = new byte[1024];
		int expectedAck = 1;
		ArrayList<Reconhecimento> sentAcks = new ArrayList<Reconhecimento>();
		
		while(true)
		{
			DatagramPacket receivePacket = new DatagramPacket(dadoRecebido, dadoRecebido.length);
			serverSocket.receive(receivePacket);
			Pacote rcvPacket = (Pacote) Dado.toObject(receivePacket.getData());
			
			/**
			 * Calculando o checksum do lado dos receptores
			 */
			String pacoteChecksum = Dado.sumData(new String(rcvPacket.getDado()));
			String resultCheckSum = Dado.sumData(pacoteChecksum + new String(rcvPacket.getChecksum()));
			boolean bitError = false;
			
			InetAddress IPAddress = receivePacket.getAddress();
			int port = receivePacket.getPort();
			
			if(Math.random() > probabilidadePacotePerdido) {
				
				int seqNumero = -1;
				if (nomeProtocolo.equalsIgnoreCase("GBN")) {
					if (rcvPacket.getNumeroDeSequencia() > rcvPacket.getTamanhoJanela() + 1) {
						seqNumero = rcvPacket.getNumeroDeSequencia() % (rcvPacket.getTamanhoJanela() + 1);
					} else {
						seqNumero = rcvPacket.getNumeroDeSequencia();
					}
				} else {
					if (rcvPacket.getNumeroDeSequencia() > rcvPacket.getTamanhoJanela() * 2) {
						seqNumero = rcvPacket.getNumeroDeSequencia() % (rcvPacket.getTamanhoJanela() * 2);
					} else {
						seqNumero = rcvPacket.getNumeroDeSequencia();
					}
				}
				System.out.println("PACOTE RECEBIDO NUMERO:" + rcvPacket.getNumeroDeSequencia() + " SEQUENCIA NUMERO: " + seqNumero);
				
				Reconhecimento ack = null;
				
				/**
				 *Envio de reconhecimento base onde o protocolo é Go-Back-N ou SR 
				 * 
				 */
				if (nomeProtocolo.equalsIgnoreCase("GBN")) {
					if (rcvPacket.getNumeroDeSequencia() == expectedAck && Integer.parseInt(resultCheckSum) == 11111111) {
						ack = new Reconhecimento(expectedAck);
						sentAcks.add(ack);
						expectedAck++;
					} else {
						System.out.println("***BIT ERRO DETECTADO***");
						ack = new Reconhecimento(sentAcks.get(sentAcks.size() - 1).getnumeroAck());
					}
				} else {
					
					// Para SR
					if (Integer.parseInt(resultCheckSum) == 11111111) {
						ack = new Reconhecimento(rcvPacket.getNumeroDeSequencia());
					} else {
						System.out.println("***BIT ERRO DETECTADO***");
						bitError = true;
					}
				}

				if (!bitError) {
					dadoEnviado = Dado.toBytes(ack);
					DatagramPacket pacoteEnviado = new DatagramPacket(dadoEnviado, dadoEnviado.length, IPAddress, port);
					
					// Imprimir o número de confirmação enviado do Receptor para o transmissor
					int ackNo = -1;
					if (nomeProtocolo.equalsIgnoreCase("GBN")) {
						if (ack.getnumeroAck() > rcvPacket.getTamanhoJanela() + 1) {
							ackNo = ack.getnumeroAck() % (rcvPacket.getTamanhoJanela() + 1);
						} else {
							ackNo = ack.getnumeroAck();
						}
					} else {
						if (ack.getnumeroAck() > rcvPacket.getTamanhoJanela() * 2) {
							ackNo = ack.getnumeroAck() % (rcvPacket.getTamanhoJanela() * 2);
						} else {
							ackNo = ack.getnumeroAck();
						}
					}
					System.out.println("ACK ENVIADO: " + ackNo);
					System.out.println("\n");
					serverSocket.send(pacoteEnviado);
				} else {
					dadoEnviado = ("Bit ERRO").getBytes();
					DatagramPacket pacoteEnviado = new DatagramPacket(dadoEnviado, dadoEnviado.length, IPAddress, port);
					serverSocket.send(pacoteEnviado);
				}

			} else {
				System.out.println("***PACOTE PERDIDO***");
				System.out.println("\n");
				String resposta = "PACOTE PERDIDO";
				dadoEnviado = resposta.getBytes();
				DatagramPacket pacoteEnviado = new DatagramPacket(dadoEnviado, dadoEnviado.length, IPAddress, port);
				serverSocket.send(pacoteEnviado);
			}
		}
	}
}