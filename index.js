const {onDocumentCreated} = require("firebase-functions/v2/firestore");
const {getMessaging} = require("firebase-admin/messaging");
const {initializeApp} = require("firebase-admin/app");

initializeApp();

exports.notifyAdminOnNewOrder = onDocumentCreated("orders/{orderId}", async (event) => {
    const orderId = event.params.orderId;

    const payload = {
        notification: {
            title: "¡Nuevo Pedido Recibido!",
            body: "Toca aquí para revisar la receta y los detalles.",
        },
        topic: "admin_orders",
        data: {
            orderId: orderId,
            type: "NEW_ORDER"
        }
    };

    try {
        const response = await getMessaging().send(payload);
        console.log("Notificación enviada correctamente al tema admin_orders:", response);
    } catch (error) {
        console.error("Error enviando notificación push:", error);
    }
});

// ====== TRUCO PARA DESPLEGAR EN RENDER ======
const express = require('express');
const app = express();
const PORT = process.env.PORT || 3000;

// Si tus funciones usan HTTPS tradicionales de Firebase (functions.https.onRequest)
// puedes redirigir el tráfico básico aquí o simplemente dejar el puerto abierto:
app.get('/', (req, res) => {
    res.send('Backend de la Óptica corriendo con éxito en Render');
});

app.listen(PORT, () => {
    console.log(`Servidor escuchando en el puerto ${PORT}`);
});
