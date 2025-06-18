import java.util.Scanner;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * ソケット通信(クライアント側)
 */
class wordleClientGUI2 extends JFrame implements ActionListener {

	private static JPanel panel;
    private static JFrame frame;
    private static JLabel stats;
    private static JTextField userText1;
    private static JLabel[] labels;
	private static JButton[] keyButtons;

    public static Scanner s = new Scanner (System.in);

	// ユーザーのトライ数
    static int tries;
	// 正答
    static String answer;
	// ユーザーの入力した文字
	static String userInput = "";
	// wordleゲームを終えるかというフラグ
    static boolean isdoneFlg;
	// 色保存用
	static String colors[] = new String[5];
	// numToColors
	static String[] numToColors = new String[5];
	// Clientの入力
	static String line = null;
	// キーボード用文字列
	static String[] KeyboardButtons = {
		"Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P", 
		"A", "S", "D", "F", "G", "H", "J", "K", "L", 
		"Z", "X", "C", "V", "B", "N", "M"
	};
	static final int WORDLELINE = 6;

	static Socket cSocket = null;
	static BufferedReader csInput = null;
	static PrintWriter writer = null;
	static BufferedReader reader = null;

	public static void main (String[] args) {

		try{		
			// IPアドレスとポート番号を指定してクライアント側のソケットを作成
			cSocket = new Socket("127.0.0.1", 8800);
			// クライアント側での入力用
			csInput = new BufferedReader
				(new InputStreamReader(System.in));
			// クライアント側からサーバへの送信用
			writer = new PrintWriter
						(cSocket.getOutputStream(), true);
			// サーバ側からの受取用
			reader = new BufferedReader
						(new InputStreamReader
							(cSocket.getInputStream()));

			// パネルとフレームをセットする
			panel = new JPanel();
			frame = new JFrame();
			frame.setSize(700, 450);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setResizable(false);
			frame.setTitle("WordleGUI");
			frame.setLocationRelativeTo(null);
			frame.add(panel);

			panel.setLayout(null);
			stats = new JLabel("<html><font size='5' color=black> Type a five letter word </font> <font");
			stats.setBounds(10, 10, 500, 60);
			panel.add(stats);

			// テキストフォーム
			userText1 = new JTextField();
			userText1.addActionListener(new wordleClientGUI2());
			userText1.setBounds(40, 60 + (0 * 50), 100, 30);
			panel.add(userText1);

			// ラベルを追加する。
			labels = new JLabel[WORDLELINE];
			for (int i = 0; i < WORDLELINE; i++) {
				labels[i] = new JLabel("<html><font size='10' color=black> \u25A0\u25A0\u25A0\u25A0\u25A0 </font> <font");
				labels[i].setBounds(300, 40 + (i * 25), 150, 60);
				panel.add(labels[i]);
			}

			// キーボードを設置
			setKeyBoard();

			frame.setVisible(true);

			// 正答を格納する
			answer = reader.readLine();

			tries = 0;
			while (true) {
				if (reader.readLine().equals("OK")) {
					for (int i = 0; i < 5; i++) {
						colors[i] = reader.readLine();
					}
					setFeedback();
				}
				if (isdoneFlg || tries > 5) {
					writer.println("bye");
					break;
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				// 結果を表示して10秒で終了する
				Thread.sleep(10000);
				frame.setVisible(false);
				frame.dispose();
				if (reader != null) {
					reader.close();
				}
				if (writer != null) {
					writer.close();
				}
				if (csInput != null) {
					csInput.close();
				}
				if (cSocket != null) {
					cSocket.close();
				}
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("クライアント側終了です");
		}
	}
	
	/**
	 * 結果を表示してWordleを終了する。
	 */
	public static void endWordle() {

		userText1.setEnabled(false);
		userText1.setVisible(false);

		if (!isdoneFlg) stats.setText("<html><font size='5' color=red> " + "The Answer Was: " + answer + ".</font> <font");
		else  stats.setText("<html><font size='5' style='color:#6aaa64'> " + "Success! You found the answer in " + tries + " tries." + "</font> <font");
	}

	/**
	 * Enterキーが押された時
	 */
	@Override
    public void actionPerformed(ActionEvent e) {
		// 入力された文字列を送信
        line = userText1.getText();
		writer.println(line);
    }

	/**
	 * キーボードを配置する
	 */
	public static void setKeyBoard() {
		keyButtons = new JButton[26];
		for (int i = 0; i < KeyboardButtons.length; i++) {
			keyButtons[i] = new JButton(KeyboardButtons[i]);
			if (i <= 9) {
				keyButtons[i].setBounds(60 + (i * 60),250,60,30);
			}
			else if (10 <= i && i <= 18) {
				keyButtons[i].setBounds(30 + ((i - 9) * 60),300,60,30);
			}
			else {
				keyButtons[i].setBounds(80 + ((i - 18) * 60),350,60,30);
			}
			panel.add(keyButtons[i]);
		}
	}

	/**
	 * フィードバックを作成する
	 */
	public static void setFeedback(){

		userText1.setBounds(40, 60 + ((tries + 1) * 25), 100, 30);
		tries++;
		userInput = userText1.getText();

        isdoneFlg = true;
        for (String i : colors) {
            if (!(i.equals("2"))) isdoneFlg = false;
        }
        if (isdoneFlg || tries > 5) {
			writer.println("bye");
			endWordle();
		}

        for (int i = 0; i < 5; i++) {
			// Green
			if (colors[i].equals("0")) numToColors[i] = "#787c7e";
			// Yellow
			else if (colors[i].equals("1")) numToColors[i] = "#c9b458";
			// Black
			else if (colors[i].equals("2")) numToColors[i] = "#6aaa64";
		}
		String feedbackString = (
			"<html><font size='6' color='white' style='background-color:" + numToColors[0] + "'>" + userInput.charAt(0) + "</font>" +
			"<html><font size='6' color='white' style='background-color:" + numToColors[1] + "'>" + userInput.charAt(1) + "</font>" +
			"<html><font size='6' color='white' style='background-color:" + numToColors[2] + "'>" + userInput.charAt(2) + "</font>" +
			"<html><font size='6' color='white' style='background-color:" + numToColors[3] + "'>" + userInput.charAt(3) + "</font>" +
			"<html><font size='6' color='white' style='background-color:" + numToColors[4] + "'>" + userInput.charAt(4) + "</font>"
		);		
		// ユーザーへのフィードバックをセットする
		labels[tries - 1].setText(feedbackString);

		// キーボードに配色する
		for (int i = 0; i < 26; i++) {
			for (int j = 0; j < 5; j++) {
				if (keyButtons[i].getText().equals(String.valueOf(userInput.charAt(j)).toUpperCase())) {
					if (keyButtons[i].getBackground().equals(new Color(106, 166, 64))) continue;
			  		else if (keyButtons[i].getBackground().equals(new Color(201,180,88)) && numToColors[j] == "#787c7e") continue;
			  		keyButtons[i].setForeground(Color.WHITE);
					keyButtons[i].setBackground(setButtonColor(numToColors[j]));  // 背景色を設定
					keyButtons[i].setUI(new javax.swing.plaf.basic.BasicButtonUI());  // ボタンのUIをデフォルトに戻す
			 	}
			}
		}

        userText1.setText("");
    }

	/**
	 * ボタンの配色を決定する
	 * @s
	 */
	public static Color setButtonColor(String s){
		Color color = null;
		// Green
		if (s.equalsIgnoreCase("#6aaa64")) {
			color = new Color(106,166,64);
		}
		// Yellow
		else if(s.equalsIgnoreCase("#c9b458")){
			color = new Color(201,180,88);
		}
		// Black
		else if(s.equalsIgnoreCase("#787c7e")){
			color = new Color(120,124,126);
		}
		return color;
	}
}