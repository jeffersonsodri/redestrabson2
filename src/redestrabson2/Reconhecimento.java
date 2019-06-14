package redestrabson2;
 


import java.io.Serializable;

@SuppressWarnings("serial")
public class Reconhecimento implements Serializable {
	
	private int numeroAck;

	public Reconhecimento(int numeroAck) {
		super();
		this.numeroAck = numeroAck;
	}

	public void setnumeroAck(int numeroAck) {
		this.numeroAck = numeroAck;
	}

	public int getnumeroAck() {
		return numeroAck;
	}

}
