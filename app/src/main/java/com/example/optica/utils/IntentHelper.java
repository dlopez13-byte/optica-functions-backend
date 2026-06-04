package com.example.optica.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

public class IntentHelper {

    public static void openWhatsApp(Context context, String phone, String message) {
        try {
            String url = "https://api.whatsapp.com/send?phone=" + phone + "&text=" + Uri.encode(message);
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            context.startActivity(i);
        } catch (Exception e) {
            Toast.makeText(context, "WhatsApp no está instalado", Toast.LENGTH_SHORT).show();
        }
    }

    public static void makeCall(Context context, String phone) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phone));
        context.startActivity(intent);
    }

    public static void sendEmail(Context context, String email, String subject) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + email));
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        context.startActivity(intent);
    }

    public static void openMaps(Context context, String latitude, String longitude, String label) {
        Uri gmmIntentUri = Uri.parse("geo:" + latitude + "," + longitude + "?q=" + Uri.encode(label));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(mapIntent);
        } else {
            context.startActivity(new Intent(Intent.ACTION_VIEW, gmmIntentUri));
        }
    }
}
