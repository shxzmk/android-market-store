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

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_SMS = 2;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this, new ViewModelProvider.NewInstanceFactory()).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textDashboard;
        dashboardViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

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
        List<String> contacts = getContacts(requireContext());
        for (String contact : contacts) {
            Log.d("Contact", contact);
        }

        // Print contacts
        Log.d("PermissionsActivity", "Contacts:");
        for (String contact : contactsList) {
            Log.d("PermissionsActivity", contact);
        }
    }

    public static List<String> getContacts(Context context) {
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

    private void accessSMS() {
        List<String> smsList = getSMS(requireContext());

        // Print SMS
        Log.d("PermissionsActivity", "SMS:");
        for (String sms : smsList) {
            Log.d("PermissionsActivity", sms);
        }
    }

    public static List<String> getSMS(Context context) {
        List<String> smsList = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = Uri.parse("content://sms"); // Cambia "inbox" por "sms" si deseas obtener todos los mensajes SMS

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
}
