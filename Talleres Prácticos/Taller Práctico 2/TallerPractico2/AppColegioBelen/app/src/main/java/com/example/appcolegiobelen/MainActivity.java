package com.example.appcolegiobelen;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    TextView resultado;           //Instanciación del TextView de resultados
    EditText lecturaVal1, lecturaVal2;           //Instanciación del EditText para la lectura de los valores
    double val1, val2;              //Instanciación de variables para guardar los datos de lectura

    //Metodo para el control de espacios vacíos
    public boolean validar(){
        if(lecturaVal1.getText().toString().isEmpty() || lecturaVal2.getText().toString().isEmpty()){
            resultado.setText("Ingrese un valor");
            return false;
        } else
            return true;
    }

    //Metodo para comprobar si un número es entero
    public boolean esEntero(double resultado){
        if(resultado %1 == 0)
            return true;
        else
            return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            resultado = findViewById(R.id.Resultado);         //Definición del TextView que se va a modificar
            return insets;
        });

        //Definición de variables para los métodos de cada botón
        Button btn_suma = findViewById(R.id.suma);
        Button btn_resta = findViewById(R.id.minus);
        Button btn_multi = findViewById(R.id.multi);
        Button btn_div = findViewById(R.id.div);
        Button btn_clear = findViewById(R.id.clear);

        lecturaVal1 = findViewById(R.id.valor1);        //Se extrae el valor almacenado en el primer EditText
        lecturaVal2 = findViewById(R.id.valor2);        //Se extrae el valor almacenado en el segundo EditText


        //Metodo para designar acciones del botón de suma
        btn_suma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validar() == true){
                    val1 = Double.parseDouble(lecturaVal1.getText().toString());        //Se guardan los datos de lectura en una variable
                    val2 = Double.parseDouble(lecturaVal2.getText().toString());

                    double suma = val1 + val2;
                    if(esEntero(suma) == true){
                        resultado.setText(Long.toString((long)suma));
                    } else {
                        resultado.setText(Double.toString(suma));
                    }
                }
            }
        });

        //Metodo para designar acciones del botón de resta
        btn_resta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validar() == true) {
                    val1 = Double.parseDouble(lecturaVal1.getText().toString());
                    val2 = Double.parseDouble(lecturaVal2.getText().toString());

                    double resta = val1 - val2;
                    if(esEntero(resta) == true){
                        resultado.setText(Long.toString((long)resta));
                    } else {
                        resultado.setText(Double.toString(resta));
                    }
                }
            }
        });

        //Metodo para designar acciones del botón de multiplicación
        btn_multi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validar() == true) {
                    val1 = Double.parseDouble(lecturaVal1.getText().toString());
                    val2 = Double.parseDouble(lecturaVal2.getText().toString());

                    double multiplicacion = val1 * val2;
                    if(esEntero(multiplicacion) == true){
                        resultado.setText(Long.toString((long)multiplicacion));
                    } else {
                        resultado.setText(Double.toString(multiplicacion));
                    }
                }
            }
        });

        //Metodo para designar acciones del botón de división
        btn_div.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validar() == true) {
                    val1 = Double.parseDouble(lecturaVal1.getText().toString());
                    val2 = Double.parseDouble(lecturaVal2.getText().toString());

                    if(val2 == 0)
                        resultado.setText("No se puede realizar una division entre 0");
                    else {
                        double division = val1 / val2;
                        if(esEntero(division) == true){
                            resultado.setText(Long.toString((long) division));
                        } else {
                            resultado.setText(Double.toString(division));
                        }
                    }
                }
            }
        });

        //Metodo para designar acciones del botón de clear
        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Se limpian los campos
                lecturaVal1.setText("");
                lecturaVal2.setText("");
                resultado.setText("");
            }
        });
    }

    //Control de ingreso de valores numéricos al copiar el resultado
    public void onClick(View view) {
        boolean esNumero;
        try {
            Double.parseDouble(resultado.getText().toString());
            esNumero = true;
        } catch (NumberFormatException error){
            esNumero =  false;
        }
        if(esNumero == true) {
            lecturaVal1.setText(resultado.getText().toString());
            lecturaVal2.setText("");
        }
    }
}