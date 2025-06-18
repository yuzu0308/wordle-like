import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.io.*;

/**
 * ソケット通信(サーバー側)
 */
public class wordleServerGUI2 {

	public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
    public static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m";
    public static final String ANSI_GREY_BACKGROUND = "\u001B[100m";

	// 5文字の英単語を格納する
	static String[] possibleWords;
	// クライアントの入力
    static char[] input;
	// 正答
    static char[] Ans;
	// 色保存用
	static String colors[] = new String[5];
	static String A;

	static int inputSamedigitsFlg;  //入力した英単語に同じ文字が含まれているか判定
	static int ansSamedigitsFlg; //正答に同じ文字が含まれているか判定
	
	static ServerSocket sSocket = null;
	static Socket socket = null;
	static BufferedReader reader = null;
	static PrintWriter writer = null;
	static BufferedReader sbInput = null;

	public static void main (String[] args) {
	
		try{
			sbInput = new BufferedReader(new InputStreamReader(System.in));

			//IPアドレスとポート番号を指定してサーバー側のソケットを作成
			sSocket = new ServerSocket();
			sSocket.bind(new InetSocketAddress("127.0.0.1",8800));
		
			System.out.println("クライアントを待っています...");

			//クライアントからの要求を待ち続けます
			socket = sSocket.accept();
			
			//クライアントからの受取用
			reader = new BufferedReader(
					new InputStreamReader
					(socket.getInputStream()));
			
			//サーバーからクライアントへの送信用
			writer = new PrintWriter(
					socket.getOutputStream(), true);

			// possiblewordsを作る、clientの入力を受け付ける配列を定義。
			makePossiblewords();
			// server側で、answerを作成してもらう。
			createAnswer();
			// 正答を予め送信
			writer.println(Ans);

			while (true) {
				
				// Clientの入力された文字列を受け付ける
				String line = null;
				line = reader.readLine();
				// "bye"で終了
				if (line.equals("bye")) {
					break;
				}
				// 5文字の英単語がチェックする
				if (inputisPossible(line)){
					// 入力された5文字を格納する配列
					input = line.toCharArray();

					inputSamedigitsFlg = 0;
					ansSamedigitsFlg = 0;

					// 同じ文字が含まれているか判定
					for (int p = 0; p < 5; p++) {
						for (int j = p + 1; j < 5; j++) {
							if (input[p] == input[j]) {
								inputSamedigitsFlg = input[p]-'a';
							}
							if (Ans[p] == Ans[j]) {
								ansSamedigitsFlg = Ans[p]-'a';
							}
						}
					}

					// 文字に色をつける。
					putColor();

					// 色の情報を送る。
					for (int i = 0; i < 5; i++) {
						if (colors[i] == ANSI_GREY_BACKGROUND) writer.println("0");
						else if (colors[i] == ANSI_YELLOW_BACKGROUND) writer.println("1");
						else if (colors[i] == ANSI_GREEN_BACKGROUND) writer.println("2");
					}

					// Server上のコマンドラインに結果を出力
					System.out.print("   | ");
					for (int i = 0; i < 5; i++) {
						System.out.print(colors[i] + input[i] + ANSI_RESET);
					}
					System.out.println(" |");	
				}
			}
			System.out.println("   | ----- |");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try{
				if (reader != null) {
					reader.close();
				}
				if (writer != null) {
					writer.close();
				}
				if (socket != null) {
					socket.close();
				}
				if (sSocket != null) {
					sSocket.close();
				}
				System.out.println("サーバー側終了です");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 5文字の英単語ファイルを生成
	 */
	//copied from https://replit.com/@skutschke/WordleWords#Main.java
	public static void makePossiblewords() {
		possibleWords = new String[12947];
		try {
			File myObj = new File("wordleWords.txt");
			Scanner myReader = new Scanner(myObj);
			int indexCounter = 0;
			while (myReader.hasNextLine()) {
				String data = myReader.nextLine();
				possibleWords[indexCounter] = data;
				indexCounter++;
			}
			myReader.close();
		} catch (FileNotFoundException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}

	/**
	 * 正答となる5文字の英単語を生成する
	 */
	public static void createAnswer() {
		BufferedReader sbInput = null;
		try {
			sbInput = new BufferedReader(new InputStreamReader(System.in));

			// Server側で回答となる文字列を入力してもらい、正しいかを判定する
			do {
				System.out.println("答えとなる5字の英単語を入力してください");
				A = sbInput.readLine();
			} while (!isPossibleword(A, possibleWords));
			
			Ans = A.toCharArray();
			System.out.println("↓↓Clientの予想↓↓");
			System.out.println("   | ----- |");
		} catch (IOException e) {
			// 入出力エラーの処理
			e.printStackTrace();
		} finally {
			// ストリームをクローズする
			if (sbInput != null) {
				try {
					sbInput.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * クライアントの入力がpossibleWordsに含まれているかを判定する
	 * @input 
	 */
	public static boolean inputisPossible(String input){
		if (isPossibleword(input, possibleWords)) {
			// "OK"をクライアントに送信する
			writer.println("OK");
			return true;
		} else {
			return false;
		}
	}

	/**
	 * inputがpossibleWordsに含まれているかを判定する
	 * @input
	 * @possibleWords
	 */
	public static boolean isPossibleword (String input, String[] possibleWords) {
		if (input.length() != 5) {
			if (Ans.equals(null)) System.out.println("文字列の長さが5文字でありません。5文字の英単語を入力してください。");
			return false;
		}
		for (String string : possibleWords) {
			if (string.equals(input)) {
				return true;
			}
		}
		if (Ans.equals(null)) System.out.println("正しい英単語ではありません。もう一度入力してください。");
		return false;
	}

	/**
	 * 受信データに彩色をする
	 */
	public static void putColor(){

		for(int i = 0; i < 5; i++) {  // black
			colors[i] = ANSI_GREY_BACKGROUND;
		}
		for(int i = 0; i < 5; i++) {  // yello
			for(int j = 0; j < 5; j++){
				if(input[i] == Ans[j]){
					colors[i] = ANSI_YELLOW_BACKGROUND;
				}
			}
		}
		for(int i = 0; i < 5; i++) {  // green
			if(input[i] == Ans[i]){
				colors[i] = ANSI_GREEN_BACKGROUND;
			}
		}

		//　重複した場合、色を上書きする。
		for (int i = 0; i < 5; i++) {
			for (int j = i + 1; j < 5; j++) {
				if (input[i] == input[j]) {
					if (inputSamedigitsFlg != ansSamedigitsFlg) {
						if (colors[i] == ANSI_YELLOW_BACKGROUND) {
							colors[i] = ANSI_GREY_BACKGROUND;
							break;
						}    
						else if (colors[j] == ANSI_YELLOW_BACKGROUND) {
							colors[j] = ANSI_GREY_BACKGROUND;
							break;
						}
					}
				}
			}
		}
	}

}
