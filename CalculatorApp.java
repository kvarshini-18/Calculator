import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class CalculatorApp extends JFrame implements ActionListener {

    private JTextField display;
    private JTextArea historyArea;
    private StringBuilder currentInput;
    private ArrayList<String> historyList;

    public CalculatorApp() {
        setTitle("Calculator App");
        setSize(520, 570);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(245, 247, 250));

        // === Display ===
        display = new JTextField();
        display.setFont(new Font("Poppins", Font.BOLD, 32));
        display.setHorizontalAlignment(JTextField.RIGHT);
        display.setEditable(false);
        display.setBackground(new Color(250, 250, 250));
        display.setForeground(Color.BLACK);
        display.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(15, 15, 15, 15),
                BorderFactory.createLineBorder(new Color(200, 200, 200))
        ));
        add(display, BorderLayout.NORTH);

        // === Buttons Panel ===
        JPanel buttonPanel = new JPanel(new GridLayout(5, 4, 10, 10));
        buttonPanel.setBackground(new Color(245, 247, 250));

        // Added AC at the end and rearranged last line as requested
        String[] buttons = {
            "7", "8", "9", "/",
            "4", "5", "6", "*",
            "1", "2", "3", "-",
            "0", ".", "=", "+",
            "←", "%", "C", "AC"
        };

        for (String text : buttons) {
            JButton btn = new JButton(text);
            btn.setFont(new Font("Poppins", Font.BOLD, 18));
            btn.setBackground(new Color(225, 235, 245));
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 220)));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            // Hover effect
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    btn.setBackground(new Color(180, 220, 240));
                }

                public void mouseExited(java.awt.event.MouseEvent evt) {
                    btn.setBackground(new Color(225, 235, 245));
                }
            });

            btn.addActionListener(this);
            buttonPanel.add(btn);
        }

        add(buttonPanel, BorderLayout.CENTER);

        // === History Panel ===
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setPreferredSize(new Dimension(200, 0));
        historyPanel.setBackground(new Color(250, 250, 250));
        historyPanel.setBorder(BorderFactory.createTitledBorder("History"));

        historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        historyArea.setBackground(new Color(250, 250, 250));
        historyArea.setForeground(Color.DARK_GRAY);

        JScrollPane scrollPane = new JScrollPane(historyArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        historyPanel.add(scrollPane, BorderLayout.CENTER);

        add(historyPanel, BorderLayout.EAST);

        currentInput = new StringBuilder();
        historyList = new ArrayList<>();

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        switch (command) {
            case "C" -> { // Clear current input
                currentInput.setLength(0);
                display.setText("");
            }
            case "←" -> { // Backspace
                if (currentInput.length() > 0) {
                    currentInput.deleteCharAt(currentInput.length() - 1);
                    display.setText(currentInput.toString());
                }
            }
            case "AC" -> { // Clear everything including history
                clearHistory();
                currentInput.setLength(0);
                display.setText("");
            }
            case "=" -> evaluateExpression(); // Evaluate expression
            default -> { // Append pressed key
                currentInput.append(command);
                display.setText(currentInput.toString());
            }
        }
    }

    private void evaluateExpression() {
        String expression = currentInput.toString();
        if (expression.isEmpty()) return;

        try {
            double result = evaluate(expression);
            display.setText(formatResult(result));

            String record = expression + " = " + formatResult(result);
            historyList.add(record);
            updateHistory();

            currentInput.setLength(0);
            currentInput.append(result);
        } catch (Exception ex) {
            display.setText("Error");
            currentInput.setLength(0);
        }
    }

    private void updateHistory() {
        historyArea.setText("");
        for (String record : historyList) {
            historyArea.append(record + "\n");
        }
    }

    private void clearHistory() {
        historyList.clear();
        historyArea.setText("");
    }

    private double evaluate(String expr) {
        Stack<Double> numbers = new Stack<>();
        Stack<Character> operations = new Stack<>();
        int i = 0;
        while (i < expr.length()) {
            char c = expr.charAt(i);
            if (Character.isDigit(c) || c == '.') {
                StringBuilder sb = new StringBuilder();
                while (i < expr.length() && (Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.')) {
                    sb.append(expr.charAt(i++));
                }
                numbers.push(Double.parseDouble(sb.toString()));
                continue;
            } else if (c == '(') {
                operations.push(c);
            } else if (c == ')') {
                while (operations.peek() != '(') {
                    numbers.push(applyOperation(operations.pop(), numbers.pop(), numbers.pop()));
                }
                operations.pop();
            } else if ("+-*/%".indexOf(c) != -1) {
                while (!operations.isEmpty() && hasPrecedence(c, operations.peek())) {
                    numbers.push(applyOperation(operations.pop(), numbers.pop(), numbers.pop()));
                }
                operations.push(c);
            }
            i++;
        }
        while (!operations.isEmpty()) {
            numbers.push(applyOperation(operations.pop(), numbers.pop(), numbers.pop()));
        }
        return numbers.pop();
    }

    private boolean hasPrecedence(char op1, char op2) {
        if (op2 == '(' || op2 == ')') return false;
        return (op1 != '*' && op1 != '/' && op1 != '%') || (op2 != '+' && op2 != '-');
    }

    private double applyOperation(char op, double b, double a) {
        return switch (op) {
            case '+' -> a + b;
            case '-' -> a - b;
            case '*' -> a * b;
            case '/' -> b == 0 ? Double.NaN : a / b;
            case '%' -> a % b;
            default -> 0;
        };
    }

    private String formatResult(double result) {
        if (result == (long) result) {
            return String.format("%d", (long) result);
        } else {
            return String.format("%.4f", result);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CalculatorApp::new);
    }
}

