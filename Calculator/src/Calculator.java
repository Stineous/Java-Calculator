import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.border.LineBorder;

public class Calculator {

    int boardWidth = 360;
    int boardHeight = 540;

    Color customLightGray = new Color(212, 212, 210);
    Color customDarkGray  = new Color(80, 80, 80);
    Color customBlack     = new Color(28, 28, 28);
    Color customOrange    = new Color(255, 149, 0);

    String[] buttonValues = {
            "AC", "+/-", "%", "÷",
            "7",  "8",   "9", "×",
            "4",  "5",   "6", "-",
            "1",  "2",   "3", "+",
            "0",  ".",   "√", "="
    };
    String[] rightSymbols = {"÷", "×", "-", "+", "="};
    String[] topSymbols   = {"AC", "+/-", "%"};

    JFrame frame        = new JFrame("Calculator");
    JLabel displayLabel = new JLabel();
    JPanel displayPanel = new JPanel();
    JPanel buttonsPanel = new JPanel();

    // ── State ─────────────────────────────────────────────────────────────────
    private double  num1             = 0;
    private String  operator         = "";
    private boolean isOperatorClicked = false;

    // FIX 3a: Track whether the display currently holds a freshly computed
    // result.  When true, the next digit press must overwrite the display
    // (start a new number) rather than append to it.
    private boolean isResultDisplayed = false;

    Calculator() {
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        displayLabel.setBackground(customBlack);
        displayLabel.setForeground(Color.white);
        displayLabel.setFont(new Font("Arial", Font.PLAIN, 60));
        displayLabel.setHorizontalAlignment(JLabel.RIGHT);
        displayLabel.setText("0");
        displayLabel.setOpaque(true);

        displayPanel.setLayout(new BorderLayout());
        displayPanel.add(displayLabel);
        frame.add(displayPanel, BorderLayout.NORTH);

        buttonsPanel.setLayout(new GridLayout(5, 4));
        buttonsPanel.setBackground(customBlack);
        frame.add(buttonsPanel, BorderLayout.CENTER);

        for (String buttonValue : buttonValues) {
            JButton button = new JButton();
            button.setFont(new Font("Arial", Font.PLAIN, 30));
            button.setText(buttonValue);
            button.setFocusable(false);
            button.setBorder(new LineBorder(customBlack));

            if (Arrays.asList(topSymbols).contains(buttonValue)) {
                button.setBackground(customLightGray);
                button.setForeground(Color.black);
            } else if (Arrays.asList(rightSymbols).contains(buttonValue)) {
                button.setBackground(customOrange);
                button.setForeground(Color.white);
            } else {
                button.setBackground(customDarkGray);
                button.setForeground(Color.white);
            }

            buttonsPanel.add(button);

            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String pressedValue = ((JButton) e.getSource()).getText();

                    // ── 1. OPERATOR KEYS (÷, ×, -, +, =) ────────────────────
                    if (Arrays.asList(rightSymbols).contains(pressedValue)) {

                        if (pressedValue.equals("=")) {
                            calculateResult();
                            operator          = "";   // clear pending op
                            isOperatorClicked = false;
                            isResultDisplayed = true; // FIX 3a: mark as result

                        } else {
                            // FIX 1 ─ Chain operations (5 + 5 + 3 = 13)
                            // If an operator is already pending AND the user has
                            // already entered a second operand (flag is false, meaning
                            // they typed at least one digit since the last operator),
                            // evaluate the pending expression before storing the new op.
                            if (!operator.isEmpty() && !isOperatorClicked) {
                                calculateResult();  // collapses left-hand side
                            }

                            num1              = Double.parseDouble(displayLabel.getText());
                            operator          = pressedValue;
                            isOperatorClicked = true;
                            isResultDisplayed = false;
                        }
                    }
                    // ── 2. UTILITY KEYS (AC, +/-, %, √) ─────────────────────
                    else if (Arrays.asList(topSymbols).contains(pressedValue)
                            || pressedValue.equals("√")) {

                        if (pressedValue.equals("AC")) {
                            // FIX 3b: AC resets every single piece of state.
                            num1              = 0;
                            operator          = "";
                            isOperatorClicked = false;
                            isResultDisplayed = false;
                            displayLabel.setText("0");

                        } else if (pressedValue.equals("+/-")) {
                            double val = Double.parseDouble(displayLabel.getText()) * -1;
                            formatDisplay(val);
                            // Unary ops update the number in place; don't flip
                            // isOperatorClicked so the user can chain digits after.

                        } else if (pressedValue.equals("%")) {
                            double val = Double.parseDouble(displayLabel.getText()) / 100;
                            formatDisplay(val);
                            // FIX 5: After a unary transformation, treat it like an
                            // operand boundary so the next digit starts a new number.
                            isOperatorClicked = true;

                        } else if (pressedValue.equals("√")) {
                            double operand = Double.parseDouble(displayLabel.getText());
                            if (operand < 0) {
                                displayLabel.setText("Error"); // √ of negative
                            } else {
                                formatDisplay(Math.sqrt(operand));
                            }
                            // FIX 5 (same as %): lock the result as an operand boundary.
                            isOperatorClicked = true;
                        }
                    }
                    // ── 3. DIGITS & DECIMAL ───────────────────────────────────
                    else {
                        if ("0123456789.".contains(pressedValue)) {

                            // FIX 3a: Typing any digit right after a result must
                            // begin a completely fresh number, not append.
                            if (isResultDisplayed) {
                                if (pressedValue.equals(".")) {
                                    displayLabel.setText("0.");
                                } else {
                                    displayLabel.setText(pressedValue);
                                }
                                isResultDisplayed = false;
                                isOperatorClicked = false;

                            } else if (isOperatorClicked) {
                                // Operator was just pressed: overwrite display.
                                if (pressedValue.equals(".")) {
                                    displayLabel.setText("0.");
                                } else {
                                    displayLabel.setText(pressedValue);
                                }
                                isOperatorClicked = false;

                            } else {
                                // Normal append logic.
                                String current = displayLabel.getText();
                                if (current.equals("0")) {
                                    if (pressedValue.equals(".")) {
                                        displayLabel.setText("0.");
                                    } else {
                                        displayLabel.setText(pressedValue);
                                    }
                                } else {
                                    if (pressedValue.equals(".")) {
                                        if (!current.contains(".")) {
                                            displayLabel.setText(current + pressedValue);
                                        }
                                        // Silently ignore a second decimal point.
                                    } else {
                                        displayLabel.setText(current + pressedValue);
                                    }
                                }
                            }
                        }
                    }
                }
            });
        }

        frame.setVisible(true);
    }

    // ── Arithmetic ────────────────────────────────────────────────────────────

    private void calculateResult() {
        if (operator.isEmpty()) return;

        double num2   = Double.parseDouble(displayLabel.getText());
        double result;

        switch (operator) {
            case "÷":
                if (num2 == 0) {
                    displayLabel.setText("Error");
                    return;
                }
                result = num1 / num2;
                break;
            case "×": result = num1 * num2; break;
            case "-": result = num1 - num2; break;
            case "+": result = num1 + num2; break;
            default:  return;
        }

        // FIX 1: After computing, store the result as the new left operand so
        // chained operators (5+5+3=) always have a valid num1.
        formatDisplay(result);
        num1 = result;
    }

    // ── Display formatting ─────────────────────────────────────────────────────

    /**
     * FIX 4 – Floating-point precision.
     *
     * Strategy: round the result to 10 significant figures using BigDecimal.
     * This collapses 0.30000000000000004 → 0.3 while preserving legitimate
     * precision like 1.0000000001.  Then strip the trailing ".0" for integers.
     */
    private void formatDisplay(double val) {
        if (Double.isInfinite(val) || Double.isNaN(val)) {
            displayLabel.setText("Error");
            return;
        }

        // Round to 10 significant figures to eliminate IEEE 754 noise.
        BigDecimal bd = new BigDecimal(val).round(new MathContext(10));
        double rounded = bd.doubleValue();

        if (rounded == (long) rounded) {
            displayLabel.setText(String.format("%d", (long) rounded));
        } else {
            // Remove any residual trailing zeros from the decimal string.
            String str = bd.stripTrailingZeros().toPlainString();
            displayLabel.setText(str);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Calculator());
    }
}