import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String op = "";
		String userNome = "";
		String userPass = "";
		Scanner entrada = new Scanner(System.in);
		
		System.out.println("----------------------------Login----------------------------");

		System.out.println("Insira a porta:");
		int port = entrada.nextInt();

		System.out.println("Insira o endereço IP do servidor:");
		String IP = entrada.next();

		System.out.println("Insira o nome do Usuario:");
		userNome = entrada.next();
		System.out.println("Insira a senha do Usuario:");
		userPass = entrada.next();
		String user = userNome + userPass;

		// Função que realiza a verificação de existência do usuário
		// na lista de permissão do servidor
		verificaLogin(user, IP, port);

		Socket sk;
		DataInputStream input;

		while (!(op.equals("3"))) {
			System.out.println();

			System.out.println("----------------------------FTP----------------------------");
			System.out.println("1 - Enviar arquivo para servidor");
			System.out.println("2 - Solicitar arquivo do servidor");
			System.out.println("3 - Sair");

			op = entrada.next();

			if (op.equals("1") || op.equals("2") || op.equals("3")) {
				switch (op) {
				case "1":
					System.out.println("Insira o caminho do arquivo a ser enviado:");
					String caminho = entrada.next();
					sk = conexao(port, IP);

					try {
						// Cria stream de saída
						DataOutputStream output = new DataOutputStream(sk.getOutputStream());
						output.writeUTF(op);

						// Cria stream de entrada
						input = new DataInputStream(sk.getInputStream());
						// Recebe mensagem do servidor
						System.out.println(input.readUTF());
					} catch (Exception e) {
						System.err.println("CLIENTE ERRO: " + e.toString());
					}
					enviaArq(caminho, sk);
					try {
						sk.close();
					} catch (Exception e) {
						System.err.println("CLIENTE ERRO: " + e.toString());
					}

					break;
				case "2":
					System.out.println("Insira o caminho do diretorio que vai receber o arquivo:");
					String diretorio = entrada.next();
					/*String diretorio = "D:\\Arquivos";*/
					diretorio += "\\";
					System.out.println("Insira o nome do arquivo solicitado:");
					String arquivo = entrada.next();
					sk = conexao(port, IP);

					try {
						DataOutputStream output = new DataOutputStream(sk.getOutputStream());
						output.writeUTF(op);

						// Cria stream de entrada
						input = new DataInputStream(sk.getInputStream());
						// Recebe mensagem do servidor
						System.out.println(input.readUTF());
					} catch (Exception e) {
						System.err.println("CLIENTE ERRO: " + e.toString());
					}

					recebeArq(arquivo, diretorio, sk);
					try {
						sk.close();
					} catch (Exception e) {
						System.err.println("CLIENTE ERRO: " + e.toString());
					}
					break;
				case "3":
					System.out.println("EXIT SUCESSFUL");
					System.exit(0);
					break;
				}
			}
		}
	}

	//Função que realiza a conexão entre cliente e servidor
	public static Socket conexao(int port, String IP) {
		try {
			// Abrindo o socket
			Socket MyClient = new Socket(IP, port);

			return MyClient;
		} catch (Exception e) {
			System.err.println("CLIENTE ERRO: " + e.toString());
			return null;
		}
	}
	
	//Função responsável por receber o arquivo do servidor
	//É enviado ao servidor o nome do arquivo a ser recebido
	//O servidor retorna um objeto com o buffer que contém os dados do arquivo a ser recebido
	//A partir do buffer é criada uma cópia do arquivo na máquina do cliente
	public static void recebeArq(String arquivo, String diretorio, Socket sk) {
		try {
			//Envia nome do arquivo
			DataOutputStream output = new DataOutputStream(sk.getOutputStream());
			output.writeUTF(arquivo);

			//Recebe o objeto do arquivo
			ObjectInputStream in = new ObjectInputStream(sk.getInputStream());
			String fileName = in.readUTF();
			if (fileName != null) {
				long size = in.readLong();
				System.out.println("Processando arquivo: " + fileName + " - " + size + " bytes.");

				// Verifica se tem a pasta de destino criada, caso nao tenha ele cria
				File file = new File(diretorio);
				if (file.exists() == false) {
					file.mkdir();
				}
				
				//Criação do arquivo a partir do objeto que contém o buffer do arquivo
				FileOutputStream fos = new FileOutputStream(diretorio + fileName);
				byte[] buf = new byte[4096];
				while (true) {
					int len = in.read(buf);
					if (len == -1)
						break;
					fos.write(buf, 0, len);
				}

				fos.flush();
				fos.close();
				System.out.println("Arquivo recebido do servidor");
			}

		} catch (Exception e) {
			System.err.println("CLIENTE ERRO: " + e.toString());
		}
	}

	//Função responsável por enviar arquivo ao servidor
	//É enviado ao servidor um buffer que contém os dados do arquivo a ser recebido
	//A partir do buffer é criada uma cópia do arquivo na máquina do servidor
	public static void enviaArq(String arquivo, Socket sk) {
		try {
			File file = new File(arquivo);
			
			//verifica se o arquivo existe
			if (file.exists()) {
				ObjectOutputStream out = new ObjectOutputStream(sk.getOutputStream());
				System.out.println("Transferindo o arquivo: " + file.getName());
				out.writeUTF(file.getName());
				out.writeLong(file.length());
				
				FileInputStream filein = new FileInputStream(file);
				byte[] buf = new byte[4096];

				while (true) {
					int len = filein.read(buf);
					if (len == -1)
						break;
					out.write(buf, 0, len);
				}
				filein.close();
				out.close();
				System.out.println("Arquivo enviado ao servidor");

			} else {
				System.out.println("Não existe o arquivo!");
			}

		} catch (Exception e) {
			System.err.println("CLIENTE ERRO: " + e.toString());
		}
	}
	
	//Função responsável por enviar via socket o usuario e senha ao servidor
	//e verificar a existência do usuario na lista de logins do servidor
	public static void verificaLogin(String user, String IP, int port) {
		Socket sk = conexao(port, IP);

		try {
			DataInputStream input = new DataInputStream(sk.getInputStream());
			DataOutputStream output = new DataOutputStream(sk.getOutputStream());

			output.writeUTF(user);
			String valida = input.readUTF();

			try {
				sk.close();
			} catch (Exception e) {
				System.err.println("CLIENTE ERRO: " + e.toString());
			}

			if (valida.equals("false")) {
				System.out.println("Falha no login");
				System.exit(0);
			}

			System.out.println("Sucesso no login");

		} catch (Exception e) {
			System.err.println("CLIENTE ERRO: " + e.toString());
		}

	}

}