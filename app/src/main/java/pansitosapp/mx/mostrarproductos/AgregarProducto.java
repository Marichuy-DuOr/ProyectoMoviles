package pansitosapp.mx.mostrarproductos;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import kotlin.reflect.KVariance;
import pansitosapp.mx.R;
import pansitosapp.mx.http.Client;
import pansitosapp.mx.http.Node;
import pansitosapp.mx.productos.Pan;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AgregarProducto extends Fragment implements View.OnClickListener {

    private NavController navController;
    ProgressDialog progress;

    TextView nombre, descripcion, precio;
    ImageView imagen;
    EditText cantidad;
    Button agregar;
    Integer ctd;

    Pancito pan;
    String token; // uso el token muchas veces, mejor lo declaro desde el inicio

    Boolean band;

    // ArrayList<Carrito> listaCarrito = new ArrayList<Carrito>();


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.agregar_producto, container, false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        // revisa si se recibieron argumentos
        if (getArguments() != null){ // en el caso de que si, se pone la opcion para agregar el pan
            pan = (Pancito) getArguments().getSerializable("elPan");
            band = false;
        } else { // en el caso de que no, se va a crear un pan nuevo
            band = true;
        }
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        // guardar el token
        SharedPreferences preferences = getActivity().getSharedPreferences("Usuario", Context.MODE_PRIVATE);
        token = preferences.getString("token","No existe la informacion");

        agregar = view.findViewById(R.id.btnAgregar);
        agregar.setOnClickListener(this);

        nombre = view.findViewById(R.id.name);
        imagen = view.findViewById(R.id.img);
        descripcion = view.findViewById(R.id.desc);
        precio = view.findViewById(R.id.txtPrecio);
        cantidad = view.findViewById(R.id.cant);

        if(!band){
            nombre.setText(pan.getNombre());
            precio.setText(pan.getPrecio().toString());
            descripcion.setText(pan.getDescripcion());
            Picasso.with(getContext()).load(pan.getImagen()).into(imagen);
        } else { // quita el boton de eliminar para que no este cuando se crea un pan
            Toast toast = Toast.makeText(getActivity().getApplicationContext(), "No se puede cargar el producto, intentalo más tarde" , Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
            toast.show();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnAgregar:
                agregarCarrito();
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) { // poner para que el menu de la flecha para regresar funcione
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { // para la flecha de regresar
        int id = item.getItemId();
        if (id == android.R.id.home) {
            ((AppCompatActivity)getActivity()).onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void agregarCarrito() {
        // recibe los datos que puso el usuario
        String can = cantidad.getText().toString();



        if( !can.equals("") ){
            Client userRest = Node.getClient().create(Client.class); // declara la clase de las peticiones al servidor

            JsonObject data = new JsonObject(); // declara un objeto json para el body
            data.addProperty("id_producto", pan.getId());
            data.addProperty("cantidad", can);
            data.addProperty("precio", pan.getPrecio());

            // inicia el progress dialog en lo que se guarda el objeto
            progress = ProgressDialog.show(getContext(), "Agregando","Espera", true);

            // hace la peticion al servidor con el token y el body
            final Call<JsonObject> call = userRest.createCarrito(token, data);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    progress.dismiss(); // cierra el progress dialog
                    if (response.code() != 200) {
                        JSONObject jObjError = null;
                        try {
                            jObjError = new JSONObject(response.errorBody().string());
                            Log.i("mainActivity", jObjError.toString());
                            System.out.println(jObjError.toString());
                            return;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Se agrego correctamente a tu carrito" , Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
                    toast.show();
                    // termina y redirige a productos
                    JsonObject json = response.body();
                    System.out.println("-> " + json.toString());
                    navController.navigate(R.id.nav_to_mostrarproductos);
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    progress.dismiss(); // aqui tambien se cierra
                    call.cancel();
                }
            });
        }else {
            Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Completa el campo CANTIDAD" , Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
            toast.show();
        }
    }

    private void updateCarrito(int cantidad){
        Client userRest = Node.getClient().create(Client.class); // declara la clase de las peticiones al servidor

        JsonObject data = new JsonObject(); // declara objeto json para el body
        data.addProperty("id", pan.getId()); // pone el id del pan que ya tenemos previamente guardado
        data.addProperty("cantidad", cantidad);

        // peticion al servidor, manda el token y el body
        final Call<JsonObject> call = userRest.updateCarrito(token, data);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                progress.dismiss(); // cierra el progress dialog
                if (response.code() != 200) {
                    JSONObject jObjError = null;
                    try {
                        jObjError = new JSONObject(response.errorBody().string());
                        Log.i("mainActivity", jObjError.toString());
                        System.out.println(jObjError.toString());
                        return;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                // recibe la respuesta del servidor y redirige a Productos
                JsonObject json = response.body();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                progress.dismiss(); // cierra esta cosa donde sea necesario
                call.cancel();
            }
        });
    }

}
