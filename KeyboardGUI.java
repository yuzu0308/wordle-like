import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class KeyboardGUI {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Keyboard Color Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 7)); // ボタンをグリッド状に配置するためのGridLayoutを使用

        JButton[] buttons = new JButton[26];
        for (int i = 0; i < 26; i++) {
            char alphabet = (char) ('A' + i);
            buttons[i] = new JButton(String.valueOf(alphabet));
            panel.add(buttons[i]);
        }

        JTextField textField = new JTextField(20);
        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String input = textField.getText().toUpperCase();
                for (JButton button : buttons) {
                    if (button.getText().equals(input)) {
                        button.setForeground(Color.RED);
                        break;
                    }
                }
                textField.setText("");
            }
        });

        frame.add(panel, BorderLayout.CENTER);
        frame.add(textField, BorderLayout.SOUTH);
        frame.pack();
        frame.setVisible(true);
    }
}




