import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Servidor {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ArrayList<String> login = new ArrayList<>();
		login.add("danielle1234");
		
		int port = 12383;
		try {
			// Abrindo o socket
			ServerSocket ss = new ServerSocket(port);
			
			// Caminho onde estão os arquivos do servidor
			//Ex: D:\\Arquivos\\
			String caminho = "D:\\Arquivos\\";
			
			while (true) {
				
				System.out.println("Esperando por solicitações.");
				Socket socket = ss.accept();         //libera socket para conexão
				
				DataInputStream input = new DataInputStream(socket.getInputStream()); //entrada de dados do cliente
				DataOutputStream output = new DataOutputStream(socket.getOutputStream());	//saída de dados para o cliente
				// Envia mensagem
				
				
				String op = input.readUTF();  //pede opção do cliente
				
				if(op.equals("1")) { //opção 1: cliente envia arquivo para servidor
					output.writeUTF("Conectado com o servidor."); //manda mensagem
					System.out.println("Recebendo arquivo do cliente");
					try {
						
						ObjectInputStream in = new ObjectInputStream(socket.getInputStream()); //variável que recebe arquivo
						String fileName = in.readUTF(); //recebe nome do arquivo
						
						if(fileName != null){    
							long size = in.readLong();   //tamanho do nome
							System.out.println("Processando arquivo: " + fileName + " - "+ size + " bytes.");
			
							// Verifica se tem a pasta de destino criada, caso nao tenha ele cria
							File file = new File(caminho);   
							if(file.exists() == false){
								file.mkdir();
							}
							
							FileOutputStream fos = new FileOutputStream(caminho + fileName);//usado para ler buffer e transformar em um buffer
							byte[] buf = new byte[4096];
							while (true) {					
								int len = in.read(buf);			//verifica buffer recebido
								if (len == -1)
									break;			
								fos.write(buf, 0, len);		 //transforma buffer em arquivo
							}
							/*in.transferTo(fos);*/
							fos.flush();
							fos.close();
							System.out.println("Arquivo recebido do cliente");
						}
						
				} catch (Exception e) {
					System.err.println("SERVIDOR ERRO: " + e.toString());
				}
			
				}else if(op.equals("2")){ //opcao 2: servidor para cliente
					output.writeUTF("Bem-vindo, você está conectado.");
					System.out.println("Enviando arquivo ao cliente");
					
					String arquivo = input.readUTF();   //recebe nome do arquivo desejado
					System.out.println("Arquivo:" + arquivo);
					File file = new File(caminho + arquivo);
					
					if(file.exists()){
						
						ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());  //cria objeto para envia arquivo
						System.out.println("Transferindo o arquivo: " + file.getName());
						out.writeUTF(file.getName());	//manda nome e tamanho
						out.writeLong(file.length());
						FileInputStream filein = new FileInputStream(file);  //usado para ler arquivo e transformar em um buffer
						byte[] buf = new byte[4096];
	
						while (true) {
							int len = filein.read(buf); 
							if (len == -1)
								break;
							out.write(buf, 0, len);  //manda objeto com buffer do arquivo
						}
						filein.close();
						out.close();
						System.out.println("Arquivo enviado ao cliente");
						
					}else{
			
						output.writeUTF("Não existe o arquivo!");
					}
				}else {//verifica login
					if(login.contains(op)) {
						output.writeUTF("true");
						System.out.println("Login aceito");
					}else {
						System.out.println("Login recusado");
						output.writeUTF("false");
					}
				}
				
				System.out.println();
			}
		} catch (Exception e) {
			System.err.println("SERVIDOR ERRO: " + e.toString());
		}

	}

}