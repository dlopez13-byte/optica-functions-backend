package com.example.optica.data;

import com.example.optica.model.Prescription;
import com.example.optica.model.Product;
import java.util.ArrayList;
import java.util.List;

public class MockRepository {

    public static List<Product> getMockProducts() {
        List<Product> products = new ArrayList<>();
        
        // Gafas de Sol
        products.add(new Product("1", "Ray-Ban Aviator Classic", "Sol", 
            "https://images.unsplash.com/photo-1572635196237-14b3f281503f?q=80&w=800", 
            "Icónicas gafas de sol de estilo aviador con montura dorada.", 160.00));
        
        products.add(new Product("2", "Oakley Holbrook Prizm", "Sol", 
            "https://images.unsplash.com/photo-1511499767390-a7335958648d?q=80&w=800", 
            "Gafas deportivas con tecnología de lentes Prizm para mejor contraste.", 145.00));
            
        // Gafas Recetadas
        products.add(new Product("3", "Titanium Minimalist", "Recetadas", 
            "https://images.unsplash.com/photo-1577803645773-f96470509666?q=80&w=800", 
            "Montura de titanio ultra ligera y resistente para uso diario.", 210.00));
            
        products.add(new Product("4", "Carrera Black Edition", "Recetadas", 
            "https://images.unsplash.com/photo-1591076482161-42ce6da69f67?q=80&w=800", 
            "Estilo retro moderno en color negro mate.", 125.00));
            
        // Lentes de Contacto
        products.add(new Product("5", "Biofinity 6-Pack", "Lentes", 
            "https://images.unsplash.com/photo-1509660933844-6910e12765a0?q=80&w=800", 
            "Lentes de contacto mensuales de alta permeabilidad al oxígeno.", 55.00));
            
        // Accesorios
        products.add(new Product("6", "Premium Cleaning Kit", "Accesorios", 
            "https://images.unsplash.com/photo-1589131013401-2946777b7890?q=80&w=800", 
            "Kit de limpieza profesional para todo tipo de lentes.", 25.00));

        return products;
    }

    public static Prescription getMockPrescription() {
        return new Prescription(
                "15/10/2023",
                "-1.25", "-0.50", "180", "+2.00",
                "-1.50", "-0.75", "175", "+2.00"
        );
    }
}
