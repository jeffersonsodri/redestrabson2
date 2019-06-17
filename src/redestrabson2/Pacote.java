package redestrabson2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("serial")
public class Pacote implements Serializable{
	
	private static final Logger LOGGER = Logger.getLogger( Pacote.class.getName() );
	private int numeroDeSequencia;
	private byte[] checksum;
	private int tamanhoJanela;
	
	public Pacote(int numeroDeSequencia, byte[] checksum, int tamanhoJanela) {
		super();
		this.numeroDeSequencia = numeroDeSequencia;
		this.checksum = checksum;
		this.tamanhoJanela = tamanhoJanela;
	}

	public void setNumeroDeSequencia(int numeroDeSequencia) {
		this.numeroDeSequencia = numeroDeSequencia;
	}

	public int getNumeroDeSequencia() {
		return numeroDeSequencia;
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
	
	public static byte[] readFileToByteArray(File file){
        FileInputStream fis = null;
        byte[] bArray = new byte[(int) file.length()];
        try{
            fis = new FileInputStream(file);
            fis.read(bArray);
            fis.close();        
            
        }catch(IOException ioExp){
        	LOGGER.log( Level.SEVERE, ioExp.toString(), ioExp );
        }
        return bArray;
    }

}