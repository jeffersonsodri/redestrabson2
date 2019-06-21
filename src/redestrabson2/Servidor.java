package redestrabson2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Servidor {
	public static double probabilidadePacotePerdido;
	public static String nomeProtocolo = "GBN";
	public static int tamanhoJanela;
	public static int tamanhoDado;
	public static String arquivo;
	public static String total = "";
	public static String tfim = "";
	public static int Acks;
	public static boolean pacoteCerto = true;
	public static int whereToStop = -1;
	public static int totalP;
	public static int firstLost = -1;

	public static void main(String args[]) throws Exception {
		arquivo = (System.getProperty("user.dir")).toString() + "//" + args[0];
		File f = new File(arquivo);
		f.createNewFile();

		Acks = -1;

		System.out.println("Servidor Iniciado");

		int numeroPorta = Integer.parseInt(args[1]);
		@SuppressWarnings("resource")
		DatagramSocket serverSocket = new DatagramSocket(numeroPorta);
		tamanhoJanela = Integer.parseInt(args[2]);
		probabilidadePacotePerdido = Double.parseDouble(args[3]);

		System.out.println("Servidor Iniciado na porta " + numeroPorta + ", o arquivo ser� salvo em :" + arquivo);

		// System.out.println("Tamanho recomendado " +tamanhoDado);

		byte[] dadoEnviado = new byte[512];
		byte[] pacoteRecebido = new byte[512];
		byte[] important = new byte[512];

		HashMap<Integer, byte[]> pacotesForaDeOrdem = new HashMap<Integer, byte[]>();

		int expectedAck = 1;
		ArrayList<Reconhecimento> sentAcks = new ArrayList<Reconhecimento>();

		while (true) {
			// System.out.println("Dado inicial " +dadoRecebido);
			if (tamanhoDado == 0 || Acks == -1 || totalP == 0) {
				DatagramPacket importantInfo = new DatagramPacket(important, pacoteRecebido.length);
				serverSocket.receive(importantInfo);

				Know rcv = (Know) Dado.toObject(importantInfo.getData());

				totalP = rcv.getNumeroTotal();
				tamanhoDado = rcv.getPackSize();
				Acks = rcv.getFastRetrasmission();
			}

			if (sentAcks.size() == totalP && pacoteCerto) {
				System.exit(0);
			} else if (sentAcks.size() == totalP) {
				System.out.println("Terminando de salvar pacotes...");
				String s;
				for (int b = whereToStop; b <= totalP; b++) {
					s = new String(pacotesForaDeOrdem.get(b));

					total = total + s;
				}
				// rcvPacket = Dado.makePacket(proximoNumSequencia,dadoEnviado);
				// System.out.println("O que temos aq �" +total);

				final StringBuffer str = new StringBuffer();
				str.append(total);

				try {
					FileWriter out = new FileWriter(arquivo);
					out.write(str.toString());
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				System.exit(0);
			}

			byte[] dadoRecebido = new byte[tamanhoDado];
			// tem que testar se funciona

			DatagramPacket receivePacket = new DatagramPacket(dadoRecebido, dadoRecebido.length);
			serverSocket.receive(receivePacket);

			DatagramPacket receivePacket2 = new DatagramPacket(pacoteRecebido, pacoteRecebido.length);
			serverSocket.receive(receivePacket2);

			Pacote rcvPacket = (Pacote) Dado.toObject(receivePacket2.getData());

			/**
			 * Calculando o checksum do lado dos receptores
			 */

			InetAddress IPAddress = receivePacket.getAddress();
			int port = receivePacket.getPort();

			if (Math.random() > probabilidadePacotePerdido) {

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
				System.out.println("PACOTE RECEBIDO NUMERO:" + rcvPacket.getNumeroDeSequencia() + " SEQUENCIA NUMERO: "
						+ seqNumero);

				Reconhecimento ack = null;

				/**
				 * Envio de reconhecimento base onde o protocolo � Go-Back-N ou SR
				 * 
				 */
				if (rcvPacket.getNumeroDeSequencia() == expectedAck) {
					ack = new Reconhecimento(expectedAck);
					sentAcks.add(ack);
					if (pacoteCerto) {
						dadoRecebido = receivePacket.getData();

						if (sentAcks.size() == totalP) {
							byte[] trimmed = trim(dadoRecebido);

							String s = new String(trimmed);

							total = total + s;


							StringBuffer str = new StringBuffer();

							str.append(total);

							try {
								FileWriter out = new FileWriter(arquivo);
								out.write(str.toString());
								out.close();
							} catch (IOException e) {
								e.printStackTrace();
							}

						} else {

							String s = new String(dadoRecebido);

							total = total + s;


							StringBuffer str = new StringBuffer();

							str.append(total);

							try {
								FileWriter out = new FileWriter(arquivo);
								out.write(str.toString());
								out.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}

					} else {
						pacotesForaDeOrdem.put(rcvPacket.getNumeroDeSequencia(), dadoRecebido);
						if (whereToStop == -1) {
							whereToStop = expectedAck;
						}
					}
					expectedAck++;
				} else if (Acks == 1) {
					if (sentAcks.size() > 0) {
						ack = new Reconhecimento(sentAcks.get(sentAcks.size() - 1).getnumeroAck());
					} else {
						firstLost = 0;
						ack = new Reconhecimento(0);
					}
					pacotesForaDeOrdem.put(rcvPacket.getNumeroDeSequencia(), dadoRecebido);
					pacoteCerto = false;
				} else {
					pacotesForaDeOrdem.put(rcvPacket.getNumeroDeSequencia(), dadoRecebido);
					if (sentAcks.size() > 0) {
						if (sentAcks.size() < totalP) {
							ack = new Reconhecimento(rcvPacket.getNumeroDeSequencia());
						} else {
							ack = new Reconhecimento(sentAcks.get(sentAcks.size() - 1).getnumeroAck());
						}
					} else {
						firstLost = 0;
						ack = new Reconhecimento(0);
					}
					pacoteCerto = false;
				}

				dadoEnviado = Dado.toBytes(ack);
				DatagramPacket pacoteEnviado = new DatagramPacket(dadoEnviado, dadoEnviado.length, IPAddress, port);

				// Imprimir o n�mero de confirma��o enviado do Receptor para o transmissor
				if (firstLost == -1) {
					System.out.println("ACK ENVIADO: " + ack.getnumeroAck());
					System.out.println("\n");
					serverSocket.send(pacoteEnviado);
				}
				firstLost = -1;

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

	// Method which write the bytes into a file
	static void writeByte(byte[] bytes) {
		try {

			// Initialize a pointer
			// in file using OutputStream
			OutputStream os = new FileOutputStream(arquivo);

			// Starts writing the bytes in it
			os.write(bytes);
			System.out.println("Successfully" + " byte inserted");

			// Close the file
			os.close();
		}

		catch (Exception e) {
			System.out.println("Exception: " + e);
		}
	}

	static byte[] trim(byte[] bytes) {
		int i = bytes.length - 1;
		while (i >= 0 && bytes[i] == 0) {
			--i;
		}

		return Arrays.copyOf(bytes, i + 1);
	}

}