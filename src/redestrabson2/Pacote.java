package redestrabson2;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Pacote implements Serializable{
	
	private int numeroDeSequencia;
	private byte[] dado;
	private byte[] checksum;
	private int tamanhoJanela;
	
	public Pacote(int numeroDeSequencia, byte[] dado, byte[] checksum, int tamanhoJanela) {
		super();
		this.numeroDeSequencia = numeroDeSequencia;
		this.dado = dado;
		this.checksum = checksum;
		this.tamanhoJanela = tamanhoJanela;
	}

	public void setNumeroDeSequencia(int numeroDeSequencia) {
		this.numeroDeSequencia = numeroDeSequencia;
	}

	public int getNumeroDeSequencia() {
		return numeroDeSequencia;
	}

	public byte[] getDado() {
		return dado;
	}
	
	public void setDado(byte[] dado) {
		this.dado = dado;
	}

	public byte[] getChecksum() {
		return checksum;
	}
	
	public void setChecksum(byte[] checksum) {
		this.checksum = checksum;
	}
	
	public int getTamanhoJanela() {
		return tamanhoJanela;
	}

	public void setTamanhoJanela(int tamanhoJanela) {
		this.tamanhoJanela = tamanhoJanela;
	}

}