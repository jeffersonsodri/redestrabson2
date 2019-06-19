package redestrabson2;

import java.io.File;
import java.io.FileInputStream;
import java.net.*;
import java.util.HashMap;
import java.util.Objects;

public class Cliente {
	public static String nomeProtocolo = "GBN";
	public static int numBitsEmSeq;
	public static int tamanhoJanela;
	public static int valorDoTimeout;
	public static int tamanhoMaximoSeguimento;
	public static int pacoteDuplicado;
	public static int totalNumPacotes;
	public static int ackR;
	public static double probabilidadeErroBit;
	public static double probabilidadeAckPerdido;
	public static double tamPac;
	public static boolean pacotesPendentes;

	public static void main(String args[]) throws Exception {

		HashMap<Integer, Pacote> pacotesEnviados = new HashMap<Integer, Pacote>();
		HashMap<Integer, byte[]> dadosEnviados = new HashMap<Integer, byte[]>();
		HashMap<Integer, Integer> acksRecebidos = new HashMap<Integer, Integer>();

		// Inicializacao e recebimento dos parametros do arquivo para o programa
		String arquivo = (System.getProperty("user.dir")).toString() + "//" + args[0];
		// List<String> parametrosAlgoritmo = new ArrayList<String>();
		File f = new File(arquivo);

		FileInputStream fi = new FileInputStream(arquivo);

		InetAddress enderecoIP = InetAddress.getByName(args[1]); // pega endereço
		int numeroPorta = Integer.parseInt(args[2]);// porta

		// int highestAckRcvd = 1;
		// List<Integer> srAcks = new ArrayList<Integer>();

		tamanhoJanela = Integer.parseInt(args[3]);
		valorDoTimeout = Integer.parseInt(args[4]);
		tamanhoMaximoSeguimento = Integer.parseInt(args[5]);
		pacoteDuplicado = Integer.parseInt(args[6]);
		probabilidadeErroBit = Integer.parseInt(args[7]);

		byte[] pacoteEnviado = new byte[512];

		totalNumPacotes = (int) f.length() / tamanhoMaximoSeguimento; // tem que concertar isso aq
		tamPac = f.length() / tamanhoMaximoSeguimento;
		if (totalNumPacotes == 0) {
			totalNumPacotes++;
		}
		
		
		if (tamPac >= totalNumPacotes) {
			totalNumPacotes++;
		}

		Dado.settamanhoJanela(tamanhoJanela);

		DatagramSocket clientSocket = new DatagramSocket();

		// String para guardar os números ACK que foram enviados
		int[] numeroDuplicadoACK = new int[valorDoTimeout * 2];

		// Contador com 0 ACKs duplicados
		int[] numeroDuplicadoCont = new int[valorDoTimeout * 2];
		for (int i = 0; i < numeroDuplicadoCont.length; i++) {
			numeroDuplicadoCont[i] = 0;
		}

		
		byte[] dadoRecebido = new byte[tamanhoMaximoSeguimento];
		byte[] important = new byte[512];

		Know know = new Know(pacoteDuplicado, tamanhoMaximoSeguimento, totalNumPacotes);
		important = Dado.toBytes(know);
		DatagramPacket importantInfo = new DatagramPacket(important, important.length, enderecoIP, numeroPorta);
		clientSocket.send(importantInfo);

		int base = 1;
		int proximoNumSequencia = 1;
		int i = 1;
		while (i <= totalNumPacotes | base <= totalNumPacotes) {
			try {
				if (proximoNumSequencia - base < tamanhoJanela) {
					// byte[] arq = Pacote.readFileToByteArray(f);
					// System.out.println("Dado limpo" + dadoEnviado);
					byte[] dadoEnviado = new byte[tamanhoMaximoSeguimento];
					int z = 0;
					while (fi.available() > 0 && z < tamanhoMaximoSeguimento) {
						dadoEnviado[z] = (byte) fi.read();
						z++;
					}

					Pacote pacote = Dado.makePacket(proximoNumSequencia);

					pacoteEnviado = Dado.toBytes(pacote);
					// System.out.println("Dado Enviado antes " + dadoEnviado);
					// Convertendo o objeto Pacote para Bytes para enviar ao receptor
					// System.out.println("O tamanho do enviado é " +pacoteEnviado.length);
					DatagramPacket pacoteDatagramEnviado = new DatagramPacket(dadoEnviado, dadoEnviado.length,
							enderecoIP, numeroPorta);
					DatagramPacket pacoteDatagramEnviado2 = new DatagramPacket(pacoteEnviado, pacoteEnviado.length,
							enderecoIP, numeroPorta);
					// System.out.println("Dado Enviado depois" + dadoEnviado);

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
					if (i <= totalNumPacotes) {
						System.out.println("PACOTE ENVIADO NUMERO: " + i + " SEQUENCIA NUMERO: " + seqNumero);
					}
					
					if (base == proximoNumSequencia) {
						clientSocket.setSoTimeout(valorDoTimeout);
					}
					if(i<=totalNumPacotes) {
					// Envio para socket do receptor
					clientSocket.send(pacoteDatagramEnviado);
					clientSocket.send(pacoteDatagramEnviado2);


					// Adicao do pacote para a lista de envio
					pacotesEnviados.put(i, pacote);
					// System.out.println("Aqui tem "+pacotesEnviados.get(i));
					dadosEnviados.put(i, dadoEnviado);
					// System.out.println("Aqui tem "+new String(dadosEnviados.get(i)));
					}	
					// Incremento do numero de sequencia
					proximoNumSequencia++;
					i++;
				}

				// Recebendo recomenhecimento do pacote
				DatagramPacket receivePacket = new DatagramPacket(dadoRecebido, dadoRecebido.length);
				clientSocket.receive(receivePacket);

				String resposta = new String(receivePacket.getData());
				if (!(resposta.contains("PERDIDO"))) {

					// Simulando ACK perdido com probabilidade 0.05
					if (Math.random() < probabilidadeAckPerdido) {
						System.out.println("***ACK PERDIDO***");
						Reconhecimento verificarAckDuplicado = (Reconhecimento) Dado.toObject(receivePacket.getData());
						int j = 0;
						for (; j < numeroDuplicadoACK.length; j++) {
							if (numeroDuplicadoACK[j] == verificarAckDuplicado.getnumeroAck()) {
								numeroDuplicadoCont[j]++;
								break;
							}
						}
						/**
						 * ACK duplicado 3 vezes, fazer retransmisssao
						 */
						if (numeroDuplicadoACK[j] > 2) {
							System.out.println("ACK FOI ENVIADO 3 VEZES.");
						}

					} else {

						// Convertendo para o display
						Reconhecimento ack = (Reconhecimento) Dado.toObject(receivePacket.getData());

						if (ack.getnumeroAck() <= totalNumPacotes) {
							System.out.println("ACK RECEBIDO: " + ack.getnumeroAck());
							
							//ackR++;
						}
						//System.out.println("acks recebidos reais" +ackR);
						//System.out.println("\n");
						if (pacoteDuplicado == 1) {

							int numAck = ack.getnumeroAck();

							if (Objects.nonNull(acksRecebidos.get(numAck))) {
								acksRecebidos.put(ack.getnumeroAck(), (acksRecebidos.get(numAck) + 1));
							} else {
								acksRecebidos.put(ack.getnumeroAck(), 1);
							}
							if (Objects.nonNull(acksRecebidos.get(numAck)) && acksRecebidos.get(numAck) == 3) {
								acksRecebidos.remove(ack.getnumeroAck());

								System.out.println("3 Pacotes ACK recebidos. \n" + "Reenviando pacotes perdidos...");
								int b = base;
								int ns = proximoNumSequencia - 1;
								if(ns>totalNumPacotes) {
									ns=totalNumPacotes;
								}
								for (int j = b - 1; j <= ns; j++) {

									// System.out.println("Aqui tem "+new String(Transmissor.dadosEnviados.get(0)));
									// System.out.println("Aqui tem "+new
									// String(Transmissor.dadosEnviados.get(base-1)));
									byte[] dadoEnviado = new byte[tamanhoMaximoSeguimento];
									dadoEnviado = dadosEnviados.get(base);
									DatagramPacket pacoteDatagramEnviado = new DatagramPacket(dadoEnviado,
											dadoEnviado.length, enderecoIP, numeroPorta);

									pacoteEnviado = Dado.toBytes(pacotesEnviados.get(base));
									DatagramPacket pacoteDatagramEnviado2 = new DatagramPacket(pacoteEnviado,
											pacoteEnviado.length, enderecoIP, numeroPorta);

									int seqNumero;
									if (base > 1) {
										if (pacotesEnviados.get(j).getNumeroDeSequencia() > tamanhoJanela + 1) {
											seqNumero = pacotesEnviados.get(j).getNumeroDeSequencia()
													% (tamanhoJanela + 1);
										} else {
											seqNumero = pacotesEnviados.get(j).getNumeroDeSequencia();
										}
									} else {
										seqNumero = 1;
									}
									if (base == 1) {
										System.out.println("REENVIANDO NUMERO PACOTE: "
												+ pacotesEnviados.get(base).getNumeroDeSequencia() + " SEQ NUMERO: "
												+ seqNumero);
									} else {
										if (j < totalNumPacotes) {
											System.out.println("REENVIANDO NUMERO PACOTE: "
													+ pacotesEnviados.get(j).getNumeroDeSequencia() + " SEQ NUMERO: "
													+ seqNumero);
										}
									}
									// Envido para o socket do receptor
									clientSocket.send(pacoteDatagramEnviado);
									clientSocket.send(pacoteDatagramEnviado2);

									// Recebendo reconhecimento para o pacote
									receivePacket = new DatagramPacket(dadoRecebido, dadoRecebido.length);
									try {
										clientSocket.receive(receivePacket);
									} catch (Exception ex) {
										System.out.println("Servidor Desligado ou Inexistente");
									}

									resposta = new String(receivePacket.getData());
									if (!(resposta.contains("PERDIDO"))) {

										// Simulando ACK perdido com probabilidade 0.05
										if (Math.random() < probabilidadeAckPerdido) {
											System.out.println("***ACK PERDIDO***");

											Reconhecimento verificarAckDuplicado = (Reconhecimento) Dado
													.toObject(receivePacket.getData());
											int w = 0;
											for (; w < numeroDuplicadoACK.length; w++) {
												if (numeroDuplicadoACK[w] == verificarAckDuplicado.getnumeroAck()) {
													numeroDuplicadoCont[w]++;
													break;
												}
											}
											/**
											 * ACK duplicado 3 vezes, fazer retransmisssao
											 */
											if (numeroDuplicadoACK[w] > 3) {
												System.out.println("ACK FOI ENVIADO 3 VEZES.");
											}
										} else {

											// Convertendo para o display
											ack = (Reconhecimento) Dado.toObject(receivePacket.getData());
											System.out.println("ACK RECEBIDO: " + ack.getnumeroAck());
											System.out.println("\n");

											if (ack.getnumeroAck() == base) {
												base = ack.getnumeroAck() + 1;
											}
										}
									}
								}

							}
						}
						if (ack.getnumeroAck() == base) {
							base = ack.getnumeroAck() + 1;
						}

					}
				}
			} catch (Exception e) {
				System.out.println("Timeout. \n" + "Reenviando pacotes perdidos..");
				int b = base;
				int ns = proximoNumSequencia - 1;
				if(ns>totalNumPacotes) {
					ns=totalNumPacotes;
				}
				for (int j = b - 1; j < ns; j++) {
					
					//System.out.println("o j é " +j);
					//System.out.println("acks recebidos reais" +ackR);
					// System.out.println("Aqui tem "+new String(Transmissor.dadosEnviados.get(0)));
					// System.out.println("Aqui tem "+new
					// String(Transmissor.dadosEnviados.get(base-1)));
					byte[] dadoEnviado = new byte[tamanhoMaximoSeguimento];
					dadoEnviado = dadosEnviados.get(base);
					DatagramPacket pacoteDatagramEnviado = new DatagramPacket(dadoEnviado, dadoEnviado.length,
							enderecoIP, numeroPorta);

					pacoteEnviado = Dado.toBytes(pacotesEnviados.get(base));
					DatagramPacket pacoteDatagramEnviado2 = new DatagramPacket(pacoteEnviado, pacoteEnviado.length,
							enderecoIP, numeroPorta);

					int seqNumero;
					if (base > 1) {
						if (pacotesEnviados.get(j).getNumeroDeSequencia() > tamanhoJanela + 1) {
							seqNumero = pacotesEnviados.get(j).getNumeroDeSequencia() % (tamanhoJanela + 1);
						} else {
							seqNumero = pacotesEnviados.get(j).getNumeroDeSequencia();
						}
					} else {
						seqNumero = 1;
					}
					if (base == 1) {
						System.out.println("REENVIANDO NUMERO PACOTE: "
								+ pacotesEnviados.get(base).getNumeroDeSequencia() + " SEQ NUMERO: " + seqNumero);
					} else if(pacotesEnviados.get(j).getNumeroDeSequencia()<=totalNumPacotes){
						System.out.println("REENVIANDO NUMERO PACOTE: " + pacotesEnviados.get(j).getNumeroDeSequencia()
								+ " SEQ NUMERO: " + seqNumero);
					}
					// Envido para o socket do receptor
					clientSocket.send(pacoteDatagramEnviado);
					clientSocket.send(pacoteDatagramEnviado2);

					// Recebendo reconhecimento para o pacote
					DatagramPacket receivePacket = new DatagramPacket(dadoRecebido, dadoRecebido.length);
					try {
						clientSocket.receive(receivePacket);
					} catch (Exception ex) {
						System.out.println("Servidor Desligado ou Inexistente");
					}

					String resposta = new String(receivePacket.getData());
					if (!(resposta.contains("PERDIDO"))) {

						// Simulando ACK perdido com probabilidade 0.05
						if (Math.random() < probabilidadeAckPerdido) {
							System.out.println("***ACK PERDIDO***");

							Reconhecimento verificarAckDuplicado = (Reconhecimento) Dado
									.toObject(receivePacket.getData());
							int w = 0;
							for (; w < numeroDuplicadoACK.length; w++) {
								if (numeroDuplicadoACK[w] == verificarAckDuplicado.getnumeroAck()) {
									numeroDuplicadoCont[w]++;
									break;
								}
							}
							/**
							 * ACK duplicado 3 vezes, fazer retransmisssao
							 */
							if (numeroDuplicadoACK[w] > 3) {
								System.out.println("ACK FOI ENVIADO 3 VEZES.");
							}
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
			}
		}
		// bOut.close();
		fi.close();
		clientSocket.close();
	}
}