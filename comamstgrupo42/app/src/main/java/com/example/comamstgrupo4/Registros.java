package com.example.comamstgrupo4;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Registros extends AppCompatActivity {

    public BarChart graficoBarras;
    private RequestQueue ListaRequest = null;
    private LinearLayout contenedorTemperaturas;
    private Map<String, TextView> temperaturasTVs;
    private Map<String, TextView> fechasTVs;
    private Registros contexto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registros);
        setTitle("Grafico de barras");
        temperaturasTVs = new HashMap<String, TextView>();
        fechasTVs = new HashMap<String, TextView>();
        ListaRequest = Volley.newRequestQueue(this);
        contexto = this;
        /* GRAFICO */
        this.iniciarGrafico();
        this.solicitarTemperaturas();
    }

    public void iniciarGrafico() {
        graficoBarras = findViewById(R.id.barChart);
        graficoBarras.getDescription().setEnabled(false);
        graficoBarras.setMaxVisibleValueCount(60);
        graficoBarras.setPinchZoom(false);
        graficoBarras.setDrawBarShadow(false);
        graficoBarras.setDrawGridBackground(false);
        XAxis xAxis = graficoBarras.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        graficoBarras.getAxisLeft().setDrawGridLines(false);
        graficoBarras.animateY(1500);
        graficoBarras.getLegend().setEnabled(false);
    }

    public void solicitarTemperaturas() {
        String url_registros = " https://amst-lab-api.herokuapp.com/api/lecturas";
        JsonArrayRequest requestRegistros = new JsonArrayRequest(Request.Method.GET, url_registros, null,
                response -> {
                    mostrarTemperaturas(response);
                    actualizarGrafico(response);
                }, error -> System.out.println(error)
        );
        ListaRequest.add(requestRegistros);
    }

    private void mostrarTemperaturas(JSONArray temperaturas) {
        String registroId;
        JSONObject registroTemp;
        LinearLayout nuevoRegistro;
        TextView fechaRegistro;
        TextView valorRegistro;
        String fecharegistro_text;
        String registrovalue_text;

        contenedorTemperaturas = findViewById(R.id.cont_temperaturas);
        LinearLayout.LayoutParams parametrosLayout = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        try {
            for (int i = 0; i < temperaturas.length(); i++) {
                registroTemp = temperaturas.getJSONObject(i);
                registroId = registroTemp.getString("id");

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    ZonedDateTime zonedDateTime = OffsetDateTime.parse(registroTemp.getString("date_created")).toZonedDateTime();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    fecharegistro_text = String.valueOf(zonedDateTime.format(formatter));
                } else {
                    fecharegistro_text = registroTemp.getString("date_created");
                }

                registrovalue_text = registroTemp.getString("value");
                if (temperaturasTVs.containsKey(registroId) && fechasTVs.containsKey(registroId)) {

                    fechaRegistro = fechasTVs.get(registroId);
                    valorRegistro = temperaturasTVs.get(registroId);

                    fechaRegistro.setText(fecharegistro_text);
                    valorRegistro.setText(registrovalue_text + " °C");

                } else {

                    nuevoRegistro = new LinearLayout(this);
                    nuevoRegistro.setOrientation(LinearLayout.HORIZONTAL);

                    fechaRegistro = new TextView(this);
                    fechaRegistro.setLayoutParams(parametrosLayout);
                    fechaRegistro.setText(fecharegistro_text);
                    nuevoRegistro.addView(fechaRegistro);

                    valorRegistro = new TextView(this);
                    valorRegistro.setLayoutParams(parametrosLayout);
                    valorRegistro.setText(registrovalue_text + " °C");
                    nuevoRegistro.addView(valorRegistro);
                    contenedorTemperaturas.addView(nuevoRegistro);
                    fechasTVs.put(registroId, fechaRegistro);
                    temperaturasTVs.put(registroId, valorRegistro);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            System.out.println("error");
        }
    }

    private void actualizarGrafico(JSONArray temperaturas) {
        JSONObject registro_temp;
        String temp;
        String date;
        int count = 1;
        float temp_val;
        ArrayList<BarEntry> dato_temp = new ArrayList<>();
        try {
            for (int i = 0; i < temperaturas.length(); i++) {

                registro_temp = temperaturas.getJSONObject(i);
                if (registro_temp.getString("key").equals("temperatura")) {
                    temp = registro_temp.getString("value");
                    date = registro_temp.getString("date_created");
                    temp_val = Float.parseFloat(temp);
                    dato_temp.add(new BarEntry(count, temp_val));
                    count++;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            System.out.println("error");
        }
        llenarGrafico(dato_temp);
    }

    private void llenarGrafico(ArrayList<BarEntry> dato_temp) {
        BarDataSet temperaturasDataSet;
        if (graficoBarras.getData() != null &&
                graficoBarras.getData().getDataSetCount() > 0) {
            temperaturasDataSet = (BarDataSet) graficoBarras.getData().getDataSetByIndex(0);
            temperaturasDataSet.setValues(dato_temp);
            graficoBarras.getData().notifyDataChanged();
            graficoBarras.notifyDataSetChanged();
        } else {
            temperaturasDataSet = new BarDataSet(dato_temp, "Data Set");
            temperaturasDataSet.setColors(ColorTemplate.VORDIPLOM_COLORS);
            temperaturasDataSet.setDrawValues(true);
            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(temperaturasDataSet);
            BarData data = new BarData(dataSets);
            graficoBarras.setData(data);
            graficoBarras.setFitBars(true);
        }
        graficoBarras.invalidate();

        new Handler(Looper.getMainLooper()).postDelayed(() -> solicitarTemperaturas(), 3000);
    }
}