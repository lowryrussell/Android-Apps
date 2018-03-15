package uark.csce4623.rblowry.calculator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.text.DecimalFormat;

public class Calculator extends AppCompatActivity {

    /*Declare the two values that are going to be computed*/
    private double valueOne = Double.NaN;
    private double valueTwo;

    /*Declare the operation chars*/
    private static final char ADDITION = '+';
    private static final char SUBTRACTION = '-';
    private static final char MULTIPLICATION = 'X';
    private static final char DIVISION = '/';

    /*Declare the calculation type defining what operation to perform*/
    private char CALC_TYPE;

    private TextView inputView;

    private DecimalFormat decimalFormat;
    private boolean newNumber = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_calculator);
        decimalFormat = new DecimalFormat("#.######E0");

        /* Initialize the text view*/
        inputView = (TextView) findViewById(R.id.inputTextView);

        /* Set up all of the button click listeners*/
        final Button button = (Button) findViewById(R.id.btn0);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*This new number check determines whether to remove the default 0 value or not*/
                if (newNumber) {
                    inputView.setText("0");
                    newNumber = false;
                }
                else {
                    inputView.setText(inputView.getText() + "0");
                }
            }
        });

        final Button button1 = (Button) findViewById(R.id.btn1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (newNumber) {
                    inputView.setText("1");
                    newNumber = false;
                }
                else {
                    inputView.setText(inputView.getText() + "1");
                }
            }
        });

        final Button button2 = (Button) findViewById(R.id.btn2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (newNumber) {
                    inputView.setText("2");
                    newNumber = false;
                }
                else {
                    inputView.setText(inputView.getText() + "2");
                }
            }
        });

        final Button button3 = (Button) findViewById(R.id.btn3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (newNumber) {
                    inputView.setText("3");
                    newNumber = false;
                }
                else {
                    inputView.setText(inputView.getText() + "3");
                }
            }
        });

        final Button button4 = (Button) findViewById(R.id.btn4);
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (newNumber) {
                    inputView.setText("4");
                    newNumber = false;
                }
                else {
                    inputView.setText(inputView.getText() + "4");
                }
            }
        });

        final Button button5 = (Button) findViewById(R.id.btn5);
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (newNumber) {
                    inputView.setText("5");
                    newNumber = false;
                }
                else {
                    inputView.setText(inputView.getText() + "5");
                }
            }
        });

        final Button button6 = (Button) findViewById(R.id.btn6);
        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (newNumber) {
                    inputView.setText("6");
                    newNumber = false;
                }
                else {
                    inputView.setText(inputView.getText() + "6");
                }
            }
        });

        final Button button7 = (Button) findViewById(R.id.btn7);
        button7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (newNumber) {
                    inputView.setText("7");
                    newNumber = false;
                }
                else {
                    inputView.setText(inputView.getText() + "7");
                }
            }
        });

        final Button button8 = (Button) findViewById(R.id.btn8);
        button8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (newNumber) {
                    inputView.setText("8");
                    newNumber = false;
                }
                else {
                    inputView.setText(inputView.getText() + "8");
                }
            }
        });

        final Button button9 = (Button) findViewById(R.id.btn9);
        button9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (newNumber) {
                    inputView.setText("9");
                    newNumber = false;
                }
                else {
                    inputView.setText(inputView.getText() + "9");
                }
            }
        });

        final Button button10 = (Button) findViewById(R.id.clearBtn);
        button10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Set inputView to 0 (default value) and clear out valueOne and valueTwo*/
                inputView.setText("0");
                valueOne = Double.NaN;
                valueTwo = Double.NaN;
                newNumber = true;
            }
        });

        final Button button11 = (Button) findViewById(R.id.deleteBtn);
        button11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Find the last char of the number sequence and remove it*/
                if(inputView.getText().length() > 0) {
                    CharSequence currentText = inputView.getText();
                    inputView.setText(currentText.subSequence(0, currentText.length()-1));
                }
            }
        });

        final Button button12 = (Button) findViewById(R.id.negateBtn);
        button12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Multiply the current value by -1 to receive the negation*/
                DecimalFormat negDecFormat = new DecimalFormat("#.######");
                double currentVal = Double.parseDouble(inputView.getText().toString());
                currentVal *= -1;
                inputView.setText(negDecFormat.format(currentVal));
            }
        });

        final Button button13 = (Button) findViewById(R.id.divideBtn);
        button13.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                computeCalculation();
                CALC_TYPE = DIVISION;
            }
        });

        final Button button14 = (Button) findViewById(R.id.multiplyBtn);
        button14.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                computeCalculation();
                CALC_TYPE = MULTIPLICATION;
            }
        });

        final Button button15 = (Button) findViewById(R.id.subtractBtn);
        button15.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                computeCalculation();
                CALC_TYPE = SUBTRACTION;
            }
        });

        final Button button16 = (Button) findViewById(R.id.additionBtn);
        button16.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                computeCalculation();
                CALC_TYPE = ADDITION;
            }
        });

        final Button button17 = (Button) findViewById(R.id.equalsBtn);
        button17.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                computeCalculation();
                valueOne = Double.NaN;
                CALC_TYPE = '0';
            }
        });

        final Button button18 = (Button) findViewById(R.id.decimalBtn);
        button18.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (newNumber) {
                    /*Set textView value to 0. if input is the start of a new number*/
                    inputView.setText("0.");
                    newNumber = false;
                }
                else {
                    inputView.setText(inputView.getText() + ".");
                }
            }
        });
    }

    /*Compute the value of valueOne (operator) valueTwo*/
    private void computeCalculation() {
        if(!Double.isNaN(valueOne)) {
            DecimalFormat newDecFormat = new DecimalFormat("#.##########");
            valueTwo = Double.parseDouble(inputView.getText().toString());
            inputView.setText(null);

            if(CALC_TYPE == ADDITION)
                valueOne = this.valueOne + valueTwo;
            else if(CALC_TYPE == SUBTRACTION)
                valueOne = this.valueOne - valueTwo;
            else if(CALC_TYPE == MULTIPLICATION)
                valueOne = this.valueOne * valueTwo;
            else if(CALC_TYPE == DIVISION)
                valueOne = this.valueOne / valueTwo;

            String stringValOne = newDecFormat.format(valueOne);
            String stringAfterDecimal = newDecFormat.format(valueOne);

            stringAfterDecimal.substring(stringAfterDecimal.lastIndexOf(".") + 1);
            System.out.println(stringAfterDecimal.length());

            /*Edge cases:
                1) Check if value after decimal point is greater than 8 (max numbers shown in view).
                2) If length is less than 8 then put value in normal digit format.
                3) If length is greater than 8, put in scientific notation.
             */
            if (stringAfterDecimal.length() > 8) {
                inputView.setText(decimalFormat.format(valueOne));
            }
            else {
                DecimalFormat normalFormat = new DecimalFormat("#.######");
                inputView.setText(normalFormat.format(valueOne));
            }
        }
        else {
            try {
                valueOne = Double.parseDouble(inputView.getText().toString());
            }
            catch (Exception e){}
        }
        /*Set newNumber to true because textView has been cleared and is ready for new input*/
        newNumber = true;
    }
}
