package redestrabson2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;

public class Dado {
	
	public static int tamanhoJanela;
	
	public static void settamanhoJanela(int size) {
		Dado.tamanhoJanela = size;
	}
	
	public static int gettamanhoJanela(){
		return Dado.tamanhoJanela;
	}
	
	/**
	 * Generates a random 16 bit binary dado
	 * @return The 16 bit binary string
	 */
	public static String getdadoBinario() {
		String binaryString = " ";
		Random r = new Random();
		int dado = r.nextInt(65536);
		binaryString = Integer.toString(dado, 2);
		if (binaryString.length() > 16) {
			binaryString = binaryString.substring(0, 15);
		} else {
			int length = 16 - binaryString.length();
			for (int i = 0; i < length; i++) {
				binaryString = "0" + binaryString;
			}
		}
		return binaryString;
	}
	
	/**
	 * Executa um complemento para um dado binario
	 * @param dado - Dado binario para pegar uns complementos para obter o checksum
	 * @return String Binaria que contem o conteudo do checksum
	 */
	public static String takeOnesComplement(String dado) {
		StringBuilder binarioChecksum = new StringBuilder();
		for(char c: dado.toCharArray()) {
			switch(c) {
				case '0':
					binarioChecksum.append('1');
				break;
				case '1':
					binarioChecksum.append('0');
				break;
			}
		}
		return binarioChecksum.toString();
	}
	
	/**
	 * Realiza operacao de calculo do checksum dividindo 16 bit do dado binario em 2 segmentos
	 * de 8 bit dado cada e performando uns complementos para obter o checksum.
	 */
	public static String sumData(String dadoBinario) {
		int dado1 = Integer.parseInt(dadoBinario.substring(0, 8), 2);
		int dado2 = Integer.parseInt(dadoBinario.substring(8, 16), 2);
		int sum = dado1 + dado2;
		String resultadoBinario = Integer.toString(sum, 2);
		
		if (resultadoBinario.length() > 8 && resultadoBinario.charAt(0) == '1') {
			dado1 = Integer.parseInt(resultadoBinario.substring(1, 9), 2);
			dado2 = Integer.parseInt(resultadoBinario.substring(0, 1), 2);
			sum = dado1 + dado2;
			resultadoBinario = Integer.toString(sum, 2);
			
			if (resultadoBinario.length() < 8) {
				int length = 8 - resultadoBinario.length();
				for (int i = 0; i < length; i++) {
					resultadoBinario = "0" + resultadoBinario;
				}
			}
		} else {
			int length = 8 - resultadoBinario.length();
			for (int i = 0; i < length; i++) {
				resultadoBinario = "0" + resultadoBinario;
			}
		}
		return resultadoBinario;
	}
	
	public static String changeBit(String dado) {
		char[] chardado = dado.toCharArray();
		
		Random m = new Random();
		int bitErrorIndex = m.nextInt(16);
		if (chardado[bitErrorIndex] == '0') {
			chardado[bitErrorIndex] = '1';
		} else {
			chardado[bitErrorIndex] = '0';
		}
		return String.valueOf(chardado);
	}
	
	/**
	 * Converte um objeto para bytes
	 * @param o -  objeto que vai ser convertido para byte
	 * @return O byte equivalente ao objeto passado
	 * @throws IOException
	 */
	public static byte[] toBytes(Object o) throws IOException {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		ObjectOutputStream objStream = new ObjectOutputStream(byteStream);
		objStream.writeObject(o);
		return byteStream.toByteArray();
	}
	
	/**
	 * Converter um byte para Objeto
	 * @param b - O byte que foi convertido para objeto
	 * @return O dado convertido de um byte para o objeto
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public static Object toObject(byte[] b) throws ClassNotFoundException, IOException {
		ByteArrayInputStream byteStream = new ByteArrayInputStream(b);
		ObjectInputStream objStream = new ObjectInputStream(byteStream);
		return objStream.readObject();
	}
	
	public static Pacote makePacket(int proximoNumeroSequencia)
	{
		String dadoBinario = null;
		String binarioChecksum = null;
		
		// Gerar dado binario e computar checksum
		dadoBinario = Dado.getdadoBinario();
		String dadoSum = Dado.sumData(dadoBinario);
		binarioChecksum = Dado.takeOnesComplement(dadoSum);
		
		// Criar um novo pacote com um novo dado, checksum e numero de sequencia
		Pacote pkt = new Pacote(proximoNumeroSequencia, dadoBinario.getBytes(), binarioChecksum.getBytes(), tamanhoJanela);
		
		return pkt;
	}
}