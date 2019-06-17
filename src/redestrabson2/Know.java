package redestrabson2;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Know implements Serializable {
	private int fastRetrasmission;
	private int packSize;
	private int numeroTotal;
	
	
	public Know(int fastRetrasmission, int packSize, int numeroTotal) {
		super();
		this.fastRetrasmission = fastRetrasmission;
		this.packSize = packSize;
		this.numeroTotal = numeroTotal;
	}
	public int getNumeroTotal() {
		return numeroTotal;
	}
	public void setNumeroTotal(int numeroTotal) {
		this.numeroTotal = numeroTotal;
	}
	public int getFastRetrasmission() {
		return fastRetrasmission;
	}
	public void setFastRetrasmission(int fastRetrasmission) {
		this.fastRetrasmission = fastRetrasmission;
	}
	public int getPackSize() {
		return packSize;
	}
	public void setPackSize(int packSize) {
		this.packSize = packSize;
	}
	
	
}
