package pansitosapp.mx.mapa;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import pansitosapp.mx.R;
import pansitosapp.mx.carrito.Carrito;
import pansitosapp.mx.http.Client;
import pansitosapp.mx.http.Node;
import pansitosapp.mx.mostrarproductos.Pancito;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Mapa extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener, View.OnClickListener{

    private NavController navController; //para navegar entre fragmentos

    ProgressDialog progress;

    SupportMapFragment mapFragment;
    private LatLng UPV;
    private GoogleMap mapa;

    Button btn1, btn2, btn3;

    double lat = 21.8835868;
    double lon = -102.2913539;


    ArrayList<Carrito> carrito;

    String token;
    Float total = new Float(0);

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.mapa, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

        carrito = (ArrayList<Carrito>) getArguments().getSerializable("elCarrito");


        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        SharedPreferences preferences = getActivity().getSharedPreferences("Usuario", Context.MODE_PRIVATE);
        token = preferences.getString("token","No existe la informacion");

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapa);
        mapFragment.getMapAsync(this);

        btn1 = view.findViewById(R.id.button1);
        btn1.setOnClickListener(this);
        btn2 = view.findViewById(R.id.button2);
        btn2.setOnClickListener(this);
        btn3 = view.findViewById(R.id.button3);
        btn3.setOnClickListener(this);

    }



    @Override
    public void onMapReady(GoogleMap googleMap) {

        UPV = new LatLng(lat, lon);
        mapa = googleMap;
        mapa.addMarker(new MarkerOptions().position(UPV).title("Marker UPV"));
        mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(UPV, 17));

        mapa.setOnMapClickListener(this);

        if (ActivityCompat.checkSelfPermission(this.getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            mapa.setMyLocationEnabled(true);
            mapa.getUiSettings().setCompassEnabled(true);

        }

    }

    @Override
    public void onMapClick(LatLng puntoPulsado) {
        mapa.clear();
        mapa.addMarker(new MarkerOptions().position(puntoPulsado)
                .icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        lat = puntoPulsado.latitude;
        lon = puntoPulsado.longitude;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            ((AppCompatActivity)getActivity()).onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button1:
                mapa.clear();
                mapa.addMarker(new MarkerOptions().position(mapa.getCameraPosition().target));
                lat = mapa.getCameraPosition().target.latitude;
                lon = mapa.getCameraPosition().target.longitude;
                break;
            case R.id.button2:
                mapa.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lon)));
                //mapa.moveCamera(CameraUpdateFactory.newLatLng(UPV)); // lo mismo que la de arriba pero sin animación
                break;
            case R.id.button3:
                registrarPedido();
                navController.navigate(R.id.nav_to_client_menu);
                break;
        }
    }

    private void registrarPedido (){

        for (int i=0; i < carrito.size(); i++) {
            total += carrito.get(i).getPrecio() * carrito.get(i).getCantidad();
        }
        Client userRest = Node.getClient().create(Client.class); // declara la clase de las peticiones al servidor

        JsonObject data = new JsonObject(); // declara un objeto json para el body
        data.addProperty("total", total);
        data.addProperty("lat", lat);
        data.addProperty("lon", lon);

        // inicia el progress dialog en lo que se guarda el objeto

        // hace la peticion al servidor con el token y el body
        final Call<JsonObject> call = userRest.createPedido(token, data);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
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


                JsonObject json = response.body();
                JsonObject object = json.getAsJsonObject("array");

                // JsonObject object = jsonArray.get(0).getAsJsonObject();
                Integer id = object.getAsJsonPrimitive("insertId").getAsInt();
                registrarProdPedido(id);
                deleteCarrito();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                call.cancel();
            }
        });
    }

    private void registrarProdPedido(Integer id_pedido){
        for (int i=0; i < carrito.size(); i++) {
            Client userRest = Node.getClient().create(Client.class); // declara la clase de las peticiones al servidor

            JsonObject data = new JsonObject(); // declara un objeto json para el body
            data.addProperty("id_producto", carrito.get(i).getId_producto());
            data.addProperty("cantidad", carrito.get(i).getCantidad());
            data.addProperty("precio", carrito.get(i).getPrecio());
            data.addProperty("id_pedido", id_pedido);

            // hace la peticion al servidor con el token y el body
            final Call<JsonObject> call = userRest.createProductoPedido(token, data);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
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

                    JsonObject json = response.body();
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    private void deleteCarrito() {
        Client userRest = Node.getClient().create(Client.class); // declara la clase de peticiones al servidor

        // progress dialog para cuando se elimina algo

        progress = ProgressDialog.show(getContext(), "Procesando","Espera", true);
        // manda la peticion al servidor, le manda el token y el id del pan
        final Call<JsonObject> call = userRest.deleteAllCarrito(token);
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

                Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Pedido realizado" , Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
                toast.show();
                // cuando se borra el producto lo regresa al fragmento de todos los productos
                JsonObject json = response.body();

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                progress.dismiss(); // cierralo aqui tambien
                call.cancel();
            }
        });
    }
}
